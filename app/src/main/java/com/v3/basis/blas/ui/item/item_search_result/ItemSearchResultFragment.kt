package com.v3.basis.blas.ui.item.item_search_result


import android.app.AlertDialog
import android.os.Bundle
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
import com.v3.basis.blas.R
import com.v3.basis.blas.blasclass.app.BlasMsg
import com.v3.basis.blas.blasclass.config.FieldType
import com.v3.basis.blas.blasclass.db.data.ItemsController
import com.v3.basis.blas.blasclass.db.field.FieldController
import com.v3.basis.blas.blasclass.helper.RestHelper
import com.v3.basis.blas.ui.ext.addTitle
import com.v3.basis.blas.ui.ext.getStringExtra
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import kotlinx.android.synthetic.main.fragment_item_search_result.*
import org.json.JSONObject

/**
 * A simple [Fragment] subclass.
 */
class ItemSearchResultFragment : Fragment() {

    //var token:String? = null
    //var projectId:String? = null
   // var freeWord :String? = ""
    //var fldSize : String? = null
    //var dateTimeCol:String = ""
    //var checkValueCol:String = ""
    private var endShow = false
    private var progressBarFlg = false
    private lateinit var rootView:View
    lateinit var token:String
    lateinit var projectId :String
    lateinit var freeWord:String
    lateinit var fldSize : String
    private var dateTimeCol : String = ""
    private var checkValueCol:String = ""
    private var msg = BlasMsg()
    private val toastErrorLen = Toast.LENGTH_LONG
    private var toastSuccessLen = Toast.LENGTH_SHORT

    private var chkReceiveData = true

    private val findValueMap:MutableMap<String,String?> = mutableMapOf()
    private val fieldMap: MutableMap<Int, MutableMap<String, String?>> = mutableMapOf()
    private val itemList: MutableList<MutableMap<String, String?>> = mutableListOf()
    private val jsonItemList:MutableMap<String, JSONObject> = mutableMapOf()
    private val dataList = mutableListOf<RowModel>()
    private var baseList :MutableList<MutableMap<String,String?>> = mutableListOf()
    private val helper:RestHelper = RestHelper()
    private val disposables = CompositeDisposable()

    private val adapter: ViewAdapter = ViewAdapter(dataList, object : ViewAdapter.ListListener {
        override fun onClickRow(tappedView: View, rowModel: RowModel) {
            //カードタップ時の処理
        }
    })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addTitle("projectName")
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        super.onCreateView(inflater, container, savedInstanceState)
        //初期値の取得
        val root = inflater.inflate(R.layout.fragment_item_search_result, container, false)
        rootView = root

        val extras = activity?.intent?.extras
        if(extras?.getString("token") != null){
            token = extras.getString("token").toString()
        }
        if(extras?.getString("project_id") != null){
            projectId = extras.getString("project_id").toString()
        }
        if(extras?.getString("fldSize")!= null){
            fldSize = extras.getString("fldSize").toString()
        }
        if(extras?.getString("freeWord")!=null){
            freeWord = extras.getString("freeWord").toString()
            findValueMap.set("freeWord",freeWord)
        }
        if(extras?.getString("dateTimeCol")!= null){
            dateTimeCol = extras.getString("dateTimeCol").toString()
        }
        if(extras?.getString("checkValueCol")!= null){
            checkValueCol = extras.getString("checkValueCol").toString()
        }
        try {
            Log.d("ログテスト" ,"fldSize = ${fldSize}")
            if(fldSize == "null"){
                throw Exception("Failed to get internal data")
            }
            else if(fldSize != null) {
                for (idx in 1..fldSize.toInt()) {
                    findValueMap.set("fld${idx}", getStringExtra("fld${idx}"))
                }
            }
        }catch (e:Exception){
            val errorMessage = msg.createErrorMessage("getFail")
            Toast.makeText(activity, errorMessage, toastErrorLen).show()
            chkReceiveData = false
        }

        val endSwitch = rootView.findViewById<Switch>(R.id.switch_end_result)
        val viewBtn = rootView.findViewById<Button>(R.id.button_view_result)


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
            Log.d("デバック管理","Hello World")
            if(progressBarFlg){
                Log.d("デバック管理","処理させない。ぐるぐるしているから")
            }else{
                Log.d("デバック管理","いいっすよ！！！")
                createCardManager(baseList,fieldMap.size,"Update")
            }

        }
        return root
    }


    // 検索ボタンを押すと呼ばれる
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.d("ItemSearchResultFragment.onViewCreated()","start")

        super.onViewCreated(view, savedInstanceState)
        Log.d("lifeCycle", "onViewCreated")
        if(chkReceiveData) {
            try {
                Log.d("デバック管理","分岐開始")
                Log.d("デバック管理","findValueMap => ${findValueMap.isEmpty()}")
                if (token != null && fldSize != null && findValueMap.isNotEmpty() ) {
                    //リサイクラ-viewを取得
                    //基本的にデータはまだ到着していないため、空のアクティビティとadapterだけ設定しておく
                    Log.d("デバック管理","こっちの処理乗った")

                    progressBarFlg = true
                    chkProgress(progressBarFlg, rootView)
                    val recyclerView = recyclerView
                    recyclerView.setHasFixedSize(true)
                    recyclerView.layoutManager = LinearLayoutManager(activity)
                    recyclerView.adapter = adapter


                    //呼ぶタイミングを確定させる！！
                    Single.fromCallable { FieldController(requireContext(),projectId).searchDisp() }
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeBy {fieldList ->

                            //カラム順に並べ替える
                            fieldMap.clear()
                            val fieldMapList = helper.createFieldList2(fieldList)
                            var cnt = 1
                            fieldMapList.forEach {
                                fieldMap[cnt] = it
                                cnt += 1
                            }

                            getItem()

                        }
                        .addTo(disposables)




//                    val payload2 = mapOf("token" to token, "project_id" to projectId)
//                    BlasRestField(payload2, ::fieldRecv, ::fieldRecvError).execute()

//                    BlasRestItem("search", payload2, ::itemRecv, ::itemRecvError).execute()
                }else{
                    throw Exception("Failed to receive internal data ")
                }
            } catch (e: Exception) {
                Log.d("デバック管理","エラー処理${e}")
                progressBarFlg = false
                chkProgress(progressBarFlg, rootView)
                val errorMessage = msg.createErrorMessage("getFail")
                Toast.makeText(activity, errorMessage, toastErrorLen).show()
            }
        }

    }


    private fun getItem() {

        val itemsController = ItemsController(requireContext(),projectId)

        // TODO:三代川　findValueMapから検索条件を作る
        Single.fromCallable { itemsController.search( findValueMap = findValueMap ) }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy {
                if ( it.isNotEmpty() ) {
                    itemList.clear()
                    jsonItemList.clear()
                    setAdapter(it)
                } else {
                    // TODO:とりあえず
                    val title = getString(R.string.dialog_title)
                    val text = getString(R.string.search_error)
                    AlertDialog.Builder(activity)
                        .setTitle(title)
                        .setMessage(text)
                        .setPositiveButton("OK"){ dialog, which ->
                            requireActivity().finish()
                        }
                        .show()
                }
            }
            .addTo(disposables)
    }

    private fun createDataList( itemInfo : MutableList<MutableMap<String, String?>> ) {
        var colMax = fieldMap.size
//        var itemInfo = helper.createItemList(jsonItemList, colMax )
//        itemInfo = searchAndroid(findValueMap,itemInfo,dateTimeCol,checkValueCol)
        itemList.addAll(itemInfo)

        baseList = itemList
        if(baseList.size > 0) {
            createCardManager(itemList, colMax, "New")
        }else{
            val title = getString(R.string.dialog_title)
            val text = getString(R.string.search_error)
            AlertDialog.Builder(activity)
                .setTitle(title)
                .setMessage(text)
                .setPositiveButton("OK"){ dialog, which ->
                    requireActivity().finish()
                }
                .show()
        }
    }

    /**
     *  データ登録
     */
    private fun setAdapter( itemInfo : MutableList<MutableMap<String, String?>> ) {
        Log.d("konishi", "setAdapter")
        createDataList( itemInfo )
        adapter.notifyItemInserted(0)
        progressBarFlg = false
        chkProgress(progressBarFlg,rootView)
    }

    /**
     * データ取得時
     */
/*
    private fun itemRecv(result: JSONObject) {
        itemList.clear()
        jsonItemList.clear()
        //val itemMap = RestHelper().createItemList(result)
        //itemMap?.also { itemList.addAll(0, itemMap) }
        jsonItemList.set("1",result)
        if (jsonItemList.isNotEmpty() && fieldMap.isNotEmpty()) {
            setAdapter()
        }
    }
*/

    /**
     * フィールド取得時
     */
/*
    private fun fieldRecv(result: JSONObject) {
        Log.d("ItemSearchResultFragment.filedRecv()","start")

        //カラム順に並べ替える
        fieldMap.clear()
        val fieldList = helper.createFieldList(result)
        var cnt = 1
        fieldList.forEach{
            fieldMap[cnt] = it
            cnt +=1
        }

        // TODO:現在jsonItemList取得されない
        if(jsonItemList.isNotEmpty() && fieldMap.isNotEmpty()) {
            setAdapter()
        }
    }
*/

    /**
     * フィールド取得失敗時
     */
    private fun fieldRecvError(errorCode: Int, aplCode:Int) {

        var message:String? = null

        message = BlasMsg().getMessage(errorCode,aplCode)

        Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show()
        itemList.clear()
        fieldMap.clear()
        progressBarFlg = false
        chkProgress(progressBarFlg,rootView)

    }

    /**
     * データ取得失敗時
     */
    private fun itemRecvError(errorCode: Int, aplCode:Int) {

        var message:String? = null
        
        message = BlasMsg().getMessage(errorCode,aplCode)

        Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show()
        //エラーのため、データを初期化する
        itemList.clear()
        fieldMap.clear()
        progressBarFlg = false
        chkProgress(progressBarFlg,rootView)
    }

    private fun  createCardManager(list:MutableList<MutableMap<String,String?>>,colMax : Int,mode:String){
        Log.d("デバック処理","エンドshowの値=>${endShow}")
        if(mode == "New"){
            list.forEach {
                val valueFlg = it["end_flg"].toString()
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
            if (endShow) {
                list.forEach {
                    val valueFlg = it["endFlg"].toString()
                    val item_id = it["item_id"].toString()
                    var text: String? = ""
                    text = createCardText(text, it, colMax)
                    createCard(item_id, text,valueFlg)
                }

            } else {
                list.forEach {
                    val valueFlg = it["endFlg"].toString()
                    if (valueFlg == FieldType.NORMAL) {
                        val item_id = it["item_id"].toString()
                        var text: String? = ""
                        text = createCardText(text, it, colMax)
                        createCard(item_id, text,valueFlg)
                    }
                }

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
                text = "[${fieldMap[col]!!["field_name"]}]"
                text += "\n${it[fldName]}\n"
            } else {
                Log.d("フィールドの値：","値=>${it[fldName]}")
                text += "[${fieldMap[col]!!["field_name"]}]\n"
                if(fieldMap[col]!!["type"] == FieldType.CHECK_VALUE){
                    val newValue = helper.createCheckValue(it[fldName].toString())
                    text += "${newValue}\n"
                }else {
                    text += "${it[fldName]}\n"
                }
            }
            loopcnt += 1
        }
        return text
    }

    fun createCard(item_id:String,text: String?,valueFlg:String){
        val rowModel = RowModel().also {
            if (item_id != null) {
                if(valueFlg == FieldType.END) {
                    it.title = "${item_id}${FieldType.ENDTEXT}"
                }else{
                    it.title = item_id
                }
            }
            if (text != null) {
                it.detail = text
            }
            it.projectId = projectId
            it.token = token
            it.itemList = itemList
        }
        dataList.add(rowModel)
    }

    private fun chkProgress(flg:Boolean,view:View){
        val progressbar = view.findViewById<ProgressBar>(R.id.progressBarItemView)
        if (flg) {
            progressbar.visibility = android.widget.ProgressBar.VISIBLE

        } else {
            progressbar.visibility = android.widget.ProgressBar.INVISIBLE
        }
    }

    override fun onDestroyView() {
        recyclerView.adapter = null
        super.onDestroyView()
    }

}
