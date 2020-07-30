package com.v3.basis.blas.ui.fixture.fixture_view


import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.v3.basis.blas.R
import com.v3.basis.blas.activity.FixtureActivity
import com.v3.basis.blas.blasclass.app.BlasMsg
import com.v3.basis.blas.blasclass.config.FixtureType.Companion.canTakeOut
import com.v3.basis.blas.blasclass.config.FixtureType.Companion.finishInstall
import com.v3.basis.blas.blasclass.config.FixtureType.Companion.notTakeOut
import com.v3.basis.blas.blasclass.config.FixtureType.Companion.rtn
import com.v3.basis.blas.blasclass.config.FixtureType.Companion.statusCanTakeOut
import com.v3.basis.blas.blasclass.config.FixtureType.Companion.statusFinishInstall
import com.v3.basis.blas.blasclass.config.FixtureType.Companion.statusNotTakeOut
import com.v3.basis.blas.blasclass.config.FixtureType.Companion.statusTakeOut
import com.v3.basis.blas.blasclass.config.FixtureType.Companion.takeOut
import com.v3.basis.blas.blasclass.db.fixture.FixtureController
import com.v3.basis.blas.blasclass.helper.RestHelper
import com.v3.basis.blas.blasclass.ldb.LdbFixtureDispRecord
import com.v3.basis.blas.blasclass.sync.Lump
import com.v3.basis.blas.databinding.FragmentFixtureViewBinding
import com.v3.basis.blas.ui.ext.addTitle
import com.v3.basis.blas.ui.ext.getStringExtra
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.databinding.GroupieViewHolder
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_fixture_view.*
import kotlinx.android.synthetic.main.fragment_item_view.recyclerView
import org.json.JSONArray
import org.json.JSONObject
import java.lang.Exception


/**
 * A simple [Fragment] subclass.
 */
class FixtureViewFragment : Fragment() {

    lateinit var token:String
    lateinit var project_id:String
    private var dataListAll = mutableListOf<FixtureListCell>()
    private var dataList = mutableListOf<FixtureListCell>()
    private var valueMap : MutableMap<Int, MutableMap<String, String?>> = mutableMapOf()
    private var searchValueMap:MutableMap<String,String?> = mutableMapOf()
    private var msg = BlasMsg()
    private val toastErrorLen = Toast.LENGTH_LONG
    private var helper = RestHelper()

    private var jsonParseList : JSONArray? = null
    private lateinit var fixtureController: FixtureController
    private val disposables = CompositeDisposable()

    private var paresUnitNum = 100
    private var parseStartNum = 0
    private var parseFinNum = paresUnitNum

    private var currentIndex: Int = 0
    private var offset: Int = 0

    private lateinit var bind: FragmentFixtureViewBinding

    companion object {
        const val CREATE_UNIT = 20
    }

    private lateinit var viewModel: FixtureListViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addTitle("project_name")
    }

    private val groupAdapter = GroupAdapter<GroupieViewHolder<*>>()

//    private val adapter: ViewAdapter = ViewAdapter(dataList, object : ViewAdapter.ListListener {
//        override fun onClickRow(tappedView: View, rowModel: com.v3.basis.blas.ui.fixture.fixture_view.RowModel) {
//            //カードタップ時の処理
//        }
//
//    })

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        super.onCreateView(inflater, container, savedInstanceState)
        Log.d("【onCreateView】","呼ばれた")
        val extras = activity?.intent?.extras
        if (extras?.getString("token") != null) {
            token = extras.getString("token").toString()
        }
        if (extras?.getString("project_id") != null) {
            project_id = extras.getString("project_id").toString()
        }

        checkSearchMap()

        fixtureController = FixtureController(requireContext(), project_id)
        fixtureController.errorMessageEvent
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy {
                //エラーのため、データを初期化する
                valueMap.clear()

                Log.d("取得失敗","$it")
                progressBar.visibility = View.INVISIBLE
            }
            .addTo(disposables)

        viewModel = ViewModelProviders.of(this).get(FixtureListViewModel::class.java)
        viewModel.errorEvent
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy { Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show() }
            .addTo(disposables)

        bind = DataBindingUtil.inflate(inflater, R.layout.fragment_fixture_view, container, false)
        bind.vm = viewModel

        return bind.root
    }

    private fun checkSearchMap() {

        searchValueMap.set("freeWord",getStringExtra("freeWord"))
        searchValueMap.set("serial_number",getStringExtra("serialNumber"))
        searchValueMap.set("fixture_id",getStringExtra("dataId"))
        searchValueMap.set("FixOrg",getStringExtra("kenpinOrg"))
        searchValueMap.set("FixUser",getStringExtra("kenpinUser"))
        searchValueMap.set("kenpinDayMin",getStringExtra("kenpinDayMin"))
        searchValueMap.set("kenpinDayMax",getStringExtra("kenpinDayMax"))
        searchValueMap.set("TakeOutOrg",getStringExtra("takeOutOrg"))
        searchValueMap.set("TakeOutUser",getStringExtra("takeOutUser"))
        searchValueMap.set("takeOutDayMin",getStringExtra("takeOutDayMin"))
        searchValueMap.set("takeOutDayMax",getStringExtra("takeOutDayMax"))
        searchValueMap.set("RtnOrg",getStringExtra("returnOrg"))
        searchValueMap.set("RtnUser",getStringExtra("returnUser"))
        searchValueMap.set("returnDayMin",getStringExtra("returnDayMin"))
        searchValueMap.set("returnDayMax",getStringExtra("returnDayMax"))
        searchValueMap.set("ItemOrg",getStringExtra("itemOrg"))
        searchValueMap.set("ItemUser",getStringExtra("itemUser"))
        searchValueMap.set("itemDayMin",getStringExtra("itemDayMin"))
        searchValueMap.set("itemDayMax",getStringExtra("itemDayMax"))
        searchValueMap.set("status",getStringExtra("status"))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //全て同期のボタン
        allSyncButton.setOnClickListener {
            //  連打禁止！！
            allSyncButton.isEnabled = false
            Log.d("フローティングボタン Fixture","Click!!!!")
            Lump(requireContext(),project_id,token){
                (requireActivity() as FixtureActivity).reloard()
            }.exec()
        }

        try {
            if(token != null && project_id != null) {
                Log.d("lifeCycle", "onViewCreated")
                //リサイクラ-viewを取得
                //基本的にデータはまだ到着していないため、空のアクティビティとadapterだけ設定しておく
                val recyclerView = recyclerView
                recyclerView.setHasFixedSize(true)
                recyclerView.layoutManager = LinearLayoutManager(activity)
                recyclerView.adapter = groupAdapter
                recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {

                    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                        super.onScrollStateChanged(recyclerView, newState)
//                        if (valueMap.isNotEmpty()) {
//                            val notOverSize = currentIndex < dataListAll.size
                            //100件のみ表示処理。
                            //val notOverSize = currentIndex <= dataListAll.size
                            if (!recyclerView.canScrollVertically(1) && progressBar.visibility == View.INVISIBLE) {
                                progressBar.visibility = View.VISIBLE
                                offset += CREATE_UNIT

                                /*100件のみ表示の処理。
                                if(currentIndex % paresUnitNum == 0){
                                    parseJson()
                                }*/
                                searchAsync()
//                                setAdapter()
                            }
//                        }
                    }
                })

                //呼ぶタイミングを確定させる！！
                searchAsync()
//                val payload2 = mapOf("token" to token, "project_id" to project_id)
//                Log.d("testtest", "取得する")
//                val list = FixtureController(requireContext(), project_id).search()
//                Log.d("FixtureViewTest", list.toString())
//                BlasRestFixture(
//                    "search",
//                    payload2,
//                    ::fixtureGetSuccess,
//                    ::fixtureGetError
//                ).execute()
            }else{
                throw java.lang.Exception("Failed to receive internal data ")
            }
        }catch (e:Exception){
            val errorMessage = msg.createErrorMessage("getFail")
            Toast.makeText(activity, errorMessage, toastErrorLen).show()
            try {
                progressBar.visibility = View.INVISIBLE
            }catch (e:Exception){

            }

        }
    }

    private fun searchAsync() {

        Single.fromCallable { fixtureController.searchDisp(offset = offset, searchMap = searchValueMap) }
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .map {
                val list = mutableListOf<FixtureListCell>()
                it.forEach {
                    val value = createValue(it) ?: ""

                    var title = it.fixture_id.toString()
                    if( it.fixture_id < 0L ) {
                        title = "(仮登録中)"
                    }

                    // itを全部渡してもいいような気もするが辞めておく・・・
                    val model = FixtureCellModel(
                        token
                        ,project_id.toInt()
                        ,it.fixture_id
                        , title.toString()
                        , value
                        , it.sync_status
                        , requireContext()
                    )
                    model.errorMessage.set(it.error_msg)

                    list.add(FixtureListCell(viewModel, model))
                }
                list
            }
            .subscribeBy {
                dataListAll.addAll(it)
                setAdapter()
            }
            .addTo(disposables)
    }

    private fun createDataList() {
        //データ管理のループ
        Log.d("ここで死んでいる","")
//        if(currentIndex < parseStartNum) {
//            dataList.addAll(dataListAll.filterIndexed { index, mutableMap ->
//                (index >= currentIndex) && (index < currentIndex + CREATE_UNIT)
//            }.toMutableList())
//            dataList.forEach{
//                Log.d("あたいチェック","datalist id =${it.title}")
//            }
//        }
        val filteredList = dataListAll.filterIndexed { index, mutableMap ->
            (index >= currentIndex) && (index < currentIndex + CREATE_UNIT)
        }
        dataList.addAll(filteredList.toMutableList())
        dataList.forEach{
            Log.d("あたいチェック","datalist id =${it.model.fixture_id}")
        }
        // update
        if (dataList.isNotEmpty()) {
            currentIndex += CREATE_UNIT
        }

        setNotSendCount(filteredList)
    }

    private fun setNotSendCount(list: List<FixtureListCell>) {

        val count = list.filter { it.model.syncVisible.get() }.size
        viewModel.sendCount.set((viewModel.sendCount.get() as Int) + count)
    }

    /**
     *  データ登録
     */
    private fun setAdapter() {
        createDataList()
        groupAdapter.update(dataList)
        try {
            progressBar.visibility = View.INVISIBLE
        }catch (e:Exception){

        }
    }

    /**
     * 機器管理取得時
     */
    private fun fixtureGetSuccess(result: JSONObject) {
        //カラム順に並べ替える
       jsonParseList = helper.createJsonArray(result)
       // Log.d("配列の中身","jsonParseList => ${jsonParseList}")
       /* for(i in 0 .. jsonParseList!!.length()-1){
            val fields = JSONObject(jsonParseList!![i].toString())
            val fixture = fields.getJSONObject("Fixture")
            val fixtureId = fixture.getInt("fixture_id")
            Log.d("配列の中身","ID => ${fixtureId}")
        }*/
        //Log.d("配列の中身","jsonParseList => ${jsonParseList!!.length()}")
        if(jsonParseList != null) {
            parseJson()
            if (valueMap.isNotEmpty()) {
                valueMap.forEach {

                    Log.d("配列の中身","key = ${it.key}")

                    //カラムの定義取得
//                    val fixture_id = it.key
//                    val fixture_value = it.value
//                    val value = createValue(fixture_value)
//
//                    val rowModel = RowModel().also {
//                        if (fixture_id != null) {
//                            it.title = fixture_id.toString()
//                        }
//                        if (value != null) {
//                            it.detail = value
//                        }
//                    }
//                    dataListAll.add(rowModel)
                }
                setAdapter()
            }
        }
    }

    /**
     * フィールド取得失敗時
     */
    private fun fixtureGetError(errorCode: Int, aplCode:Int) {

        var message:String? = null

        message = BlasMsg().getMessage(errorCode,aplCode)

        Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show()

        //エラーのため、データを初期化する
        valueMap.clear()

        Log.d("取得失敗","${errorCode}")
        progressBar.visibility = View.INVISIBLE
    }

    /**
     * 表示する値を作成する
     */
    private fun createValue(rcd: LdbFixtureDispRecord): String? {
        var value:String? =null
        try {
            value = "[${getString(R.string.col_serialnumber)}]"
            value += "\n${rcd.serial_number}"
            value += "\n[${getString(R.string.col_status)}]\n"
            value += when (rcd.status) {//config.FixtureTypeにて定義している。
                canTakeOut.toInt() -> {
                    "${statusCanTakeOut}"
                }
                rtn.toInt() -> {
                    "${statusCanTakeOut}"
                }
                takeOut.toInt() -> {
                    "${statusTakeOut}"
                }
                finishInstall.toInt() -> {
                    "${statusFinishInstall}"
                }
                notTakeOut.toInt() -> {
                    "${statusNotTakeOut}"
                }
                else -> {
                }
            }
            value += "\n\n\n[${getString(R.string.col_kenpin_org)}]\n"
            value += setValue(rcd.fix_org_name)
            value += "\n[${getString(R.string.col_kenpin_user)}]\n"
            value += setValue(rcd.fix_user_name)
            value += "\n[${getString(R.string.col_kenpin_date)}]\n"
            value += setValue(rcd.fix_date)
            value += "\n\n\n[${getString(R.string.col_takeout_org)}]\n"
            value += setValue(rcd.takeout_org_name)
            value += "\n[${getString(R.string.col_takeout_user)}]\n"
            value += setValue(rcd.takeout_user_name)
            value += "\n[${getString(R.string.col_takeout_date)}]\n"
            value += setValue(rcd.takeout_date)
            value += "\n\n\n[${getString(R.string.col_return_org)}]\n"
            value += setValue(rcd.rtn_org_name)
            value += "\n[${getString(R.string.col_return_user)}]\n"
            value += setValue(rcd.rtn_user_name)
            value += "\n[${getString(R.string.col_return_date)}]\n"
            value += setValue(rcd.rtn_date)
            value += "\n\n\n[${getString(R.string.col_item_org)}]\n"
            value += setValue(rcd.item_org_name)
            value += "\n[${getString(R.string.col_item_user)}]\n"
            value += setValue(rcd.item_user_name)
            value += "\n[${getString(R.string.col_item_date)}]\n"
            value += setValue(rcd.item_date)
            value += "\n[${getString(R.string.col_item_id)}]\n"

            if( rcd.item_id < 0L ) {
                value += setValue("仮登録中")
            }
            else if( 0L == rcd.item_id ) {
                value += setValue("")
            }
            else {
                value += setValue(rcd.item_id.toString())
            }

        }catch (e:Exception){

        }

        return value

    }


    private fun setValue(value:String): String {
        if(value == "null"){
            return "  "
        }else{
            return value
        }
    }

    override fun onDestroyView() {
        recyclerView.adapter = null
        super.onDestroyView()
    }


    //パースする処理
    private fun parseJson(){
        if(jsonParseList != null ) {
            val chk = jsonParseList?.length()
            if(chk != null) {
                if (chk <= parseFinNum) {
                     valueMap =  helper.createJsonFix(jsonParseList!!,valueMap,parseStartNum,chk)
                }
                else{
                    valueMap = helper.createJsonFix(jsonParseList!!,valueMap,parseStartNum,parseFinNum)
                }
                parseFinNum += paresUnitNum
                parseStartNum += paresUnitNum
            }
        }
    }

}
