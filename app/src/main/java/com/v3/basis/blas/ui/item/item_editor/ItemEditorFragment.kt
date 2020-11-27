package com.v3.basis.blas.ui.item.item_editor


import android.Manifest
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.media.ToneGenerator
import android.os.*
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.databinding.DataBindingUtil
import androidx.databinding.ObservableBoolean
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProviders
import com.v3.basis.blas.R
import com.v3.basis.blas.activity.ItemActivity
import com.v3.basis.blas.activity.QRActivity
import com.v3.basis.blas.blasclass.app.BlasMsg
import com.v3.basis.blas.blasclass.config.FieldType
import com.v3.basis.blas.blasclass.db.data.ItemsController
import com.v3.basis.blas.blasclass.db.field.FieldController
import com.v3.basis.blas.blasclass.formaction.FormActionDataCreate
import com.v3.basis.blas.blasclass.helper.RestHelper
import com.v3.basis.blas.blasclass.ldb.LdbFieldRecord
import com.v3.basis.blas.blasclass.rest.BlasRest
import com.v3.basis.blas.databinding.*
import com.v3.basis.blas.ui.ext.addTitle
import com.v3.basis.blas.ui.ext.hideKeyboardWhenTouch
import com.v3.basis.blas.ui.ext.requestPermissions
import com.v3.basis.blas.ui.ext.startActivityWithResult
import com.v3.basis.blas.ui.item.common.*
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import org.json.JSONObject
import java.util.*


/**
 * データの新規作成、または、更新を行うフラグメント
 */
class ItemEditorFragment : Fragment() {

    private lateinit var token: String
    private lateinit var projectId: String
    private var itemId: String? = null
    private val isUpdateMode: Boolean
        get() = itemId?.toLong() != 0L
    private var userMap :MutableMap<String,String?> = mutableMapOf()
    private val fieldValues: MutableMap<String, String?> = mutableMapOf()

    private val calender = Calendar.getInstance()
    private val year = calender.get(Calendar.YEAR)
    private val month = calender.get(Calendar.MONTH)
    private val day = calender.get(Calendar.DAY_OF_MONTH)
    private val hour = calender.get(Calendar.YEAR)
    private val minute = calender.get(Calendar.MONTH)

    //private lateinit var formAction: FormActionDataCreate
    private var msg = BlasMsg()
    private val helper:RestHelper = RestHelper()

    private lateinit var viewModel: ItemViewModel
    private lateinit var bind: ViewItems0FormBinding
    private var fields: MutableList<FieldDataModel> = mutableListOf()
    private val disposables = CompositeDisposable()
    private lateinit var itemsController: ItemsController

    private lateinit var vibrator: Vibrator
    private var vibrationEffect = VibrationEffect.createOneShot(300, VibrationEffect.DEFAULT_AMPLITUDE)
    private var tone: ToneGenerator? = null

    //シングルセレクトを取得
    private val singleSelectMap = mutableMapOf<Int,ViewItems5SelectBinding>()
    private val singleSelectChoiceMap = mutableMapOf<Int,String?>()
    private val singleSelectList = mutableListOf<MutableMap<String?,ViewItems5SelectBinding>>()
    var singleCnt = 1


    private val singleSelectSpinner = mutableMapOf<Int,Spinner>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addTitle("projectName")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        super.onCreateView(inflater, container, savedInstanceState)

        //引数の取得 token,projectId, item_idの取得
        setArgs()

        //コントローラーの作成
        itemsController = ItemsController(requireContext(), projectId)

        //ViewModelの作成
        viewModel = ViewModelProviders.of(this).get(ItemViewModel::class.java)

        viewModel.itemsController = itemsController
        viewModel.projectId = projectId
        viewModel.token = token
        if (isUpdateMode) {
            viewModel.setupUpdateMode(itemId?.toLong() ?: 0L)
        }

        //データを新規保存した場合に呼ばれる。多分、ViewModelに持つ必要のないメソッド
        viewModel.completeSave
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy {
                (requireActivity() as ItemActivity).transitionItemListScreen()
            }
            .addTo(disposables)


        //データを更新した場合に呼ばれる。多分、ViewModelに持つ必要のないメソッド
        viewModel.completeUpdate
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy {
                ItemActivity.setRestartFlag()
                requireActivity().finish()
            }
            .addTo(disposables)

        //バインドの設定
        bind = DataBindingUtil.inflate(inflater, R.layout.view_items_0_form, container, false)
        bind.vm = viewModel
        bind.scrollView.hideKeyboardWhenTouch(this)

        subscribeFormEvent()
        vibrator = requireActivity().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        Log.d("ItemEditorFragment.onCreateView()","end")
        return bind.root
    }

    /**
     * 引数の取得
     */
    private fun setArgs() {
        var ret = true

        //パラメータ―の取得
        val extras = activity?.intent?.extras
        if (extras?.getString("token") != null) {
            token = extras.getString("token").toString()
        }
        if (extras?.getString("project_id") != null) {
            projectId = extras.getString("project_id").toString()
        }
        if (extras?.getString("item_id") != null) {
            itemId = extras.getString("item_id").toString()
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //レイアウトの設置位置の設定
        Single.fromCallable {
            FieldController(requireContext(),projectId).getFieldRecords()
        }.observeOn(AndroidSchedulers.mainThread())
            .subscribeBy {fieldList->
                if( fieldList.isNotEmpty() ) {
                    fieldList.forEachIndexed{ index,field ->
                        //入力用のフィールドを追加する
                        addField(field,index)
                    }

                    readItem()
                }
                else {
                    throw Exception()
                }
            }
            .addTo(disposables)
    }

    /**
     * カレンダー、時刻、QRコードなどの子ウインドウからのイベントを取得する。
     */
    private fun subscribeFormEvent() {

        viewModel.dateEvent
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy {
                setClickDateTime(it)
            }
            .addTo(disposables)

        viewModel.timeEvent
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy {
                setClickTime(it)
            }
            .addTo(disposables)

        viewModel.qrEvent
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy {
                //QRコードの処理
                val extra = "colNumber" to it.fieldNumber.toString()
                startActivityWithResult(QRActivity::class.java, QRActivity.QR_CODE, extra) { r ->
                    val qr = r.data?.getStringExtra("qr_code")
                    it.text.set(qr)
                }
            }
            .addTo(disposables)

        viewModel.qrKenpinEvent
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy {
                val extra = "colNumber" to it.fieldNumber.toString()
                startActivityWithResult(QRActivity::class.java, QRActivity.QR_CODE_KENPIN, extra) { r ->
                    val qr = r.data?.getStringExtra("qr_code")

                    try {
                        itemsController.qrCodeCheck( qr )
                        it.text.set(qr)
                    }
                    catch ( ex : ItemsController.ItemCheckException ) {
                        // 設置不可の時
                        Toast.makeText(context, ex.message, Toast.LENGTH_LONG).show()
                    }

                }
            }
            .addTo(disposables)

        viewModel.qrTekkyoEvent
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy {
                val extra = "colNumber" to it.fieldNumber.toString()
                startActivityWithResult(QRActivity::class.java, QRActivity.QR_CODE_TEKKYO, extra) { r ->
                    val qr = r.data?.getStringExtra("qr_code")

                    try {
                        itemsController.rmQrCodeCheck( qr )
                        it.text.set(qr)
                    }
                    catch ( ex : ItemsController.ItemCheckException ) {
                        // 撤去不可の時
                        Toast.makeText(BlasRest.context, ex.message, Toast.LENGTH_LONG).show()
                    }

                }
            }
            .addTo(disposables)

        viewModel.locationEvent
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy { field ->
                requestPermissions(arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION)) {
                    (requireActivity() as ItemActivity).fetchLocationAddress {address ->
                        field.text.set(address)
                    }
                }
            }
            .addTo(disposables)

        viewModel.accountNameEvent
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy {
                it.text.set(userMap["name"])
            }
            .addTo(disposables)
    }

    /**
     * シングルセレクトの編集フィールド処理
     */
    private fun whenSingleSelect(singleColCnt:Int, fieldValue:String? ,parentFieldId:Int) {
        var choice = singleSelectChoiceMap[singleColCnt]
        val model = singleSelectMap[singleColCnt]

        if( model != null ) {

            var position = 0
            var choiceIndex = 0
            if( null != choice ) {


                if( 0 != parentFieldId ) {
                    // 連動パラメータの時
                    val parentSpinner = singleSelectSpinner[parentFieldId]
                    val parentValue = parentSpinner?.selectedItem as String

                    Log.d("parentValue",parentValue)

                    val json = JSONObject(choice)
                    choice = json.getString(parentValue)
                }
                val aChoice = choice?.split(",")

                run loop@ {
                    if (aChoice != null) {
                        aChoice.forEachIndexed { index, s ->
                            if( s == fieldValue ) {
                                choiceIndex = index
                                return@loop
                            }
                        }
                    }
                }

                position = choiceIndex
            }

            model.spinner.setSelection(position)
        }


    }


    private fun readItem() {
        Log.d("readItem()","start")
        var singleColCnt = 1

        //サービスが自動送信した後、仮IDが本IDに変わっていたら、画面のIDも変更する
        //仮IDのままの場合はnullが返る
        val newItemId = itemsController.getRealItemId(itemId.toString())
        if(newItemId != null) {
            itemId = newItemId
            itemsController.deleteCacheRecord(itemId.toString())
        }

        itemId?.also { id ->
            Single
                .fromCallable { itemsController.search(id.toLong()) }
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy {
                    fieldValues.putAll(it.first())
                    viewModel.fields.forEachIndexed { index, any ->
                        val field = (any as FieldModel)
                        val columnName = "fld${field.col}"
                        val value = fieldValues[columnName]?.replace("\\r","")

                        with(field.javaClass.canonicalName!!) {
                            when {
                                contains("FieldSingleSelect") -> {
                                    val fieldSingleSelect = field as FieldSingleSelect

                                    //シングルセレクトの処理
                                    whenSingleSelect(singleColCnt,value,fieldSingleSelect.parentFieldId!!)
                                    singleColCnt ++
                                }
                                else -> {
                                    //その他の処理
                                    field.setValue(value)
                                }
                            }
                        }
                    }
                }
                .addTo(disposables)
        }
        Log.d("readItem()","end")
    }


    private fun addField(field: LdbFieldRecord, cellNumber: Int) {

        val name = field.name!!
        val mustInput = field.essential == 1    //必須入力
        var rootView:View? = null
        var fieldModel:FieldModel? = null

        //edit_id　0は非表示、1は表示だけ 2は編集可能という意味っぽい
        if( 1 == field.edit_id ) {
            val l: ViewItems0ReadOnlySingleLineBinding =
                DataBindingUtil.inflate(layoutInflater, R.layout.view_items_0_read_only_single_line, null, false)
            l.model = FieldText(cellNumber,field.col!!, name, mustInput)
            rootView = l.root
            fieldModel = l.model
        }
        else {
            when (field.type.toString()) {
                FieldType.TEXT_FIELD -> {
                    val l: ViewItems1TextSingleLineBinding =
                        DataBindingUtil.inflate(layoutInflater, R.layout.view_items_1_text_single_line, null, false)
                    l.model = FieldText(cellNumber,field.col!!, name, mustInput)
                    rootView = l.root
                    fieldModel = l.model
                }
                FieldType.TEXT_AREA -> {
                    val l: ViewItems2TextMultiLineBinding =
                        DataBindingUtil.inflate(layoutInflater, R.layout.view_items_2_text_multi_line, null, false)
                    l.model = FieldText(cellNumber,field.col!!, name, mustInput)
                    rootView = l.root
                    fieldModel = l.model
                }
                FieldType.DATE_TIME -> {
                    val l: ViewItems3DateBinding =
                        DataBindingUtil.inflate(layoutInflater, R.layout.view_items_3_date, null, false)
                    l.model = FieldText(cellNumber,field.col!!, name, mustInput)
                    l.vm = viewModel
                    rootView = l.root
                    fieldModel = l.model
                }
                FieldType.TIME -> {
                    val l: ViewItems4TimeBinding =
                        DataBindingUtil.inflate(layoutInflater, R.layout.view_items_4_time, null, false)
                    l.model = FieldText(cellNumber,field.col!!, name, mustInput)
                    l.vm = viewModel
                    rootView = l.root
                    fieldModel = l.model
                }
                FieldType.SINGLE_SELECTION -> {

                    val l: ViewItems5SelectBinding =
                        DataBindingUtil.inflate(
                            layoutInflater, R.layout.view_items_5_select, null, false
                        )
                    val model = FieldSingleSelect(cellNumber,field.col!!, name, mustInput,field.parent_field_id)

                    if( 0 != field.parent_field_id ) {
                        // 連動パラメータ
                        val jsonChoice = JSONObject(field.choice)
                        val parents = jsonChoice.names()

                        val child = jsonChoice.getString(parents[0].toString())
                        model.values = child.split(",").toMutableList()

                        l.spinner.createChildren(child, model)

                        val parentSpinner = singleSelectSpinner[field.parent_field_id!!]

                        if (parentSpinner != null) {
                            // 親項目が変わったら子も連動して変える(親のセレクトチェンジイベントに細工する)
                            val listener = parentSpinner.onItemSelectedListener as SpinnerItemSelectedListener
                            listener.optionalAction = { parent, position ->

                                if (parent?.adapter is ArrayAdapter<*>) {// ArrayAdapter<String>
                                    val item = parent.adapter.getItem(position) as String
                                    try {
                                        val newChild = jsonChoice.getString(item)
                                        model.values = newChild.split(",").toMutableList()

                                        Log.d("child",newChild)
                                        (l.spinner.adapter as ArrayAdapter<*>).clear()
                                        l.spinner.createChildren(newChild, model)
                                        l.spinner.setSelection(0)
                                        model.selectedIndex.set(position)
                                    } catch (e: Exception) {
                                        val ad = ArrayAdapter<String>(requireContext(), android.R.layout.simple_spinner_item)
                                        ad.addAll("")
                                        l.spinner.adapter = ad
                                    }
                                }
                            }
                        }
                    }
                    else  {
                        l.spinner.createChildren(field.choice, model)
                    }

                    l.spinner.onItemSelectedListener = SpinnerItemSelectedListener().apply {
                        this.selectAction = {
                            model.selectedIndex.set(it)
                        }
                    }


                    l.model = model
                    l.vm = viewModel
                    singleSelectSpinner[field.field_id!!] = l.spinner

                    if (l.spinner.count > 0) {
                        l.spinner.setSelection(0)
                    }

                    //一回シングルセレクトをマップに格納
                    singleSelectMap[singleCnt] = l
                    //チョイスの値を格納
                    singleSelectChoiceMap[singleCnt] = field.choice
                    singleCnt++

                    rootView = l.root
                    fieldModel = l.model

                }
                FieldType.MULTIPLE_SELECTION -> {
                    val l: ViewItems6SelectMultiBinding =
                        DataBindingUtil.inflate(layoutInflater, R.layout.view_items_6_select_multi, null, false)
                    val model = FieldMultiSelect(cellNumber,field.col!!, name, mustInput)
                    l.model = model
                    l.vm = viewModel
                    l.checkBoxGroup.createChildren(layoutInflater, field.choice, model)
                    rootView = l.root
                    fieldModel = l.model
                }
                FieldType.LOCATION -> {
                    val l: ViewItems7LocationBinding =
                        DataBindingUtil.inflate(layoutInflater, R.layout.view_items_7_location, null, false)
                    l.model = FieldText(cellNumber,field.col!!, name, mustInput)
                    l.vm = viewModel
                    rootView = l.root
                    fieldModel = l.model
                }
                FieldType.KENPIN_RENDOU_QR -> {
                    val l: ViewItems8QrKenpinBinding =
                        DataBindingUtil.inflate(layoutInflater, R.layout.view_items_8_qr_kenpin, null, false)
                    l.model = FieldText(cellNumber,field.col!!, name, mustInput)
                    l.vm = viewModel
                    rootView = l.root
                    fieldModel = l.model
                }
                FieldType.SIG_FOX -> {
                    val l: ViewItems9SigfoxBinding =
                        DataBindingUtil.inflate(layoutInflater, R.layout.view_items_9_sigfox, null, false)
                    l.model = FieldSigFox(cellNumber,field.col!!, name, mustInput)
                    rootView = l.root
                    fieldModel = l.model
                }
                FieldType.QR_CODE -> {
                    val l: ViewItemsAQrBinding =
                        DataBindingUtil.inflate(layoutInflater, R.layout.view_items_a_qr, null, false)
                    l.model = FieldText(cellNumber,field.col!!, name, mustInput)
                    l.vm = viewModel
                    rootView = l.root
                    fieldModel = l.model
                }
                FieldType.TEKKILYO_RENDOU_QR -> {
                    val l: ViewItemsBQrTekkyoBinding =
                        DataBindingUtil.inflate(layoutInflater, R.layout.view_items_b_qr_tekkyo, null, false)
                    l.model = FieldText(cellNumber,field.col!!, name, mustInput)
                    l.vm = viewModel
                    rootView = l.root
                    fieldModel = l.model
                }
                FieldType.ACOUNT_NAME -> {
                    val l: ViewItemsCAccountBinding =
                        DataBindingUtil.inflate(layoutInflater, R.layout.view_items_c_account, null, false)
                    l.model = FieldText(cellNumber,field.col!!, name, mustInput)
                    l.vm = viewModel
                    rootView = l.root
                    fieldModel = l.model
                }
                FieldType.CHECK_VALUE -> {
                    val l: CellCheckvalueBinding =
                        DataBindingUtil.inflate(layoutInflater, R.layout.cell_checkvalue, null, false)
                    l.model = FieldCheckText(cellNumber,field.col!!, name, mustInput)
                    rootView = l.root
                    fieldModel = l.model
                }
                else -> { null }
            }
        }

        bind.innerView.addView(rootView)
        viewModel.fields.add(fieldModel)

    }


    private fun Spinner.createChildren(separatedText: String?, model: FieldSingleSelect) {
        separatedText?.also {
            val list = it.split(",")
            model.values.addAll(list)
            val ad = ArrayAdapter<String>(requireContext(), android.R.layout.simple_spinner_item)
            ad.addAll(list)
            this.adapter = ad
        }
    }

    private fun LinearLayout.createChildren(inflater: LayoutInflater, separatedText: String?, model: FieldMultiSelect) {
        separatedText?.also {
            val list = it.split(",")
            model.values.addAll(list)
            list.forEachIndexed { index, s ->
                val layout = DataBindingUtil.inflate<ViewItemsCheckboxBinding>(inflater, R.layout.view_items_checkbox, null, false)
                val selected = ObservableBoolean(false)
                layout.selected = selected
                layout.model = model
                layout.checkBox.text = s
                model.selected(index, selected)
                this.addView(layout.root)
            }
        }
    }


    /**
     * 日付フィールドタップ時の処理
     */
    fun setClickDateTime(field: FieldText) {
        //editTextタップ時の処理
        val dtp = DatePickerDialog(
            requireContext(),
            DatePickerDialog.OnDateSetListener { view, y, m, d ->
                //フォーマットを作成
                field.text.set(String.format("%d/%02d/%02d", y, m + 1, d))
            },
            year,
            month,
            day
        )
        dtp.show()
    }

    /**
     * 時間フィールドタップ時の処理
     */
    private fun setClickTime(field: FieldText) {
        //editTextタップ時の処理
        val tp = TimePickerDialog(
            context,
            TimePickerDialog.OnTimeSetListener { view, hour, minute ->
                field.text.set(String.format("%02d:%02d", hour, minute))
            }, hour, minute, true
        )
        tp.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        disposables.dispose()
        singleSelectList.forEach{
            it.values.clear()
        }
    }

    override fun onPause() {
        super.onPause()
        singleSelectList.forEach{
            it.values.clear()
        }
    }

    inner class SpinnerItemSelectedListener : AdapterView.OnItemSelectedListener {

        var selectAction: ((position: Int) -> Unit)? = null
        var optionalAction: ((parent: AdapterView<*>?, position: Int) -> Unit)? = null
        override fun onNothingSelected(parent: AdapterView<*>?) {
        }

        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            selectAction?.invoke(position)
            optionalAction?.invoke(parent, position)
        }
    }
}


