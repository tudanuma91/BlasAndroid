package com.v3.basis.blas.ui.fixture.fixture_view


import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.v3.basis.blas.R
import com.v3.basis.blas.blasclass.app.BlasMsg
import com.v3.basis.blas.blasclass.config.FixtureType.Companion.canTakeOut
import com.v3.basis.blas.blasclass.config.FixtureType.Companion.finishInstall
import com.v3.basis.blas.blasclass.config.FixtureType.Companion.notTakeOut
import com.v3.basis.blas.blasclass.config.FixtureType.Companion.statusCanTakeOut
import com.v3.basis.blas.blasclass.config.FixtureType.Companion.statusFinishInstall
import com.v3.basis.blas.blasclass.config.FixtureType.Companion.statusNotTakeOut
import com.v3.basis.blas.blasclass.config.FixtureType.Companion.statusTakeOut
import com.v3.basis.blas.blasclass.config.FixtureType.Companion.takeOut
import com.v3.basis.blas.blasclass.db.fixture.FixtureController
import com.v3.basis.blas.blasclass.helper.RestHelper
import com.v3.basis.blas.blasclass.rest.BlasRestErrCode
import com.v3.basis.blas.blasclass.rest.BlasRestFixture
import com.v3.basis.blas.ui.ext.addTitle
import com.v3.basis.blas.ui.ext.getStringExtra
import kotlinx.android.synthetic.main.fragment_fixture_view.*
import kotlinx.android.synthetic.main.fragment_item_view.*
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
    private var dataListAll = mutableListOf<RowModel>()
    private var dataList = mutableListOf<RowModel>()
    private var valueMap : MutableMap<Int, MutableMap<String, String?>> = mutableMapOf()
    private var msg = BlasMsg()
    private val toastErrorLen = Toast.LENGTH_LONG
    private var helper = RestHelper()

    private var jsonParseList : JSONArray? = null

    private var paresUnitNum = 100
    private var parseStartNum = 0
    private var parseFinNum = paresUnitNum

    private var currentIndex: Int = 0
    companion object {
        const val CREATE_UNIT = 20
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addTitle("project_name")
    }

    private val adapter: ViewAdapter = ViewAdapter(dataList, object : ViewAdapter.ListListener {
        override fun onClickRow(tappedView: View, rowModel: com.v3.basis.blas.ui.fixture.fixture_view.RowModel) {
            //カードタップ時の処理
        }

    })

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

        return inflater.inflate(R.layout.fragment_fixture_view, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        try {
            if(token != null && project_id != null) {
                Log.d("lifeCycle", "onViewCreated")
                //リサイクラ-viewを取得
                //基本的にデータはまだ到着していないため、空のアクティビティとadapterだけ設定しておく
                val recyclerView = recyclerView
                recyclerView.setHasFixedSize(true)
                recyclerView.layoutManager = LinearLayoutManager(activity)
                recyclerView.adapter = adapter
                recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {

                    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                        super.onScrollStateChanged(recyclerView, newState)
                        if (valueMap.isNotEmpty()) {
                            val notOverSize = currentIndex < dataListAll.size
                            //100件のみ表示処理。
                            //val notOverSize = currentIndex <= dataListAll.size
                            if (!recyclerView.canScrollVertically(1) && notOverSize) {
                                progressBar.visibility = View.VISIBLE
                                /*100件のみ表示の処理。
                                if(currentIndex % paresUnitNum == 0){
                                    parseJson()
                                }*/
                                setAdapter()
                            }
                        }
                    }
                })

                //呼ぶタイミングを確定させる！！
                val payload2 = mapOf("token" to token, "project_id" to project_id)
                Log.d("testtest", "取得する")
                val list = FixtureController(requireContext(), project_id).search()
                Log.d("FixtureViewTest", list.toString())
                BlasRestFixture(
                    "search",
                    payload2,
                    ::fixtureGetSuccess,
                    ::fixtureGetError
                ).execute()
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

    private fun createDataList() {
        //データ管理のループ
        Log.d("ここで死んでいる","")
        if(currentIndex < parseStartNum) {
            dataList.addAll(dataListAll.filterIndexed { index, mutableMap ->
                (index >= currentIndex) && (index < currentIndex + CREATE_UNIT)
            }.toMutableList())
            dataList.forEach{
                Log.d("あたいチェック","datalist id =${it.title}")
            }
        }
        // update
        if (dataList.isNotEmpty()) {
            currentIndex += CREATE_UNIT
        }
    }

    /**
     *  データ登録
     */
    private fun setAdapter() {
        createDataList()
        adapter.notifyDataSetChanged()
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
                    val fixture_id = it.key
                    val fixture_value = it.value
                    val value = createValue(fixture_value)

                    val rowModel = RowModel().also {
                        if (fixture_id != null) {
                            it.title = fixture_id.toString()
                        }
                        if (value != null) {
                            it.detail = value
                        }
                    }
                    dataListAll.add(rowModel)
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
    private fun createValue(list: MutableMap<String,String?>): String? {
        var value:String? =null
        try {
            value = "[${getString(R.string.col_serialnumber)}]"
            value += "\n${list["serial_number"]}"
            value += "\n[${getString(R.string.col_status)}]\n"
            value += when (list["status"]) {//config.FixtureTypeにて定義している。
                canTakeOut -> {
                    "${statusCanTakeOut}"
                }
                takeOut -> {
                    "${statusTakeOut}"
                }
                finishInstall -> {
                    "${statusFinishInstall}"
                }
                notTakeOut -> {
                    "${statusNotTakeOut}"
                }
                else -> {
                }
            }
            value += "\n\n\n[${getString(R.string.col_kenpin_org)}]\n"
            value += setValue(list["fix_org"].toString())
            value += "\n[${getString(R.string.col_kenpin_user)}]\n"
            value += setValue(list["fix_user"].toString())
            value += "\n[${getString(R.string.col_kenpin_date)}]\n"
            value += setValue(list["fix_date"].toString())
            value += "\n\n\n[${getString(R.string.col_takeout_org)}]\n"
            value += setValue(list["takeout_org"].toString())
            value += "\n[${getString(R.string.col_takeout_user)}]\n"
            value += setValue(list["takeout_user"].toString())
            value += "\n[${getString(R.string.col_takeout_date)}]\n"
            value += setValue(list["takeout_date"].toString())
            value += "\n\n\n[${getString(R.string.col_return_org)}]\n"
            value += setValue(list["rtn_org"].toString())
            value += "\n[${getString(R.string.col_return_user)}]\n"
            value += setValue(list["rtn_user"].toString())
            value += "\n[${getString(R.string.col_return_date)}]\n"
            value += setValue(list["rtn_date"].toString())
            value += "\n\n\n[${getString(R.string.col_item_org)}]\n"
            value += setValue(list["item_org"].toString())
            value += "\n[${getString(R.string.col_item_user)}]\n"
            value += setValue(list["item_user"].toString())
            value += "\n[${getString(R.string.col_item_date)}]\n"
            value += setValue(list["item_date"].toString())
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
