package com.v3.basis.blas.ui.item.item_view


import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.v3.basis.blas.R
import com.v3.basis.blas.activity.ItemActivity
import com.v3.basis.blas.activity.ItemEditActivity
import com.v3.basis.blas.activity.ItemImageActivity
import com.v3.basis.blas.activity.MapsActivity
import com.v3.basis.blas.blasclass.app.BlasMsg
import com.v3.basis.blas.blasclass.config.FieldType
import com.v3.basis.blas.blasclass.db.data.ItemsController
import com.v3.basis.blas.blasclass.db.field.FieldController
import com.v3.basis.blas.blasclass.helper.RestHelper
import com.v3.basis.blas.blasclass.ldb.LdbFieldRecord
import com.v3.basis.blas.blasclass.log.BlasLog
import com.v3.basis.blas.ui.ext.addTitle
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.databinding.GroupieViewHolder
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_item_view.*
import org.json.JSONArray
import org.json.JSONObject


/**
 * A simple [Fragment] subclass.
 */
class ItemViewFragment : Fragment() {
    lateinit var token:String
    private var projectNames:String? = null
    lateinit var projectId :String
    private var msg = BlasMsg()
    private val toastErrorLen = Toast.LENGTH_LONG

    private var endShow = false
    private var progressBarFlg = false
    private var parseNum = 20
    private var parseStartNum = 0
    private var parseFinNum = parseNum

    private var findValueMap:MutableMap<String,String?>? = null
    private var isErrorOnly:Boolean = false

//    private val fieldMap: MutableMap<Int, MutableMap<String, String?>> = mutableMapOf()
    private var fields:List<LdbFieldRecord> = mutableListOf()

    private val itemListAll: MutableList<MutableMap<String, String?>> = mutableListOf()
    private val itemList: MutableList<MutableMap<String, String?>> = mutableListOf()
    private var jsonItemList: JSONObject? = null
    private val dataList = mutableListOf<ItemsListCell>()
    private var jsonParseList :JSONArray? = null
    private lateinit var itemsController: ItemsController
    private val disposables = CompositeDisposable()
    private lateinit var viewModel: ItemsListViewModel

    private lateinit var rootView:View
    private val helper:RestHelper = RestHelper()
    private var handler = Handler()
    private var currentIndex: Int = 0
    private var offset: Int = 0
    private var mapAddressCol = ""
    private var mapTitleCol = ""

    companion object {
        const val CREATE_UNIT = 20
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    private val groupAdapter = GroupAdapter<GroupieViewHolder<*>>()


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        addTitle("projectName")
        val root = inflater.inflate(R.layout.fragment_item_view, container, false)
        rootView = root

        viewModel = ViewModelProviders.of(this).get(ItemsListViewModel::class.java)
        //???????????????????????????????????????????????????????????????????????????????????????
        viewModel.imageBtnCallBack = ::clickImageButton
        //?????????????????????????????????????????????????????????????????????????????????????????????
        viewModel.editBtnCallBack = ::clickEditButton
        //?????????????????????????????????????????????????????????????????????????????????
        viewModel.mapBtnCallBack = ::clickMapButton


        val extras = activity?.intent?.extras
        if(extras?.getString("token") != null ) {
            token = extras.getString("token").toString() //???????????????????????????
        }
        if(extras?.getString("project_id") != null ) {
            projectId = extras.getString("project_id").toString()
        }
        if(extras?.getString("projectName") != null ) {
            projectNames = extras.getString("projectName")
        }

        itemsController = ItemsController(requireContext(), projectId)


        val endSwitch = rootView.findViewById<Switch>(R.id.switch_end_flg)
        val viewBtn = rootView.findViewById<Button>(R.id.button_view)

        //??????????????????????????????
        endSwitch.setOnCheckedChangeListener{_ ,isChecked->
            endShow = isChecked
        }

        /**
         * ????????????????????????????????????????????????????????????????????????
         */
        viewBtn.setOnClickListener{
            if(progressBarFlg){
                Log.d("??????????????????","???????????????????????????????????????????????????")
            }else{
                Log.d("??????????????????","????????????")

                //?????????????????????
                progressBarFlg = true
                chkProgress(progressBarFlg,rootView)

                //???????????????
                dataList.clear()
                groupAdapter.notifyDataSetChanged()
                currentIndex = 0
                parseStartNum = 0
                parseFinNum = parseNum
                itemListAll.clear()

                //?????????????????????
                //jsonParse(parseStartNum, parseFinNum)
                reload(parseStartNum, parseFinNum)
                setAdapter()
                progressBarFlg = false
                chkProgress(progressBarFlg,rootView)
            }

        }

        val searchWord = ItemActivity.searchFreeWord
        if (searchWord.isNullOrBlank().not()) {
            findValueMap = mutableMapOf()
            findValueMap?.set("freeWord", searchWord)
        } else {
            findValueMap = null
        }

        isErrorOnly = ItemActivity.isErrorOnly
        ItemActivity.isErrorOnly = false

        return root
    }

    /**
     * ???????????????????????????????????????????????????????????????
     */
    fun clickImageButton(model:ItemsCellModel) {
        val intent = Intent(requireContext(), ItemImageActivity::class.java)
        intent.putExtra("item_id", "${model.item_id}")
        intent.putExtra("token", token)
        intent.putExtra("project_id", projectId)
        requireContext().startActivity(intent)
    }

    /**
     * ???????????????????????????????????????????????????????????????????????????
     */
    fun clickEditButton(model:ItemsCellModel) {
        val intent = Intent(requireContext(), ItemEditActivity::class.java)
        intent.putExtra("item_id", "${model.item_id}")
        intent.putExtra("token", token)
        intent.putExtra("project_id", projectId)
        val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            //??????????????????
        }

        startForResult.launch(intent)
    }

    /**
     * ???????????????????????????????????????????????????????????????????????????
     */
    fun clickMapButton(model:ItemsCellModel) {
        val intent = Intent(requireContext(), MapsActivity::class.java)
        val record = itemsController.findByItemId(model.item_id.toString())
        var address = ""
        var mapTitle = ""

        if(mapAddressCol != "") {
            address = record[mapAddressCol].toString()
            if((address == "") || (address == null)) {
                Toast.makeText(context, "??????????????????????????????????????????", Toast.LENGTH_LONG).show()
                return
            }
        }

        if(mapTitleCol != "") {
            mapTitle = record[mapTitleCol].toString()
        }

        intent.putExtra("address", address)
        intent.putExtra("title", mapTitle)
        requireActivity().startActivity(intent)
    }

    private fun chkProgress(flg:Boolean,view:View){
        val progressbar = view.findViewById<ProgressBar>(R.id.progressBarItemView)
        if (flg) {
            progressbar.visibility = android.widget.ProgressBar.VISIBLE

        } else {
            progressbar.visibility = android.widget.ProgressBar.INVISIBLE
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //???????????????-view?????????
        //??????????????????????????????????????????????????????????????????????????????????????????adapter????????????????????????
        val recyclerView = recyclerView
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = groupAdapter

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                Log.d("ItemViewFragment.onScrollStateChanged()","start")

                super.onScrollStateChanged(recyclerView, newState)
                if (itemListAll.isNotEmpty()) {

                    if (!recyclerView.canScrollVertically(1) && progressBarFlg.not()) {
                        progressBarFlg = true
                        chkProgress(true, rootView)

                        offset += CREATE_UNIT
                        searchASync()

                    }
                }
            }
        })
        
        //?????????????????????????????????????????????
        try {
            Log.d("?????????????????????","?????????????????????=>${projectNames}")
            if(token != null || projectId != null || projectNames != null) {
                progressBarFlg = true
                chkProgress(progressBarFlg, rootView)

                Single.fromCallable { FieldController(requireContext(),projectId).getFieldRecords() }
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeBy {
                        if( it.isNotEmpty() ) {
                            fields = it
                            searchASync()
                        }
                        else {
                            //fieldMap.clear()
                            fields = mutableListOf()
                            // TODO:??????????????????
                            progressBarFlg = false
                            chkProgress(progressBarFlg,rootView)
                        }

                        fields.forEach {field->

                            //?????????????????????????????????????????????????????????????????????????????????
                            if(field.address == 1) {
                                mapAddressCol = "fld${field.col}"
                            }
                            //??????????????????????????????????????????????????????????????????
                            if(field.map == 1) {
                                mapTitleCol = "fld${field.col}"
                            }

                        }
                    }
                    .addTo(disposables)

            }else{
                throw Exception("Failed to receive internal data ")
            }
        }catch (e:Exception){
            progressBarFlg = false
            chkProgress(progressBarFlg, rootView)
            val errorMessage = msg.createErrorMessage("getFail")
            Toast.makeText(activity, errorMessage, toastErrorLen).show()
        }
    }

    private fun searchASync() {

        Single.fromCallable {
            itemsController.search(offset = offset, paging = CREATE_UNIT, findValueMap = findValueMap, isErrorOnly = isErrorOnly)
        }
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy {
                isErrorOnly = false
                if (it.isNotEmpty()) {
//                    itemListAll.clear()
                    itemListAll.addAll(it)
                    setAdapter()
                } else {
                    //fieldMap.clear()
                    fields = mutableListOf()
                    progressBarFlg = false
                    chkProgress(progressBarFlg,rootView)
                }
            }
            .addTo(disposables)
    }


    /**
     * ??????????????????????????????
     */
    private fun fieldRecvError(errorCode: Int , aplCode:Int) {
        Log.d("itemViewFragment", "?????????????????????")

        var message:String? = null

        message = BlasMsg().getMessage(errorCode,aplCode)

        handler.post {
            Toast.makeText(getActivity(), message, toastErrorLen).show()
        }
        progressBarFlg = false
        chkProgress(progressBarFlg,rootView)
    }


    /**
     *  ???????????????
     */
    private fun setAdapter() {
        createCardView()
        groupAdapter.update(dataList)
        progressBarFlg = false
        chkProgress(progressBarFlg,rootView)
    }


    private fun createCardView() {

        val colMax = fields.size
        val list = mutableListOf<MutableMap<String, String?>>()
        //?????????ItemListAll???????????????????????????

        if(currentIndex == 0) {
            list.addAll(itemListAll.filterIndexed { index, mutableMap ->
                (index >= currentIndex) && (index < currentIndex + CREATE_UNIT)
            }.toMutableList())
        }else{
            list.addAll(itemListAll.filterIndexed { index, mutableMap ->
                (index >= currentIndex) && (index < currentIndex + CREATE_UNIT)
            }.toMutableList())
        }

        if (list.isNotEmpty()) {
            currentIndex += CREATE_UNIT
            itemList.clear()
            itemList.addAll(list)
        }

        createCardManager(list,colMax)
    }

    /**
     * ??????????????????????????????????????????
     * endflg?????????????????????????????????????????????????????????????????????????????????
     *
     */
    private fun createCardManager(list:MutableList<MutableMap<String,String?>>,colMax : Int){
        Log.d("??????????????????","?????????show??????=>${endShow}")

        if (endShow) {
            Log.d("cardManager","???????????????")
            list.forEach {
                val valueFlg = it["end_flg"].toString()
                val item_id = it["item_id"].toString()
                val syncStatus = it["sync_status"]?.toInt() ?: 0
                var text: String? = ""
                text = createCardText(text, it, colMax)
                createCard(item_id, text,valueFlg, syncStatus,it["error_msg"],it)
            }
        } else {
            Log.d("cardManager","??????????????????")
            list.forEach {
                val valueFlg = it["end_flg"].toString()
                if (valueFlg == FieldType.NORMAL) {
                    val item_id = it["item_id"].toString()
                    val syncStatus = it["sync_status"]?.toInt() ?: 0
                    var text: String? = ""
                    text = createCardText(text, it, colMax)
                    createCard(item_id, text, valueFlg, syncStatus,it["error_msg"],it)
                }
            }
        }

    }


    /**
     * ??????????????????????????????????????????????????????
     */
    private fun createCardText(text:String?, item:MutableMap<String,String?>, colMax: Int): String? {

        var text = text

        text += "<table border=\"1\" style=\"border-collapse: collapse; table-layout:fixed; border-style: solid; border-color: #FF69B4;\" width=\"100%\">"

        fields.forEachIndexed { index, field ->
            val fldName = "fld${field.col}"

            text += "<tr>"
            text += "<td bgcolor=\"#FFEFFF\">${field.name}</td>"

                if ((field.type.toString() == FieldType.CHECK_VALUE) ||
                    (field.type.toString() == FieldType.QR_CODE_WITH_CHECK) ||
                    (field.type.toString() == FieldType.BAR_CODE_WITH_CHECK)
                ) {
                    val newValue = helper.createCheckValue(item[fldName].toString())
                    text += "<td>${newValue}</td>"
                } else {
                    val fldVal = item[fldName]?.replace("\\r", "")
                    text += "<td>${fldVal}</td>"
                }

            text += "</tr>"
         }

        text += "</table>"

        return text
    }

    /**
     * ???????????????????????????????????????
     */
    fun createCard(item_id:String,text: String?,valueFlg : String, syncStatus: Int,errMsg:String?,item:MutableMap<String,String?>){
        val rowModel = RowModel().also {
            if(valueFlg == FieldType.END) {
                it.title = "${item_id}${FieldType.ENDTEXT}"
            }else{
                if( item_id.toLong() < 0L ) {
                    it.title = "(????????????)"
                }
                else {
                    it.title = item_id
                }
            }
            it.itemId = item_id

            it.detail = text.toString()
            it.projectId = projectId
            it.token = token
            it.itemList = itemListAll
            it.projectNames = projectNames
            if( null != errMsg ) {
                it.errMsg = errMsg
            }
        }

        val model = ItemsCellModel(
            token,
            projectId.toInt(),
            rowModel.itemId?.toLong() ?: 0,
            rowModel.title,
            rowModel.detail,
            //valueList,
            syncStatus,
            requireContext()
        )

        val user = itemsController.getUserInfo()
        if( null == user ) {
            BlasLog.trace("E", "user???????????????????????????????????????")
            return
        }

        var editEnabled = true
        if(user.group_id == 1 || user.group_id == 2) {
            //?????????????????????????????????????????????????????????????????????
            editEnabled = true
        }
        else {
            //????????????????????????????????????
            val worker = itemsController.getUserInfo(item["worker_user_id"])
            if(worker == null ) {
                BlasLog.trace("E", "?????????ID???????????????")
                //????????????false????????????????????????????????????????????????????????????????????????true???????????????
                editEnabled = true
            }
            else {

                if (!item["worker_user_id"].isNullOrBlank()) {
                    val loginOrgId = user.org_id
                    val loginGroup = user.group_id
                    val workerOrgId = worker.org_id
                    val workerGroup = worker.group_id
                    if(loginGroup <= workerGroup) {
                        if (loginOrgId == workerOrgId) {
                            editEnabled = true
                        } else {
                            editEnabled = false
                        }
                    }
                } else {
                    //??????????????????????????????????????????????????????
                    editEnabled = true
                }
            }
        }

        val itemCell = ItemsListCell(viewModel, model, fields, editEnabled)
        dataList.add(itemCell)
    }


    fun reload(parseStartNum:Int,parseFinNum:Int){
        //val colMax = fieldMap.size

        Single.fromCallable { itemsController.search(0L,parseStartNum,parseFinNum,endShow) }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy {
                if (it.isNotEmpty()) {
                    itemListAll.clear()
                    itemListAll.addAll(it)
                    setAdapter()
                } else {
                    //fieldMap.clear()
                    fields = mutableListOf()
                    progressBarFlg = false
                    chkProgress(progressBarFlg,rootView)
                }
            }
            .addTo(disposables)
    }


    override fun onDestroyView() {
        recyclerView.adapter = null
        disposables.dispose()
        super.onDestroyView()
    }

}

