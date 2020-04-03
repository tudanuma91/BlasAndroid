package com.v3.basis.blas.ui.test3.test3First

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.v3.basis.blas.R
import kotlinx.android.synthetic.main.fragment_test3_first.*

/**
 * A simple [Fragment] subclass.
 */
class Test3FirstFragment : Fragment() {

    private var ENTER_DATA_LIST : MutableMap<String, String> = mutableMapOf()
    private var DATA_LIST : MutableList<RowModel> = mutableListOf()
    //private var PROGRESS_FLG = false
    private val DATA_SIZE = 10500
    private val PAGE_NUMBER = 20

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val root = inflater.inflate(R.layout.fragment_test3_first, container, false)
        chkProgress(false,root)
        return root
    }

    private fun chkProgress(flg:Boolean,view:View){
        val progressbar = view.findViewById<ProgressBar>(R.id.progressBars)
        if (flg) {
            progressbar.visibility = android.widget.ProgressBar.VISIBLE

        } else {
            progressbar.visibility = android.widget.ProgressBar.INVISIBLE
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ENTER_DATA_LIST = createEnterdataList(DATA_SIZE)
        val recyclerview = recyclerView
        val adapter =ViewAdapter(DATA_LIST,
            object : ViewAdapter.ListListener{
                override fun onClickRow(tappendView: View, rowModel: RowModel) {
                    Toast.makeText(activity, rowModel.title, Toast.LENGTH_LONG).show()
                    Log.d(
                        "DataManagement",
                        "click_NAME => ${rowModel.title}/click_ID => ${rowModel.detail}"
                    )
                }
            })
        recyclerview?.setHasFixedSize(true)
        recyclerview?.layoutManager = LinearLayoutManager(activity)
        recyclerview?.adapter = adapter
        //ここから追加処理
        recyclerview?.addOnScrollListener(object : RecyclerView.OnScrollListener()  {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                //合計の件数
                val totalItemCount = recyclerview?.layoutManager?.run { itemCount }
                //表示件数
                val childCount = recyclerview?.childCount
                val layoutManager = recyclerView.layoutManager
                val linearLayoutManager = layoutManager as LinearLayoutManager
                //今表示されている件の中から一番上の値
                val firstPosition = linearLayoutManager.findFirstVisibleItemPosition() // RecyclerViewの一番上に表示されているアイテムのポジション

                Log.d("データの総数","${DATA_SIZE}")
                Log.d("表示している全件数","${totalItemCount}")
                Log.d("画面に表示している件数","${childCount}")
                Log.d("画面一番上にいるデータの番号","${firstPosition}")

                if (totalItemCount!!-1 < childCount + firstPosition) {
                    val value_size = DATA_SIZE - totalItemCount
                    when{
                        value_size >= PAGE_NUMBER -> {//残りの表示データが20件以上の時
                            chkProgress(true,view)
                            Log.d("aaaaaaaaaaaaa","ここでリロードしたい！！")
                            //var newDataList = createDataList(ENTER_DATA_LIST,0,totalItemCount+PAGE_NUMBER)
                            //DATA_LIST.clear()
                            //DATA_LIST.addAll(newDataList)
                            for(i in totalItemCount .. totalItemCount+PAGE_NUMBER){
                                val data : RowModel =
                                    RowModel().also {
                                        val value1: String = ENTER_DATA_LIST["id${i}"]!!
                                        val value2:String = ENTER_DATA_LIST["title${i}"]!!
                                        it.title = value1
                                        it.detail = value2
                                    }
                                val datas = createValue(ENTER_DATA_LIST,i)
                                DATA_LIST.add(data)
                            }
                            adapter.notifyDataSetChanged()
                            chkProgress(false,view)
                        }
                        (PAGE_NUMBER > value_size)&&(value_size > 0) ->{//残りの表示データが19件以下0件を超える場合の処理
                            chkProgress(true,view)
                            Log.d("aaaaaaaaaaaaa","少しリロード！！")
                            // var newDataList = createDataList(ENTER_DATA_LIST,0,DATA_SIZE)
                            //DATA_LIST.addAll(newDataList)
                            for(i in totalItemCount .. DATA_SIZE){
                                val data : RowModel =
                                    RowModel().also {
                                        val value1: String = ENTER_DATA_LIST["id${i}"]!!
                                        val value2:String = ENTER_DATA_LIST["title${i}"]!!
                                        it.title = value1
                                        it.detail = value2
                                    }
                                DATA_LIST.add(data)
                            }
                            adapter.notifyDataSetChanged()
                            chkProgress(false,view)
                        }
                        (value_size == 0 )->{
                            chkProgress(true,view)
                            Log.d("aaaaaaaaaaaaa","1件のみリロード！！")
                            val data : RowModel =
                                RowModel().also {
                                    val value1: String = ENTER_DATA_LIST["id${totalItemCount}"]!!
                                    val value2:String = ENTER_DATA_LIST["title${totalItemCount}"]!!
                                    it.title = value1
                                    it.detail = value2
                                }
                            DATA_LIST.add(data)
                            adapter.notifyDataSetChanged()
                            chkProgress(false,view)
                        }
                    }
                }
            }
        })
    }

    private fun createValue(enterDataList:MutableMap<String, String>,i:Int){

    }



    //これは無視してOK
    fun createEnterdataList(cnt : Int): MutableMap<String, String> {
        val dataList = mutableMapOf<String,String>()
        for(i in 0..cnt){
            //val value = mapOf("id" to i.toString(), "title" to "title${i}")
            // dataList["id" to i.toString(), "title" to "title${i}"]
            dataList["id${i}"] = i.toString()
            dataList["title${i}"] = "title${i}"
        }

        return dataList
    }
}