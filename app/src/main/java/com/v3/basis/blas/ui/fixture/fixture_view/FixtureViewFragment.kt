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
import com.v3.basis.blas.ui.fixture.fixture_view.RowModel
import com.v3.basis.blas.ui.fixture.fixture_view.ViewAdapter
import kotlinx.android.synthetic.main.fragment_item_view.*
import com.v3.basis.blas.blasclass.config.FixtureType
import com.v3.basis.blas.blasclass.config.FixtureType.Companion.canTakeOut
import com.v3.basis.blas.blasclass.config.FixtureType.Companion.finishInstall
import com.v3.basis.blas.blasclass.config.FixtureType.Companion.notTakeOut
import com.v3.basis.blas.blasclass.config.FixtureType.Companion.statusCanTakeOut
import com.v3.basis.blas.blasclass.config.FixtureType.Companion.statusFinishInstall
import com.v3.basis.blas.blasclass.config.FixtureType.Companion.statusNotTakeOut
import com.v3.basis.blas.blasclass.config.FixtureType.Companion.statusTakeOut
import com.v3.basis.blas.blasclass.config.FixtureType.Companion.takeOut


/**
 * A simple [Fragment] subclass.
 */
class FixtureViewFragment : Fragment() {
    private var token:String? = null
    private var project_id:String? = null
    private var dataList = mutableListOf<RowModel>()
    private var valueMap : MutableMap<Int, MutableMap<String, String?>> = mutableMapOf<Int, MutableMap<String, String?>>()
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
        // Inflate the layout for this fragment
        super.onCreateView(inflater, container, savedInstanceState)
        Log.d("【onCreateView】","呼ばれた")
        val extras = activity?.intent?.extras
        if(extras?.getString("token") != null) {
            token = extras?.getString("token")
            Log.d("token_fixture","${token}")
        }
        if(extras?.getString("project_id") != null) {
            project_id = extras?.getString("project_id")
            Log.d("project_id","${project_id}")
        }

        val root = inflater.inflate(R.layout.fragment_item_view, container, false)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d("lifeCycle", "onViewCreated")
        //リサイクラ-viewを取得
        //基本的にデータはまだ到着していないため、空のアクティビティとadapterだけ設定しておく
        val recyclerView = recycler_list
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.adapter = adapter

        var payload = mapOf("token" to token )
        Log.d("会社を取得する","取得開始")
        Log.d("会社を取得する","${token}")
        BlasRestOrgs(payload, ::orgGetSuccess, ::orgGetError).execute()

        //呼ぶタイミングを確定させる！！
        var payload2 = mapOf("token" to token, "project_id" to project_id)
        Log.d("testtest","取得する")
        BlasRestFixture("search",payload2, ::fixtureGetSuccess, ::fixtureGetError).execute()
    }

    private fun createDataList(): List<RowModel> {
        var cnt = 1
        Log.d("aaaa","${cnt}")

        //データ管理のループ
        valueMap?.forEach {
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
        if(adapter != null){
            adapter.notifyItemInserted(0)
        }

    }

    /**
     * 機器管理取得時
     */
    private fun fixtureGetSuccess(result: MutableList<MutableMap<String, String?>>?) {
        //カラム順に並べ替える
        Log.d("testtest","${result}")

        if(result != null){
            result.forEach {
                val id = it["fixture_id"]?.toInt()
                if(id != null){
                    valueMap[id] = it
                }
            }
        }
        Log.d("value_map","${valueMap}")
        Log.d("value_map","${valueMap.size}")
        valueSize = valueMap.size

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
        valueMap = mutableMapOf<Int, MutableMap<String, String?>>()
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
        value += "\n【ステータス】"
        value += when(list["status"]){//config.FixtureTypeにて定義している。
            canTakeOut -> {"\n  ${statusCanTakeOut}"}
            takeOut -> {"\n  ${statusTakeOut}"}
            finishInstall -> {"\n  ${statusFinishInstall}"}
            notTakeOut -> {"\n  ${statusNotTakeOut}"}
            else -> { }
        }
        value += "\n【検品した会社】"
        value += "\n  ${list["fix_org_id"]}"
        value += "\n【検品したユーザ】"
        value += "\n  ${list["fix_user_id"]}"
        value += "\n【持出した会社】"
        value += "\n  ${list["takeout_org_id"]}"
        value += "\n【持出したユーザ】"
        value += "\n  ${list["takeout_user_id"]}"
        value += "\n【返却した会社】"
        value += "\n  ${list["rtn_org_id"]}"
        value += "\n【返却したユーザ】"
        value += "\n  ${list["rtn_user_id"]}"
        value += "\n【設置した会社】"
        value += "\n  ${list["item_org_id"]}"
        value += "\n【設置したユーザ】"
        value += "\n  ${list["item_user_id"]}"

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

}
