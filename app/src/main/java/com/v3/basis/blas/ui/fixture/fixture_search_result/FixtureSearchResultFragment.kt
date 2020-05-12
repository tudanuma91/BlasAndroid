package com.v3.basis.blas.ui.fixture.fixture_search_result


import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.v3.basis.blas.R
import com.v3.basis.blas.blasclass.app.BlasMsg
import com.v3.basis.blas.blasclass.app.searchAndroid
import com.v3.basis.blas.blasclass.config.FixtureType.Companion.canTakeOut
import com.v3.basis.blas.blasclass.config.FixtureType.Companion.finishInstall
import com.v3.basis.blas.blasclass.config.FixtureType.Companion.notTakeOut
import com.v3.basis.blas.blasclass.config.FixtureType.Companion.statusCanTakeOut
import com.v3.basis.blas.blasclass.config.FixtureType.Companion.statusFinishInstall
import com.v3.basis.blas.blasclass.config.FixtureType.Companion.statusNotTakeOut
import com.v3.basis.blas.blasclass.config.FixtureType.Companion.statusTakeOut
import com.v3.basis.blas.blasclass.config.FixtureType.Companion.takeOut
import com.v3.basis.blas.blasclass.rest.BlasRestErrCode
import com.v3.basis.blas.blasclass.rest.BlasRestFixture
import com.v3.basis.blas.ui.ext.addTitle
import com.v3.basis.blas.ui.ext.getStringExtra
import kotlinx.android.synthetic.main.fragment_fixture_search_result.*
import org.json.JSONObject
import java.lang.Exception

/**
 * A simple [Fragment] subclass.
 */
class FixtureSearchResultFragment : Fragment() {
    private var token:String? = null
    private var project_id:String? = null
    private var freeWord:String? = null
    private var dataList = mutableListOf<RowModel>()
    private val baseDataList:MutableList<MutableMap<String,String?>> = mutableListOf()
    private var searchValueMap:MutableMap<String,String?> = mutableMapOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addTitle("project_name")
    }

    private val adapter: ViewAdapter = ViewAdapter(dataList, object : ViewAdapter.ListListener {
        override fun onClickRow(tappedView: View, rowModel: RowModel) {
            //カードタップ時の処理
            Toast.makeText(activity, rowModel.title, Toast.LENGTH_LONG).show()
            Log.d(
                "DataManagement",
                "click_NAME => ${rowModel.title}/click_ID => ${rowModel.detail}"
            )

        }

    })

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        //トークンの取得
        Log.d("【onCreateView】","呼ばれた")
        if(getStringExtra("token") != null){
            token = getStringExtra("token")
        }

        //プロジェクトIDの取得
        if(getStringExtra("project_id") != null){
            project_id = getStringExtra("project_id")
        }

        //検索ワードの取得
        if(getStringExtra("freeWord") != null){
            freeWord = getStringExtra("freeWord")
            searchValueMap.set("freeWord",freeWord)
        }

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


        Log.d("freewordの値","freeWordの値=>${freeWord}")

        return inflater.inflate(R.layout.fragment_fixture_search_result, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        try {
            Log.d("lifeCycle", "onViewCreated")
            //リサイクラ-viewを取得
            //基本的にデータはまだ到着していないため、空のアクティビティとadapterだけ設定しておく
            val recyclerView = recyclerView
            recyclerView.setHasFixedSize(true)
            recyclerView.layoutManager = LinearLayoutManager(activity)
            recyclerView.adapter = adapter

            //呼ぶタイミングを確定させる！！
            val payload2 = mapOf("token" to token, "project_id" to project_id)
            Log.d("testtest", "取得する")
            BlasRestFixture("search", payload2, ::fixtureGetSuccess, ::fixtureGetError).execute()
        }catch (e:Exception){

        }
    }

    private fun createDataList() {
        val cnt = 1
        Log.d("aaaa","${cnt}")

        //ここで検索処理をする
        val searchResult = searchAndroid(searchValueMap,baseDataList,"","")
        if(searchResult.size > 0) {

            //検索処理の結果を表示
            searchResult.forEach {
                val fixture_id = it["fixture_id"]
                //バリューの取得
                val value = createValue(it)
                val rowModel = RowModel().also {
                    //モデル作成
                    if (fixture_id != null) {
                        it.title = fixture_id.toString()
                    }
                    if (value != null) {
                        it.detail = value
                    }
                }
                //作成したもでモデルを追加。表示する。
                dataList.add(rowModel)
            }
        }else{
            val title = getString(R.string.dialog_title)
            val text = getString(R.string.search_error)
            AlertDialog.Builder(activity)
                .setTitle(title)
                .setMessage(text)
                .setPositiveButton("OK"){ dialog, which ->
                    activity!!.finish()
                }
                .show()
        }

    }

    /**
     *  データ登録
     */
    private fun setAdapter() {
        Log.d("konishi", "setAdapter")
        createDataList()
        adapter.notifyItemInserted(0)
    }

    /**
     * 機器管理取得時
     */
    private fun fixtureGetSuccess(result: JSONObject) {
        //カラム順に並べ替える
        val records = result.getJSONArray("records")
        for(i in 0 until records.length()) {
            val fields = JSONObject(records[i].toString())
            val fixture = fields.getJSONObject("Fixture")

            val baseValueMap:MutableMap<String,String?> = mutableMapOf()
            baseValueMap.set("fixture_id",fixture.get("fixture_id").toString())
            baseValueMap.set("project_id",fixture.get("project_id").toString())
            baseValueMap.set("serial_number",fixture.get("serial_number").toString())
            baseValueMap.set("fix_date",fixture.get("fix_date").toString())
            baseValueMap.set("rtn_date",fixture.get("rtn_date").toString())
            baseValueMap.set("item_date",fixture.get("item_date").toString())
            baseValueMap.set("takeout_date",fixture.get("takeout_date").toString())
            baseValueMap.set("status",fixture.get("status").toString())
            baseValueMap.set("create_date",fixture.get("create_date").toString())
            baseValueMap.set("update_date",fixture.get("update_date").toString())
            baseValueMap.set("FixUser",fields.getJSONObject("FixUser").getString("name"))

            baseValueMap.set("TakeOutUser",fields.getJSONObject("TakeOutUser").getString("name"))

            baseValueMap.set("RtnUser",fields.getJSONObject("RtnUser").getString("name"))
            baseValueMap.set("ItemUser",fields.getJSONObject("ItemUser").getString("name"))
            baseValueMap.set("FixOrg",fields.getJSONObject("FixOrg").getString("name"))
            baseValueMap.set("TakeOutOrg",fields.getJSONObject("TakeOutOrg").getString("name"))
            baseValueMap.set("RtnOrg",fields.getJSONObject("RtnOrg").getString("name"))
            baseValueMap.set("ItemOrg",fields.getJSONObject("ItemOrg").getString("name"))


            baseDataList.add(baseValueMap)

        }
        /*
        if(records != null){
            records.forEach {
                val id = it["fixture_id"]?.toInt()
                if(id != null){
                    valueMap[id] = it
                }
            }
        }
        Log.d("value_map","${valueMap}")
        Log.d("value_map","${valueMap.size}")
        valueSize = valueMap.size
*/
        if(baseDataList.isNotEmpty()) {
            setAdapter()
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
        baseDataList.clear()
        Log.d("取得失敗","${errorCode}")
    }

    /**
     * 表示する値を作成する
     */
    private fun createValue(list: MutableMap<String,String?>): String? {
        var value:String? =null
        value = "[${getString(R.string.col_serialnumber)}]"
        value += "\n${list["serial_number"]}"
        value += "\n[${getString(R.string.col_status)}]\n"
        value += when(list["status"]){//config.FixtureTypeにて定義している。
            canTakeOut -> {"${statusCanTakeOut}"}
            takeOut -> {"${statusTakeOut}"}
            finishInstall -> {"${statusFinishInstall}"}
            notTakeOut -> {"${statusNotTakeOut}"}
            else -> { }
        }
        value += "\n\n\n[${getString(R.string.col_kenpin_org)}]\n"
        value += setValue(list["FixOrg"].toString())
        value += "\n[${getString(R.string.col_kenpin_user)}]\n"
        value += setValue(list["FixUser"].toString())
        value += "\n[${getString(R.string.col_kenpin_date)}]\n"
        value += setValue(list["fix_date"].toString())
        value += "\n\n[${getString(R.string.col_takeout_org)}]\n"
        value += setValue(list["TakeOutOrg"].toString())
        value += "\n[${getString(R.string.col_takeout_user)}]\n"
        value += setValue(list["TakeOutUser"].toString())
        value += "\n[${getString(R.string.col_takeout_date)}]\n"
        value += setValue(list["takeout_date"].toString())
        value += "\n\n[${getString(R.string.col_return_org)}]\n"
        value += setValue(list["RtnOrg"].toString())
        value += "\n[${getString(R.string.col_return_user)}]\n"
        value += setValue(list["RtnUser"].toString())
        value += "\n[${getString(R.string.col_return_date)}]\n"
        value += setValue(list["rtn_date"].toString())
        value += "\n\n[${getString(R.string.col_item_org)}]\n"
        value += setValue(list["ItemOrg"].toString())
        value += "\n[${getString(R.string.col_item_user)}]\n"
        value += setValue(list["ItemUser"].toString())
        value += "\n[${getString(R.string.col_item_date)}]\n"
        value += setValue(list["item_date"].toString())



      /*  value = "【${getString(R.string.col_serialnumber)}】"
        value += "\n  ${list["serial_number"]}"
        value += "\n\n${getString(R.string.col_status)}"
        value += when(list["status"]){//config.FixtureTypeにて定義している。
            canTakeOut -> {"${statusCanTakeOut}"}
            takeOut -> {"${statusTakeOut}"}
            finishInstall -> {"${statusFinishInstall}"}
            notTakeOut -> {"${statusNotTakeOut}"}
            else -> { }
        }
        value += "\n\n\n${getString(R.string.col_kenpin_org)}"
        value += setValue(list["FixOrg"].toString())
        value += "\n${getString(R.string.col_kenpin_user)}"
        value += setValue(list["FixUser"].toString())
        value += "\n${getString(R.string.col_kenpin_date)}"
        value += setValue(list["fix_date"].toString())
        value += "\n\n${getString(R.string.col_takeout_org)}"
        value += setValue(list["TakeOutOrg"].toString())
        value += "\n${getString(R.string.col_takeout_user)}"
        value += setValue(list["TakeOutUser"].toString())
        value += "\n${getString(R.string.col_takeout_date)}"
        value += setValue(list["takeout_date"].toString())
        value += "\n\n${getString(R.string.col_return_org)}"
        value += setValue(list["RtnOrg"].toString())
        value += "\n${getString(R.string.col_return_user)}"
        value += setValue(list["RtnUser"].toString())
        value += "\n${getString(R.string.col_return_date)}"
        value += setValue(list["rtn_date"].toString())
        value += "\n\n${getString(R.string.col_item_org)}"
        value += setValue(list["ItemOrg"].toString())
        value += "\n${getString(R.string.col_item_user)}"
        value += setValue(list["ItemUser"].toString())
        value += "\n${getString(R.string.col_item_date)}"
        value += setValue(list["item_date"].toString())*/
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

}
