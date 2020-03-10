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
import com.v3.basis.blas.blasclass.rest.BlasRestOrgs
import kotlinx.android.synthetic.main.fragment_item_view.*
import com.v3.basis.blas.blasclass.config.FixtureType.Companion.canTakeOut
import com.v3.basis.blas.blasclass.config.FixtureType.Companion.finishInstall
import com.v3.basis.blas.blasclass.config.FixtureType.Companion.notTakeOut
import com.v3.basis.blas.blasclass.config.FixtureType.Companion.statusCanTakeOut
import com.v3.basis.blas.blasclass.config.FixtureType.Companion.statusFinishInstall
import com.v3.basis.blas.blasclass.config.FixtureType.Companion.statusNotTakeOut
import com.v3.basis.blas.blasclass.config.FixtureType.Companion.statusTakeOut
import com.v3.basis.blas.blasclass.config.FixtureType.Companion.takeOut
import com.v3.basis.blas.ui.ext.getStringExtra
import org.json.JSONObject


/**
 * A simple [Fragment] subclass.
 */
class FixtureViewFragment : Fragment() {

    private var token:String? = null
    private var project_id:String? = null
    private var dataList = mutableListOf<RowModel>()
    private val valueMap : MutableMap<Int, MutableMap<String, String?>> = mutableMapOf()
    private var valueSize :Int = 0

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

        super.onCreateView(inflater, container, savedInstanceState)
        Log.d("【onCreateView】","呼ばれた")
        token = getStringExtra("token")
        project_id = getStringExtra("project_id")

        return inflater.inflate(R.layout.fragment_item_view, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d("lifeCycle", "onViewCreated")
        //リサイクラ-viewを取得
        //基本的にデータはまだ到着していないため、空のアクティビティとadapterだけ設定しておく
        val recyclerView = recyclerView
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.adapter = adapter

        val payload = mapOf("token" to token )
        Log.d("会社を取得する","取得開始")
        Log.d("会社を取得する","${token}")
        BlasRestOrgs(payload, ::orgGetSuccess, ::orgGetError).execute()

        //呼ぶタイミングを確定させる！！
        val payload2 = mapOf("token" to token, "project_id" to project_id)
        Log.d("testtest","取得する")
        BlasRestFixture("search",payload2, ::fixtureGetSuccess, ::fixtureGetError).execute()
    }

    private fun createDataList(): List<RowModel> {
        val cnt = 1
        Log.d("aaaa","${cnt}")

        //データ管理のループ
        valueMap.forEach {
            /*val itemRecord = it
            val colMax = valueMap.size
            val item_id = it["item_id"]
            var text: String? = "String"
            var loopcnt = 1*/
            Log.d("aaa","ここまで来たぞー！！")
            var id = "1"
            var text= "test"
            //カラムの定義取得
            val fixture_id = it.key
            val fixture_value  = it.value
            Log.d("aaa","${it}")
            Log.d("aaa","${fixture_id}")
            Log.d("aaa","${ fixture_value.get("fixture_id")}")
            val value = createValue(fixture_value)

            val rowModel = RowModel().also {
                if (fixture_id != null) {
                    it.title = fixture_id.toString()
                }
                if (value != null) {
                    //値を何とかしなくちゃね
                    it.detail = value
                }
            }
            dataList.add(rowModel)
        }
        return dataList
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
        Log.d("testtest","${result}")
        val records = result.getJSONArray("records")
        for(i in 0 until records.length()) {
            val fields = JSONObject(records[i].toString())
            val fixture = fields.getJSONObject("Fixture")
            val fixtureId = fixture.getInt("fixture_id")
            val fixtureStatus = fixture.getString("status")
            val serialNumber = fixture.getString("serial_number")
            val fixUser = fields.getJSONObject("FixUser").getString("name")
            val takeOutUser = fields.getJSONObject("TakeOutUser").getString("name")
            val rtnUser = fields.getJSONObject("RtnUser").getString("name")
            val itemUser = fields.getJSONObject("ItemUser").getString("name")
            val fixOrg = fields.getJSONObject("FixOrg").getString("name")
            val takeOutOrg = fields.getJSONObject("TakeOutOrg").getString("name")
            val rtnOrg = fields.getJSONObject("RtnOrg").getString("name")
            val itemOrg = fields.getJSONObject("ItemOrg").getString("name")

            valueMap[fixtureId] = mutableMapOf("serial_number" to serialNumber,
                "status" to fixtureStatus,
                "fix_user" to fixUser,
                "takeout_user" to takeOutUser,
                "rtn_user" to rtnUser,
                "item_user" to itemUser,
                "fix_org" to fixOrg,
                "takeout_org" to takeOutOrg,
                "rtn_org" to rtnOrg,
                "item_org" to itemOrg)
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
        if(valueMap.isNotEmpty()) {
            setAdapter()
        }
    }

    /**
     * フィールド取得失敗時
     */
    private fun fixtureGetError(errorCode: Int) {
        Toast.makeText(getActivity(), errorCode.toString(), Toast.LENGTH_LONG).show()
        //エラーのため、データを初期化する
        valueMap.clear()
        Log.d("取得失敗","取得失敗")
        Log.d("取得失敗","${errorCode}")
    }

    /**
     * 表示する値を作成する
     */
    private fun createValue(list: MutableMap<String,String?>): String? {
        var value:String? =null
        value = "【シリアルナンバー】"
        value += "\n  ${list["serial_number"]}"
        value += "\n\nステータス："
        value += when(list["status"]){//config.FixtureTypeにて定義している。
            canTakeOut -> {"${statusCanTakeOut}"}
            takeOut -> {"${statusTakeOut}"}
            finishInstall -> {"${statusFinishInstall}"}
            notTakeOut -> {"${statusNotTakeOut}"}
            else -> { }
        }
        value += "\n\n\n検品した会社："
        value += setValue(list["fix_org"]!!)
        value += "\n検品したユーザ："
        value += setValue(list["fix_user"]!!)
        value += "\n\n持出した会社："
        value += setValue(list["takeout_org"]!!)
        value += "\n持出したユーザ："
        value += setValue(list["takeout_user"]!!)
        value += "\n\n返却した会社："
        value += setValue(list["rtn_org"]!!)
        value += "\n返却したユーザ："
        value += setValue(list["rtn_user"]!!)
        value += "\n\n設置した会社："
        value += setValue(list["item_org"]!!)
        value += "\n設置したユーザ："
        value += setValue(list["item_user"]!!)

        return value
    }

    /**
     * 会社取得成功時
     */
    private fun orgGetSuccess(result: MutableList<MutableMap<String, String?>>?){
        Log.d("取得成功","${result}")
    }

    /**
     * 会社取得失敗時
     */
    private fun orgGetError(errorCode: Int){
        Log.d("取得失敗","${errorCode}")
    }

    private fun setValue(value:String): String {
        if(value == "null"){
            return "  "
        }else{
            return value
        }
    }

}
