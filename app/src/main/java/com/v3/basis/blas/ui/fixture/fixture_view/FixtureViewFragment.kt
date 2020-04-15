package com.v3.basis.blas.ui.fixture.fixture_view


import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.v3.basis.blas.R
import com.v3.basis.blas.blasclass.config.FixtureType.Companion.canTakeOut
import com.v3.basis.blas.blasclass.config.FixtureType.Companion.finishInstall
import com.v3.basis.blas.blasclass.config.FixtureType.Companion.notTakeOut
import com.v3.basis.blas.blasclass.config.FixtureType.Companion.statusCanTakeOut
import com.v3.basis.blas.blasclass.config.FixtureType.Companion.statusFinishInstall
import com.v3.basis.blas.blasclass.config.FixtureType.Companion.statusNotTakeOut
import com.v3.basis.blas.blasclass.config.FixtureType.Companion.statusTakeOut
import com.v3.basis.blas.blasclass.config.FixtureType.Companion.takeOut
import com.v3.basis.blas.blasclass.rest.BlasRestFixture
import com.v3.basis.blas.ui.ext.getStringExtra
import kotlinx.android.synthetic.main.fragment_item_view.*
import org.json.JSONObject


/**
 * A simple [Fragment] subclass.
 */
class FixtureViewFragment : Fragment() {

    private var token:String? = null
    private var project_id:String? = null
    private var dataListAll = mutableListOf<RowModel>()
    private var dataList = mutableListOf<RowModel>()
    private val valueMap : MutableMap<Int, MutableMap<String, String?>> = mutableMapOf()
    private var valueSize :Int = 0

    private var currentIndex: Int = 0
    companion object {
        const val CREATE_UNIT = 20
    }

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
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (valueMap.isNotEmpty()) {
                    val notOverSize = currentIndex + CREATE_UNIT <= dataListAll.size
                    if (!recyclerView.canScrollVertically(1) && notOverSize) {
                        progressBarItemView.visibility = View.VISIBLE
                        setAdapter()
                    }
                }
            }
        })

        //呼ぶタイミングを確定させる！！
        val payload2 = mapOf("token" to token, "project_id" to project_id)
        Log.d("testtest","取得する")
        BlasRestFixture("search",payload2, ::fixtureGetSuccess, ::fixtureGetError).execute()
    }

    private fun createDataList() {
        val cnt = 1
        Log.d("aaaa","${cnt}")

        //データ管理のループ
        dataList.addAll(dataListAll.filterIndexed { index, mutableMap ->
            (index >= currentIndex) && (index <= currentIndex + CREATE_UNIT)
        }.toMutableList())

        // update
        if (dataList.isNotEmpty()) {
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
        progressBarItemView.visibility = View.INVISIBLE
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


            valueMap[fixtureId] = mutableMapOf("serial_number" to fixture.getString("serial_number"),
                "status" to fixture.getString("status"),
                "fix_user" to fields.getJSONObject("FixUser").getString("name"),
                "takeout_user" to fields.getJSONObject("TakeOutUser").getString("name"),
                "rtn_user" to fields.getJSONObject("RtnUser").getString("name"),
                "item_user" to fields.getJSONObject("ItemUser").getString("name"),
                "fix_org" to fields.getJSONObject("FixOrg").getString("name"),
                "takeout_org" to fields.getJSONObject("TakeOutOrg").getString("name"),
                "rtn_org" to fields.getJSONObject("RtnOrg").getString("name"),
                "item_org" to fields.getJSONObject("ItemOrg").getString("name"),
                "fix_date" to fixture.getString("fix_date"),
                "takeout_date" to fixture.getString("takeout_date"),
                "rtn_date" to fixture.getString("rtn_date"),
                "item_date" to fixture.getString("item_date")
                )
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
                dataListAll.add(rowModel)
            }
            dataListAll.reverse()
            setAdapter()
        }
    }

    /**
     * フィールド取得失敗時
     */
    private fun fixtureGetError(errorCode: Int, aplCode:Int) {
        Toast.makeText(getActivity(), errorCode.toString(), Toast.LENGTH_LONG).show()
        //エラーのため、データを初期化する
        valueMap.clear()
        Log.d("取得失敗","取得失敗")
        Log.d("取得失敗","${errorCode}")
        progressBarItemView.visibility = View.INVISIBLE
    }

    /**
     * 表示する値を作成する
     */
    private fun createValue(list: MutableMap<String,String?>): String? {
        var value:String? =null
        value = "【${getString(R.string.col_serialnumber)}】"
        value += "\n  ${list["serial_number"]}"
        value += "\n\n${getString(R.string.col_serialnumber)}"
        value += when(list["status"]){//config.FixtureTypeにて定義している。
            canTakeOut -> {"${statusCanTakeOut}"}
            takeOut -> {"${statusTakeOut}"}
            finishInstall -> {"${statusFinishInstall}"}
            notTakeOut -> {"${statusNotTakeOut}"}
            else -> { }
        }
        value += "\n\n\n${getString(R.string.col_kenpin_org)}"
        value += setValue(list["fix_org"]!!)
        value += "\n${getString(R.string.col_kenpin_user)}"
        value += setValue(list["fix_user"]!!)
        value += "\n${getString(R.string.col_kenpin_date)}"
        value += setValue(list["fix_date"]!!)
        value += "\n\n${getString(R.string.col_takeout_org)}"
        value += setValue(list["takeout_org"]!!)
        value += "\n${getString(R.string.col_takeout_user)}"
        value += setValue(list["takeout_user"]!!)
        value += "\n${getString(R.string.col_takeout_date)}"
        value += setValue(list["takeout_date"]!!)
        value += "\n\n${getString(R.string.col_return_org)}"
        value += setValue(list["rtn_org"]!!)
        value += "\n${getString(R.string.col_return_user)}"
        value += setValue(list["rtn_user"]!!)
        value += "\n${getString(R.string.col_return_date)}"
        value += setValue(list["rtn_date"]!!)
        value += "\n\n${getString(R.string.col_item_org)}"
        value += setValue(list["item_org"]!!)
        value += "\n${getString(R.string.col_item_user)}"
        value += setValue(list["item_user"]!!)
        value += "\n${getString(R.string.col_item_date)}"
        value += setValue(list["item_date"]!!)

        return value
    }


    private fun setValue(value:String): String {
        if(value == "null"){
            return "  "
        }else{
            return value
        }
    }

}
