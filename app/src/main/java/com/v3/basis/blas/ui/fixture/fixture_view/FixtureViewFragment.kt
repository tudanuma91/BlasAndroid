package com.v3.basis.blas.ui.fixture.fixture_view


import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.v3.basis.blas.R
import com.v3.basis.blas.blasclass.rest.BlasRestFixture
import com.v3.basis.blas.ui.fixture.fixture_view.RowModel
import com.v3.basis.blas.ui.fixture.fixture_view.ViewAdapter
import kotlinx.android.synthetic.main.fragment_item_view.*


/**
 * A simple [Fragment] subclass.
 */
class FixtureViewFragment : Fragment() {
    private var token:String? = null
    private var project_id:String? = null
    private var fieldMap : MutableMap<Int, MutableMap<String, String?>> = mutableMapOf<Int, MutableMap<String, String?>>()
    private var itemList :MutableList<MutableMap<String, String?>>? = null
    private var dataList = mutableListOf<RowModel>()
    private val adapter: ViewAdapter = ViewAdapter(dataList, object : ViewAdapter.ListListener {
        override fun onClickRow(tappedView: View, rowModel: com.v3.basis.blas.ui.fixture.fixture_view.RowModel) {
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
            Log.d("token_fixture","${token}")
        }
        if(extras?.getString("project_id") != null) {
            project_id = extras?.getString("project_id")
        }

        val root = inflater.inflate(R.layout.fragment_item_view, container, false)
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
        Log.d("testtest","取得する")
        BlasRestFixture("search",payload2, ::fieldRecv, ::fieldRecvError).execute()
    }

    private fun createDataList(): List<RowModel> {
      /*  var cnt = 1
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
        */
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
     * フィールド取得時
     */
    private fun fieldRecv(result: MutableList<MutableMap<String, String?>>?) {
        //カラム順に並べ替える
        Log.d("testtest","${result}")
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
        Log.d("取得失敗","取得失敗")
        Log.d("取得失敗","${errorCode}")


    }

}
