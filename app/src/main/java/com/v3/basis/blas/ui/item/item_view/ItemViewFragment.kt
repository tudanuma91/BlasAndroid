package com.v3.basis.blas.ui.item.item_view


import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.v3.basis.blas.R
import com.v3.basis.blas.activity.ItemImageActivity
import com.v3.basis.blas.blasclass.helper.RestHelper
import com.v3.basis.blas.blasclass.rest.BlasRestField
import com.v3.basis.blas.blasclass.rest.BlasRestItem
import com.v3.basis.blas.ui.ext.getStringExtra
import kotlinx.android.synthetic.main.fragment_item_view.*
import org.json.JSONObject


/**
 * A simple [Fragment] subclass.
 */
class ItemViewFragment : Fragment() {

    var token:String? = null
    var projectId:String? = null
    private val fieldMap: MutableMap<Int, MutableMap<String, String?>> = mutableMapOf()
    private val itemList: MutableList<MutableMap<String, String?>> = mutableListOf()
    private val jsonItemList:MutableMap<String,JSONObject> = mutableMapOf()
    private val dataList = mutableListOf<RowModel>()


    private val adapter:ViewAdapter = ViewAdapter(dataList, object : ViewAdapter.ListListener {

        override fun onClickRow(tappedView: View, rowModel: RowModel) {
            //カードタップ時の処理
        }
<<<<<<< HEAD
=======

        override fun onClickImage(itemId: String?) {

            Log.d("test","$itemId")
            val context = requireContext()
            val intent = ItemImageActivity.createIntent(context, token, projectId, itemId)
            context.startActivity(intent)
        }
>>>>>>> d7b7520430702dee8020eef22f14840d7b67c91f
    })

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        val root = inflater.inflate(R.layout.fragment_item_view, container, false)
        token = getStringExtra("token")
        projectId = getStringExtra("project_id")

        return root
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

    private fun createDataList(): List<RowModel> {
        var colMax = fieldMap.size
        Log.d("gafeaqwaf","${colMax}")
        val itemInfo = RestHelper().createItemList(jsonItemList, colMax )
        itemList.addAll(itemInfo)
        itemList.forEach {
            val item_id = it["item_id"].toString()
            var text: String? = "String"
            var loopcnt = 1
            Log.d("freaga", "${it}")
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
<<<<<<< HEAD
                if (text != null) {
                    it.detail = text
=======
                val rowModel = RowModel().also {
                    if (item_id != null) {
                        it.title = item_id
                        it.itemId = item_id
                    }
                    if (text != null) {
                        it.detail = text
                    }
                    it.projectId = projectId
                    it.token = token
                    it.itemList = itemList
>>>>>>> d7b7520430702dee8020eef22f14840d7b67c91f
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
        Log.d("aaaaaaa","${result}")
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
        Log.d("aaaaa","${result}")
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
    private fun fieldRecvError(errorCode: Int) {
        Toast.makeText(getActivity(), errorCode.toString(), Toast.LENGTH_LONG).show()
        itemList.clear()
        fieldMap.clear()
    }

    /**
     * データ取得失敗時
     */
    private fun itemRecvError(errorCode: Int) {
        Toast.makeText(getActivity(), errorCode.toString(), Toast.LENGTH_LONG).show()
        //エラーのため、データを初期化する
        itemList.clear()
        fieldMap.clear()
    }

}
