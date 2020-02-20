package com.v3.basis.blas.ui.item.item_view


import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager

import com.v3.basis.blas.R
import com.v3.basis.blas.blasclass.rest.BlasRestField
import com.v3.basis.blas.blasclass.rest.BlasRestItem
import kotlinx.android.synthetic.main.fragment_item_view.*
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.v3.basis.blas.activity.ItemActivity


/**
 * A simple [Fragment] subclass.
 */
class ItemViewFragment : Fragment() {
    private var token:String? = null
    private var project_id:String? = null
    private var fieldMap: MutableMap<Int, MutableMap<String, String?>> = mutableMapOf<Int, MutableMap<String, String?>>()
    private var itemList:MutableList<MutableMap<String, String?>>? = null
    private var dataList = mutableListOf<RowModel>()
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
        // Inflate the layout for this fragment
        super.onCreateView(inflater, container, savedInstanceState)

        Log.d("【onCreateView】","呼ばれた")
        val extras = activity?.intent?.extras
        if(extras?.getString("token") != null) {
            token = extras?.getString("token")
        }
        if(extras?.getString("project_id") != null) {
            project_id = extras?.getString("project_id")
        }

        val root = inflater.inflate(R.layout.fragment_item_view, container, false)


        //リロードのぐるぐる出す処理
        val mSwipeRefresh = root.findViewById<SwipeRefreshLayout>(R.id.swipe)


        //下にスワイプした時の処理
        mSwipeRefresh.setOnRefreshListener {
            //ここにリロード処理
            //https://stackoverflow.com/questions/20702333/refresh-fragment-at-reload/20702418
            //https://tutorialmore.com/questions-776478.htm
            //TODO Twitterみたいなリロードできるようにする。
            //activityを削除、再生成する処理
            activity?.finish()
            val intent = Intent(activity, ItemActivity::class.java)
            intent.putExtra("project_id",project_id)
            intent.putExtra("toke",token)
            startActivity(intent)

            //上記の処理が終了次第ぐるぐるの表記をなくす処理
            if (mSwipeRefresh.isRefreshing()) {
                mSwipeRefresh.setRefreshing(false)
            }
            Log.d("【test】","リロードしました")
        }
        return root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("lifeCycle", "onViewCreated")
        //リサイクラ-viewを取得
        //基本的にデータはまだ到着していないため、空のアクティビティとadapterだけ設定しておく
        val recyclerView = recycler_list
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.adapter = adapter

        //呼ぶタイミングを確定させる！！
        var payload2 = mapOf("token" to token, "project_id" to project_id)
        BlasRestField(payload2, ::fieldRecv, ::fieldRecvError).execute()
        BlasRestItem("search", payload2, ::itemRecv, ::itemRecvError).execute()

    }

    private fun createDataList(): List<RowModel> {

        //プレファレンスにアクセス。値を取得する
        //val pref = PreferenceManager.getDefaultSharedPreferences(activity)
        //val aaa = pref.getStringSet("item_col${project_id}",mutableSetOf())
        var cnt = 1
        var old_item_id = "0"
        itemList?.forEach {
            cnt += 1
        }

        //データ管理のループ
        itemList?.forEach {
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
        if(adapter != null){
            adapter.notifyItemInserted(0)
        }

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
        itemList = result
        if(itemList != null && !fieldMap.isEmpty()) {
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

        if(itemList != null && !fieldMap.isEmpty()) {
            setAdapter()
        }
    }

    /**
     * フィールド取得失敗時
     */
    private fun fieldRecvError(errorCode: Int) {
        Toast.makeText(getActivity(), errorCode.toString(), Toast.LENGTH_LONG).show()
        //エラーのため、データを初期化する
        itemList = null
        fieldMap = mutableMapOf<Int, MutableMap<String, String?>>()

    }

    /**
     * データ取得失敗時
     */
    private fun itemRecvError(errorCode: Int) {
        Toast.makeText(getActivity(), errorCode.toString(), Toast.LENGTH_LONG).show()
        //エラーのため、データを初期化する
        itemList = null
        fieldMap = mutableMapOf<Int, MutableMap<String, String?>>()
    }

}
