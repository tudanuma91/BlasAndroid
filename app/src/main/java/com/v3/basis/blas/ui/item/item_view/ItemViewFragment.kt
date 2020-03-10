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
import com.v3.basis.blas.blasclass.rest.BlasRestField
import com.v3.basis.blas.blasclass.rest.BlasRestItem
import com.v3.basis.blas.ui.ext.getStringExtra
import kotlinx.android.synthetic.main.fragment_item_view.*


/**
 * A simple [Fragment] subclass.
 */
class ItemViewFragment : Fragment() {

    private var token:String? = null
    private var projectId:String? = null
    private val fieldMap: MutableMap<Int, MutableMap<String, String?>> = mutableMapOf()
    private val itemList: MutableList<MutableMap<String, String?>> = mutableListOf()
    private val dataList = mutableListOf<RowModel>()

    private val adapter:ViewAdapter = ViewAdapter(dataList, object : ViewAdapter.ListListener {
        override fun onClickRow(tappedView: View, rowModel: RowModel) {
            //カードタップ時の処理
            Toast.makeText(activity, rowModel.title, Toast.LENGTH_LONG).show()
            Log.d(
                "DataManagement",
                "click_NAME => ${rowModel.title}/click_ID => ${rowModel.detail}"
            )
        }
    })

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        Log.d("【onCreateView】","呼ばれた")
        token = getStringExtra("token")
        projectId = getStringExtra("project_id")

        return inflater.inflate(R.layout.fragment_item_view, container, false)
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

        //プレファレンスにアクセス。値を取得する
        //val pref = PreferenceManager.getDefaultSharedPreferences(activity)
        //val aaa = pref.getStringSet("item_col${project_id}",mutableSetOf())
        var cnt = 1
        var old_item_id = "0"
        itemList.forEach {
            cnt += 1
        }

        //データ管理のループ
        itemList.forEach {
            val itemRecord = it
            val colMax = fieldMap.size
            val item_id = it["item_id"]
            var text: String? = "String"
            var loopcnt = 1

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
                        text = "${fieldMap[col]!!["name"]}"
                        text += "\n${itemRecord[fldName]}"
                    } else {
                        text += "\n${fieldMap[col]!!["name"]}"
                        text += "\n${itemRecord[fldName]}"
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

                }
                dataList.add(rowModel)
                old_item_id = item_id!!
            }
        }
        /*
        for (i in 0..49) {
            var text = "test"
            var cnt = 1
            val data: RowModel = RowModel()
                .also {
                    // 一番上行を登録
                    it.title = "タイトル" + i + "だよ"
                    //2行目以降のテキスト作成
                    for(j in 1..l){

                    }
                    field_map?.forEach {
                        when(cnt){
                            1 ->text = "【${it}】"
                            else ->text += "\n【${it}】"
                        }
                        cnt +=1
                    }
                    //二行目以降のテキストを登録
                    it.detail = text
                }

            dataList.add(data)
        }*/
        return dataList
    }

    /**
     *  データ登録
     */
    private fun setAdapter() {
        Log.d("konishi", "setAdapter")
        createDataList()
        adapter.notifyItemInserted(0)

        /*
        val adapter = ViewAdapter(createDataList(), object : ViewAdapter.ListListener {
            override fun onClickRow(tappedView: View, rowModel: RowModel) {
                //カードタップ時の処理
                Toast.makeText(activity, rowModel.title, Toast.LENGTH_LONG).show()
                Log.d(
                    "DataManagement",
                    "click_NAME => ${rowModel.title}/click_ID => ${rowModel.detail}"
                )
            }
        })*/
    }

    /**
     * データ取得時
     */
    private fun itemRecv(result: MutableList<MutableMap<String, String?>>?) {
        itemList.clear()
        result?.also { itemList.addAll(0, it) }
        if (itemList.isNotEmpty() && fieldMap.isNotEmpty()) {
            setAdapter()
        }
    }

    /**
     * フィールド取得時
     */
    private fun fieldRecv(result: MutableList<MutableMap<String, String?>>?) {
        //カラム順に並べ替える
        if(result != null){
            result.forEach {
                val col = it["col"]?.toInt()
                if(col != null){
                    //から矛盾にデータを入れ替える。リスト形式からmap形式に変更
                    fieldMap[col] = it
                }
            }
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
