package com.v3.basis.blas.ui.item.item_search_result


import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.v3.basis.blas.R
import com.v3.basis.blas.blasclass.app.searchAndroid
import com.v3.basis.blas.blasclass.config.FieldType
import com.v3.basis.blas.blasclass.helper.RestHelper
import com.v3.basis.blas.blasclass.rest.BlasRestErrCode
import com.v3.basis.blas.blasclass.rest.BlasRestField
import com.v3.basis.blas.blasclass.rest.BlasRestItem
import com.v3.basis.blas.ui.ext.addTitle
import com.v3.basis.blas.ui.ext.getStringExtra
import kotlinx.android.synthetic.main.fragment_item_search_result.*
import org.json.JSONObject
import java.lang.Exception

/**
 * A simple [Fragment] subclass.
 */
class ItemSearchResultFragment : Fragment() {

    var token:String? = null
    var projectId:String? = null
    var freeWord :String? = ""
    var fldSize : String? = null
    var dateTimeCol:String = ""
    var checkValueCol:String = ""
    private var normalShow = true
    private var endShow = false
    private var progressBarFlg = false
    private lateinit var rootView:View

    private val findValueMap:MutableMap<String,String?> = mutableMapOf()
    private val fieldMap: MutableMap<Int, MutableMap<String, String?>> = mutableMapOf()
    private val itemList: MutableList<MutableMap<String, String?>> = mutableListOf()
    private val jsonItemList:MutableMap<String, JSONObject> = mutableMapOf()
    private val dataList = mutableListOf<RowModel>()
    private var baseList :MutableList<MutableMap<String,String?>> = mutableListOf()
    private val helper:RestHelper = RestHelper()

    private val adapter: ViewAdapter = ViewAdapter(dataList, object : ViewAdapter.ListListener {
        override fun onClickRow(tappedView: View, rowModel: RowModel) {
            //カードタップ時の処理
        }
    })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addTitle("projectName")
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        super.onCreateView(inflater, container, savedInstanceState)
        //初期値の取得
        val root = inflater.inflate(R.layout.fragment_item_search_result, container, false)
        rootView = root
        if(getStringExtra("token") != null){
            token = getStringExtra("token")
        }
        if(getStringExtra("project_id") != null){
            projectId = getStringExtra("project_id")
        }
        if(getStringExtra("fldSize")!= null){
            fldSize = getStringExtra("fldSize")
        }
        if(getStringExtra("freeWord")!=null){
            freeWord = getStringExtra("freeWord")
            findValueMap.set("freeWord",freeWord)
        }
        if(getStringExtra("dateTimeCol")!= null){
            dateTimeCol = getStringExtra("dateTimeCol").toString()
        }
        if(getStringExtra("checkValueCol")!= null){
            checkValueCol = getStringExtra("checkValueCol").toString()
        }
        for (idx in 1 .. fldSize!!.toInt()){
            findValueMap.set("fld${idx}",getStringExtra("fld${idx}"))
        }

        val normalSwitch = rootView.findViewById<Switch>(R.id.switch_normal_result)
        val endSwitch = rootView.findViewById<Switch>(R.id.switch_end_result)
        val viewBtn = rootView.findViewById<Button>(R.id.button_view_result)
        normalSwitch.setOnCheckedChangeListener{ _, isChecked->
            if(isChecked){
                Log.d("デバック管理","チェックされた。ONになった")
                normalShow = true
            }else{
                Log.d("デバック管理","チェックされた。OFFになった")
                normalShow = false
            }
        }

        endSwitch.setOnCheckedChangeListener{_ ,isChecked->
            if(isChecked){
                Log.d("デバック管理","チェックされた。ONになった")
                endShow = true
            }else{
                Log.d("デバック管理","チェックされた。OFFになった")
                endShow = false
            }
        }

        viewBtn.setOnClickListener{
            Log.d("デバック管理","Hello World")
            if(progressBarFlg){
                Log.d("デバック管理","処理させない。ぐるぐるしているから")
            }else{
                Log.d("デバック管理","いいっすよ！！！")
                createCardManager(baseList,fieldMap.size,"Update")
            }

        }
        return root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("lifeCycle", "onViewCreated")
        try {
            if(token != null && fldSize != null && findValueMap != null && dateTimeCol != null && checkValueCol != null) {
                //リサイクラ-viewを取得
                //基本的にデータはまだ到着していないため、空のアクティビティとadapterだけ設定しておく
                progressBarFlg = true
                chkProgress(progressBarFlg, rootView)
                val recyclerView = recyclerView
                recyclerView.setHasFixedSize(true)
                recyclerView.layoutManager = LinearLayoutManager(activity)
                recyclerView.adapter = adapter

                //呼ぶタイミングを確定させる！！
                val payload2 = mapOf("token" to token, "project_id" to projectId)
                BlasRestField(payload2, ::fieldRecv, ::fieldRecvError).execute()
                BlasRestItem("search", payload2, ::itemRecv, ::itemRecvError).execute()
            }
        }catch (e:Exception){

        }

    }

    private fun createDataList() {
        var colMax = fieldMap.size
        var itemInfo = helper.createItemList(jsonItemList, colMax )
        itemInfo = searchAndroid(findValueMap,itemInfo,dateTimeCol,checkValueCol)
        itemList.addAll(itemInfo)

        baseList = itemList
        if(baseList.size > 0) {
            createCardManager(itemList, colMax, "New")
        }else{
            val title = getString(R.string.dialog_title)
            val text = getString(R.string.search_error)
            AlertDialog.Builder(activity)
                .setTitle(title)
                .setMessage(text)
                .setPositiveButton("OK"){ dialog, which ->
                    activity!!.finish()
                }
                .show()
        }
    }

    /**
     *  データ登録
     */
    private fun setAdapter() {
        Log.d("konishi", "setAdapter")
        createDataList()
        adapter.notifyItemInserted(0)
        progressBarFlg = false
        chkProgress(progressBarFlg,rootView)
    }

    /**
     * データ取得時
     */
    private fun itemRecv(result: JSONObject) {
        itemList.clear()
        jsonItemList.clear()
        //val itemMap = RestHelper().createItemList(result)
        //itemMap?.also { itemList.addAll(0, itemMap) }
        jsonItemList.set("1",result)
        if (jsonItemList.isNotEmpty() && fieldMap.isNotEmpty()) {
            setAdapter()
        }
    }

    /**
     * フィールド取得時
     */
    private fun fieldRecv(result: JSONObject) {
        //カラム順に並べ替える
        fieldMap.clear()
        val fieldList = helper.createFieldList(result)
        var cnt = 1
        fieldList.forEach{
            fieldMap[cnt] = it
            cnt +=1
        }

        if(jsonItemList.isNotEmpty() && fieldMap.isNotEmpty()) {
            setAdapter()
        }
    }

    /**
     * フィールド取得失敗時
     */
    private fun fieldRecvError(errorCode: Int, aplCode:Int) {

        var message:String? = null
        when(errorCode) {
            BlasRestErrCode.DB_NOT_FOUND_RECORD -> {
                message = getString(R.string.record_not_found)
            }
            BlasRestErrCode.NETWORK_ERROR -> {
                //サーバと通信できません
                message = getString(R.string.network_error)
            }
            else-> {
                //サーバでエラーが発生しました(要因コード)
                message = getString(R.string.server_error, errorCode)
            }
        }
        Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show()
        itemList.clear()
        fieldMap.clear()
        progressBarFlg = false
        chkProgress(progressBarFlg,rootView)

    }

    /**
     * データ取得失敗時
     */
    private fun itemRecvError(errorCode: Int, aplCode:Int) {

        var message:String? = null
        when(errorCode) {
            BlasRestErrCode.DB_NOT_FOUND_RECORD -> {
                message = getString(R.string.record_not_found)
            }
            BlasRestErrCode.NETWORK_ERROR -> {
                //サーバと通信できません
                message = getString(R.string.network_error)
            }
            else-> {
                //サーバでエラーが発生しました(要因コード)
                message = getString(R.string.server_error, errorCode)
            }
        }

        Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show()
        //エラーのため、データを初期化する
        itemList.clear()
        fieldMap.clear()
        progressBarFlg = false
        chkProgress(progressBarFlg,rootView)
    }

    private fun  createCardManager(list:MutableList<MutableMap<String,String?>>,colMax : Int,mode:String){
        Log.d("デバック処理","ノーマルshowの値=>${normalShow}")
        Log.d("デバック処理","エンドshowの値=>${endShow}")
        if(mode == "New"){
            list.forEach {
                val valueFlg = it["endFlg"].toString()
                if (valueFlg == FieldType.NORMAL) {
                    val item_id = it["item_id"].toString()
                    var text: String? = ""
                    text = createCardText(text, it, colMax)
                    createCard(item_id, text,valueFlg)
                }
            }

        }else {
            //アダプターにつないでいるデータリストを削除する
            progressBarFlg = true
            chkProgress(progressBarFlg,rootView)
            dataList.clear()

            //以下はカードを作成する処理。画面上部のスイッチの状態によって処理内容を変更する
            if (normalShow && endShow) {
                list.forEach {
                    val valueFlg = it["endFlg"].toString()
                    val item_id = it["item_id"].toString()
                    var text: String? = ""
                    text = createCardText(text, it, colMax)
                    createCard(item_id, text,valueFlg)
                }

            } else if (!normalShow && endShow) {
                list.forEach {
                    val valueFlg = it["endFlg"].toString()
                    if (valueFlg == FieldType.END) {
                        val item_id = it["item_id"].toString()
                        var text: String? = ""
                        text = createCardText(text, it, colMax)
                        createCard(item_id, text,valueFlg)
                    }
                }
            } else if (normalShow && !endShow) {
                list.forEach {
                    val valueFlg = it["endFlg"].toString()
                    if (valueFlg == FieldType.NORMAL) {
                        val item_id = it["item_id"].toString()
                        var text: String? = ""
                        text = createCardText(text, it, colMax)
                        createCard(item_id, text,valueFlg)
                    }
                }

            } else {

            }
            //adapterにリストの内容を変更したことを伝える処理
            adapter.notifyDataSetChanged()
            progressBarFlg = false
            chkProgress(progressBarFlg,rootView)
        }
    }

    private fun createCardText(text:String?,it:MutableMap<String,String?>,colMax: Int): String? {
        var loopcnt = 1
        var text = text
        for (col in 1..colMax) {
            val fldName = "fld${col}"
            //レコードの定義取得
            if (loopcnt == 1) {
                text = "[${fieldMap[col]!!["field_name"]}]"
                text += "\n${it[fldName]}\n"
            } else {
                Log.d("フィールドの値：","値=>${it[fldName]}")
                text += "[${fieldMap[col]!!["field_name"]}]\n"
                if(fieldMap[col]!!["type"] == FieldType.CHECK_VALUE){
                    val newValue = helper.createCheckValue(it[fldName].toString())
                    text += "${newValue}\n"
                }else {
                    text += "${it[fldName]}\n"
                }
            }
            loopcnt += 1
        }
        return text
    }

    fun createCard(item_id:String,text: String?,valueFlg:String){
        val rowModel = RowModel().also {
            if (item_id != null) {
                if(valueFlg == FieldType.END) {
                    it.title = "${item_id}${FieldType.ENDTEXT}"
                }else{
                    it.title = item_id
                }
            }
            if (text != null) {
                it.detail = text
            }
            it.projectId = projectId
            it.token = token
            it.itemList = itemList
        }
        dataList.add(rowModel)
    }

    private fun chkProgress(flg:Boolean,view:View){
        val progressbar = view.findViewById<ProgressBar>(R.id.progressBarItemView)
        if (flg) {
            progressbar.visibility = android.widget.ProgressBar.VISIBLE

        } else {
            progressbar.visibility = android.widget.ProgressBar.INVISIBLE
        }
    }

}
