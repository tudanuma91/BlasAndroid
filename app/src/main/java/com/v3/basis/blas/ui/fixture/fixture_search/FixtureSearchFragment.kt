package com.v3.basis.blas.ui.fixture.fixture_search

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
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
import com.v3.basis.blas.blasclass.config.FieldType
import com.v3.basis.blas.blasclass.rest.BlasRestFixture
import com.v3.basis.blas.ui.ext.addTitle
import com.v3.basis.blas.ui.ext.getStringExtra
import org.json.JSONObject
import java.lang.Exception
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
    private var errorList:MutableList<String> = mutableListOf()
    private var titleMap:MutableMap<String,TextView> = mutableMapOf()

    val calender = Calendar.getInstance()
    val year = calender.get(Calendar.YEAR)
    val month = calender.get(Calendar.MONTH)
    val day = calender.get(Calendar.DAY_OF_MONTH)
    lateinit var root : View


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        token = getStringExtra("token")
        projectId = getStringExtra("project_id")
        addTitle("project_name")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        root = inflater.inflate(R.layout.fragment_fixture_search, container, false)
        try {
            if(token != null && projectId != null) {
                Log.d("機器管理検索画面", "token/${token}:projectId/${projectId}")

                //配列に入力フォームを格納する。
                formMap.set("freeWord", root.findViewById<EditText>(R.id.fixFreeWordValue))
                formMap.set("serialNumber", root.findViewById<EditText>(R.id.fixSerialNumberSelect))
                formMap.set("dataId", root.findViewById<EditText>(R.id.fixDataIdSelect))
                //ステータスは別口で取得すること
                formMap.set("kenpinOrg", root.findViewById<EditText>(R.id.fixKenpinOrgSelect))
                formMap.set("kenpinUser", root.findViewById<EditText>(R.id.fixKenpinUserSelect))
                formMap.set("kenpinDayMin", root.findViewById<EditText>(R.id.fixKenpinDayMin))
                formMap.set("kenpinDayMax", root.findViewById<EditText>(R.id.fixKenpinDayMax))
                formMap.set("takeOutOrg", root.findViewById<EditText>(R.id.fixTakeoutOrgSelect))
                formMap.set("takeOutUser", root.findViewById<EditText>(R.id.fixTakeoutUserSelect))
                formMap.set("takeOutDayMin", root.findViewById<EditText>(R.id.fixTakeoutDayMin))
                formMap.set("takeOutDayMax", root.findViewById<EditText>(R.id.fixTakeoutDayMax))
                formMap.set("returnOrg", root.findViewById<EditText>(R.id.fixReturnOrgSelect))
                formMap.set("returnUser", root.findViewById<EditText>(R.id.fixReturnUserSelect))
                formMap.set("returnDayMin", root.findViewById<EditText>(R.id.fixReturnDayMin))
                formMap.set("returnDayMax", root.findViewById<EditText>(R.id.fixReturnDayMax))
                formMap.set("itemOrg", root.findViewById<EditText>(R.id.fixItemOrgSelect))
                formMap.set("itemUser", root.findViewById<EditText>(R.id.fixItemUserSelect))
                formMap.set("itemDayMin", root.findViewById<EditText>(R.id.fixItemDayMin))
                formMap.set("itemDayMax", root.findViewById<EditText>(R.id.fixItemDayMax))


                //検品日付最小値タップ処理
                val kenpinDayMin = formMap["kenpinDayMin"]
                if (kenpinDayMin != null) {
                    kenpinDayMin.setOnClickListener {
                        setDateTimeAction(kenpinDayMin)
                    }
                }

                //検品最大値タップ時処理
                val kenpinDayMax = formMap["kenpinDayMax"]
                if (kenpinDayMax != null) {
                    kenpinDayMax.setOnClickListener {
                        setDateTimeAction(kenpinDayMax)
                    }
                }

                //持ち出し日付最小値タップ処理
                val takeOutDayMin = formMap["takeOutDayMin"]
                if (takeOutDayMin != null) {
                    takeOutDayMin.setOnClickListener {
                        setDateTimeAction(takeOutDayMin)
                    }
                }

                //持ち出し日付最大値タップ処理
                val takeOutDayMax = formMap["takeOutDayMax"]
                if (takeOutDayMax != null) {
                    takeOutDayMax.setOnClickListener {
                        setDateTimeAction(takeOutDayMax)
                    }
                }

                //返却最小値タップ処理
                val returnDayMin = formMap["returnDayMin"]
                if (returnDayMin != null) {
                    returnDayMin.setOnClickListener {
                        setDateTimeAction(returnDayMin)
                    }
                }

                //返却最大値タップ処理
                val returnDayMax = formMap["returnDayMax"]
                if (returnDayMax != null) {
                    returnDayMax.setOnClickListener {
                        setDateTimeAction(returnDayMax)
                    }
                }

                //最小値
                val ItemDayMin = formMap["itemDayMin"]
                if (ItemDayMin != null) {
                    ItemDayMin.setOnClickListener {
                        setDateTimeAction(ItemDayMin)
                    }
                }

                //返却最大値タップ処理
                val ItemDayMax = formMap["itemDayMax"]
                if (ItemDayMax != null) {
                    ItemDayMax.setOnClickListener {
                        setDateTimeAction(ItemDayMax)
                    }
                }


                //検索ボタンタップ処理
                val btnSearch = root.findViewById<Button>(R.id.fixSerchBtn)
                btnSearch.setOnClickListener {

                    if (errorList.size > 0) {
                        errorList.forEach {
                            //val text = formAction.test2(it,titleMap)
                            // titleMap[it]?.setText(text)
                            //titleMap[it]?.setTextColor(Color.DKGRAY)
                            titleRecover(it)
                            Log.d("デバック用ログ", "ここの値はこれだぜ=>${it}")
                        }

                    }

                    errorList.clear()

                    val errorFlg = checkSearchValueManager()
                    if (errorFlg) {
                        errorList.forEach {
                            errorTitleCreate(it)
                        }
                    } else {
                        val freeWordEdit = root.findViewById<EditText>(R.id.fixFreeWordValue)
                        freeWord = freeWordEdit.text.toString()
                        Log.d("testtest", "取得する")
                        startSearch()
                    }
                }
            }
        }catch (e:Exception){

        }
        return root
    }


    private fun startSearch() {
        //検索フィールドの値処理
        val searchValueMap:MutableMap<String,String?> = mutableMapOf()

        //検索項目に入力されている値を取得。searchValueMapに格納する
        formMap.forEach{
            //入力されていないフィールドは""になる
            searchValueMap.set(it.key,it.value.text.toString())
            Log.d("機器管理検索画面","key = ${it.key}, value = ${it.value.text}")
        }
        //edittext以外のフォームはここで個別で格納。（増えてきたらなんか策考えます。）
        val statusSpinner = root.findViewById<Spinner>(R.id.fixSelectStatus)
        searchValueMap.set("status",statusSpinner.selectedItem.toString())
        Log.d("機器管理検索画面","value = ${statusSpinner.selectedItem}")


        val intent = Intent(activity, FixtureSearchResultActivity::class.java)
        intent.putExtra("token",token)
        intent.putExtra("project_id",projectId)
        intent.putExtra("freeWord",searchValueMap["freeWord"].toString())
        intent.putExtra("serialNumber",searchValueMap["serialNumber"].toString())
        intent.putExtra("dataId",searchValueMap["dataId"].toString())
        intent.putExtra("kenpinOrg",searchValueMap["kenpinOrg"].toString())
        intent.putExtra("kenpinUser",searchValueMap["kenpinUser"].toString())
        intent.putExtra("kenpinDayMin",searchValueMap["kenpinDayMin"].toString())
        intent.putExtra("kenpinDayMax",searchValueMap["kenpinDayMax"].toString())
        intent.putExtra("takeOutOrg",searchValueMap["takeOutOrg"].toString())
        intent.putExtra("takeOutUser",searchValueMap["takeOutUser"].toString())
        intent.putExtra("takeOutDayMin",searchValueMap["takeOutDayMin"].toString())
        intent.putExtra("takeOutDayMax",searchValueMap["takeOutDayMax"].toString())
        intent.putExtra("returnOrg",searchValueMap["returnOrg"].toString())
        intent.putExtra("returnUser",searchValueMap["returnUser"].toString())
        intent.putExtra("returnDayMin",searchValueMap["returnDayMin"].toString())
        intent.putExtra("returnDayMax",searchValueMap["returnDayMax"].toString())
        intent.putExtra("itemOrg",searchValueMap["itemOrg"].toString())
        intent.putExtra("itemUser",searchValueMap["itemUser"].toString())
        intent.putExtra("itemDayMin",searchValueMap["itemDayMin"].toString())
        intent.putExtra("itemDayMax",searchValueMap["itemDayMax"].toString())
        intent.putExtra("status",searchValueMap["status"].toString())

        startActivity(intent)

    }


    /**
     * タップ時の処理
     */
    private fun setDateTimeAction(kenpinDay:EditText){
        val dtp = DatePickerDialog(getContext()!!, DatePickerDialog.OnDateSetListener{ view, y, m, d ->
            Toast.makeText(activity, "日付を選択しました${y}/${m+1}/${d}", Toast.LENGTH_LONG).show()
            //フォーマットを作成
            kenpinDay.setText(String.format("%d/%02d/%02d",y,m+1,d))
        }, year,month,day)
        dtp.show()
    }


    private fun checkSearchValueManager(): Boolean {
        //検索フィールドの値処理
        var errorFlg = false

        formMap.forEach{
            val text = it.value.text.toString()
            val chk = valueChk(text)
            if(chk){
                errorList.add(it.key)
                errorFlg = true
            }
        }
        return errorFlg
    }

    fun valueChk(value:String): Boolean {
        val valueLen = value.length
        var chk = false
        Log.d("デバック用ログ","値の中身はコレ!!=>${value}")
        loop@ for(idx in 0 until valueLen){
            when(value.get(idx)){
                '[', ']', '(', ')', '\\', '{', '}',
                '*', '+', '.', '$', '^', '|', ':', '!'
                ->{
                    chk = true
                    break@loop
                }
            }
        }
        Log.d("結果","処理結果はこの通り=>${chk.toString()}")
        return chk
    }

    private fun errorTitleCreate(idx:String){
        val title = titlePicker(idx)
        if(title != null) {
            val titleText = title.text.toString()
            val newTitleText = "${titleText}${FieldType.SEARCH_ERROR}"
            title.setText(newTitleText)
            title.setTextColor(Color.RED)
        }
    }

    private fun titleRecover(idx: String){
        val title = titlePicker(idx)
        if(title != null) {
            val delNum = FieldType.SEARCH_ERROR.length
            val newTitleText = title.text.toString().dropLast(delNum)
            title.setText(newTitleText)
            title.setTextColor(Color.DKGRAY)
        }
    }

    private fun titlePicker(idx: String): TextView? {
        var title :TextView ?
        when(idx) {
            "freeWord" -> { title = root.findViewById<TextView>(R.id.fixFreeWord) }
            "serialNumber"->{ title = root.findViewById<TextView>(R.id.fixSerialNumber) }
            "dataId"->{ title = root.findViewById<TextView>(R.id.fixDataId) }
            "kenpinOrg"->{ title = root.findViewById<TextView>(R.id.fixKenpinOrg) }
            "kenpinUser"->{ title = root.findViewById<TextView>(R.id.fixKenpinUser) }
            "takeOutOrg"->{ title = root.findViewById<TextView>(R.id.fixTakeoutOrg) }
            "takeOutUser"->{ title = root.findViewById<TextView>(R.id.fixTakeoutUser) }
            "returnOrg"->{ title = root.findViewById<TextView>(R.id.fixReturnOrg) }
            "returnUser"->{ title = root.findViewById<TextView>(R.id.fixReturnUser) }
            "itemOrg"->{ title = root.findViewById<TextView>(R.id.fixItemOrg) }
            "itemUser"->{ title = root.findViewById<TextView>(R.id.fixItemUser) }
            else ->{ title = null }
        }
        return title
    }

}
