package com.v3.basis.blas.ui.item.item_view


import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.v3.basis.blas.R
import com.v3.basis.blas.activity.ItemImageActivity
import com.v3.basis.blas.blasclass.helper.RestHelper
import com.v3.basis.blas.blasclass.rest.BlasRestField
import com.v3.basis.blas.blasclass.rest.BlasRestItem
import com.v3.basis.blas.ui.ext.getStringExtra
import kotlinx.android.synthetic.main.fragment_item_view.recyclerView
import org.json.JSONObject


/**
 * A simple [Fragment] subclass.
 */
class ItemViewFragment : Fragment() {
    var token:String? = null
    var projectId:String? = null
    private val fieldMap: MutableMap<Int, MutableMap<String, String?>> = mutableMapOf()
    private val itemListAll: MutableList<MutableMap<String, String?>> = mutableListOf()
    private val jsonItemList:MutableMap<String,JSONObject> = mutableMapOf()
    private val dataList = mutableListOf<RowModel>()
    private var rootView:View? = null

    private var currentIndex: Int = 0
    companion object {
        const val CREATE_UNIT = 20
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }


    private val adapter:ViewAdapter = ViewAdapter(dataList, object : ViewAdapter.ListListener {

        override fun onClickRow(tappedView: View, rowModel: RowModel) {
            //カードタップ時の処理
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
        rootView = root
        token = getStringExtra("token")
        projectId = getStringExtra("project_id")
        chkProgress(false,root)

        return root
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
        Log.d("lifeCycle", "onViewCreated")
        //リサイクラ-viewを取得
        //基本的にデータはまだ到着していないため、空のアクティビティとadapterだけ設定しておく
        val recyclerView = recyclerView
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (itemListAll.isNotEmpty()) {
                    val notOverSize = currentIndex + CREATE_UNIT <= itemListAll.size
                    if (!recyclerView.canScrollVertically(1) && notOverSize) {
                        chkProgress(true, rootView!!)
                        setAdapter()
                    }
                }
            }
        })

        //呼ぶタイミングを確定させる！！
        chkProgress(true,rootView!!)
        val payload2 = mapOf("token" to token, "project_id" to projectId)
        Log.d("aaaaaaa", "処理開始")
        BlasRestField(payload2, ::fieldRecv, ::fieldRecvError).execute()
    }

    private fun createDataList() {
        var colMax = fieldMap.size
        Log.d("gafeaqwaf","${colMax}")
        val itemInfo = RestHelper().createItemList(jsonItemList, colMax )
        itemListAll.addAll(itemInfo)
        makeDataList()
    }

    private fun makeDataList() {

        val colMax = fieldMap.size
        val list = mutableListOf<MutableMap<String, String?>>()

        list.addAll(itemListAll.filterIndexed { index, mutableMap ->
            (index >= currentIndex) && (index <= currentIndex + CREATE_UNIT)
        }.toMutableList())

        list.forEach {
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
                    it.itemId = item_id
                    // it.detail = text!!
                }
                if (text != null) {
                    it.detail = text
                }
                it.projectId = projectId
                it.token = token
                it.itemList = itemListAll
            }

            dataList.add(rowModel)
        }

        // update
        if (list.isNotEmpty()) {
            currentIndex += CREATE_UNIT
        }
    }

    /**
     *  データ登録
     */
    private fun setAdapter() {
        Log.d("konishi", "setAdapter")
        createDataList()
        adapter.notifyItemInserted(0)
        chkProgress(false,rootView!!)
    }

    /**
     * データ取得時
     */
    private fun itemRecv(result: JSONObject) {
        Log.d("aaaaaaa", "成功")
        itemListAll.clear()
        jsonItemList.clear()
        Log.d("aaaaaaa", "${result}")
        //val itemMap = RestHelper().createItemList(result)
        //itemMap?.also { itemList.addAll(0, itemMap) }
        jsonItemList.set("1", result)
        if(jsonItemList.isEmpty()){
            Log.d("fteay","jsonitemListはnull")
        }
        if (jsonItemList.isNotEmpty() && fieldMap.isNotEmpty()) {
            setAdapter()
        }
    }

    /**
     * フィールド取得時
     */
    private fun fieldRecv(result: JSONObject) {
        Log.d("aaaaaaa", "成功")
        //カラム順に並べ替える
        fieldMap.clear()
        Log.d("aaaaa","${result}")
        val fieldList = RestHelper().createFieldList(result)
        var cnt = 1
        fieldList.forEach{
            fieldMap[cnt] = it
            cnt +=1
        }
        if(fieldMap.isEmpty()){
            Log.d("fteay","fieldMapはnull")
        } else {
            val payload2 = mapOf("token" to token, "project_id" to projectId)
            BlasRestItem("search", payload2, ::itemRecv, ::itemRecvError).execute()
        }
    }

    /**
     * フィールド取得失敗時
     */
    private fun fieldRecvError(errorCode: Int , aplCode:Int) {
        Log.d("aaaaaaa", "失敗")
        Toast.makeText(getActivity(), errorCode.toString(), Toast.LENGTH_LONG).show()
        itemListAll.clear()
        fieldMap.clear()
    }

    /**
     * データ取得失敗時
     */
    private fun itemRecvError(errorCode: Int , aplCode:Int) {
        Log.d("aaaaaaa", "失敗")
        Toast.makeText(getActivity(), errorCode.toString(), Toast.LENGTH_LONG).show()
        //エラーのため、データを初期化する
        itemListAll.clear()
        fieldMap.clear()
    }
}
