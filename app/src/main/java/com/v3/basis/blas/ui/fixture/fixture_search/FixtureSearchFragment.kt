package com.v3.basis.blas.ui.fixture.fixture_search

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*

import com.v3.basis.blas.R
import com.v3.basis.blas.activity.FixtureSearchResultActivity
import com.v3.basis.blas.activity.ItemSearchResultActivity
import com.v3.basis.blas.blasclass.app.searchAndroid
import com.v3.basis.blas.blasclass.rest.BlasRestFixture
import com.v3.basis.blas.ui.ext.getStringExtra
import org.json.JSONObject
import java.util.*

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [FixtureSearchFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [FixtureSearchFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class FixtureSearchFragment : Fragment() {
    private var token:String? = null
    private var projectId:String? = null
    private var freeWord:String?= null
    private val formMap :MutableMap<String,EditText> = mutableMapOf()
    val calender = Calendar.getInstance()
    val year = calender.get(Calendar.YEAR)
    val month = calender.get(Calendar.MONTH)
    val day = calender.get(Calendar.DAY_OF_MONTH)
    var root : View? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        root = inflater.inflate(R.layout.fragment_fixture_search, container, false)
        token = getStringExtra("token")
        projectId = getStringExtra("project_id")
        Log.d("機器管理検索画面","token/${token}:projectId/${projectId}")

        //配列に入力フォームを格納する。
        formMap.set("freeWord",root!!.findViewById<EditText>(R.id.fixFreeWordValue))
        formMap.set("serialNumber",root!!.findViewById<EditText>(R.id.fixSerialNumberSelect))
        formMap.set("dataId",root!!.findViewById<EditText>(R.id.fixDataIdSelect))
        //ステータスは別口で取得すること
        //formMap.set("selectStatus",root!!.findViewById<Spinner>(R.id.fixSelectStatus))
        formMap.set("kenpinOrg",root!!.findViewById<EditText>(R.id.fixKenpinOrgSelect))
        formMap.set("kenpinUser",root!!.findViewById<EditText>(R.id.fixKenpinUserSelect))
        formMap.set("kenpinDayMin",root!!.findViewById<EditText>(R.id.fixKenpinDayMin))
        formMap.set("kenpinDayMax",root!!.findViewById<EditText>(R.id.fixKenpinDayMax))
        formMap.set("takeOutOrg",root!!.findViewById<EditText>(R.id.fixTakeoutOrgSelect))
        formMap.set("takeOutUser",root!!.findViewById<EditText>(R.id.fixTakeoutUserSelect))
        formMap.set("takeOutDayMin",root!!.findViewById<EditText>(R.id.fixTakeoutDayMin))
        formMap.set("takeOutDayMax",root!!.findViewById<EditText>(R.id.fixTakeoutDayMax))
        formMap.set("returnOrg",root!!.findViewById<EditText>(R.id.fixReturnOrgSelect))
        formMap.set("returnUser",root!!.findViewById<EditText>(R.id.fixReturnUserSelect))
        formMap.set("returnDayMin",root!!.findViewById<EditText>(R.id.fixReturnDayMin))
        formMap.set("returnDayMax",root!!.findViewById<EditText>(R.id.fixReturnDayMax))


        //検品日付最小値タップ処理
        val kenpinDayMin = formMap["kenpinDayMin"]!!
        kenpinDayMin.setOnClickListener{
            setDateTimeAction(kenpinDayMin)
        }

        //検品最大値タップ時処理
        val kenpinDayMax = formMap["kenpinDayMax"]!!
        kenpinDayMax.setOnClickListener{
            setDateTimeAction(kenpinDayMax)
        }

        //持ち出し日付最小値タップ処理
        val takeOutDayMin = formMap["takeOutDayMin"]!!
        takeOutDayMin.setOnClickListener{
            setDateTimeAction(takeOutDayMin)
        }

        //持ち出し日付最大値タップ処理
        val takeOutDayMax = formMap["takeOutDayMax"]!!
        takeOutDayMax.setOnClickListener{
            setDateTimeAction(takeOutDayMax)
        }

        //返却最小値タップ処理
        val returnDayMin = formMap["returnDayMin"]!!
        returnDayMin.setOnClickListener{
            setDateTimeAction(returnDayMin)
        }

        //返却最大値タップ処理
        val returnDayMax = formMap["returnDayMax"]!!
        returnDayMax.setOnClickListener{
            setDateTimeAction(returnDayMax)
        }


        //検索ボタンタップ処理
        val btnSearch = root!!.findViewById<Button>(R.id.fixSerchBtn)
        btnSearch.setOnClickListener{
            val freeWordEdit = root!!.findViewById<EditText>(R.id.fixFreeWordValue)
            freeWord = freeWordEdit.text.toString()
            val payload2 = mapOf("token" to token, "project_id" to projectId)
            Log.d("testtest","取得する")
            BlasRestFixture("search",payload2, ::fixtureGetSuccess, ::fixtureGetError).execute()
        }
        return root
    }


    private fun fixtureGetSuccess(result: JSONObject) {
        val searchValue:MutableMap<String,String?> = mutableMapOf()
        val baseValueList:MutableList<MutableMap<String,String?>> = mutableListOf()

        val records = result.getJSONArray("records")
        //登録されているレコードの処理。
        for(i in 0 until records.length()) {
            val baseValueMap:MutableMap<String,String?> = mutableMapOf()
            val fields = JSONObject(records[i].toString())
            val fixture = fields.getJSONObject("Fixture")
            baseValueMap.set("fixture_id",fixture.get("fixture_id").toString())
            baseValueMap.set("project_id",fixture.get("project_id").toString())
            baseValueMap.set("serial_number",fixture.get("serial_number").toString())
            baseValueMap.set("fix_date",fixture.get("fix_date").toString())
            baseValueMap.set("rtn_date",fixture.get("rtn_date").toString())
            baseValueMap.set("item_date",fixture.get("item_date").toString())
            baseValueMap.set("status",fixture.get("status").toString())
            baseValueMap.set("create_date",fixture.get("create_date").toString())
            baseValueMap.set("update_date",fixture.get("update_date").toString())
            baseValueList.add(i,baseValueMap)
           /* val fields = JSONObject(records[i].toString())
            val fixture = fields.getJSONObject("Fixture")
            val fixId = fixture.get("fixture_id").toString()
            val proId = fixture.get("project_id").toString()
            val serial_number = fixture.get("serial_number").toString()
            val fix_date = fixture.get("fix_date").toString()
            val rtn_date = fixture.get("rtn_date").toString()
            val item_date = fixture.get("item_date").toString()
            val status = fixture.get("status").toString()
            val create_date = fixture.get("create_date").toString()
            val update_date = fixture.get("update_date").toString()
            baseValueMap.set("fixture_id",fixId)
            baseValueMap.set("project_id",proId)
            baseValueMap.set("serial_number",serial_number)
            baseValueMap.set("fix_date",fix_date)
            baseValueMap.set("rtn_date",rtn_date)
            baseValueMap.set("item_date",item_date)
            baseValueMap.set("status",status)
            baseValueMap.set("create_date",create_date)
            baseValueMap.set("update_date",update_date)
            baseValueList.add(i,baseValueMap)*/

            Log.d("機器管理検索画面","${fixture}")
        }

        //検索フィールドの値処理
        val searchValueMap:MutableMap<String,String?> = mutableMapOf()

        //検索項目に入力されている値を取得。searchValueMapに格納する
        formMap.forEach{
            //入力されていないフィールドは""になる
            searchValueMap.set(it.key,it.value.text.toString())
            Log.d("機器管理検索画面","key = ${it.key}, value = ${it.value.text.toString()}")
        }
        //edittext以外のフォームはここで個別で格納。（増えてきたらなんか策考えます。）
        val statusSpinner = root!!.findViewById<Spinner>(R.id.fixSelectStatus)
        searchValue.set("status",statusSpinner.selectedItem.toString())
        Log.d("機器管理検索画面","value = ${statusSpinner.selectedItem.toString()}")

        //ここでバリデーション処理。例えば最小日:20019/03/11 最大日2018/03/17　の時エラー表示など。
        //ただ、これは後回しでいい。

        val aaa = searchAndroid(searchValueMap,baseValueList)
        aaa.forEach{
            Log.d("機器管理検索画面・検索結果","${it}")
        }

        val intent = Intent(activity, FixtureSearchResultActivity::class.java)
        intent.putExtra("token",token)
        intent.putExtra("project_id",projectId)
        searchValueMap.forEach{
            Log.d("検索画面のループ","${it}")
        }
        intent.putExtra("freeWord",searchValueMap["freeWord"].toString())
        //とりあえずfreewordのみ渡す
        startActivity(intent)

    }

    private fun fixtureGetError(errorCode: Int) {

    }

    /**
     * タップ時の処理
     */
    private fun setDateTimeAction(kenpinDay:EditText){
       // Toast.makeText(activity, "タップした", Toast.LENGTH_LONG).show()
        val dtp = DatePickerDialog(getContext()!!, DatePickerDialog.OnDateSetListener{ view, y, m, d ->
            Toast.makeText(activity, "日付を選択しました${y}/${m+1}/${d}", Toast.LENGTH_LONG).show()
            //フォーマットを作成
            kenpinDay.setText(String.format("%d/%02d/%02d",y,m+1,d))
        }, year,month,day)
        dtp.show()
    }

}
