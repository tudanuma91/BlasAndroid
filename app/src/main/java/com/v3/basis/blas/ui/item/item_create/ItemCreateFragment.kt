package com.v3.basis.blas.ui.item.item_create


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
import com.v3.basis.blas.ui.ext.startActivityWithResult
import com.v3.basis.blas.ui.item.common.*
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
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
                     playSoundAndVibe()
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
                        // TODO:福田さん　エラーのためいったんコメントアウト
                        // playSoundAndVibe()
                    }
                    catch ( ex : ItemsController.ItemCheckException ) {
                        // 設置不可の時
                        Toast.makeText(BlasRest.context, ex.message, Toast.LENGTH_LONG).show()
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
                        // TODO:福田さん　エラーのためいったんコメントアウト
                        // playSoundAndVibe()
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
            .subscribeBy {
                //TODO
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

    private fun playSoundAndVibe() {

        vibrationEffect =
            VibrationEffect.createOneShot(300, VibrationEffect.DEFAULT_AMPLITUDE)
        vibrator.vibrate(vibrationEffect)
        // tone.startTone(ToneGenerator.TONE_DTMF_S,200)
        //tone.startTone(ToneGenerator.TONE_CDMA_ANSWER,200)
        playTone(ToneGenerator.TONE_CDMA_ANSWER)
    }

    private fun playTone(mediaFileRawId: Int) {

        try {
            if (tone == null) {
                tone = ToneGenerator(AudioManager.STREAM_SYSTEM, ToneGenerator.MAX_VOLUME)
            }
            tone?.also {
                it.startTone(mediaFileRawId, 200)
                val handler = Handler(Looper.getMainLooper())
                handler.postDelayed({
                    if (it != null) {
                        Log.d("FixtureActivity", "ToneGenerator released")
                        it.release()
                        tone = null
                    }
                }, 200)
            }
        } catch (e: Exception) {
            Log.d("FixtureActivity", "Exception while playing sound:$e")
        }
    }

    private fun readItem() {
        Log.d("readItem()","start")

        itemId?.also { id ->
            Single
                .fromCallable { itemsController.search(id.toLong()) }
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy {
                    fieldValues.putAll(it.first())
                    viewModel.fields.forEachIndexed { index, any ->
                        val field = (any as FieldModel)
                        val columnName = "fld${index + 1}"
                        val value = fieldValues[columnName]
                        field.setValue(value)
                    }
                }
                .addTo(disposables)
        }
        Log.d("readItem()","end")
    }

/*
    private fun getSuccess(result: JSONObject?) {
        result?.also {

            val fields = GsonBuilder().serializeNulls().create().fromJson(result.toString(), FieldsModel::class.java)
            fields.records.sortedBy { it.Field.col }.map { it.Field }.toMutableList().also {
                this.fields.addAll(it)
            }
            this.fields.forEachIndexed { index, fieldDataModel ->
                addField(fieldDataModel, index)
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
                            val columnName = "fld${index + 1}"
                            val value = fieldValues[columnName]
                            field.setValue(value)
                        }
                    }
                    .addTo(disposables)
            }
        }
    }
*/
/*
    private fun addField(field: FieldDataModel, cellNumber: Int) {

        val name = field.name!!
        val mustInput = field.essential == 1

        val view = when (field.type.toString()) {
            FieldType.TEXT_FIELD -> {
                val l: ViewItems1TextSingleLineBinding =
                    DataBindingUtil.inflate(layoutInflater, R.layout.view_items_1_text_single_line, null, false)
                l.model = FieldText(cellNumber, name, mustInput)
                l.root to l.model
            }
            FieldType.TEXT_AREA -> {
                val l: ViewItems2TextMultiLineBinding =
                    DataBindingUtil.inflate(layoutInflater, R.layout.view_items_2_text_multi_line, null, false)
                l.model = FieldText(cellNumber, name, mustInput)
                l.root to l.model
            }
            FieldType.DATE_TIME -> {
                val l: ViewItems3DateBinding =
                    DataBindingUtil.inflate(layoutInflater, R.layout.view_items_3_date, null, false)
                l.model = FieldText(cellNumber, name, mustInput)
                l.vm = viewModel
                l.root to l.model
            }
            FieldType.TIME -> {
                val l: ViewItems4TimeBinding =
                    DataBindingUtil.inflate(layoutInflater, R.layout.view_items_4_time, null, false)
                l.model = FieldText(cellNumber, name, mustInput)
                l.vm = viewModel
                l.root to l.model
            }
            FieldType.SINGLE_SELECTION -> {
                val l: ViewItems5SelectBinding =
                    DataBindingUtil.inflate(layoutInflater, R.layout.view_items_5_select, null, false)
                val model = FieldSingleSelect(cellNumber, name, mustInput)
                l.model = model
                l.vm = viewModel
                l.radioGroup.createChildren(layoutInflater, field.choice, model)
                l.root to l.model
            }
            FieldType.MULTIPLE_SELECTION -> {
                val l: ViewItems6SelectMultiBinding =
                    DataBindingUtil.inflate(layoutInflater, R.layout.view_items_6_select_multi, null, false)
                val model = FieldMultiSelect(cellNumber, name, mustInput)
                l.model = model
                l.vm = viewModel
                l.checkBoxGroup.createChildren(layoutInflater, field.choice, model)
                l.root to l.model
            }
            FieldType.LOCATION -> {
                val l: ViewItems7LocationBinding =
                    DataBindingUtil.inflate(layoutInflater, R.layout.view_items_7_location, null, false)
                l.model = FieldText(cellNumber, name, mustInput)
                l.vm = viewModel
                l.root to l.model
            }
            FieldType.KENPIN_RENDOU_QR -> {
                val l: ViewItems8QrKenpinBinding =
                    DataBindingUtil.inflate(layoutInflater, R.layout.view_items_8_qr_kenpin, null, false)
                l.model = FieldText(cellNumber, name, mustInput)
                l.vm = viewModel
                l.root to l.model
            }
            FieldType.SIG_FOX -> {
                val l: ViewItems9SigfoxBinding =
                    DataBindingUtil.inflate(layoutInflater, R.layout.view_items_9_sigfox, null, false)
                l.model = FieldSigFox(cellNumber, name, mustInput)
                l.root to l.model
            }
            FieldType.QR_CODE -> {
                val l: ViewItemsAQrBinding =
                    DataBindingUtil.inflate(layoutInflater, R.layout.view_items_a_qr, null, false)
                l.model = FieldText(cellNumber, name, mustInput)
                l.vm = viewModel
                l.root to l.model
            }
            FieldType.TEKKILYO_RENDOU_QR -> {
                val l: ViewItemsBQrTekkyoBinding =
                    DataBindingUtil.inflate(layoutInflater, R.layout.view_items_b_qr_tekkyo, null, false)
                l.model = FieldText(cellNumber, name, mustInput)
                l.vm = viewModel
                l.root to l.model
            }
            FieldType.ACOUNT_NAME -> {
                val l: ViewItemsCAccountBinding =
                    DataBindingUtil.inflate(layoutInflater, R.layout.view_items_c_account, null, false)
                l.model = FieldText(cellNumber, name, mustInput)
                l.vm = viewModel
                l.root to l.model
            }
            FieldType.CHECK_VALUE -> {
                val l: CellCheckvalueBinding =
                    DataBindingUtil.inflate(layoutInflater, R.layout.cell_checkvalue, null, false)
                l.model = FieldCheckText(cellNumber, name, mustInput)
                l.root to l.model
			}
            else -> { null }
        }
        view?.also {
            bind.innerView.addView(it.first)
            viewModel.fields.add(it.second)
        }
    }
*/

    // TODO:これどうにかならんか？？
    private fun addField2(field: LdbFieldRecord, cellNumber: Int) {

        val name = field.name!!
        val mustInput = field.essential == 1

        val view = if( 1 == field.edit_id ) {
            val l: ViewItems0ReadOnlySingleLineBinding =
                DataBindingUtil.inflate(layoutInflater, R.layout.view_items_0_read_only_single_line, null, false)
            l.model = FieldText(cellNumber, name, mustInput)
            l.root to l.model
        }
        else {
            when (field.type.toString()) {
                FieldType.TEXT_FIELD -> {
                    val l: ViewItems1TextSingleLineBinding =
                        DataBindingUtil.inflate(layoutInflater, R.layout.view_items_1_text_single_line, null, false)
                    l.model = FieldText(cellNumber, name, mustInput)
                    l.root to l.model

                }
                FieldType.TEXT_AREA -> {
                    val l: ViewItems2TextMultiLineBinding =
                        DataBindingUtil.inflate(layoutInflater, R.layout.view_items_2_text_multi_line, null, false)
                    l.model = FieldText(cellNumber, name, mustInput)
                    l.root to l.model
                }
                FieldType.DATE_TIME -> {
                    val l: ViewItems3DateBinding =
                        DataBindingUtil.inflate(layoutInflater, R.layout.view_items_3_date, null, false)
                    l.model = FieldText(cellNumber, name, mustInput)
                    l.vm = viewModel
                    l.root to l.model
                }
                FieldType.TIME -> {
                    val l: ViewItems4TimeBinding =
                        DataBindingUtil.inflate(layoutInflater, R.layout.view_items_4_time, null, false)
                    l.model = FieldText(cellNumber, name, mustInput)
                    l.vm = viewModel
                    l.root to l.model
                }
                FieldType.SINGLE_SELECTION -> {
                    val l: ViewItems5SelectBinding =
                        DataBindingUtil.inflate(layoutInflater, R.layout.view_items_5_select, null, false)
                    val model = FieldSingleSelect(cellNumber, name, mustInput)
                    l.model = model
                    l.vm = viewModel
                    l.radioGroup.createChildren(layoutInflater, field.choice, model)
                    l.root to l.model

                }
                FieldType.MULTIPLE_SELECTION -> {
                    val l: ViewItems6SelectMultiBinding =
                        DataBindingUtil.inflate(layoutInflater, R.layout.view_items_6_select_multi, null, false)
                    val model = FieldMultiSelect(cellNumber, name, mustInput)
                    l.model = model
                    l.vm = viewModel
                    l.checkBoxGroup.createChildren(layoutInflater, field.choice, model)
                    l.root to l.model
                }
                FieldType.LOCATION -> {
                    val l: ViewItems7LocationBinding =
                        DataBindingUtil.inflate(layoutInflater, R.layout.view_items_7_location, null, false)
                    l.model = FieldText(cellNumber, name, mustInput)
                    l.vm = viewModel
                    l.root to l.model
                }
                FieldType.KENPIN_RENDOU_QR -> {
                    val l: ViewItems8QrKenpinBinding =
                        DataBindingUtil.inflate(layoutInflater, R.layout.view_items_8_qr_kenpin, null, false)
                    l.model = FieldText(cellNumber, name, mustInput)
                    l.vm = viewModel
                    l.root to l.model
                }
                FieldType.SIG_FOX -> {
                    val l: ViewItems9SigfoxBinding =
                        DataBindingUtil.inflate(layoutInflater, R.layout.view_items_9_sigfox, null, false)
                    l.model = FieldSigFox(cellNumber, name, mustInput)
                    l.root to l.model
                }
                FieldType.QR_CODE -> {
                    val l: ViewItemsAQrBinding =
                        DataBindingUtil.inflate(layoutInflater, R.layout.view_items_a_qr, null, false)
                    l.model = FieldText(cellNumber, name, mustInput)
                    l.vm = viewModel
                    l.root to l.model
                }
                FieldType.TEKKILYO_RENDOU_QR -> {
                    val l: ViewItemsBQrTekkyoBinding =
                        DataBindingUtil.inflate(layoutInflater, R.layout.view_items_b_qr_tekkyo, null, false)
                    l.model = FieldText(cellNumber, name, mustInput)
                    l.vm = viewModel
                    l.root to l.model
                }
                FieldType.ACOUNT_NAME -> {
                    val l: ViewItemsCAccountBinding =
                        DataBindingUtil.inflate(layoutInflater, R.layout.view_items_c_account, null, false)
                    l.model = FieldText(cellNumber, name, mustInput)
                    l.vm = viewModel
                    l.root to l.model
                }
                FieldType.CHECK_VALUE -> {
                    val l: CellCheckvalueBinding =
                        DataBindingUtil.inflate(layoutInflater, R.layout.cell_checkvalue, null, false)
                    l.model = FieldCheckText(cellNumber, name, mustInput)
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


    private fun RadioGroup.createChildren(inflater: LayoutInflater, separatedText: String?, model: FieldSingleSelect) {
        separatedText?.also {
            val list = it.split(",")
            model.values.addAll(list)
            list.forEachIndexed { index, s ->
                val layout = DataBindingUtil.inflate<ViewItemsRadioBinding>(inflater, R.layout.view_items_radio, null, false)
                layout.idx = index
                layout.model = model
                layout.text = s
                this.addView(layout.root, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
            }
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
//    private fun getSuccess(result: JSONObject?) {
//        if (result != null) {
//
//            val fields = Gson().fromJson(result.toString(), FieldsModel::class.java)
//            fields.records?.sortedBy { it.col }?.toMutableList()?.also { this.fields.addAll(it) }
//            this.fields.forEach { addField(it) }
//
//
//            //        //カラム順に並べ替える
//            //        var cnt = 1
//            //        var radioCount = 1
//            //        var checkCount = 1
//            //colによる並び替えが発生しているため、ソートを行う
//            val sortFormFieldList = helper.createFormField(result)
//
////            val formFields = sortFormFieldList.values.sortedBy { it["field_col"]!!.toInt() }
//
////            formFields.forEach {
////                /**
////                 * formInfoには以下の情報が入っている。
////                 * ・title => 項目のタイトル
////                 * ・type => 項目のタイプ(日付入力など)
////                 * ・choiceValue => 項目が持つ選択肢
////                 * ・require => 項目がnullが許容するかの定義
////                 * ・unique => 項目が重複を許可するかの定義
////                 */
////                val formInfo = formAction.typeCheck(it)
////                idMap.set(cnt.toString(),formInfo.fieldId.toString() )
////                //先に項目のタイトルをセットする
////                val formSectionTitle = formAction.createFormSectionTitle(layoutParams, formInfo)
////                //formSectionTitle.setError("入力必須です")
////                textViewMap.set(cnt.toString(), formSectionTitle)
////                rootView.addView(formSectionTitle)
////
////                //フォームの項目の情報をメンバ変数に格納
////                val typeMap = formAction.createFormInfoMap(formInfo)
////
////                formInfoMap.set(key = "${cnt}", value = typeMap)
////
////                when (formInfo.type) {
////                    FieldType.TEXT_FIELD -> {
////                        //自由入力(複数行)
////                        //editTextを作成
////                        val formPart = formAction.createTextField(layoutParams, cnt, formInfo)
////                        rootView.addView(formPart)
////                        //配列にeditTextの情報を格納。
////                        editMap.set(key = "col_${cnt}", value = formPart)
////                    }
////
////                    FieldType.TEXT_AREA -> {
////                        //自由入力(複数行)
////                        //editTextを作成
////                        val formPart = formAction.createTextAlea(layoutParams, cnt, formInfo)
////                        rootView.addView(formPart)
////                        //配列にeditTextの情報を格納。
////                        editMap.set(key = "col_${cnt}", value = formPart)
////                    }
////
////                    FieldType.DATE_TIME -> {
////                        //日付入力
////                        //editTextを作成
////                        var formPart = formAction.createDateTime(layoutParams, cnt, formInfo)
////                        formPart = setClickDateTime(formPart)
////                        rootView.addView(formPart)
////
////
////                        //配列にeditTextを格納
////                        editMap.set(key = "col_${cnt}", value = formPart)
////                    }
////
////                    FieldType.TIME -> {
////                        //時間入力
////                        //editText作成
////                        var formPart = formAction.createDateTime(layoutParams, cnt, formInfo)
////                        formPart = setClickTime(formPart)
////                        rootView.addView(formPart)
////
////                        //配列にeditTextを格納
////                        editMap.set(key = "col_${cnt}", value = formPart)
////                    }
////
////                    FieldType.SINGLE_SELECTION -> {
////                        //ラジオボタンの時
////                        val formGroup = formAction.createRadioGrop(layoutParams, cnt)
////                        if(formInfo.parentFieldId == "0") {
//                            val chkValueMap = formInfo.choiceValue
////                            val colTargetPart:MutableList<String> = mutableListOf()
////                            if (chkValueMap != null) {
////                                chkValueMap.forEach {
////                                    val formPart =
//                                        formAction.createSingleSelection(
//                                            layoutParams,
//                                            it,
//                                            radioCount
//                                        )
////                                    formPart.setOnClickListener{
////                                        Log.d("デバック用のログ","${formPart.text}")
////
////                                        var colNum:String? = null
////                                        valueIdColMap.forEach{
////                                            val protoNum = it.key
////                                            colNum = formAction.getColNum(protoNum)
////                                            val colId = idMap[colNum.toString()]
////                                            var flg = false
////                                            parentMap.forEach{
////                                                //親IDが同じかつkeyWordが一致した時の処理
////                                                Log.d("値","${it}")
////                                                if(it.value["parentId"] == colId && it.value["keyWord"] == formPart.text) {
////                                                    val list = selectValueMap[it.key]!!
////                                                    list.forEach {
////                                                        //ラジオボタンを編集可能にする
////                                                        radioValue[it]!!.isEnabled = true
////                                                        Log.d("デバック用ログ", "${it}")
////                                                    }
////                                                    flg = true
////                                                }
////                                            }
////                                            if(!flg){
////                                                //親IDまたはkeyWordが不一致の処理
////                                                parentMap.forEach{
////                                                    if(it.value["parentId"] == colId.toString()){
////                                                        val list = selectValueMap[it.key]!!
////                                                        list.forEach {
////                                                            //チェックをはずす。編集不可状態にする
////                                                            radioValue[it]!!.isChecked = false
////                                                            radioValue[it]!!.isEnabled = false
////                                                            Log.d("デバック用ログ", "${it}")
////                                                        }
////                                                    }
////                                                }
////                                            }
////                                        }
////                                    }
////                                    radioValue.set(key = "${radioCount}", value = formPart)
////                                    valueIdColMap.set(key = "${cnt}_${radioCount}",value = "${formPart.text}")
////                                    colTargetPart.add(radioCount.toString())
////                                    radioCount += 1
////                                    formGroup.addView(formPart)
////                                }
////                                selectValueMap.set(cnt.toString(),colTargetPart)
////                                rootView.addView(formGroup)
////                                radioGroupMap.set(key = "col_${cnt}", value = formGroup)
////                            }
////                        }else{
////                            val information :MutableMap<String,String> = mutableMapOf()
////                            val chkValueMap = formAction.getSelectValue(formInfo.choiceValue)
////                            val parentSelect = formAction.getParentSelect(formInfo.choiceValue)
////                            val colTargetPart:MutableList<String> = mutableListOf()
////                            information.set(key = "keyWord" ,value = parentSelect.toString())
////                            information.set(key = "parentId",value = formInfo.parentFieldId.toString())
////                            parentMap.set(cnt.toString(),information)
////                            chkValueMap.forEach {
////                                val formPart =
////                                    formAction.createSingleSelection(
////                                        layoutParams,
////                                        it,
////                                        radioCount
////                                    )
////                                radioValue.set(key = "${radioCount}", value = formPart)
////                                colTargetPart.add(radioCount.toString())
////                                radioCount += 1
////                                formGroup.addView(formPart)
////                                formPart.isEnabled = false
////                            }
////                            selectValueMap.set(cnt.toString(),colTargetPart)
////                            rootView.addView(formGroup)
////                            radioGroupMap.set(key = "col_${cnt}", value = formGroup)
////                        }
////                    }
////
////                    FieldType.MULTIPLE_SELECTION -> {
////                        //チェックボックスの時
////                        val colCheckMap: MutableMap<String?, CheckBox?> = mutableMapOf()
////                        val choicevalues = formInfo.choiceValue
////                        if (choicevalues != null)
////                            choicevalues.forEach {
////                                val formPart =
////                                    formAction.createMutipleSelection(layoutParams, it, checkCount)
////                                rootView.addView(formPart)
////                                colCheckMap.set(key = "col_${cnt}_${checkCount}", value = formPart)
////                                checkCount += 1
////
////                            }
////                        checkMap.set(key = "col_${cnt}", value = colCheckMap)
////                    }
////                    FieldType.QR_CODE,
////                    FieldType.KENPIN_RENDOU_QR,
////                    FieldType.TEKKILYO_RENDOU_QR->{
////                        //QRコードの処理
////                        val layout = requireActivity().layoutInflater.inflate(R.layout.cell_qr_item, null)
////                        rootView.addView(layout)
////                        val ed = layout.findViewById<EditText>(R.id.editText)
////                        layout.findViewById<Button>(R.id.button)?.setOnClickListener{
////
////                            qrCodeView = layout.findViewById(R.id.editText)
////                            val intent = Intent(activity, QRActivity::class.java)
////                            intent.putExtra("colNumber","${cnt}")
////                            startActivityForResult(intent, QRActivity.QR_CODE)
////                        }
////                        //配列に値を格納//
////                        editMap.set(key = "col_${cnt}", value = ed)
////                    }
////
////                    FieldType.CHECK_VALUE->{
////                        //入力チェック
////                        val layout = requireActivity().layoutInflater.inflate(R.layout.cell_checkvalue, null)
////                        rootView.addView(layout)
////                        val value = layout.findViewById<EditText>(R.id.editValue)
////                        val memo = layout.findViewById<EditText>(R.id.editMemo)
////
////                        editMap.set(key = "col_${cnt}", value = value)
////                        memoMap.set(key = "col_${cnt}",value = memo)
////
////                        val information :MutableMap<String,String> = mutableMapOf()
////                        information.set(key = "parentId",value = formInfo.parentFieldId.toString())
////                        parentMap.set(cnt.toString(),information)
////
////                    }
////
////                    FieldType.ACOUNT_NAME->{
////                        //アカウント名。
////                        val layout = requireActivity().layoutInflater.inflate(R.layout.cell_account, null)
////                        rootView.addView(layout)
////                        val editAccount = layout.findViewById<EditText>(R.id.editAccount)
////                        val btn = layout.findViewById<Button>(R.id.buttonAccount)
////                        btn.setOnClickListener{
////                            editAccount.setText(userMap["name"])
////                        }
////                        editMap.set(key = "col_${cnt}", value = editAccount)
////                    }
////
////                    FieldType.SIG_FOX->{
////                        //シグフォックス。
////                        val view = TextView(activity)
////                        view.setText("シグフォックスは使用できません")
////                        //文字の色変更したい。
////                        view.setTextColor(Color.BLACK)
////                        view.setLayoutParams(layoutParams)
////                        rootView.addView(view)
////
////                    }
////                }
////
////                //フォームセクションごとにスペース入れる処理。試しに入れてみた。
////                val space = Space(activity)
////                space.setLayoutParams(layoutParamsSpace)
////                rootView.addView(space)
////                cnt += 1
////            }
////
////            //ボタンの作成処理
////            val button = Button(activity)
////            button.text = BlasDef.BTN_SAVE
////            button.setLayoutParams(layoutParams)
////            rootView.addView(button)
////
////            //ボタン押下時の処理
////            button.setOnClickListener {
////                formAction.setDefaultTitle(textViewMap,formInfoMap)
////                nullChk.clear()
////
////                parentChk = true
////                val parentErrorMap:MutableMap<String,MutableMap<String,String?>> = mutableMapOf()
////
////                val payload: MutableMap<String, String?> =
//////                    mutableMapOf("token" to token, "project_id" to projectId)
////                    mutableMapOf("project_id" to projectId)
////                var cnt = 1
////                var errorCnt = 0
////                formInfoMap.forEach {
////                    var cnt = 1
////
////                    formInfoMap.forEach {
////                        var value = ""
////                        when (it.value["type"]) {
////                            FieldType.TEXT_FIELD,
////                            FieldType.TEXT_AREA,
////                            FieldType.DATE_TIME,
////                            FieldType.TIME -> {
////                                //自由入力(1行)・自由入力(複数行)・日付入力・時間入力
////                                value = formAction.pickUpValue(editMap, cnt)
////                                payload.set("fld${cnt}", value)
////                            }
////
////                            FieldType.SIG_FOX->{
////                                payload.set("fld${cnt}", value)
////                            }
////
////                            FieldType.SINGLE_SELECTION -> {
////                                //ラジオボタン
////                                val checkedRadioId = formAction.getCheckedRadioId(radioGroupMap, cnt)
////                                val radioGrp = radioValue.get(checkedRadioId)
////                                if(radioGrp != null) {
////                                    if (radioGrp.isEnabled) {
////                                        value = formAction.getCheckedValue(radioValue, checkedRadioId)
////                                    }
////                                }
////                                payload.set("fld${cnt}", "${value}")
////                            }
////
////                            FieldType.MULTIPLE_SELECTION -> {
////                                //チェックボックス
////                                val colCheckMap = checkMap.get("col_${cnt}")
////                                value = formAction.getCheckedValues(colCheckMap)
////                                payload.set("fld${cnt}", "${value}")
////
////                            }
////                            FieldType.KENPIN_RENDOU_QR,
////                            FieldType.QR_CODE,
////                            FieldType.TEKKILYO_RENDOU_QR -> {
////                                //配列に格納した値を取得
////
////                                val colCheckMap = editMap.get("col_${cnt}")
////                                if(colCheckMap != null) {
////                                    value = colCheckMap.text.toString()
////                                    payload.set("fld${cnt}", "${value}")
////                                }
////                            }
////                            FieldType.ACOUNT_NAME->{
////                                val colCheckMap = editMap.get("col_${cnt}")
////                                if(colCheckMap != null) {
////                                    value = colCheckMap.text.toString()
////                                    payload.set("fld${cnt}", "${value}")
////                                }
////                            }
////                            FieldType.CHECK_VALUE-> {
////                                value = formAction.pickUpValue(editMap, cnt)
////                                val memoValue = memoMap["col_${cnt}"]?.text.toString()
////                                val protoMap = parentMap[cnt.toString()]
////                                val parentId = protoMap?.get("parentId")
////                                idMap.forEach{
////                                    if(it.value == parentId){
////                                        var parentValue = ""
////                                        val parentCol = it.key
////                                        when(formInfoMap[parentCol]?.get("type")){
////                                            FieldType.TEXT_FIELD,
////                                            FieldType.TEXT_AREA,
////                                            FieldType.DATE_TIME,
////                                            FieldType.TIME -> {
////                                                //自由入力(1行)・自由入力(複数行)・日付入力・時間入力
////                                                parentValue = formAction.pickUpValue(editMap, parentCol.toInt())
////                                            }
////                                            FieldType.SINGLE_SELECTION -> {
////                                                //ラジオボタン
////                                                val checkedRadioId = formAction.getCheckedRadioId(radioGroupMap, parentCol.toInt())
////                                                parentValue = formAction.getCheckedValue(radioValue, checkedRadioId)
////                                                Log.d("値チェック(ラジオボタン)","parentValue = ${parentValue}")
////                                            }
////                                            FieldType.MULTIPLE_SELECTION -> {
////                                                //チェックボックス
////                                                val colCheckMap = checkMap.get("col_${parentCol}")
////                                                parentValue = formAction.getCheckedValues(colCheckMap)
////                                                Log.d("値チェック(チェックボックス)","parentValue = ${parentValue}")
////                                            }
////                                            FieldType.KENPIN_RENDOU_QR,
////                                            FieldType.QR_CODE,
////                                            FieldType.TEKKILYO_RENDOU_QR -> {
////                                                //配列に格納した値を取得
////                                                val colCheckMap = editMap.get("col_${parentCol}")
////                                                if (colCheckMap != null) {
////                                                    parentValue = colCheckMap.text.toString()
////                                                }
////                                            }
////                                            FieldType.ACOUNT_NAME->{
////                                                val colCheckMap = editMap.get("col_${parentCol}")
////                                                if (colCheckMap != null) {
////                                                    parentValue = colCheckMap.text.toString()
////                                                }
////                                            }
////                                        }
////                                        if(parentValue != value){
////                                            val status :MutableMap<String,String?> = mutableMapOf()
////                                            status.set(key = "parentId",value = parentId)
////                                            status.set(key = "parentCol",value = parentCol)
////                                            parentErrorMap.set(cnt.toString(),status)
////                                            if(memoValue == ""){
////                                                parentChk = false
////                                                Toast.makeText(activity, getText(R.string.check_error), Toast.LENGTH_SHORT).show()
////                                            }
////                                        }
////                                    }
////                                    payload.set("fld${cnt}", "{\"value\": \"${value}\", \"memo\": \"${memoValue}\"}")
////                                }
////                            }
////                        }
////
////                        val nullChkMap: MutableMap<String, String> =
////                            formAction.chkNull(it.value["require"], value)
////                        nullChk.set(cnt, nullChkMap)
////                        cnt += 1
////                    }
////
////                }
////                errorCnt = formAction.countNullError(nullChk, textViewMap,formInfoMap)
////                Log.d("デバックログの取得","nullChk => ${nullChk}")
////                if (errorCnt == 0 && parentChk) {
////                    val d = CompositeDisposable()
////                    Completable.fromAction { ItemsController(requireContext(), projectId).create(payload) }
////                        .subscribeOn(Schedulers.newThread())
////                        .observeOn(AndroidSchedulers.mainThread())
////                        .doOnError { d.dispose() }
////                        .doOnComplete { d.dispose() }
////                        .subscribe()
////                        .addTo(d)
//////                    BlasRestItem("create", payload, ::createSuccess, ::createError).execute()
////                }
////            }
//        }
//    }

**/

    /**
     * フィールド取得失敗時
     */
/*
    fun getFail(errorCode: Int ,aplCode :Int) {
    
        var message:String? = null

        message = BlasMsg().getMessage(errorCode,aplCode)

        Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show()
        //エラーのため、データを初期化する
        //fieldMap = mutableMapOf<Int, MutableMap<String, String?>>()
    }
*/
    /**
     * データの作成失敗時の処理
     */
/*
    fun createError(errorCode: Int, aplCode:Int) {
        Log.d("sippai ", "失敗")
        var message:String? = null

        message = BlasMsg().getMessage(errorCode,aplCode)

        handler.post {
            Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show()
        }
    }
*/

    /**
     * データの作成成功時
     */
/*
    fun createSuccess(result: JSONObject) {
        Log.d("seikou ", "成功")
        Toast.makeText(activity, getText(R.string.success_data_create), Toast.LENGTH_SHORT)
            .show()
        (requireActivity() as ItemActivity).transitionItemListScreen()
    }
*/

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
    }

/*
    private fun userGetSuccess(result: JSONObject){
        val test = helper.createUserList(result)
        test.forEach{
            val map = it.value
            map.forEach{
                userMap.set(key = it.key,value = it.value)
            }
        }
        Log.d("デバックログ","ユーザの中身=>${userMap}")
    }
*/


/*
    private fun userGetFail(errorCode: Int, aplCode:Int){

    }
*/

}


