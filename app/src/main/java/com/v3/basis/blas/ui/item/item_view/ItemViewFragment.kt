package com.v3.basis.blas.ui.item.item_view


import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.v3.basis.blas.R
import com.v3.basis.blas.activity.ItemImageActivity
import com.v3.basis.blas.blasclass.config.FieldType
import com.v3.basis.blas.blasclass.helper.RestHelper
import com.v3.basis.blas.blasclass.rest.BlasRestErrCode
import com.v3.basis.blas.blasclass.rest.BlasRestField
import com.v3.basis.blas.blasclass.rest.BlasRestItem
import com.v3.basis.blas.ui.ext.addTitle
import com.v3.basis.blas.ui.ext.getStringExtra
import kotlinx.android.synthetic.main.fragment_item_view.recyclerView
import org.json.JSONObject
import java.lang.Exception


/**
 * A simple [Fragment] subclass.
 */
class ItemViewFragment : Fragment() {
    var token:String? = null
    var projectId:String? = null
    private var projectNames:String? = null
    private var normalShow = true
    private var endShow = false
    private var progressBarFlg = false

    private val fieldMap: MutableMap<Int, MutableMap<String, String?>> = mutableMapOf()
    private val itemListAll: MutableList<MutableMap<String, String?>> = mutableListOf()
    private val jsonItemList:MutableMap<String,JSONObject> = mutableMapOf()
    private val dataList = mutableListOf<RowModel>()
    private var baseList :MutableList<MutableMap<String,String?>> = mutableListOf()
    private lateinit var rootView:View
    private val helper:RestHelper = RestHelper()
    private var handler = Handler()
    private var currentIndex: Int = 0
    companion object {
        const val CREATE_UNIT = 20
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addTitle("projectName")
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
        projectNames = getStringExtra("projectName")
        progressBarFlg = true
        chkProgress(progressBarFlg,root)

        val normalSwitch = rootView.findViewById<Switch>(R.id.switch_normal)
        val endSwitch = rootView.findViewById<Switch>(R.id.switch_end_flg)
        val viewBtn = rootView.findViewById<Button>(R.id.button_view)
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
            Log.d("デバック管理","HEllo world")
            if(progressBarFlg){
                Log.d("デバック管理","処理させない。ぐるぐるしているから")
            }else{
                Log.d("デバック管理","いいっすよ！！！")
                createCardManager(baseList,fieldMap.size,"Update")
            }

        }

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
                        progressBarFlg = true
                        chkProgress(true, rootView)
                        setAdapter()
                    }
                }
            }
        })

        //呼ぶタイミングを確定させる！！
        try {
            progressBarFlg = true
            chkProgress(progressBarFlg, rootView)
            val payload2 = mapOf("token" to token, "project_id" to projectId)
            BlasRestField(payload2, ::fieldRecv, ::fieldRecvError).execute()
        }catch (e:Exception){
            //::TODO:: ここエラー内容分岐すること（可能性としてはtokenの受け渡し失敗等）
            Log.d("エラー","エラー発生:${e}")
        }
    }

    private fun createDataList() {
        var colMax = fieldMap.size
        val itemInfo = helper.createItemList(jsonItemList, colMax )
        itemListAll.addAll(itemInfo)
        createCardView()
    }

    private fun createCardView() {

        val colMax = fieldMap.size
        val list = mutableListOf<MutableMap<String, String?>>()

        list.addAll(itemListAll.filterIndexed { index, mutableMap ->
            (index >= currentIndex) && (index <= currentIndex + CREATE_UNIT)
        }.toMutableList())

        baseList = list


        createCardManager(list,colMax,"New")

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
        progressBarFlg = false
        chkProgress(progressBarFlg,rootView)
    }

    /**
     * データ取得時
     */
    private fun itemRecv(result: JSONObject) {
        itemListAll.clear()
        jsonItemList.clear()
        jsonItemList.set("1", result)
        setAdapter()
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
        val payload2 = mapOf("token" to token, "project_id" to projectId)
        BlasRestItem("search", payload2, ::itemRecv, ::itemRecvError).execute()

    }

    /**
     * フィールド取得失敗時
     */
    private fun fieldRecvError(errorCode: Int , aplCode:Int) {

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

        handler.post {
            Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show()
        }

        itemListAll.clear()
        fieldMap.clear()
        progressBarFlg = false
        chkProgress(progressBarFlg,rootView)
    }

    /**
     * データ取得失敗時
     */
    private fun itemRecvError(errorCode: Int , aplCode:Int) {
        Log.d("aaaaaaa", "失敗")

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
        handler.post {
            Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show()
        }

        //エラーのため、データを初期化する
        itemListAll.clear()
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
                text = "【${fieldMap[col]!!["field_name"]}】\n"
                text += "${it[fldName]}\n"
            } else {
                text += "【${fieldMap[col]!!["field_name"]}】\n"

                if (fieldMap[col]!!["type"] == FieldType.CHECK_VALUE) {
                    val newValue = helper.createCheckValue(it[fldName].toString())
                    text += "${newValue}\n"
                } else {
                    text += "${it[fldName]}\n"
                }

            }
            loopcnt += 1
        }
        return text
    }

    fun createCard(item_id:String,text: String?,valueFlg : String){
        val rowModel = RowModel().also {
            if(valueFlg == FieldType.END) {
                it.title = "${item_id}${FieldType.ENDTEXT}"
            }else{
                it.title = item_id
            }
            it.itemId = item_id

            it.detail = text.toString()
            it.projectId = projectId
            it.token = token
            it.itemList = itemListAll
            it.projectNames = projectNames
        }

        dataList.add(rowModel)
    }


}
