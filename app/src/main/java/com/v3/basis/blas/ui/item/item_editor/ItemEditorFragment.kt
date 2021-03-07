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
import android.media.ToneGenerator
import android.os.*
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.databinding.ObservableBoolean
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.google.gson.GsonBuilder
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
import org.json.JSONObject
import java.text.SimpleDateFormat
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
        viewModel = ViewModelProviders.of(this).get(ItemViewModel::class.java)

        viewModel.itemsController = itemsController
        viewModel.projectId = projectId
        viewModel.token = token
        if (isUpdateMode) {
            viewModel.setupUpdateMode(itemId?.toLong() ?: 0L)
        }

        //データを新規保存した場合に呼ばれる。
        viewModel.completeSave
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy {
                (requireActivity() as ItemActivity).transitionItemListScreen()
            }
            .addTo(disposables)


        //データを更新した場合に呼ばれる。
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
        //サブスクライバの登録
        //カレンダー、時刻、QRコードなど、ボタンを押下されたとき(イベント)に値を取得する
        //フォームの処理を登録する
        subscribeFormEvent()

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
                        //入力用のフィールドを追加する
                        addField(field,index)
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

        //入力値チェック連動_QRコード(検品と連動)からカメラが起動されたとき
        viewModel.qrCheckEvent
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy {
                val extra = "colNumber" to it.fieldNumber.toString()
                startActivityWithResult(QRActivity::class.java, QRActivity.QR_CODE, extra) { r ->
                    val qr = r.data?.getStringExtra("qr_code")
                    it.text.set(qr);
                }
            }
            .addTo(disposables)

        //多分設置のことを言っている。検品ではない
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

        //撤去
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
                if(ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    //GPSの権限がある場合
                    gpsListener = GPSLocationListener(resources,
                                                      field,
                                                      GPSLocationListener.ADDRESS,
                                                      GPSLocationListener.ONCE)

                    locationManager?.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f, gpsListener)
                }
                else {
                    //権限がない場合、権限をリクエストするだけ
                    requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1000)
                }
            }
            .addTo(disposables)


        viewModel.latEvent
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy { field ->
                if(ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    //GPSの権限がある場合
                    gpsListener = GPSLocationListener(resources,
                        field,
                        GPSLocationListener.LAT,
                        GPSLocationListener.ONCE)

                    locationManager?.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f, gpsListener)
                }
                else {
                    //権限がない場合、権限をリクエストするだけ
                    requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1000)
                }
            }
            .addTo(disposables)

        viewModel.lngEvent
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy { field ->
                if(ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    //GPSの権限がある場合
                    gpsListener = GPSLocationListener(resources,
                        field,
                        GPSLocationListener.LNG,
                        GPSLocationListener.ONCE)

                    locationManager?.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f, gpsListener)
                }
                else {
                    //権限がない場合、権限をリクエストするだけ
                    requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1000)
                }
            }
            .addTo(disposables)


        viewModel.accountNameEvent
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy {
                it.text.set(userMap["name"])
            }
            .addTo(disposables)

        viewModel.currentDateTimeEvent
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy {
                val df = SimpleDateFormat("yyyy/MM/dd HH:mm")
                val date = Date()
                it.text.set(df.format(date))
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
                    viewModel.fields.forEachIndexed { index, any ->
                        val field = (any as FieldModel)
                        val columnName = "fld${field.field.col}"
                        val value = fieldValues[columnName]?.replace("\\r","")

                        with(field.javaClass.canonicalName!!) {
                            when {
                                contains("FieldSingleSelect") -> {
                                    val fieldSingleSelect = field as FieldSingleSelect

                                    //シングルセレクトの処理
                                    whenSingleSelect(singleColCnt,value,fieldSingleSelect.field.parent_field_id!!)
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
            l.model = FieldText(cellNumber,field)
            //どこでフィールドの型を認識させるべきか？朝礼前の考え事
            rootView = l.root
            fieldModel = l.model
        }
        else {
            when (field.type.toString()) {
                FieldType.TEXT_FIELD -> {
                    //自由入力(1行)
                    val l: ViewItems1TextSingleLineBinding =
                        DataBindingUtil.inflate(layoutInflater, R.layout.view_items_1_text_single_line, null, false)
                    l.model = FieldText(cellNumber,field)
                    rootView = l.root
                    fieldModel = l.model
                }
                FieldType.TEXT_AREA -> {
                    //自由入力(複数行)
                    val l: ViewItems2TextMultiLineBinding =
                        DataBindingUtil.inflate(layoutInflater, R.layout.view_items_2_text_multi_line, null, false)
                    l.model = FieldText(cellNumber,field)
                    rootView = l.root
                    fieldModel = l.model
                }
                FieldType.DATE_TIME -> {
                    //日付
                    val l: ViewItems3DateBinding =
                        DataBindingUtil.inflate(layoutInflater, R.layout.view_items_3_date, null, false)
                    l.model = FieldText(cellNumber,field)
                    l.vm = viewModel
                    rootView = l.root
                    fieldModel = l.model
                }
                FieldType.TIME -> {
                    //時間
                    val l: ViewItems4TimeBinding =
                        DataBindingUtil.inflate(layoutInflater, R.layout.view_items_4_time, null, false)
                    l.model = FieldText(cellNumber,field)
                    l.vm = viewModel
                    rootView = l.root
                    fieldModel = l.model
                }
                FieldType.WORK_CONTENT_SELECTION,
                FieldType.CATEGORY_SELECTION,
                FieldType.SINGLE_SELECTION -> {
                    //単一選択
                    val l: ViewItems5SelectBinding =
                        DataBindingUtil.inflate(
                            //作業内容、カテゴリも単一選択と同じなので、5番を使いまわす。
                            layoutInflater, R.layout.view_items_5_select, null, false
                        )
                    val model = FieldSingleSelect(cellNumber,field)

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

                    //アイテムが選択されたときに行う処理を登録する
                    l.spinner.onItemSelectedListener = SpinnerItemSelectedListener().apply {
                        this.selectAction = {
                            model.selectedIndex.set(it)
                        }
                    }

                    l.model = model
                    l.vm = viewModel
                    singleSelectSpinner[field.field_id!!] = l.spinner

                    if (l.spinner.count > 0) {
                        //あとで呼ばれるcreateForm関数内で値はセットされる。
                        //ここでは未入力のときに先頭の選択肢を表示するために0を入力している。
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
                    //複数選択
                    val l: ViewItems6SelectMultiBinding =
                        DataBindingUtil.inflate(layoutInflater, R.layout.view_items_6_select_multi, null, false)
                    val model = FieldMultiSelect(cellNumber, field)
                    l.model = model
                    l.vm = viewModel
                    l.checkBoxGroup.createChildren(layoutInflater, field.choice, model)
                    rootView = l.root
                    fieldModel = l.model
                }
                FieldType.LOCATION -> {
                    //場所
                    val l: ViewItems7LocationBinding =
                        DataBindingUtil.inflate(layoutInflater, R.layout.view_items_7_location, null, false)
                    l.model = FieldText(cellNumber, field)
                    l.vm = viewModel
                    rootView = l.root
                    fieldModel = l.model
                }

                FieldType.LAT_LOCATION -> {
                    //緯度
                    val l: ViewItems14LocationBinding =
                        DataBindingUtil.inflate(layoutInflater, R.layout.view_items_14_location, null, false)
                    l.model = FieldText(cellNumber, field)
                    l.vm = viewModel
                    rootView = l.root
                    fieldModel = l.model
                }

                FieldType.LNG_LOCATION -> {
                    //経度
                    val l: ViewItems15LocationBinding =
                        DataBindingUtil.inflate(layoutInflater, R.layout.view_items_15_location, null, false)
                    l.model = FieldText(cellNumber, field)
                    l.vm = viewModel
                    rootView = l.root
                    fieldModel = l.model
                }

                FieldType.KENPIN_RENDOU_QR -> {
                    //QRコード(検品と連動)
                    val l: ViewItems8QrKenpinBinding =
                        DataBindingUtil.inflate(layoutInflater, R.layout.view_items_8_qr_kenpin, null, false)
                    l.model = FieldText(cellNumber, field)
                    l.vm = viewModel
                    rootView = l.root
                    fieldModel = l.model
                }
                FieldType.SIG_FOX -> {
                    //シグフォックス（使っていない)
                    val l: ViewItems9SigfoxBinding =
                        DataBindingUtil.inflate(layoutInflater, R.layout.view_items_9_sigfox, null, false)
                    l.model = FieldSigFox(cellNumber, field)
                    rootView = l.root
                    fieldModel = l.model
                }
                FieldType.QR_CODE -> {
                    //QRコード
                    val l: ViewItemsAQrBinding =
                        DataBindingUtil.inflate(layoutInflater, R.layout.view_items_a_qr, null, false)
                    l.model = FieldText(cellNumber, field)
                    l.vm = viewModel
                    rootView = l.root
                    fieldModel = l.model
                }
                FieldType.TEKKYO_RENDOU_QR -> {
                    //QRコード(撤去と連動)
                    val l: ViewItemsBQrTekkyoBinding =
                        DataBindingUtil.inflate(layoutInflater, R.layout.view_items_b_qr_tekkyo, null, false)
                    l.model = FieldText(cellNumber, field)
                    l.vm = viewModel
                    rootView = l.root
                    fieldModel = l.model
                }
                FieldType.ACOUNT_NAME -> {
                    //アカウント名
                    val l: ViewItemsCAccountBinding =
                        DataBindingUtil.inflate(layoutInflater, R.layout.view_items_c_account, null, false)
                    l.model = FieldText(cellNumber, field)
                    l.vm = viewModel
                    rootView = l.root
                    fieldModel = l.model
                }
                FieldType.CHECK_VALUE -> {
                    //入力値チェック連動
                    val l: ViewItems13CheckValueBinding =
                        DataBindingUtil.inflate(layoutInflater, R.layout.view_items_13_check_value, null, false)
                    l.model = FieldCheckText(cellNumber, field)
                    rootView = l.root
                    fieldModel = l.model
                }

                //type:16 入力値チェック連動_QRコード(検品と連動)
                FieldType.QR_CODE_WITH_CHECK -> {
                    val l: ViewItems16QrCheckBinding =
                        DataBindingUtil.inflate(layoutInflater, R.layout.view_items_16_qr_check, null, false)
                    //ここは連動パラメーターをどう扱うかを考えないとダメ。
                    //TODO ここから直す
                    l.model = FieldCheckText(cellNumber, field)
                    l.vm = viewModel
                    rootView = l.root
                    fieldModel = l.model
                }
                //type:17
                FieldType.CURRENT_DATE_AND_TIME -> {
                    //現在日時のフォーマット yyyy/mm/dd hh:mmをボタンを押したら入力できるようにする
                    val l: ViewItems17CurrentdatetimeBinding =
                        DataBindingUtil.inflate(layoutInflater, R.layout.view_items_17_currentdatetime, null, false)
                    l.model = FieldText(cellNumber, field)
                    l.vm = viewModel
                    rootView = l.root
                    fieldModel = l.model

                }

                //type:18 単一選択のため、type5で処理する

                //type:19
                FieldType.WORKER_NAME -> {
                    val l: ViewItems19WorkerBinding =
                        DataBindingUtil.inflate(layoutInflater, R.layout.view_items_19_worker, null, false)
                    l.model = FieldText(cellNumber, field)
                    l.vm = viewModel
                    rootView = l.root
                    fieldModel = l.model
                }

                //type:20
                FieldType.SCHEDULE_DATE -> {
                    val l: ViewItems20ScheduleDateBinding =
                        DataBindingUtil.inflate(layoutInflater, R.layout.view_items_20_schedule_date, null, false)
                    l.model = FieldText(cellNumber, field)
                    l.vm = viewModel
                    rootView = l.root
                    fieldModel = l.model
                }

                //type:21　単一選択なので5で処理する

                //type:22
                FieldType.ADDRESS -> {
                    val l: ViewItems22AddressBinding =
                        DataBindingUtil.inflate(layoutInflater, R.layout.view_items_22_address, null, false)
                    l.model = FieldText(cellNumber, field)
                    l.vm = viewModel
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
        gpsListener?.let{
            locationManager?.removeUpdates(it)
        }
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

/**
 * GPSから取得した値をフィールドにセットする。
 * field:テキストフィールド
 * kind:0 住所 1:緯度 2:経度を返す
 */
class GPSLocationListener(val resources:Resources, val field:FieldText, val kind:Int, once:Int) : LocationListener {
    companion object{
        val ADDRESS = 0
        val LAT = 1
        val LNG = 2

        val ONCE = 0
        val CONTINUE = 1

    }
    override fun onLocationChanged(location: Location?) {
        val lat = location?.latitude.toString()
        val lng = location?.longitude.toString()
        when(kind) {
            ADDRESS->{
                try {
                    val address = getAddressFromGeoCoord(lat, lng)
                    field.text.set(address)
                }
                catch(e:Exception) {
                    BlasLog.trace("E", "住所変換に失敗しました", e)
                }
            }
            LAT->{
                field.text.set(lat)
            }
            LNG->{
                field.text.set(lng)
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

