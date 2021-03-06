package com.v3.basis.blas.ui.item.item_editor


import android.Manifest
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Resources
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.*
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.app.ActivityCompat

import androidx.databinding.DataBindingUtil
import androidx.databinding.ObservableField
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.maps.GeoApiContext
import com.google.maps.GeocodingApi
import com.google.maps.model.LatLng
import com.v3.basis.blas.R
import com.v3.basis.blas.activity.ItemActivity
import com.v3.basis.blas.activity.QRActivity
import com.v3.basis.blas.blasclass.app.BlasMsg
import com.v3.basis.blas.blasclass.config.FieldType
import com.v3.basis.blas.blasclass.db.data.ItemsController
import com.v3.basis.blas.blasclass.db.field.FieldController
import com.v3.basis.blas.blasclass.helper.RestHelper
import com.v3.basis.blas.blasclass.ldb.LdbFieldRecord
import com.v3.basis.blas.blasclass.log.BlasLog
import com.v3.basis.blas.blasclass.service.BlasSyncMessenger
import com.v3.basis.blas.blasclass.service.SenderHandler
import com.v3.basis.blas.databinding.*
import com.v3.basis.blas.ui.ext.addTitle
import com.v3.basis.blas.ui.ext.hideKeyboardWhenTouch
import com.v3.basis.blas.ui.ext.startActivityWithResult
import com.v3.basis.blas.ui.item.common.*
import com.v3.basis.blas.ui.item.item_search_result.RowModel
import com.v3.basis.blas.ui.item.item_search_result.ViewAdapter
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.input_field23.view.*
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.withLock


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

    private lateinit var formModel: ItemViewModel
    private lateinit var form: ViewItems0FormBinding
    private val disposables = CompositeDisposable()
    private lateinit var itemsController: ItemsController

    private lateinit var vibrator: Vibrator

    //シングルセレクトを取得
    private val singleSelectMap = mutableMapOf<Int,InputField5Binding>()
    private val singleSelectChoiceMap = mutableMapOf<Int,String?>()
    private val singleSelectSpinner = mutableMapOf<Int,Spinner>()

    //GPS
    private var locationManager: LocationManager? = null
    private var gpsListener:GPSLocationListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addTitle("projectName")

        activity?.let{
            locationManager = it.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        }


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
        formModel = ViewModelProviders.of(this).get(ItemViewModel::class.java)

        formModel.itemsController = itemsController
        formModel.projectId = projectId
        formModel.token = token
        if (isUpdateMode) {
            formModel.setupUpdateMode(itemId?.toLong() ?: 0L)
        }

        //データを新規保存した場合に呼ばれる。
        formModel.completeSave
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy {
                (requireActivity() as ItemActivity).transitionItemListScreen()
            }
            .addTo(disposables)


        //データを更新した場合に呼ばれる。
        formModel.completeUpdate
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy {
                ItemActivity.setRestartFlag()
                requireActivity().finish()
            }
            .addTo(disposables)

        //バインドの設定
        form = DataBindingUtil.inflate(inflater, R.layout.view_items_0_form, container, false)
        form.vm = formModel
        form.scrollView.hideKeyboardWhenTouch(this)

        vibrator = requireActivity().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        Log.d("ItemEditorFragment.onCreateView()","end")
        return form.root
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
        //フォームを作成する
        createForms();

    }

    private fun createForms() {
        //レイアウトの設置位置の設定
        Single.fromCallable {
            FieldController(requireContext(),projectId).getFieldRecords()
        }.observeOn(AndroidSchedulers.mainThread())
            .subscribeBy {fieldList->
                if( fieldList.isNotEmpty() ) {
                    fieldList.forEachIndexed{ index,field ->
                        //子供のフィールドを取得
                        //入力用のフィールドを追加する
                        addField(field, index)
                    }

                    //親子関係構築(従属系)
                    formModel.fields.forEach {me->
                        if(me.field.parent_field_id != 0) {
                            //親フィールドを取得する
                            val parentInputField = formModel.fields.first {parent->
                                parent.field.field_id == me.field.parent_field_id
                            }

                            //親に子供を登録する
                            parentInputField.addChildField(me)
                            //子に親を登録する
                            me.addParentField(parentInputField)

                        }
                    }

                    //親子関係構築(条件付き(武内Ver))
                    formModel.fields.forEach {me->
                        //武内データ型(条件付き必須の場合)
                        if(!me.field.case_required.isNullOrBlank()) {
                            val jsonText = me.field.case_required?.replace("\\", "")
                            if(!jsonText.isNullOrBlank()) {
                                val json = JSONObject(jsonText)
                                json.keys().forEach {choice->
                                    //選択肢に紐づく親フィールド名を取得する
                                    val parentFieldNames = json.getString(choice)

                                    formModel.fields.forEach {fModel->
                                        parentFieldNames.split(",").forEach {parentFieldName->
                                            if(parentFieldName == fModel.field.name) {
                                                me.addParentField(fModel)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    //フォームに値を設定する
                    setValue()
                }
                else {
                    throw Exception()
                }
            }
            .addTo(disposables)
    }



    private fun setValue() {
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
                    formModel.fields.forEachIndexed { index, any ->
                        val field = (any as FieldModel)
                        val columnName = "fld${field.field.col}"
                        val value = fieldValues[columnName]?.replace("\\r","")
                        field.setValue(value)
                    }
                }
                .addTo(disposables)
        }
        Log.d("readItem()","end")
    }


    private fun addField(field: LdbFieldRecord, cellNumber: Int):FieldModel? {

        var rootView:View? = null
        var fieldModel:FieldModel? = null
        var inputField:FieldModel? = null
        //edit_id　0は非表示、1は表示だけ 2は編集可能という意味っぽい
        if( 1 == field.edit_id ) {
            val l: ViewItems0ReadOnlySingleLineBinding =
                DataBindingUtil.inflate(layoutInflater, R.layout.view_items_0_read_only_single_line, null, false)
            l.model = FieldText(requireContext(), layoutInflater, cellNumber, field)
            //どこでフィールドの型を認識させるべきか？朝礼前の考え事
            rootView = l.root
            fieldModel = l.model
        }
        else {
            when (field.type.toString()) {
                FieldType.TEXT_FIELD -> {
                    //自由入力(1行)
                    inputField = FieldText(requireContext(), layoutInflater, cellNumber,field)
                    //親フォームにフィールドを追加する
                    formModel.fields.add(inputField)
                    //入力フィールドを表示する
                    form.innerView.addView(inputField.layout.root)
                }

                FieldType.TEXT_AREA -> {
                    //自由入力(複数行)
                    inputField = FieldMultiText(requireContext(), layoutInflater, cellNumber,field)
                    //親フォームにフィールドを追加する
                    formModel.fields.add(inputField)
                    //入力フィールドを表示する
                    form.innerView.addView(inputField.layout.root)
                }
                // type:3 日付
                FieldType.DATE_TIME -> {
                    inputField = FieldDate(requireContext(), layoutInflater, cellNumber,field)
                    //親フォームにフィールドを追加する
                    formModel.fields.add(inputField)
                    //入力フィールドを表示する
                    form.innerView.addView(inputField.layout.root)
                    inputField.layout.text.setOnClickListener {
                        // カレンダー選択を表示
                        setClickDateTime(inputField as FieldDate)
                    }
                }
                // type:4 時間
                FieldType.TIME -> {
                    inputField = FieldTime(requireContext(), layoutInflater, cellNumber,field)
                    inputField.layout.text.setOnClickListener {
                        setClickTime(inputField as FieldTime)
                    }
                    //親フォームにフィールドを追加する
                    formModel.fields.add(inputField)
                    //入力フィールドを表示する
                    form.innerView.addView(inputField.layout.root)
                }

                FieldType.SINGLE_SELECTION -> {
                    //単一選択
                    inputField = FieldSingleSelect(requireContext(), layoutInflater, cellNumber,field)
                    //親フォームにフィールドを追加する
                    formModel.fields.add(inputField)
                    //入力フィールドを表示する
                    form.innerView.addView(inputField.layout.root)
                }

                FieldType.MULTIPLE_SELECTION -> {
                    //複数選択
                    inputField = FieldMultiSelect(requireContext(), layoutInflater, cellNumber, field)
                    //親フォームにフィールドを追加する
                    formModel.fields.add(inputField)
                    //入力フィールドを表示する
                    form.innerView.addView(inputField.layout.root)
                }
                // type:7 場所
                FieldType.LOCATION -> {
                    inputField = FieldLocation(requireContext(), layoutInflater, cellNumber, field)
                    inputField.layout.button.setOnClickListener {
                        //ボタンを押したときの処理
                        startGetGetCoord((inputField as FieldLocation).text, GPSLocationListener.ADDRESS)
                    }
                    //親フォームにフィールドを追加する
                    formModel.fields.add(inputField)
                    //入力フィールドを表示する
                    form.innerView.addView(inputField.layout.root)
                }
                // type:14  緯度
                FieldType.LAT_LOCATION -> {
                    inputField = FieldLat(requireContext(), layoutInflater, cellNumber, field)
                    inputField.layout.button.setOnClickListener {
                        //ボタンを押したときの処理
                        startGetGetCoord((inputField as FieldLat).text, GPSLocationListener.LAT)

                    }
                    //親フォームにフィールドを追加する
                    formModel.fields.add(inputField)
                    //入力フィールドを表示する
                    form.innerView.addView(inputField.layout.root)
                }
                // type:15 経度
                FieldType.LNG_LOCATION -> {
                    inputField = FieldLng(requireContext(), layoutInflater, cellNumber, field)
                    inputField.layout.button.setOnClickListener {
                        //ボタンを押したときの処理
                        startGetGetCoord((inputField as FieldLng).text, GPSLocationListener.LNG)
                    }
                    //親フォームにフィールドを追加する
                    formModel.fields.add(inputField)
                    //入力フィールドを表示する
                    form.innerView.addView(inputField.layout.root)
                }

                // type:8 QRコード(検品と連動)
                FieldType.KENPIN_RENDOU_QR -> {
                    inputField = FieldQRCodeWithKenpin(requireContext(), layoutInflater, cellNumber, field)
                    inputField.layout.button.setOnClickListener {
                        //カメラ起動
                        val extra = "colNumber" to (inputField as FieldQRCodeWithKenpin).fieldNumber.toString()
                        startActivityWithResult(QRActivity::class.java, QRActivity.QR_CODE_KENPIN, extra) { r ->
                            val qr = r.data?.getStringExtra("qr_code")
                            if(!qr.isNullOrEmpty()){
                                try {
                                    itemsController.qrCodeCheck(qr)
                                    (inputField as FieldQRCodeWithKenpin).text.set(qr)
                                } catch (ex: ItemsController.ItemCheckException) {
                                    // 設置不可の時
                                    Toast.makeText(context, ex.message, Toast.LENGTH_LONG).show()
                                }
                                catch(e:Exception) {
                                    BlasLog.trace("E", "例外が発生しました", e)
                                }
                            }
                        }
                    }

                    //親フォームにフィールドを追加する
                    formModel.fields.add(inputField)
                    //入力フィールドを表示する
                    form.innerView.addView(inputField.layout.root)
                }

                FieldType.QR_CODE -> {
                    //QRコード
                    inputField = FieldQRCode(requireContext(), layoutInflater, cellNumber, field)
                    inputField.layout.button.setOnClickListener {
                        val extra = "colNumber" to (inputField as FieldQRCode).fieldNumber.toString()
                        startActivityWithResult(QRActivity::class.java, QRActivity.QR_CODE, extra) { r ->
                            val qr = r.data?.getStringExtra("qr_code")
                            (inputField as FieldQRCode).text.set(qr)
                        }
                    }
                    //親フォームにフィールドを追加する
                    formModel.fields.add(inputField)
                    //入力フィールドを表示する
                    form.innerView.addView(inputField.layout.root)
                }

                FieldType.TEKKYO_RENDOU_QR -> {
                    //QRコード(撤去と連動)
                    inputField = FieldQRCodeWithTekkyo(requireContext(), layoutInflater, cellNumber, field)
                    inputField.layout.button.setOnClickListener {
                        //カメラ起動
                        val extra = "colNumber" to (inputField as FieldQRCodeWithTekkyo).fieldNumber.toString()
                        startActivityWithResult(QRActivity::class.java, QRActivity.QR_CODE_TEKKYO, extra) { r ->
                            val qr = r.data?.getStringExtra("qr_code")
                            if(!qr.isNullOrEmpty()){
                                try {
                                    // itemsController.qrCodeCheck(qr)
                                    itemsController.rmQrCodeCheck(qr)
                                    (inputField as FieldQRCodeWithTekkyo).text.set(qr)
                                } catch (ex: ItemsController.ItemCheckException) {
                                    // 設置不可の時
                                    Toast.makeText(context, ex.message, Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    }
                    //親フォームにフィールドを追加する
                    formModel.fields.add(inputField)
                    //入力フィールドを表示する
                    form.innerView.addView(inputField.layout.root)
                }

                // type:12 アカウント名
                FieldType.ACOUNT_NAME -> {
                    inputField = FieldAccount(requireContext(), layoutInflater, cellNumber, field)

                    //ボタンがクリックされたらログインユーザ名を表示する
                    inputField.layout.button.setOnClickListener {
                        val user = itemsController.getUserInfo()
                        (inputField as FieldAccount).text.set( user?.name )
                    }

                    // 値が入っていなかったらログインユーザ名を入れる
                    if(inputField.text.get() == "") {
                        val user = itemsController.getUserInfo()
                        inputField.text.set( user?.name )
                    }

                    //親フォームにフィールドを追加する
                    formModel.fields.add(inputField)
                    //入力フィールドを表示する
                    form.innerView.addView(inputField.layout.root)
                }
                // type:13 入力値チェック連動
                FieldType.CHECK_VALUE -> {
                    inputField = FieldCheckText(requireContext(), layoutInflater, cellNumber, field)
                    //親フォームにフィールドを追加する
                    formModel.fields.add(inputField)
                    //入力フィールドを表示する
                    form.innerView.addView(inputField.layout.root)
                }
                //type:16 入力値チェック連動_QRコード(検品と連動)
                FieldType.QR_CODE_WITH_CHECK -> {
                    inputField = FieldQRWithCheckText(requireContext(), layoutInflater, cellNumber, field)
                    //親フォームにフィールドを追加する
                    formModel.fields.add(inputField)
                    //入力フィールドを表示する
                    form.innerView.addView(inputField.layout.root)

                    inputField.layout.button.setOnClickListener {
                        val extra = "colNumber" to (inputField as FieldQRWithCheckText).fieldNumber.toString()
                        startActivityWithResult(QRActivity::class.java, QRActivity.QR_CODE, extra) { r ->
                            val qr = r.data?.getStringExtra("qr_code")
                            if(!qr.isNullOrEmpty()){
                                try {
                                    itemsController.qrCodeCheck(qr)
                                    (inputField as FieldQRWithCheckText).text.set(qr)
                                } catch (ex: ItemsController.ItemCheckException) {
                                    // 設置不可の時
                                    Toast.makeText(context, ex.message, Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    }
                }
                //type:17 現在日時
                FieldType.CURRENT_DATE_AND_TIME -> {
                    //現在日時のフォーマット yyyy/mm/dd hh:mmをボタンを押したら入力できるようにする
                    inputField = FieldCurrentDateTime(requireContext(), layoutInflater, cellNumber, field)
                    inputField.layout.button.setOnClickListener {
                        val df = SimpleDateFormat("yyyy/MM/dd HH:mm")
                        val date = Date()
                        (inputField as FieldCurrentDateTime).text.set(df.format(date))
                    }

                    //親フォームにフィールドを追加する
                    formModel.fields.add(inputField)
                    //入力フィールドを表示する
                    form.innerView.addView(inputField.layout.root)
                }

                //type:18 カテゴリ
                //FieldType.WORK_CONTENT_SELECTION,
                FieldType.CATEGORY_SELECTION -> {
                    //単一選択
                    inputField = FieldCategorySelect(requireContext(), layoutInflater, cellNumber,field)
                    //親フォームにフィールドを追加する
                    formModel.fields.add(inputField)
                    //入力フィールドを表示する
                    form.innerView.addView(inputField.layout.root)

                    // MEMO:専用DBカラム へは ItemViewModel.clickSave() で入れる
                }

                //type:19 作業者
                FieldType.WORKER_NAME -> {
                    val workers = itemsController.getWorkers(projectId.toInt())

                    inputField = FieldWorkerNameAutoComplete(requireContext(), layoutInflater, cellNumber, field,workers)

                    //親フォームにフィールドを追加する
                    formModel.fields.add(inputField)
                    //入力フィールドを表示する
                    form.innerView.addView(inputField.layout.root)

                    inputField.layout.button.setOnClickListener {

                        val user = itemsController.getUserInfo()
                        if (user != null) {
                            (inputField as FieldWorkerNameAutoComplete).setValue( user.name )
                        }
                        // MEMO:専用DBカラム へは ItemViewModel.clickSave() で入れる
                    }
                }
                //type:20 予定日
                FieldType.SCHEDULE_DATE -> {
                    inputField = FieldScheduleDate(requireContext(), layoutInflater, cellNumber, field)
                    //親フォームにフィールドを追加する
                    formModel.fields.add(inputField)
                    //入力フィールドを表示する
                    form.innerView.addView(inputField.layout.root)

                    inputField.layout.text.setOnClickListener {
                        // カレンダー選択を表示
                        setClickDateTime(inputField as FieldScheduleDate)
                        // MEMO:専用DBカラム へは ItemViewModel.clickSave() で入れる
                    }
                }

                //type:21 作業内容
                FieldType.WORK_CONTENT_SELECTION -> {
                    //単一選択
                    inputField = FieldWorkContentSelect(requireContext(), layoutInflater, cellNumber,field)
                    //親フォームにフィールドを追加する
                    formModel.fields.add(inputField)
                    //入力フィールドを表示する
                    form.innerView.addView(inputField.layout.root)
                }

                //type:22
                FieldType.ADDRESS -> {
                    inputField = FieldAddress(requireContext(), layoutInflater, cellNumber, field)
                    //親フォームにフィールドを追加する
                    formModel.fields.add(inputField)
                    //入力フィールドを表示する
                    form.innerView.addView(inputField.layout.root)
                }

                //type:23
                FieldType.EVENT_FIELD -> {
                    inputField = FieldEvent(requireContext(), layoutInflater, cellNumber, field)
                    //親フォームにフィールドを追加する
                    formModel.fields.add(inputField)
                    //入力フィールドを表示する
                    form.innerView.addView(inputField.layout.root)

                    inputField.layout.button.text = field.name

                    inputField.layout.button.setOnClickListener {
                        //疎通確認ボタン押下時
                        (inputField as FieldEvent).setValue("処理中")
                    }
                }

                // type:24 バーコード
                FieldType.BAR_CODE -> {
                    inputField = FieldBarCode(requireContext(), layoutInflater, cellNumber, field)
                    inputField.layout.button.setOnClickListener {
                        val extra = "colNumber" to (inputField as FieldBarCode).fieldNumber.toString()
                        //QRコードでバーコードも読む。
                        startActivityWithResult(QRActivity::class.java, QRActivity.QR_CODE, extra) { r ->
                            val qr = r.data?.getStringExtra("qr_code")
                            (inputField as FieldBarCode).text.set(qr)
                        }
                    }
                    //親フォームにフィールドを追加する
                    formModel.fields.add(inputField)
                    //入力フィールドを表示する
                    form.innerView.addView(inputField.layout.root)
                }
                // type:25 バーコード(検品連動)
                FieldType.KENPIN_RENDOU_BAR_CODE -> {
                    inputField =   FieldBarCodeWithKenpin(requireContext(), layoutInflater, cellNumber, field)

                    inputField.layout.button.setOnClickListener {
                        val extra = "colNumber" to (inputField as FieldBarCodeWithKenpin).fieldNumber.toString()
                        //QRコードでバーコードも読む。
                        startActivityWithResult(QRActivity::class.java, QRActivity.QR_CODE, extra) { r ->
                            val qr = r.data?.getStringExtra("qr_code")
                            if(!qr.isNullOrEmpty()){
                                try {
                                    itemsController.qrCodeCheck(qr)
                                    (inputField as FieldBarCodeWithKenpin).text.set(qr)
                                } catch (ex: ItemsController.ItemCheckException) {
                                    // 設置不可の時
                                    Toast.makeText(context, ex.message, Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    }

                    //親フォームにフィールドを追加する
                    formModel.fields.add(inputField)
                    //入力フィールドを表示する
                    form.innerView.addView(inputField.layout.root)
                }
                // type:26 バーコード(撤去連動)
                FieldType.TEKKYO_RENDOU_BAR_CODE -> {
                    inputField = FieldBarCodeWithTekkyo(requireContext(), layoutInflater, cellNumber, field)

                    inputField.layout.button.setOnClickListener {
                        //カメラ起動
                        val extra = "colNumber" to (inputField as FieldBarCodeWithTekkyo).fieldNumber.toString()
                        startActivityWithResult(QRActivity::class.java, QRActivity.QR_CODE_TEKKYO, extra) { r ->
                            val qr = r.data?.getStringExtra("qr_code")
                            if(!qr.isNullOrEmpty()){
                                try {
                                    itemsController.rmQrCodeCheck(qr)
                                    (inputField as FieldBarCodeWithTekkyo).text.set(qr)
                                } catch (ex: ItemsController.ItemCheckException) {
                                    // 設置不可の時
                                    Toast.makeText(context, ex.message, Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    }

                    //親フォームにフィールドを追加する
                    formModel.fields.add(inputField)
                    //入力フィールドを表示する
                    form.innerView.addView(inputField.layout.root)
                }

                // type:27　入力値チェック連動_バーコード(検品連動)
                FieldType.BAR_CODE_WITH_CHECK -> {
                    inputField = FieldBarCodeWithCheckText(requireContext(), layoutInflater, cellNumber, field)

                    //親フォームにフィールドを追加する
                    formModel.fields.add(inputField)
                    //入力フィールドを表示する
                    form.innerView.addView(inputField.layout.root)

                    inputField.layout.button.setOnClickListener {
                        val extra = "colNumber" to (inputField as FieldBarCodeWithCheckText).fieldNumber.toString()

                        startActivityWithResult(QRActivity::class.java, QRActivity.QR_CODE, extra) { r ->
                            val qr = r.data?.getStringExtra("qr_code")
                            if(!qr.isNullOrEmpty()){
                                try {
                                    itemsController.qrCodeCheck(qr)
                                    (inputField as FieldBarCodeWithCheckText).text.set(qr)
                                } catch (ex: ItemsController.ItemCheckException) {
                                    // 設置不可の時
                                    Toast.makeText(context, ex.message, Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    }

                }
                else -> { null }
            }
        }

        return inputField
    }


    /**
     * GPSから緯度経度を取得する。非同期。
     */
    private fun startGetGetCoord(inputText:ObservableField<String>, GeoType:Int) {
        BlasLog.trace("I","startGetGetCoord()")

        if(ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            BlasLog.trace("I","GPS権限あり")
            //GPSの権限がある場合
            gpsListener = GPSLocationListener(resources,
                inputText,
                GeoType,
                GPSLocationListener.ONCE)

            locationManager?.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f, gpsListener)
        }
        else {
            BlasLog.trace("I","GPS権限なし")
            //権限がない場合、権限をリクエストするだけ
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1000)
        }
    }


    /**
     * 日付フィールドタップ時の処理
     */
    fun setClickDateTime(field: FieldModel) {
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
     * 時刻フィールドタップ時の処理
     */
    private fun setClickTime(field: FieldModel) {
        //editTextタップ時の処理
        val tp = TimePickerDialog(context,
            TimePickerDialog.OnTimeSetListener{ view, hour, minute->
                    field.text.set(String.format("%02d:%02d",hour,minute))
            },hour,minute,true)
        tp.show()
    }

    override fun onDestroyView() {
        //ItemActivity.setRestartFlag()  新規作成時にQRコードを読み取ると画面がリセットされるバグの対応
        //requireActivity().finish()
        super.onDestroyView()
        disposables.dispose()

    }

    override fun onPause() {
        super.onPause()
        gpsListener?.let{
            locationManager?.removeUpdates(it)
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

/**
 * GPSから取得した値をフィールドにセットする。
 * field:テキストフィールド
 * kind:0 住所 1:緯度 2:経度を返す
 */
class GPSLocationListener(val resources:Resources, val field: ObservableField<String>, val kind:Int, once:Int) : LocationListener {
    companion object{
        val ADDRESS = 0
        val LAT = 1
        val LNG = 2

        val ONCE = 0
        val CONTINUE = 1

    }
    var endflg = false

    override fun onLocationChanged(location: Location?) {
        val lat = location?.latitude.toString()
        val lng = location?.longitude.toString()

        if(endflg) {
            return
        }
        endflg = true
        when(kind) {
            ADDRESS->{
                try {
                    val address = getAddressFromGeoCoord(lat, lng)
                    field.set(address)
                }
                catch(e:Exception) {
                    BlasLog.trace("E", "住所変換に失敗しました", e)
                }
            }
            LAT->{
                // 小数点は下6桁
                // field.set(lat)
                field.set("%.6f".format(lat.toFloat()))
            }
            LNG->{
                // 小数点は下6桁
                // field.set(lng)
                field.set("%.6f".format(lng.toFloat()))
            }
        }
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
    }

    override fun onProviderEnabled(provider: String?) {
    }

    override fun onProviderDisabled(provider: String?) {
    }

    /**
     * 緯度・経度を住所に変換する
     */
    fun getAddressFromGeoCoord(lat:String, lng:String):String {
        var address = ""
        val latLng = LatLng(lat.toDouble(), lng.toDouble())
        try {
            val apiKey = resources.getString(R.string.geo_api_key)
            val geoContext = GeoApiContext.Builder().apiKey(apiKey).build()
            val results = GeocodingApi.reverseGeocode(geoContext, latLng).language("ja").awaitIgnoreError()
            val gson = GsonBuilder().setPrettyPrinting().create()
            address =  gson.toJson(results[0].formattedAddress)
            address = address.replace("\"", "")
            address = address.replace("日本、", "")
        }
        catch(e:Exception) {
            BlasLog.trace("E", "緯度経度から住所への変換に失敗しました", e)
        }

        return address
    }
}

