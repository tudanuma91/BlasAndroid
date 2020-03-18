package com.v3.basis.blas.ui.item.item_view


import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.v3.basis.blas.R
import com.v3.basis.blas.activity.ItemImageActivity
import com.v3.basis.blas.blasclass.helper.RestHelper
import com.v3.basis.blas.blasclass.rest.BlasRestField
import com.v3.basis.blas.blasclass.rest.BlasRestImageField
import com.v3.basis.blas.blasclass.rest.BlasRestItem
import com.v3.basis.blas.ui.ext.getStringExtra
import com.v3.basis.blas.ui.item.item_view.model.ImageFieldModel
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
    private val dataList = mutableListOf<RowModel>()

    private lateinit var imageField: ImageFieldModel


    private val adapter:ViewAdapter = ViewAdapter(dataList, object : ViewAdapter.ListListener {

        override fun onClickRow(tappedView: View, rowModel: RowModel) {
            //カードタップ時の処理
            Toast.makeText(activity, rowModel.title, Toast.LENGTH_LONG).show()
            Log.d(
                "DataManagement",
                "click_NAME => ${rowModel.title}/click_ID => ${rowModel.detail}"
            )
        }

        override fun onClickImage(itemId: String?) {

            Log.d("test","$itemId")
            val context = requireContext()
            val intent = ItemImageActivity.createIntent(context, token, projectId, itemId)
            context.startActivity(intent)
        }
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
        BlasRestImageField(payload2, ::imageFieldCallback, ::itemRecvError).execute()
    }

    private fun imageFieldCallback(json: JSONObject) {

        Log.d("image field","${json}")
        json.toString().also {
            val gson = Gson()
            imageField = gson.fromJson(it, ImageFieldModel::class.java)
        }
    }

    private fun createDataList(): List<RowModel> {

        var cnt = 1
        var old_item_id = "0"
        itemList.forEach {
            cnt += 1
        }

        //データ管理のループ
        itemList.forEach {
            val itemRecord = it
            val colMax = fieldMap.size
            val item_id = itemRecord["item_id"].toString()
            var text: String? = "String"
            var loopcnt = 1
            Log.d("freaga","${it}")

            if (item_id?.toInt() !== old_item_id.toInt()) {
                //Log.d("【old_item_id】", "${old_item_id}")
                //Log.d("【item_id】", "${item_id}")
                //カラムの定義取得
                for (col in 1..colMax) {
                    val fldName = "fld${col}"
                    //レコードの定義取得
                    if (loopcnt == 1) {
                        // val text = itemRecord[fldName]
                        //カラムの型取得 nullableうぜえええええ
                        //var type:String? = fieldMap[col]!!["type"]
                        //カラム名取得
                        //val colName = fieldMap[col]!!["name"]
                        //カラム名の取得
                        text = "${fieldMap[col]!!["field_name"]}"
                        text += "\n${itemRecord[fldName]}"
                    } else {
                        text += "\n${fieldMap[col]!!["field_name"]}"
                        text += "\n${itemRecord[fldName]}"
                    }
                    loopcnt += 1
                }
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
                }
                dataList.add(rowModel)
                old_item_id = item_id!!
            }
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
        Log.d("aaaaaaa","${result}")
        val itemMap = RestHelper().createItemList(result)

        itemMap?.also { itemList.addAll(0, itemMap) }
        if (itemList.isNotEmpty() && fieldMap.isNotEmpty()) {
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

        if(itemList.isNotEmpty() && fieldMap.isNotEmpty()) {
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
