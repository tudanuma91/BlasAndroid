package com.v3.basis.blas.ui.item.item_search_result


import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.v3.basis.blas.R
import com.v3.basis.blas.blasclass.app.searchAndroid
import com.v3.basis.blas.blasclass.helper.RestHelper
import com.v3.basis.blas.blasclass.rest.BlasRestErrCode
import com.v3.basis.blas.blasclass.rest.BlasRestField
import com.v3.basis.blas.blasclass.rest.BlasRestItem
import com.v3.basis.blas.ui.ext.getStringExtra
import kotlinx.android.synthetic.main.fragment_item_search_result.*
import org.json.JSONObject

/**
 * A simple [Fragment] subclass.
 */
class ItemSearchResultFragment : Fragment() {

    var token:String? = null
    var projectId:String? = null
    var freeWord :String? = ""
    var fldSize : String? = null
    var dateTimeCol:String = ""
    private val findValueMap:MutableMap<String,String?> = mutableMapOf()
    private val fieldMap: MutableMap<Int, MutableMap<String, String?>> = mutableMapOf()
    private val itemList: MutableList<MutableMap<String, String?>> = mutableListOf()
    private val jsonItemList:MutableMap<String, JSONObject> = mutableMapOf()
    private val dataList = mutableListOf<RowModel>()

    private val adapter: ViewAdapter = ViewAdapter(dataList, object : ViewAdapter.ListListener {
        override fun onClickRow(tappedView: View, rowModel: RowModel) {
            //カードタップ時の処理
        }
    })


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        //初期値の取得
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
            Log.d("デバック用のログ","日付・時間検索はcol${dateTimeCol}")
        }
        for (idx in 1 .. fldSize!!.toInt()){
            findValueMap.set("fld${idx}",getStringExtra("fld${idx}"))
        }

        Log.d("検索結果","freeword = ${freeWord}")
        Log.d("検索結果","fldSize = ${fldSize}")
        return inflater.inflate(R.layout.fragment_item_search_result, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("lifeCycle", "onViewCreated")
        //リサイクラ-viewを取得
        //基本的にデータはまだ到着していないため、空のアクティビティとadapterだけ設定しておく
        val recyclerView = recyclerView
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.adapter = adapter

        //呼ぶタイミングを確定させる！！
        val payload2 = mapOf("token" to token, "project_id" to projectId)
        BlasRestField(payload2, ::fieldRecv, ::fieldRecvError).execute()
        BlasRestItem("search", payload2, ::itemRecv, ::itemRecvError).execute()

    }

    private fun createDataList(): MutableList<RowModel> {
        var colMax = fieldMap.size
        var itemInfo = RestHelper().createItemList(jsonItemList, colMax )
        itemInfo = searchAndroid(findValueMap,itemInfo,dateTimeCol)
        itemList.addAll(itemInfo)
        itemList.forEach {
            val item_id = it["item_id"].toString()
            var text: String? = "String"
            var loopcnt = 1
            //カラムの定義取得
            for (col in 1..colMax) {
                val fldName = "fld${col}"
                //レコードの定義取得
                if (loopcnt == 1) {
                    text = "${fieldMap[col]!!["field_name"]}"
                    text += "\n${it[fldName]}"
                } else {
                    text += "\n${fieldMap[col]!!["field_name"]}"
                    text += "\n${it[fldName]}"
                }
                loopcnt += 1
            }
            val rowModel = RowModel().also {
                if (item_id != null) {
                    it.title = item_id
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
        return dataList
    }

    /**
     *  データ登録
     */
    private fun setAdapter() {
        Log.d("konishi", "setAdapter")
        createDataList()
        adapter.notifyItemInserted(0)
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
        val fieldList = RestHelper().createFieldList(result)
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
    }



}
