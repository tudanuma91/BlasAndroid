package com.v3.basis.blas.ui.itemlist


import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager

import com.v3.basis.blas.R
import com.v3.basis.blas.blasclass.rest.BlasRestField
import kotlinx.android.synthetic.main.fragment_item_list.*


/**
 * A simple [Fragment] subclass.
 */
class ItemListFragment : Fragment() {
    private var token:String? = null
    private var project_id:String? = null
    private var map = mutableMapOf<String, String>()


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val extras = activity?.intent?.extras
        if(extras?.getString("token") != null) {
            token = extras?.getString("token")
        }
        if(extras?.getString("project_id") != null) {
            project_id = extras?.getString("project_id")
        }
        //呼ぶタイミングを確定させる！！
        var payload2 = mapOf("token" to token, "project_id" to project_id)
        BlasRestField(payload2, ::fieldS, ::fieldE).execute()



        val root = inflater.inflate(R.layout.fragment_item_list, container, false)
        return root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("lifeCycle", "onViewCreated")
        //リサイクラ-viewを取得
        val recyclerView = recycler_list
        //アダプター起動
        val adapter = ViewAdapter(createDataList(), object : ViewAdapter.ListListener {
            override fun onClickRow(tappedView: View, rowModel: RowModel) {
                //カードタップ時の処理
                Toast.makeText(activity, rowModel.title, Toast.LENGTH_LONG).show()
                Log.d(
                    "DataManagement",
                    "click_NAME => ${rowModel.title}/click_ID => ${rowModel.detail}"
                )
            }
        })
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.adapter = adapter
        Log.d("【onViewCreated時mapの値】","${map}")
    }

    private fun createDataList(): List<RowModel> {
        val dataList = mutableListOf<RowModel>()
        //プレファレンスにアクセス。値を取得する
        val pref = PreferenceManager.getDefaultSharedPreferences(activity)
        val aaa = pref.getStringSet("item_col${project_id}",mutableSetOf())

        for (i in 0..49) {
            var text = "test"
            var cnt = 1
            val data: RowModel = RowModel()
                .also {
                    // 一番上行を登録
                    it.title = "タイトル" + i + "だよ"
                    //2行目以降のテキスト作成
                    aaa?.forEach {
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
        }
        return dataList
    }

    private fun fieldS(result: MutableList<MutableMap<String, String?>>?) {
        val maps = mutableMapOf<String, String>()
        //共有プリファレンス起動
        val pref = PreferenceManager.getDefaultSharedPreferences(activity)
        val itemList= pref.getStringSet("item_col${project_id}", mutableSetOf())
        var cnt = 1
       // try {
        //初期化処理
        pref.edit().remove("item_col${project_id}").apply()
       /* }catch() {

        }*/
        if (result != null) {
            result.forEach {
                for ((k, v) in it) {
                    if(k == "name") {
                        // Log.d("aaaaa","${k}=>${v}")
                        //mapsに値を追加//
                        maps["fld_${cnt}"] = v.toString()
                        //itemListに値を追加//
                        itemList?.add(v.toString())
                        Log.d("【testtest】","${v}")
                        cnt += 1
                    }
                    /*else {
                        Log.d("aaaaaaaa","${k}=>${v}")
                    }*/
                }
            }
            //Log.d("aaa","${map}")
            //共有プリファレンスに値を格納
            Log.d("【aaa】","${itemList}")
            pref.edit().putStringSet("item_col${project_id}",itemList).apply()
            //mapsの値をmapに格納
            map = maps
            Log.d("【fieldS後のmapの値】","${map}")

        }
    }

    private fun fieldE(errorCode: Int) {
        Toast.makeText(getActivity(), errorCode.toString(), Toast.LENGTH_LONG).show()
    }



}
