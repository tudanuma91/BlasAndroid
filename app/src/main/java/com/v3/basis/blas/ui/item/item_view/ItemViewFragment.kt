package com.v3.basis.blas.ui.item.item_view


import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Switch
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.JsonArray
import com.v3.basis.blas.R
import com.v3.basis.blas.activity.ItemImageActivity
import com.v3.basis.blas.blasclass.app.BlasMsg
import com.v3.basis.blas.blasclass.config.FieldType
import com.v3.basis.blas.blasclass.helper.RestHelper
import com.v3.basis.blas.blasclass.rest.BlasRestErrCode
import com.v3.basis.blas.blasclass.rest.BlasRestField
import com.v3.basis.blas.blasclass.rest.BlasRestItem
import com.v3.basis.blas.ui.ext.addTitle
import com.v3.basis.blas.ui.ext.getStringExtra
import kotlinx.android.synthetic.main.fragment_item_view.*
import org.json.JSONArray
import org.json.JSONObject


/**
 * A simple [Fragment] subclass.
 */
class ItemViewFragment : Fragment() {
    lateinit var token:String
    private var projectNames:String? = null
    lateinit var projectId :String
    private var msg = BlasMsg()
    private val toastErrorLen = Toast.LENGTH_LONG

    private var endShow = false
    private var progressBarFlg = false
    private var parseNum = 10
    private var parseStartNum = 0
    private var parseFinNum = parseNum

    private val fieldMap: MutableMap<Int, MutableMap<String, String?>> = mutableMapOf()
    private val itemListAll: MutableList<MutableMap<String, String?>> = mutableListOf()
    private var jsonItemList: JSONObject? = null
    private val dataList = mutableListOf<RowModel>()
    private var jsonParseList :JSONArray? = null

    private lateinit var rootView:View
    private val helper:RestHelper = RestHelper()
    private var handler = Handler()
    private var currentIndex: Int = 0
    companion object {
        const val CREATE_UNIT = 5
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

            val context = requireContext()
            val intent = ItemImageActivity.createIntent(context, token, projectId, itemId)

            context.startActivity(intent)
        }
    })

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        val root = inflater.inflate(R.layout.fragment_item_view, container, false)
        rootView = root

        val extras = activity?.intent?.extras
        if(extras?.getString("token") != null ) {
            token = extras.getString("token").toString() //トークンの値を取得
        }
        if(extras?.getString("project_id") != null ) {
            projectId = extras.getString("project_id").toString() //トークンの値を取得
        }
        if(extras?.getString("projectName") != null ) {
            projectNames = extras.getString("projectName") //トークンの値を取得
        }

        progressBarFlg = true
        chkProgress(progressBarFlg,root)

        val endSwitch = rootView.findViewById<Switch>(R.id.switch_end_flg)
        val viewBtn = rootView.findViewById<Button>(R.id.button_view)

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
            if(progressBarFlg){
                Log.d("デバック管理","処理させない。ぐるぐるしているから")
            }else{
                Log.d("デバック管理","処理開始")
                //ぐるぐるを呼ぶ
                progressBarFlg = true
                chkProgress(progressBarFlg,rootView)

                //値の初期化
                dataList.clear()
                adapter.notifyDataSetChanged()
                currentIndex = 0
                parseStartNum = 0
                parseFinNum = parseNum
                itemListAll.clear()

                //カードの再作成
                jsonParse(parseStartNum, parseFinNum)
                setAdapter()
                progressBarFlg = false
                chkProgress(progressBarFlg,rootView)
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

                    val notOverSize = currentIndex  <= itemListAll.size
                    if (!recyclerView.canScrollVertically(1) && notOverSize) {
                        progressBarFlg = true
                        chkProgress(true, rootView)

                        //下追加分
                        /*
                        [随時追加処理(不安なので記述する。)]
                            currentIndexは現在作成したカードの枚数、parseNumは○○枚ずつパースするを定義した数
                            カードの作成枚数が○○枚に達した場合、再びパースする
                            パースするのは同じ数（ただし、端数は除く）
                            (例：42件のレコードを20件ずつパースし、10枚ずつ作成する場合)
                            parseNum = 20
                            ①   currentIndex = 10
                                 10 % 20 !=0よって、パースを実行せずカードを作成する。
                            ②   currentIndex = 20
                                 20%20 = 0 よってパースし、カードを作成する。
                            ③   currentIndex = 30
                                 30 % 20 != 10 よってパースを実行せずに、カードを作成する
                            ④   currentIndex = 40
                                 40 % 20 = 0 よってパースし、カードを作成する。
                            ⑤   currentIndex = 42
                                 上のval notOverSize = currentIndex  <= itemListAll.size がfalseになり下の処理が走らない


                        */
                        if(currentIndex%parseNum  == 0) {
                            nextParseNum()
                            jsonParse(parseStartNum, parseFinNum)
                        }

                        setAdapter()
                    }
                }
            }
        })

        //呼ぶタイミングを確定させる！！
        try {
            Log.d("プロジェクト名","プロジェクト名=>${projectNames}")
            if(token != null || projectId != null || projectNames != null) {
                progressBarFlg = true
                chkProgress(progressBarFlg, rootView)
                val payload2 = mapOf("token" to token, "project_id" to projectId)
                BlasRestField(payload2, ::fieldRecv, ::fieldRecvError).execute()
            }else{
                throw java.lang.Exception("Failed to receive internal data ")
            }
        }catch (e:Exception){
            progressBarFlg = false
            chkProgress(progressBarFlg, rootView)
            val errorMessage = msg.createErrorMessage("getFail")
            Toast.makeText(activity, errorMessage, toastErrorLen).show()
        }
    }


    /**
     * データ取得時
     */
    private fun itemRecv(result: JSONObject) {
        //初期化
        itemListAll.clear()
        jsonParseList = null

        jsonItemList = result
        if(jsonItemList != null) {
            jsonParseList = helper.createJsonArray(jsonItemList)
            jsonParse(parseStartNum, parseFinNum)
            setAdapter()
        }
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
        Log.d("itemViewFragment", "カラム取得失敗")

        var message:String? = null

        message = BlasMsg().getMessage(errorCode,aplCode)

        handler.post {
            Toast.makeText(getActivity(), message, toastErrorLen).show()
        }
        progressBarFlg = false
        chkProgress(progressBarFlg,rootView)
    }

    /**
     * データ取得失敗時
     */
    private fun itemRecvError(errorCode: Int , aplCode:Int) {
        Log.d("itemViewFragment", "データ取得失敗")

        var message:String? = null

        message = BlasMsg().getMessage(errorCode,aplCode)

        handler.post {
            Toast.makeText(getActivity(), message, toastErrorLen).show()
        }

        //エラーのため、データを初期化する
        fieldMap.clear()
        progressBarFlg = false
        chkProgress(progressBarFlg,rootView)
    }

    /**
     *  データ登録
     */
    private fun setAdapter() {
        Log.d("konishi", "setAdapter")
        createCardView()
        adapter.notifyDataSetChanged()
        progressBarFlg = false
        chkProgress(progressBarFlg,rootView)
    }


    private fun createCardView() {

        val colMax = fieldMap.size
        val list = mutableListOf<MutableMap<String, String?>>()
        //ここでItemListAllの追加処理を入れる

        if(currentIndex == 0) {
            list.addAll(itemListAll.filterIndexed { index, mutableMap ->
                (index >= currentIndex) && (index < currentIndex + CREATE_UNIT)
            }.toMutableList())
        }else{
            list.addAll(itemListAll.filterIndexed { index, mutableMap ->
                (index >= currentIndex) && (index < currentIndex + CREATE_UNIT)
            }.toMutableList())
        }



        createCardManager(list,colMax)
        if (list.isNotEmpty()) {
            currentIndex += CREATE_UNIT
        }
    }



    /**
     * カードを作るときに使う関数。
     * endflgから表示するか否かを判断。表示する場合は処理を投げる。
     *
     */
    private fun  createCardManager(list:MutableList<MutableMap<String,String?>>,colMax : Int){
        Log.d("デバック処理","エンドshowの値=>${endShow}")

        if (endShow) {
            Log.d("cardManager","ゴミ箱非表示")
            list.forEach {
                val valueFlg = it["endFlg"].toString()
                val item_id = it["item_id"].toString()
                var text: String? = ""
                text = createCardText(text, it, colMax)
                createCard(item_id, text,valueFlg)
            }
        } else {
            Log.d("cardManager","ゴミ箱非表示")
            list.forEach {
                val valueFlg = it["endFlg"].toString()
                if (valueFlg == FieldType.NORMAL) {
                    val item_id = it["item_id"].toString()
                    var text: String? = ""
                    text = createCardText(text, it, colMax)
                    createCard(item_id, text, valueFlg)
                }
            }
        }

    }


    /**
     * カードビューのテキストを作成する関数
     */
    private fun createCardText(text:String?,it:MutableMap<String,String?>,colMax: Int): String? {
        var loopcnt = 1
        var text = text
        for (col in 1..colMax) {
            val fldName = "fld${col}"
            //レコードの定義取得
            if (loopcnt == 1) {
                text = "[${fieldMap[col]!!["field_name"]}]\n"
                text += "${it[fldName]}\n"
            } else {
                text += "[${fieldMap[col]!!["field_name"]}]\n"

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

    /**
     * カードビューを作成する関数
     */
    fun createCard(item_id:String,text: String?,valueFlg : String){
        val rowModel = RowModel().also {
            if(valueFlg == FieldType.END) {
                it.title = "${item_id}${FieldType.ENDTEXT}"
            }else{
                it.title = item_id
            }
            it.itemId = item_id
            Log.d("testtst","${item_id}")

            it.detail = text.toString()
            it.projectId = projectId
            it.token = token
            it.itemList = itemListAll
            it.projectNames = projectNames
        }

        dataList.add(rowModel)
        Log.d("チェック!!","dataListの値 => ${dataList}")
    }


    /**
     * Jsonをパースする関数。
     */
    fun jsonParse(parseStartNum:Int,parseFinNum:Int){
        val colMax = fieldMap.size
        Log.d("jsonParse","fildMap.size =>${fieldMap.size}")
        try {
            if(jsonParseList != null) {
                val totalRecordNum = jsonParseList!!.length()
                Log.d("jsonParse","jsonParseList.size =>${jsonParseList!!.length()}")
                if(totalRecordNum < parseFinNum) {
                    val itemInfo =
                        helper.createSeparateItemList(
                            jsonParseList,
                            colMax,
                            parseStartNum,
                            totalRecordNum
                        )
                    itemListAll.addAll(itemInfo)
                }else if (totalRecordNum >= parseFinNum){
                    val itemInfo =
                        helper.createSeparateItemList(
                            jsonParseList,
                            colMax,
                            parseStartNum,
                            parseFinNum
                        )
                    itemListAll.addAll(itemInfo)
                }
            }
        }catch (e:java.lang.Exception){

        }
    }

    /**
     * パースした件数と次に行う件数を記録する
     */
    fun nextParseNum(){
        parseStartNum = parseFinNum
        parseFinNum += parseNum
    }



    override fun onDestroyView() {
        recyclerView.adapter = null
        super.onDestroyView()
    }
}

