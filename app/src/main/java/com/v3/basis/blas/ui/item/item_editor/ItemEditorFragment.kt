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
import androidx.databinding.ObservableField
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

    private lateinit var formModel: ItemViewModel
    private lateinit var form: ViewItems0FormBinding
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
        /*
        formModel.dateEvent
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy {
                setClickDateTime(it)
            }
            .addTo(disposables)

        formModel.timeEvent
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy {
                setClickTime(it)
            }
            .addTo(disposables)

        formModel.qrEvent
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
        formModel.qrCheckEvent
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
        formModel.qrKenpinEvent
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
        formModel.qrTekkyoEvent
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

        formModel.locationEvent
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


        formModel.latEvent
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

        formModel.lngEvent
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


        formModel.accountNameEvent
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy {
                it.text.set(userMap["name"])
            }
            .addTo(disposables)

        formModel.currentDateTimeEvent
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy {
                val df = SimpleDateFormat("yyyy/MM/dd HH:mm")
                val date = Date()
                it.text.set(df.format(date))
            }
            .addTo(disposables)

         */
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
                    formModel.fields.forEachIndexed { index, any ->
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

        var rootView:View? = null
        var fieldModel:FieldModel? = null

        //edit_id　0は非表示、1は表示だけ 2は編集可能という意味っぽい
        if( 1 == field.edit_id ) {
            val l: ViewItems0ReadOnlySingleLineBinding =
                DataBindingUtil.inflate(layoutInflater, R.layout.view_items_0_read_only_single_line, null, false)
            l.model = FieldText(layoutInflater, cellNumber, field)
            //どこでフィールドの型を認識させるべきか？朝礼前の考え事
            rootView = l.root
            fieldModel = l.model
        }
        else {
            when (field.type.toString()) {
                FieldType.TEXT_FIELD -> {
                    //自由入力(1行)
                    val inputField = FieldText(layoutInflater, cellNumber,field)
                    //親フォームにフィールドを追加する
                    formModel.fields.add(inputField)
                    //入力フィールドを表示する
                    form.innerView.addView(inputField.layout.root)
                }

                FieldType.TEXT_AREA -> {
                    //自由入力(複数行)
                    val inputField = FieldMultiText(layoutInflater, cellNumber,field)
                    //親フォームにフィールドを追加する
                    formModel.fields.add(inputField)
                    //入力フィールドを表示する
                    form.innerView.addView(inputField.layout.root)
                }
                // type:3 日付
                FieldType.DATE_TIME -> {
                    val inputField = FieldDate(layoutInflater, cellNumber,field)
                    //親フォームにフィールドを追加する
                    formModel.fields.add(inputField)
                    //入力フィールドを表示する
                    form.innerView.addView(inputField.layout.root)
                    inputField.layout.text.setOnClickListener {
                        //TODO:三代川　カレンダーを表示できるようにしました。こちらを参考にSCHEDULE_DATE、TIMEなどを修正お願い致します。
                        // カレンダー選択を表示
                        setClickDateTime(inputField)
                    }
                }
                // type:4 時間
                FieldType.TIME -> {
                    val inputField = FieldTime(layoutInflater, cellNumber,field)
                    inputField.layout.text.setOnClickListener {
                        //TODO:三代川 時刻を入力できるようにしてください
                    }
                    //親フォームにフィールドを追加する
                    formModel.fields.add(inputField)
                    //入力フィールドを表示する
                    form.innerView.addView(inputField.layout.root)
                }

                FieldType.WORK_CONTENT_SELECTION,
                FieldType.CATEGORY_SELECTION,
                FieldType.SINGLE_SELECTION -> {
                    //単一選択
                    val inputField = FieldSingleSelect(layoutInflater, cellNumber,field)
                    //親フォームにフィールドを追加する
                    formModel.fields.add(inputField)
                    //入力フィールドを表示する
                    form.innerView.addView(inputField.layout.root)


                    // 選択肢の生成
                    val choice = createOption(field,inputField )
                    inputField.layout.spinner.createChildren(choice, inputField)



                    // 連動パラメータで親を取り出すために配列に入れておく
                    singleSelectSpinner[field.field_id!!] = inputField.layout.spinner

                    //アイテムが選択されたときに行う処理を登録する
                    inputField.layout.spinner.onItemSelectedListener = SpinnerItemSelectedListener().apply {
                        this.selectAction = {
                            BlasLog.trace("I","selectAction")
                            inputField.selectedIndex.set(it)
                        }
                    }



                    /*
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
                    fieldModel = l.model*/

                }
                FieldType.MULTIPLE_SELECTION -> {
                    //複数選択
                    val l: ViewItems6SelectMultiBinding =
                        DataBindingUtil.inflate(layoutInflater, R.layout.view_items_6_select_multi, null, false)
                    val model = FieldMultiSelect(layoutInflater, cellNumber, field)
                    l.model = model
                    l.vm = formModel
                    l.checkBoxGroup.createChildren(layoutInflater, field.choice, model)
                    rootView = l.root
                    fieldModel = l.model
                }
                // type:7 場所
                FieldType.LOCATION -> {
                    val inputField = FieldLocation(layoutInflater, cellNumber, field)
                    inputField.layout.button.setOnClickListener {
                        //ボタンを押したときの処理
                        startGetGetCoord(inputField.text, GPSLocationListener.ADDRESS)
                    }
                    //親フォームにフィールドを追加する
                    formModel.fields.add(inputField)
                    //入力フィールドを表示する
                    form.innerView.addView(inputField.layout.root)
                }
                // type:14  緯度
                FieldType.LAT_LOCATION -> {
                    val inputField = FieldLat(layoutInflater, cellNumber, field)
                    inputField.layout.button.setOnClickListener {
                        //ボタンを押したときの処理
                        startGetGetCoord(inputField.text, GPSLocationListener.LAT)

                    }
                    //親フォームにフィールドを追加する
                    formModel.fields.add(inputField)
                    //入力フィールドを表示する
                    form.innerView.addView(inputField.layout.root)
                }
                // type:15 経度
                FieldType.LNG_LOCATION -> {
                    val inputField = FieldLng(layoutInflater, cellNumber, field)
                    inputField.layout.button.setOnClickListener {
                        //ボタンを押したときの処理
                        startGetGetCoord(inputField.text, GPSLocationListener.LNG)
                    }
                    //親フォームにフィールドを追加する
                    formModel.fields.add(inputField)
                    //入力フィールドを表示する
                    form.innerView.addView(inputField.layout.root)
                }

                FieldType.KENPIN_RENDOU_QR -> {
                    //QRコード(検品と連動)
                    val inputField = FieldQRCodeWithKenpin(layoutInflater, cellNumber, field)
                    inputField.layout.button.setOnClickListener {
                        //カメラ起動
                        val extra = "colNumber" to inputField.fieldNumber.toString()
                        startActivityWithResult(QRActivity::class.java, QRActivity.QR_CODE_KENPIN, extra) { r ->
                            val qr = r.data?.getStringExtra("qr_code")
                            try {
                                itemsController.qrCodeCheck(qr)
                                inputField.text.set(qr)
                            } catch (ex: ItemsController.ItemCheckException) {
                                // 設置不可の時
                                Toast.makeText(context, ex.message, Toast.LENGTH_LONG).show()
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
                    val inputField = FieldQRCode(layoutInflater, cellNumber, field)
                    inputField.layout.button.setOnClickListener {
                        val extra = "colNumber" to inputField.fieldNumber.toString()
                        startActivityWithResult(QRActivity::class.java, QRActivity.QR_CODE, extra) { r ->
                            val qr = r.data?.getStringExtra("qr_code")
                            inputField.text.set(qr)
                        }
                    }
                    //親フォームにフィールドを追加する
                    formModel.fields.add(inputField)
                    //入力フィールドを表示する
                    form.innerView.addView(inputField.layout.root)

                }

                FieldType.TEKKYO_RENDOU_QR -> {
                    //QRコード(撤去と連動)
                    val inputField = FieldQRCodeWithTekkyo(layoutInflater, cellNumber, field)
                    inputField.layout.button.setOnClickListener {
                        //カメラ起動
                        val extra = "colNumber" to inputField.fieldNumber.toString()
                        startActivityWithResult(QRActivity::class.java, QRActivity.QR_CODE_TEKKYO, extra) { r ->
                            val qr = r.data?.getStringExtra("qr_code")
                            try {
                                itemsController.qrCodeCheck(qr)
                                inputField.text.set(qr)
                            } catch (ex: ItemsController.ItemCheckException) {
                                // 設置不可の時
                                Toast.makeText(context, ex.message, Toast.LENGTH_LONG).show()
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
                    val inputField = FieldAccount(layoutInflater, cellNumber, field)

                    //ボタンがクリックされたらログインユーザ名を表示する
                    inputField.layout.button.setOnClickListener {
                        val user = itemsController.getUserInfo()
                        inputField.text.set( user?.name )
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
                    val inputField = FieldCheckText(layoutInflater, cellNumber, field)
                    //親フォームにフィールドを追加する
                    formModel.fields.add(inputField)
                    //入力フィールドを表示する
                    form.innerView.addView(inputField.layout.root)
                }
                //type:16 入力値チェック連動_QRコード(検品と連動)
                FieldType.QR_CODE_WITH_CHECK -> {
                    val inputField = FieldQRWithCheckText(layoutInflater, cellNumber, field)
                    //親フォームにフィールドを追加する
                    formModel.fields.add(inputField)
                    //入力フィールドを表示する
                    form.innerView.addView(inputField.layout.root)
                }
                //type:17 現在日時
                FieldType.CURRENT_DATE_AND_TIME -> {
                    //現在日時のフォーマット yyyy/mm/dd hh:mmをボタンを押したら入力できるようにする
                    val inputField = FieldCurrentDateTime(layoutInflater, cellNumber, field)
                    inputField.layout.button.setOnClickListener {
                        val df = SimpleDateFormat("yyyy/MM/dd HH:mm")
                        val date = Date()
                        inputField.text.set(df.format(date))
                    }

                    //親フォームにフィールドを追加する
                    formModel.fields.add(inputField)
                    //入力フィールドを表示する
                    form.innerView.addView(inputField.layout.root)
                }

                //type:18 単一選択のため、type5で処理する

                //type:19 作業者
                FieldType.WORKER_NAME -> {
                    val inputField = FieldWorkerName(layoutInflater, cellNumber, field)
                    //親フォームにフィールドを追加する
                    formModel.fields.add(inputField)
                    //入力フィールドを表示する
                    form.innerView.addView(inputField.layout.root)

                    inputField.layout.button.setOnClickListener {
                        //TODO:現在はただのテキストフィールドだけど、単一選択の入力画面に変更する必要もあり。
                        //TODO:後回し

                        //ボタンが押されたらログインユーザー名を設定
                        val user = itemsController.getUserInfo()
                        inputField.text.set( user?.name )

                    }
                }
                //type:20 予定日
                FieldType.SCHEDULE_DATE -> {
                    val inputField = FieldScheduleDate(layoutInflater, cellNumber, field)
                    //親フォームにフィールドを追加する
                    formModel.fields.add(inputField)
                    //入力フィールドを表示する
                    form.innerView.addView(inputField.layout.root)
                    inputField.layout.text.setOnClickListener {
                        // カレンダー選択を表示
                        setClickDateTime(inputField)


                        //TODO:ちょっと型20の使い方がわからないのでWEB版を調べて同じ動きにして欲しいです
                    }
                }

                //type:21　単一選択なので5で処理する

                //type:22
                FieldType.ADDRESS -> {
                    val inputField = FieldAddress(layoutInflater, cellNumber, field)
                    //親フォームにフィールドを追加する
                    formModel.fields.add(inputField)
                    //入力フィールドを表示する
                    form.innerView.addView(inputField.layout.root)
                }


                else -> { null }
            }
        }
    }

    /**
     * 単一選択の選択肢を作成する
     * todo:ここにあっていいのかは？
     */
    private fun createOption( field: LdbFieldRecord,inputField : FieldSingleSelect ) : String {
        BlasLog.trace("I","createOption() start field:" + field.name)
        var ret = ""

        if( 0 != field.parent_field_id ) {
            // 連動パラメータの時
            val jsonChoice = JSONObject(field.choice)
            val parents = jsonChoice.names()
            // とりあえず一番最初のchildを入れておく
            ret = jsonChoice.getString(parents[0].toString())
            BlasLog.trace("I","child:::" + ret)

            // 親を取得
            val parentSpinner = singleSelectSpinner[field.parent_field_id!!]

            if( parentSpinner != null ) {
                val parentValue = parentSpinner?.selectedItem as String
                BlasLog.trace("I","parent value::::" + parentValue)
                // childをちゃんとしたものに入替える
                ret = jsonChoice.getString(parentValue)

                // 親項目が変更されたら子も変える。ここでやるしかない！
                val listener = parentSpinner.onItemSelectedListener as SpinnerItemSelectedListener
                listener.optionalAction = { parent, position ->

                    BlasLog.trace("I","親変更！！！  position:" + position)
                    val newChoice = jsonChoice.getString(parents[ position ].toString())
                    inputField.layout.spinner.createChildren(newChoice, inputField)
                }
            }
        }
        else {
            // 連動パラメータではない時
            // choiceに入ってる文字列をそのまま使用
            ret = field.choice.toString()
        }

        return ret
    }

    /**
     * セレクタの選択肢を設定する
     */
    private fun Spinner.createChildren(separatedText: String?, model: FieldSingleSelect) {
        separatedText?.also {
            val list = it.split(",")
            model.values.addAll(list)
            val ad = ArrayAdapter<String>(requireContext(), android.R.layout.simple_spinner_item)
            ad.addAll(list)
            this.adapter = ad
        }
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
    fun setClickDateTime(field: FieldDateModel) {
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
class GPSLocationListener(val resources:Resources, val field: ObservableField<String>, val kind:Int, once:Int) : LocationListener {
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
                field.set("%.6f".format(lat.toFloat()))
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

