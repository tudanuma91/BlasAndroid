package com.v3.basis.blas.ui.item.item_create


import android.Manifest
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.media.AudioManager
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
 * A simple [Fragment] subclass.
 */
class ItemCreateFragment : Fragment() {

    private var receiveData : Boolean = true
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

    private lateinit var formAction: FormActionDataCreate
    private var handler = Handler()
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

    /**
    private lateinit var rootView: LinearLayout
    private var parentChk :Boolean = true
    private val toastErrorLen = Toast.LENGTH_LONG

    private lateinit var qrCodeView: EditText

    private var formInfoMap: MutableMap<String, MutableMap<String, String?>> = mutableMapOf()
    private var editMap: MutableMap<String, EditText?> = mutableMapOf()
    private var radioGroupMap: MutableMap<String, RadioGroup?> = mutableMapOf()
    private var radioValue: MutableMap<String, RadioButton?> = mutableMapOf()
    private var checkMap: MutableMap<String, MutableMap<String?, CheckBox?>> = mutableMapOf()
    private var textViewMap: MutableMap<String, TextView> = mutableMapOf()
    private var layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
    private var layoutParamsSpace = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 50)
    private var memoMap:MutableMap<String,EditText> = mutableMapOf()

    private val nullChk: MutableMap<Int, MutableMap<String, String>> = mutableMapOf()

    private val idMap:MutableMap<String,String?> = mutableMapOf()
    private val parentMap:MutableMap<String,MutableMap<String,String>> = mutableMapOf()
    private val selectValueMap:MutableMap<String,MutableList<String>> = mutableMapOf()
    private val valueIdColMap : MutableMap<String,String> = mutableMapOf()

    **/



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addTitle("projectName")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        super.onCreateView(inflater, container, savedInstanceState)
        Log.d("ItemCreateFragment.onCreateView()","start")
        initializeData()

        viewModel = ViewModelProviders.of(this).get(ItemViewModel::class.java)
        bind = DataBindingUtil.inflate(inflater, R.layout.view_items_0_form, container, false)
        bind.vm = viewModel

        bind.scrollView.hideKeyboardWhenTouch(this)

        itemsController = ItemsController(requireContext(), projectId)
        viewModel.itemsController = itemsController
        viewModel.projectId = projectId
        if (isUpdateMode) {
            viewModel.setupUpdateMode(itemId?.toLong() ?: 0L)
        }

        viewModel.completeSave
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy {
                (requireActivity() as ItemActivity).transitionItemListScreen()
            }
            .addTo(disposables)

        viewModel.completeUpdate
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy {
                ItemActivity.setRestartFlag()
                requireActivity().finish()
            }
            .addTo(disposables)

        subscribeFormEvent()
        vibrator = requireActivity().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        Log.d("ItemCreateFragment.onCreateView()","end")
        return bind.root
    }

    private fun initializeData() {

        //初期値の取得
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

        try {
            if (token != null && activity != null&& projectId != null) {
                formAction = FormActionDataCreate(token, requireActivity())
            }else{
                throw java.lang.Exception("Failed to receive internal data ")
            }
        }catch (e:Exception){
            receiveData = false
            val errorMessage = msg.createErrorMessage("getFail")
            Toast.makeText(activity, errorMessage, Toast.LENGTH_LONG).show()
        }
    }

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("ItemCreateFragment.onViewCreated()","start")

        /**
//        //コンテンツを配置するLinearLayoutを取得
//        rootView = view.findViewById<LinearLayout>(R.id.item_create_liner)
//
//        //フォームセクションごとにスペース入れる処理。試しに入れてみた。
//        val space = Space(activity)
//        space.setLayoutParams(layoutParamsSpace)
//        rootView.addView(space)
**/

        //レイアウトの設置位置の設定
        if(receiveData) {

            Single.fromCallable { FieldController(requireContext(),projectId).searchDisp() }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy {
                    if( it.isNotEmpty() ) {
                        var fieldList : List<LdbFieldRecord> = it

                        fieldList.forEachIndexed{ index,field ->
                            addField2(field,index)
                        }
                        readItem()
                    }
                    else {
                        throw Exception()
                    }
                }
                .addTo(disposables)

//            val payload = mapOf("token" to token, "project_id" to projectId)
//            val payload2 = mapOf("token" to token, "my_self" to "1")
//            BlasRestField(payload, ::getSuccess, ::getFail).execute()
//            BlasRestUser(payload2, ::userGetSuccess, ::userGetFail).execute()
        }
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

        itemId?.also { id ->
            Single
                .fromCallable { itemsController.search(id.toLong()) }
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy {
                    fieldValues.putAll(it.first())
                    viewModel.fields.forEachIndexed { index, any ->
                        val field = (any as FieldModel)
//                        val columnName = "fld${index + 1}"
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

    // TODO:これどうにかならんか？？
    private fun addField2(field: LdbFieldRecord, cellNumber: Int) {

        val name = field.name!!
        val mustInput = field.essential == 1

        val view = if( 1 == field.edit_id ) {
            val l: ViewItems0ReadOnlySingleLineBinding =
                DataBindingUtil.inflate(layoutInflater, R.layout.view_items_0_read_only_single_line, null, false)
            l.model = FieldText(cellNumber,field.col!!, name, mustInput)
            l.root to l.model
        }
        else {
            when (field.type.toString()) {
                FieldType.TEXT_FIELD -> {
                    val l: ViewItems1TextSingleLineBinding =
                        DataBindingUtil.inflate(layoutInflater, R.layout.view_items_1_text_single_line, null, false)
                    l.model = FieldText(cellNumber,field.col!!, name, mustInput)
                    l.root to l.model

                }
                FieldType.TEXT_AREA -> {
                    val l: ViewItems2TextMultiLineBinding =
                        DataBindingUtil.inflate(layoutInflater, R.layout.view_items_2_text_multi_line, null, false)
                    l.model = FieldText(cellNumber,field.col!!, name, mustInput)
                    l.root to l.model
                }
                FieldType.DATE_TIME -> {
                    val l: ViewItems3DateBinding =
                        DataBindingUtil.inflate(layoutInflater, R.layout.view_items_3_date, null, false)
                    l.model = FieldText(cellNumber,field.col!!, name, mustInput)
                    l.vm = viewModel
                    l.root to l.model
                }
                FieldType.TIME -> {
                    val l: ViewItems4TimeBinding =
                        DataBindingUtil.inflate(layoutInflater, R.layout.view_items_4_time, null, false)
                    l.model = FieldText(cellNumber,field.col!!, name, mustInput)
                    l.vm = viewModel
                    l.root to l.model
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
                    //val valueMap = mutableMapOf<String?,ViewItems5SelectBinding>()
                    //valueMap[field.choice] = l
                    //singleSelectList.add(valueMap)
                    l.root to l.model

                }
                FieldType.MULTIPLE_SELECTION -> {
                    val l: ViewItems6SelectMultiBinding =
                        DataBindingUtil.inflate(layoutInflater, R.layout.view_items_6_select_multi, null, false)
                    val model = FieldMultiSelect(cellNumber,field.col!!, name, mustInput)
                    l.model = model
                    l.vm = viewModel
                    l.checkBoxGroup.createChildren(layoutInflater, field.choice, model)
                    l.root to l.model
                }
                FieldType.LOCATION -> {
                    val l: ViewItems7LocationBinding =
                        DataBindingUtil.inflate(layoutInflater, R.layout.view_items_7_location, null, false)
                    l.model = FieldText(cellNumber,field.col!!, name, mustInput)
                    l.vm = viewModel
                    l.root to l.model
                }
                FieldType.KENPIN_RENDOU_QR -> {
                    val l: ViewItems8QrKenpinBinding =
                        DataBindingUtil.inflate(layoutInflater, R.layout.view_items_8_qr_kenpin, null, false)
                    l.model = FieldText(cellNumber,field.col!!, name, mustInput)
                    l.vm = viewModel
                    l.root to l.model
                }
                FieldType.SIG_FOX -> {
                    val l: ViewItems9SigfoxBinding =
                        DataBindingUtil.inflate(layoutInflater, R.layout.view_items_9_sigfox, null, false)
                    l.model = FieldSigFox(cellNumber,field.col!!, name, mustInput)
                    l.root to l.model
                }
                FieldType.QR_CODE -> {
                    val l: ViewItemsAQrBinding =
                        DataBindingUtil.inflate(layoutInflater, R.layout.view_items_a_qr, null, false)
                    l.model = FieldText(cellNumber,field.col!!, name, mustInput)
                    l.vm = viewModel
                    l.root to l.model
                }
                FieldType.TEKKILYO_RENDOU_QR -> {
                    val l: ViewItemsBQrTekkyoBinding =
                        DataBindingUtil.inflate(layoutInflater, R.layout.view_items_b_qr_tekkyo, null, false)
                    l.model = FieldText(cellNumber,field.col!!, name, mustInput)
                    l.vm = viewModel
                    l.root to l.model
                }
                FieldType.ACOUNT_NAME -> {
                    val l: ViewItemsCAccountBinding =
                        DataBindingUtil.inflate(layoutInflater, R.layout.view_items_c_account, null, false)
                    l.model = FieldText(cellNumber,field.col!!, name, mustInput)
                    l.vm = viewModel
                    l.root to l.model
                }
                FieldType.CHECK_VALUE -> {
                    val l: CellCheckvalueBinding =
                        DataBindingUtil.inflate(layoutInflater, R.layout.cell_checkvalue, null, false)
                    l.model = FieldCheckText(cellNumber,field.col!!, name, mustInput)
                    l.root to l.model
                }
                else -> { null }
            }

        }

        view?.also {
            bind.innerView.addView(it.first)
            viewModel.fields.add(it.second)
        }
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
            Log.d("[木島テスト]","${it.values}を削除する")
            it.values.clear()

        }
    }

    override fun onPause() {
        super.onPause()
        singleSelectList.forEach{
            Log.d("[木島テスト]","${it.values}を削除する")
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


