package com.v3.basis.blas.ui.fixture.fixture_search

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CalendarView
import android.widget.EditText
import android.widget.Toast

import com.v3.basis.blas.R
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
    private var valueMap : MutableMap<Int, MutableMap<String, String?>> = mutableMapOf()
    private var freeWord:String?= null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val calender = Calendar.getInstance()
        val year = calender.get(Calendar.YEAR)
        val month = calender.get(Calendar.MONTH)
        val day = calender.get(Calendar.DAY_OF_MONTH)
        val root = inflater.inflate(R.layout.fragment_fixture_search, container, false)
        token = getStringExtra("token")
        projectId = getStringExtra("project_id")
        Log.d("機器管理検索画面","token/${token}:projectId/${projectId}")


        //日付タップ処理
        val kenpinDayMin = root.findViewById<EditText>(R.id.fixKenpinDayMin)
        kenpinDayMin.setOnClickListener{
            Toast.makeText(activity, "タップした", Toast.LENGTH_LONG).show()
            val date = DatePickerDialog(getContext()!!, DatePickerDialog.OnDateSetListener{ view, y, m, d ->
                Toast.makeText(activity, "日付を選択しました${y}/${m+1}/${d}", Toast.LENGTH_LONG).show()
            }, year,month,day)
            date.show()
            val aaa = year.toString()
            kenpinDayMin.setText(aaa)
        }


        //検索ボタンタップ処理
        val btnSearch = root.findViewById<Button>(R.id.fixSerchBtn)
        btnSearch.setOnClickListener{
            val freeWordEdit = root.findViewById<EditText>(R.id.fixFreeWordValue)
            freeWord = freeWordEdit.text.toString()
            val payload2 = mapOf("token" to token, "project_id" to projectId)
            Log.d("testtest","取得する")
            BlasRestFixture("search",payload2, ::fixtureGetSuccess, ::fixtureGetError).execute()
            /*AlertDialog.Builder(activity)
                .setTitle("メッセージ")
                .setMessage("検索結果が表示されます。\n検索機能は現在作成中です。")
                .setPositiveButton("戻る",{dialog, which ->
                    //TODO YESを押したときの処理
                })
                /*.setNegativeButton("NO", { dialog, which ->
                    //TODO NOを押したときの処理S
                })*/
                .show()*/
        }
        return root
    }

    private fun fixtureGetSuccess(result: JSONObject) {
        val searchValue:MutableMap<String,String?> = mutableMapOf()
        val baseValue:MutableList<MutableMap<String,String?>> = mutableListOf()

        val records = result.getJSONArray("records")
        for(i in 0 until records.length()) {
            val fields = JSONObject(records[i].toString())
            val fixture = fields.getJSONObject("Fixture")
            val test = fixture.get("fixture_id").toString()
            Log.d("機器管理検索画面","${fixture}")
        }
    }

    private fun fixtureGetError(errorCode: Int) {

    }

}
