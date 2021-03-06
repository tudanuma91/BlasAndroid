package com.v3.basis.blas.ui.item.item_search

import android.Manifest
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModelProvider
import com.v3.basis.blas.R
import com.v3.basis.blas.activity.ItemActivity
import com.v3.basis.blas.activity.ItemSearchResultActivity
import com.v3.basis.blas.activity.QRActivity
import com.v3.basis.blas.blasclass.app.BlasDef.Companion.BTN_FIND
import com.v3.basis.blas.blasclass.app.BlasMsg
import com.v3.basis.blas.blasclass.config.FieldType
import com.v3.basis.blas.blasclass.controller.FixtureController
import com.v3.basis.blas.blasclass.db.data.ItemsController
import com.v3.basis.blas.blasclass.db.field.FieldController
import com.v3.basis.blas.blasclass.formaction.FormActionDataSearch
import com.v3.basis.blas.blasclass.ldb.LdbFieldRecord
import com.v3.basis.blas.blasclass.log.BlasLog
import com.v3.basis.blas.databinding.FragmentItemSearchBinding
import com.v3.basis.blas.ui.ext.addTitle
import com.v3.basis.blas.ui.ext.hideKeyboardWhenTouch
import com.v3.basis.blas.ui.ext.startActivityWithResult
import com.v3.basis.blas.ui.item.common.FieldQRCodeWithKenpin
import com.v3.basis.blas.ui.item.item_editor.GPSLocationListener
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import kotlinx.android.synthetic.main.fragment_item_search.*
import java.lang.Exception
import java.util.*

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [ItemSearchFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [ItemSearchFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ItemSearchFragment : Fragment() {
    private lateinit var viewModel: ItemSearchViewModel
    private lateinit var binding: FragmentItemSearchBinding
    //GPS
    private var locationManager: LocationManager? = null
    private var gpsListener:GPSLocationListener? = null

    private var receiveData : Boolean = true
    private var msg = BlasMsg()
    private val toastErrorLen = Toast.LENGTH_LONG
    private var toastSuccessLen = Toast.LENGTH_SHORT

    //??????????????????view???
    private lateinit var token: String
    private lateinit var projectId: String
    private lateinit var rootView: LinearLayout
    //????????????????????????
    private var formInfoMap:MutableMap<String, MutableMap<String, String?>> = mutableMapOf()
    private var editMap:MutableMap<String, EditText?> = mutableMapOf()
    private var dateTime:MutableMap<String,EditText> = mutableMapOf()
    private var checkMap:MutableMap<String,MutableMap<String?, CheckBox?>> = mutableMapOf()
    private var searchValue:MutableMap<String,String?> = mutableMapOf()
    private var errorList:MutableList<String> = mutableListOf()
    private var titleMap:MutableMap<String,TextView> = mutableMapOf()
    //???????????????
    private var layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
    private var layoutParamsSpace = LinearLayout.LayoutParams( LinearLayout.LayoutParams.MATCH_PARENT,50)
    //????????????????????????????????????????????????
    private val calender = Calendar.getInstance()
    private val year = calender.get(Calendar.YEAR)
    private val month = calender.get(Calendar.MONTH)
    private val day = calender.get(Calendar.DAY_OF_MONTH)
    private val hour = calender.get(Calendar.YEAR)
    private val minute = calender.get(Calendar.MONTH)
    //????????????????????????
    private lateinit var formAction:FormActionDataSearch
    private val disposables = CompositeDisposable()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //????????????????????????
        viewModel = ViewModelProvider(this).get(ItemSearchViewModel::class.java)
        activity?.let{
            locationManager = it.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        }
        addTitle("projectName")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val extras = activity?.intent?.extras
        if (extras?.getString("token") != null) {
            token = extras.getString("token").toString()
        }
        if (extras?.getString("project_id") != null) {
            projectId = extras.getString("project_id").toString()
        }
        try {
            if(token != null && projectId != null && activity != null) {
                formAction = FormActionDataSearch(token, requireActivity())
            }else{
                throw java.lang.Exception("Failed to receive internal data ")
            }
        }catch (e:Exception){
            receiveData = false
            val errorMessage = msg.createErrorMessage("getFail")
            Toast.makeText(activity, errorMessage, toastErrorLen).show()
        }
        //?????????????????????
        binding = FragmentItemSearchBinding.inflate(inflater, container, false)
        binding.viewmodel = viewModel

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //rootView = view.findViewById<LinearLayout>(R.id.item_search_liner)
        //?????????????????????????????????
        freeWordButton.setOnClickListener {
            var freeWordText = viewModel.freeWord.get()
            val intent = Intent(activity, ItemSearchResultActivity::class.java)
            val fldSize = 1 //freeword???
            intent.putExtra("token", token)
            intent.putExtra("project_id", projectId)
            intent.putExtra("freeWord", freeWordText)
            intent.putExtra("fldSize", fldSize.toString())


            //?????????????????????????????????????????????
            for (idx in 1..searchValue.size - 1) {
                intent.putExtra("fld${idx}", searchValue["fld${idx}"])
            }

            ItemActivity.searchFreeWord = freeWordText
            ItemActivity.isErrorOnly = network_error_switch.isChecked
            val parent = (requireActivity() as ItemActivity)
            parent.reload()
        }

        QrBarCodeButton.setOnClickListener {
            //QR/????????????????????????????????????????????????????????????
            val extra = Pair("","")
            val fixtureController = context?.let { con -> FixtureController(con, projectId) }
            startActivityWithResult(QRActivity::class.java, QRActivity.QR_CODE, extra) { r ->
                var qr = r.data?.getStringExtra("qr_code")
                if(!qr.isNullOrEmpty()){
                    val qrCodes = fixtureController?.passPurser(qr)
                    if(!qrCodes.isNullOrEmpty()) {
                        val qrOrBarCode = qrCodes[0]
                        viewModel.freeWord.set(qrOrBarCode)
                    }
                    else {
                        viewModel.freeWord.set(qr)
                    }
                }
            }
        }

        AddressButton.setOnClickListener {
            startGetGetCoord(viewModel.freeWord, GPSLocationListener.ADDRESS)
        }


        search_scroller.hideKeyboardWhenTouch(this)
    }

    override fun onPause() {
        super.onPause()
        gpsListener?.let{
            locationManager?.removeUpdates(it)
        }
    }
    /**
     * GPS????????????????????????????????????????????????
     */
    private fun startGetGetCoord(inputText: ObservableField<String>, GeoType:Int) {
        BlasLog.trace("I","startGetGetCoord()")

        if(ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            BlasLog.trace("I","GPS????????????")
            //GPS????????????????????????
            gpsListener = GPSLocationListener(resources,
                inputText,
                GeoType,
                GPSLocationListener.ONCE)

            locationManager?.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f, gpsListener)
        }
        else {
            BlasLog.trace("I","GPS????????????")
            //????????????????????????????????????????????????????????????
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), 1000)
        }
    }

    // getSuccess()?????????????????????
    private fun setDisplay( fieldList:List<LdbFieldRecord> ) {

        var cnt = 1
        var checkCount = 1

        //?????????????????????????????????????????????????????????
        val space = formAction.createSpace(layoutParamsSpace)
        val title = formAction.createFreeWordSearchTitle(layoutParams)
        val freeWordTextBox = formAction.createFreeWordTextBox(layoutParams)

        editMap.set(key = "col_${0}", value = freeWordTextBox)
        rootView.addView(space)
        rootView.addView(title)
        rootView.addView(freeWordTextBox)
        titleMap.set(key = "freeWord", value = title)

        // TODO: ?????????(??????) ????????????????????????????????????????????????
/*
        fieldList.forEach {field ->

            val formSectionTitle = formAction.createFormSectionTitle2(layoutParams,field.name)
            rootView.addView(formSectionTitle)
            titleMap.set(key = cnt.toString(),value = formSectionTitle)

            //?????????????????????????????????????????????????????????
            val typeMap = formAction.createFormInfoMap2(field)
            formInfoMap.set(key = "${cnt}",value =typeMap )

            when(field.type.toString()){
                FieldType.TEXT_FIELD,
                FieldType.TEXT_AREA,
                FieldType.QR_CODE,
                FieldType.TEKKYO_RENDOU_QR,
                FieldType.KENPIN_RENDOU_QR,
                FieldType.SIG_FOX,
                FieldType.ACOUNT_NAME->{
                    //????????????(1???)?????????????????????(?????????)
                    val formPart =formAction.createTextField(layoutParams,cnt)
                    rootView.addView(formPart)

                    //?????????editText?????????????????????
                    editMap.set(key="col_${cnt}",value = formPart)
                }
                FieldType.DATE_TIME->{
                    //????????????
                    val layout = requireActivity().layoutInflater.inflate(R.layout.cell_search_day, null)
                    val minDay = layout.findViewById<EditText>(R.id.MinDay)
                    val maxDay = layout.findViewById<EditText>(R.id.MaxDay)
                    setClickDateTime(minDay)
                    setClickDateTime(maxDay)
                    rootView.addView(layout)
                    dateTime.set(key = "col_${cnt}_minDay",value =minDay )
                    dateTime.set(key = "col_${cnt}_maxDay",value =maxDay )
                }

                FieldType.TIME->{
                    //????????????
                    val layout = requireActivity().layoutInflater.inflate(R.layout.cell_search_datetime, null)
                    val minTime = layout.findViewById<EditText>(R.id.MinTime)
                    val maxTime = layout.findViewById<EditText>(R.id.MaxTime)
                    setClickTime(minTime)
                    setClickTime(maxTime)
                    rootView.addView(layout)
                    dateTime.set(key = "col_${cnt}_minTime",value = minTime )
                    dateTime.set(key = "col_${cnt}_maxTime",value = maxTime )

                }

                FieldType.MULTIPLE_SELECTION , FieldType.SINGLE_SELECTION->{
                    //??????????????????????????????
                    var colCheckMap : MutableMap<String?,CheckBox?> = mutableMapOf()
                    //val colCheckBoxValues = formInfo.choiceValue
                    val colCheckBoxValues = field.choice?.split(",")

                    if(field.parent_field_id.toString() =="0") {
                        if (colCheckBoxValues != null) {
                            colCheckBoxValues.forEach {
                                val formPart =
                                    formAction.createMutipleSelection(
                                        layoutParams,
                                        it,
                                        checkCount
                                    )
                                rootView.addView(formPart)
                                colCheckMap.set(
                                    key = "col_${cnt}_${checkCount}",
                                    value = formPart
                                )
                                checkCount += 1

                            }
                            checkMap.set(key = "col_${cnt}", value = colCheckMap)
                        }
                    }else{
                        if (colCheckBoxValues != null) {
                            val testL = formAction.getSelectValue(colCheckBoxValues)
                            var vCnt = 0
                            colCheckBoxValues.forEach {
                                val formPart =
                                    formAction.createMutipleSelection(
                                        layoutParams,
                                        testL[vCnt],
                                        checkCount
                                    )
                                rootView.addView(formPart)
                                colCheckMap.set(
                                    key = "col_${cnt}_${checkCount}",
                                    value = formPart
                                )
                                checkCount += 1
                                vCnt += 1

                            }
                            checkMap.set(key = "col_${cnt}", value = colCheckMap)
                        }
                    }
                }

                FieldType.CHECK_VALUE->{
                    val layout = requireActivity().layoutInflater.inflate(R.layout.cell_search_checkvalue, null)
                    val value = layout.findViewById<EditText>(R.id.searchValue)
                    val memo = layout.findViewById<EditText>(R.id.searchMemo)

                    rootView.addView(layout)
                    editMap.set(key = "col_${cnt}_value",value = value )
                    editMap.set(key = "col_${cnt}_memo",value = memo )
                }

            }

            //?????????????????????????????????????????????????????????????????????????????????????????????
            val space = Space(activity)
            space.setLayoutParams(layoutParamsSpace)
            rootView.addView(space)
            cnt += 1
        }
*/

        //????????????????????????
        val button = Button(activity)
        button.text = BTN_FIND
        button.setLayoutParams(layoutParams)
        rootView.addView(button)

        //???????????????????????????
        button.setOnClickListener{

            if(errorList.size>0) {
                errorList.forEach {
                    val text = formAction.test2(it,titleMap)
                    titleMap[it]?.setText(text)
                    titleMap[it]?.setTextColor(Color.DKGRAY)
                }
            }
            errorList.clear()


            val freeWordEdit = editMap.get("col_0")!!
            val freeWordValue ="${freeWordEdit.text}"
            Log.d("????????????(??????????????????)","${freeWordValue}")
            if(freeWordValue != "") {
                val chk = formAction.valueChk(freeWordValue)
                if (chk) {
                    errorList.add("freeWord")
                }
            }
            searchValue.set("freeWord",freeWordValue)
            var cnt = 1
            var dateTimeCol = ""
            var checkValueCol = ""
            formInfoMap.forEach{
                var value = ""
                when(it.value["type"]){
                    FieldType.TEXT_FIELD,
                    FieldType.TEXT_AREA,
                    FieldType.QR_CODE,
                    FieldType.TEKKYO_RENDOU_QR,
                    FieldType.KENPIN_RENDOU_QR,
                    FieldType.SIG_FOX ,
                    FieldType.CURRENT_DATE_AND_TIME,
                    FieldType.CATEGORY_SELECTION,
                    FieldType.WORKER_NAME,
                    FieldType.WORK_CONTENT_SELECTION,
                    FieldType.ADDRESS,
                    FieldType.ACOUNT_NAME->{
                        //????????????(1???)???????????????(?????????)
                        value = formAction.pickUpValue(editMap,cnt)
                        if(value != "") {
                            val chk = formAction.valueChk(value)
                            if (chk) {
                                errorList.add(cnt.toString())
                            }
                        }
                        searchValue.set("fld${cnt}",value)
                    }

                    FieldType.DATE_TIME->{
                        //????????????
                        value = formAction.pickUpDateTime(dateTime,cnt,"Day")
                        searchValue.set("fld${cnt}",value)
                        dateTimeCol += "${cnt},"
                    }
                    FieldType.TIME->{
                        //????????????
                        value = formAction.pickUpDateTime(dateTime,cnt,"Time")
                        searchValue.set("fld${cnt}",value)
                        dateTimeCol += "${cnt},"
                    }

                    FieldType.MULTIPLE_SELECTION,
                    FieldType.SINGLE_SELECTION,
                    FieldType.CATEGORY_SELECTION,
                    FieldType.WORK_CONTENT_SELECTION->{
                        //?????????????????????????????????????????????
                        val colCheckMap = checkMap.get("col_${cnt}")
                        value = formAction.getCheckedValues(colCheckMap)
                        searchValue.set("fld${cnt}",value)
                    }

                    FieldType.CHECK_VALUE->{
                        //??????????????????
                        value = formAction.pickupCheckValue(editMap,cnt)
                        searchValue.set("fld${cnt}",value)
                        checkValueCol += "${cnt},"
                    }
                }
                cnt += 1
            }


            if(errorList.size>0) {
                //?????????????????????????????????
                //????????????????????????????????????????????????
                errorList.forEach{
                    titleMap[it] = formAction.test(it,titleMap)
                }

            }else {
                //??????????????????????????????????????????
                val intent = Intent(activity, ItemSearchResultActivity::class.java)
                val fldSize = searchValue.size - 1
                intent.putExtra("token", token)
                intent.putExtra("project_id", projectId)
                intent.putExtra("freeWord", searchValue["freeWord"])
                intent.putExtra("fldSize", fldSize.toString())

                //dateTimeCol???????????????????????????????????????????????????
                if (dateTimeCol != "") {
                    val newDateTimeCol = dateTimeCol.dropLast(1)
                    intent.putExtra("dateTimeCol", newDateTimeCol)
                }

                if (checkValueCol != "") {
                    val newCheckValueCol = checkValueCol.dropLast(1)
                    intent.putExtra("checkValueCol", newCheckValueCol)
                }

                //?????????????????????????????????????????????
                for (idx in 1..searchValue.size - 1) {
                    intent.putExtra("fld${idx}", searchValue["fld${idx}"])
                }

                Log.d("ItemSearchFragment.setDisplay()","startActivity")
//                startActivity(intent)
                ItemActivity.searchFreeWord = searchValue["freeWord"]
                ItemActivity.isErrorOnly = network_error_switch.isChecked
                val parent = (requireActivity() as ItemActivity)
                parent.reload()
            }

        }

    }

/*
    private fun getSuccess(result:JSONObject?) {
        //??????????????????????????????
        var cnt = 1
        var checkCount = 1
        if (result != null) {

            //?????????????????????????????????????????????????????????
            val space = formAction.createSpace(layoutParamsSpace)
            val title = formAction.createFreeWordSearchTitle(layoutParams)
            val freeWordSearch = formAction.createFreeWordSearch(layoutParams)
            editMap.set(key = "col_${0}", value = freeWordSearch)
            rootView.addView(space)
            rootView.addView(title)
            rootView.addView(freeWordSearch)
            titleMap.set(key = "freeWord", value = title)

            //col?????????????????????????????????????????????????????????????????????
            val sortFormFieldList = RestHelper().createFormField(result)
            val test = sortFormFieldList.values.sortedBy { it["field_col"] !!.toInt()}
            test.forEach  {
                /**
                 * formInfo??????????????????????????????????????????
                 * ???title => ?????????????????????
                 * ???type => ??????????????????(??????????????????)
                 * ???choiceValue => ????????????????????????
                 * ???nullable => ?????????null???????????????????????????
                 * ???unique => ??????????????????????????????????????????
                 */
                val formInfo= formAction.typeCheck(it)
                //?????????????????????????????????????????????
                val formSectionTitle = formAction.createFormSectionTitle(layoutParams,formInfo)
                rootView.addView(formSectionTitle)
                titleMap.set(key = cnt.toString(),value = formSectionTitle)

                //?????????????????????????????????????????????????????????
                val typeMap = formAction.createFormInfoMap(formInfo)
                formInfoMap.set(key = "${cnt}",value =typeMap )


                when(formInfo.type){
                    FieldType.TEXT_FIELD,
                    FieldType.TEXT_AREA,
                    FieldType.QR_CODE,
                    FieldType.TEKKYO_RENDOU_QR,
                    FieldType.KENPIN_RENDOU_QR,
                    FieldType.SIG_FOX,
                    FieldType.ACOUNT_NAME->{
                        //????????????(1???)?????????????????????(?????????)
                        val formPart =formAction.createTextField(layoutParams,cnt,formInfo)
                        rootView.addView(formPart)

                        //?????????editText?????????????????????
                        editMap.set(key="col_${cnt}",value = formPart)
                    }

                    FieldType.DATE_TIME->{
                        //????????????
                        val layout = requireActivity().layoutInflater.inflate(R.layout.cell_search_day, null)
                        val minDay = layout.findViewById<EditText>(R.id.MinDay)
                        val maxDay = layout.findViewById<EditText>(R.id.MaxDay)
                        setClickDateTime(minDay)
                        setClickDateTime(maxDay)
                        rootView.addView(layout)
                        dateTime.set(key = "col_${cnt}_minDay",value =minDay )
                        dateTime.set(key = "col_${cnt}_maxDay",value =maxDay )
                    }

                    FieldType.TIME->{
                        //????????????
                        val layout = requireActivity().layoutInflater.inflate(R.layout.cell_search_datetime, null)
                        val minTime = layout.findViewById<EditText>(R.id.MinTime)
                        val maxTime = layout.findViewById<EditText>(R.id.MaxTime)
                        setClickTime(minTime)
                        setClickTime(maxTime)
                        rootView.addView(layout)
                        dateTime.set(key = "col_${cnt}_minTime",value = minTime )
                        dateTime.set(key = "col_${cnt}_maxTime",value = maxTime )

                    }

                    FieldType.MULTIPLE_SELECTION , FieldType.SINGLE_SELECTION->{
                        //??????????????????????????????
                        var colCheckMap : MutableMap<String?,CheckBox?> = mutableMapOf()
                        val colCheckBoxValues = formInfo.choiceValue
                        if(formInfo.parentFieldId =="0") {
                            if (colCheckBoxValues != null) {
                                colCheckBoxValues.forEach {
                                    val formPart =
                                        formAction.createMutipleSelection(
                                            layoutParams,
                                            it,
                                            checkCount
                                        )
                                    rootView.addView(formPart)
                                    colCheckMap.set(
                                        key = "col_${cnt}_${checkCount}",
                                        value = formPart
                                    )
                                    checkCount += 1

                                }
                                checkMap.set(key = "col_${cnt}", value = colCheckMap)
                            }
                        }else{
                            if (colCheckBoxValues != null) {
                                val testL = formAction.getSelectValue(colCheckBoxValues)
                                var vCnt = 0
                                colCheckBoxValues.forEach {
                                    val formPart =
                                        formAction.createMutipleSelection(
                                            layoutParams,
                                            testL[vCnt],
                                            checkCount
                                        )
                                    rootView.addView(formPart)
                                    colCheckMap.set(
                                        key = "col_${cnt}_${checkCount}",
                                        value = formPart
                                    )
                                    checkCount += 1
                                    vCnt += 1

                                }
                                checkMap.set(key = "col_${cnt}", value = colCheckMap)
                            }
                        }
                    }


                    FieldType.CHECK_VALUE->{
                        val layout = requireActivity().layoutInflater.inflate(R.layout.cell_search_checkvalue, null)
                        val value = layout.findViewById<EditText>(R.id.searchValue)
                        val memo = layout.findViewById<EditText>(R.id.searchMemo)

                        rootView.addView(layout)
                        editMap.set(key = "col_${cnt}_value",value = value )
                        editMap.set(key = "col_${cnt}_memo",value = memo )
                    }

                }
                //?????????????????????????????????????????????????????????????????????????????????????????????
                val space = Space(activity)
                space.setLayoutParams(layoutParamsSpace)
                rootView.addView(space)
                cnt += 1
            }


            //????????????????????????
            val button = Button(activity)
            button.text = BTN_FIND
            button.setLayoutParams(layoutParams)
            rootView.addView(button)

            //???????????????????????????
            button.setOnClickListener{

                if(errorList.size>0) {
                    errorList.forEach {
                        val text = formAction.test2(it,titleMap)
                        titleMap[it]?.setText(text)
                        titleMap[it]?.setTextColor(Color.DKGRAY)
                    }
                }
                errorList.clear()


                val freeWordEdit = editMap.get("col_0")!!
                val freeWordValue ="${freeWordEdit.text}"
                Log.d("????????????(??????????????????)","${freeWordValue}")
                if(freeWordValue != "") {
                    val chk = formAction.valueChk(freeWordValue)
                    if (chk) {
                        errorList.add("freeWord")
                    }
                }
                searchValue.set("freeWord",freeWordValue)
                var cnt = 1
                var dateTimeCol = ""
                var checkValueCol = ""
                formInfoMap.forEach{
                    var value = ""
                    when(it.value["type"]){
                        FieldType.TEXT_FIELD,
                        FieldType.TEXT_AREA,
                        FieldType.QR_CODE,
                        FieldType.TEKKYO_RENDOU_QR,
                        FieldType.KENPIN_RENDOU_QR,
                        FieldType.SIG_FOX ,
                        FieldType.ACOUNT_NAME->{
                            //????????????(1???)???????????????(?????????)
                            value = formAction.pickUpValue(editMap,cnt)
                            if(value != "") {
                                val chk = formAction.valueChk(value)
                                if (chk) {
                                    errorList.add(cnt.toString())
                                }
                            }
                            searchValue.set("fld${cnt}",value)
                        }

                        FieldType.DATE_TIME->{
                            //????????????
                            value = formAction.pickUpDateTime(dateTime,cnt,"Day")
                            searchValue.set("fld${cnt}",value)
                            dateTimeCol += "${cnt},"
                        }
                        FieldType.TIME->{
                            //????????????
                            value = formAction.pickUpDateTime(dateTime,cnt,"Time")
                            searchValue.set("fld${cnt}",value)
                            dateTimeCol += "${cnt},"
                        }

                        FieldType.MULTIPLE_SELECTION , FieldType.SINGLE_SELECTION->{
                            //?????????????????????????????????????????????
                            val colCheckMap = checkMap.get("col_${cnt}")
                            value = formAction.getCheckedValues(colCheckMap)
                            searchValue.set("fld${cnt}",value)
                        }

                        FieldType.CHECK_VALUE->{
                            //??????????????????
                            value = formAction.pickupCheckValue(editMap,cnt)
                            searchValue.set("fld${cnt}",value)
                            checkValueCol += "${cnt},"

                        }
                    }
                    cnt += 1
                }


                if(errorList.size>0) {
                    //?????????????????????????????????
                    //????????????????????????????????????????????????
                    errorList.forEach{
                        titleMap[it] = formAction.test(it,titleMap)
                    }

                }else {
                    //??????????????????????????????????????????
                    val intent = Intent(activity, ItemSearchResultActivity::class.java)
                    val fldSize = searchValue.size - 1
                    intent.putExtra("token", token)
                    intent.putExtra("project_id", projectId)
                    intent.putExtra("freeWord", searchValue["freeWord"])
                    intent.putExtra("fldSize", fldSize.toString())

                    //dateTimeCol???????????????????????????????????????????????????
                    if (dateTimeCol != "") {
                        val newDateTimeCol = dateTimeCol.dropLast(1)
                        intent.putExtra("dateTimeCol", newDateTimeCol)
                    }

                    if (checkValueCol != "") {
                        val newCheckValueCol = checkValueCol.dropLast(1)
                        intent.putExtra("checkValueCol", newCheckValueCol)
                    }

                    //?????????????????????????????????????????????
                    for (idx in 1..searchValue.size - 1) {
                        intent.putExtra("fld${idx}", searchValue["fld${idx}"])
                    }
                    startActivity(intent)
                }

            }
        }
    }
*/

    /**
     * ??????????????????????????????
     */
/*
    private fun getFail(errorCode: Int, aplCode:Int) {

        var message:String? = null

        message = BlasMsg().getMessage(errorCode,aplCode)

        Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show()

        //????????????????????????????????????????????????
        //fieldMap = mutableMapOf<Int, MutableMap<String, String?>>()
    }
*/

    /**
     * ??????????????????????????????????????????
     */
    private fun setClickDateTime(formPart: EditText): EditText {
        formPart.setOnClickListener{
            val dtp = DatePickerDialog(requireContext(), DatePickerDialog.OnDateSetListener{ view, y, m, d ->
                //???????????????????????????
                formPart.setText(String.format("%d-%02d-%02d",y,m+1,d))
            }, year,month,day)
            dtp.show()
        }
        return formPart
    }

    /**
     * ??????????????????????????????????????????
     */
    private fun setClickTime(formPart: EditText): EditText {
        //editText?????????????????????
        formPart.setOnClickListener{
            val tp = TimePickerDialog(context,
                TimePickerDialog.OnTimeSetListener{ view, hour, minute->
                    formPart.setText(String.format("%02d:%02d",hour,minute))
                },hour,minute,true)
            tp.show()
        }
        return formPart
    }

    override fun onDestroyView() {
        super.onDestroyView()
        disposables.dispose()
    }

}
