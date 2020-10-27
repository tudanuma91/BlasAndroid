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
import androidx.fragment.app.FragmentManager

import com.v3.basis.blas.R
import com.v3.basis.blas.activity.ItemSearchResultActivity
import com.v3.basis.blas.blasclass.app.BlasMsg
import com.v3.basis.blas.blasclass.app.searchAndroid
import com.v3.basis.blas.blasclass.config.FieldType
import com.v3.basis.blas.blasclass.rest.BlasRestFixture
import com.v3.basis.blas.ui.ext.addTitleWithProjectName
import com.v3.basis.blas.ui.ext.getStringExtra
import com.v3.basis.blas.ui.ext.hideKeyboardWhenTouch
import com.v3.basis.blas.ui.fixture.FixtureBaseFragment
import com.v3.basis.blas.ui.fixture.fixture_kenpin_multi.FixtureKenpinMultiFragment
import com.v3.basis.blas.ui.fixture.fixture_view.FixtureViewFragment
import kotlinx.android.synthetic.main.fragment_fixture_search.*
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
class FixtureSearchFragment : FixtureBaseFragment() {

    lateinit var freeWord:String
    private val formMap :MutableMap<String,EditText> = mutableMapOf()
    private var errorList:MutableList<String> = mutableListOf()

    private var msg = BlasMsg()
    private val toastErrorLen = Toast.LENGTH_LONG

    val calender = Calendar.getInstance()
    val year = calender.get(Calendar.YEAR)
    val month = calender.get(Calendar.MONTH)
    val day = calender.get(Calendar.DAY_OF_MONTH)
    lateinit var root : View


    companion object {
        fun newInstance() = FixtureSearchFragment()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val extras = activity?.intent?.extras

        if(extras?.getString("token") != null) {
            token = extras.getString("token").toString()
        }
        if(extras?.getString("project_id") != null) {
            projectId = extras.getString("project_id").toString()
        }

        addTitleWithProjectName("検索画面")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        root = inflater.inflate(R.layout.fragment_fixture_search, container, false)

        //配列に入力フォームを格納する。
//        formMap.set("freeWord", root.findViewById<EditText>(R.id.fixFreeWordValue))
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
            setDateTimeAction(kenpinDayMin)
        }
        //検品最大値タップ時処理
        val kenpinDayMax = formMap["kenpinDayMax"]
        if (kenpinDayMax != null) {
            setDateTimeAction(kenpinDayMax)
        }

        //持ち出し日付最小値タップ処理
        val takeOutDayMin = formMap["takeOutDayMin"]
        if (takeOutDayMin != null) {
            setDateTimeAction(takeOutDayMin)
        }

        //持ち出し日付最大値タップ処理
        val takeOutDayMax = formMap["takeOutDayMax"]
        if (takeOutDayMax != null) {
            setDateTimeAction(takeOutDayMax)
        }

        //返却最小値タップ処理
        val returnDayMin = formMap["returnDayMin"]
        if (returnDayMin != null) {
            setDateTimeAction(returnDayMin)
        }

        //返却最大値タップ処理
        val returnDayMax = formMap["returnDayMax"]
        if (returnDayMax != null) {
            setDateTimeAction(returnDayMax)

        }

        //最小値
        val ItemDayMin = formMap["itemDayMin"]
        if (ItemDayMin != null) {
            setDateTimeAction(ItemDayMin)
        }

        //返却最大値タップ処理
        val ItemDayMax = formMap["itemDayMax"]
        if (ItemDayMax != null) {
            setDateTimeAction(ItemDayMax)
        }

        val btnSearch = root.findViewById<Button>(R.id.fixSerchBtn)

        try {
            if(token != null && projectId != null) {
                //検索ボタンタップ処理
                btnSearch.setOnClickListener {

                    if (errorList.size > 0) {
                        errorList.forEach {
                            titleRecover(it)
                        }

                    }

                    errorList.clear()

                    val errorFlg = checkSearchValueManager()
                    if (errorFlg) {
                        //エラーケース
                        errorList.forEach {
                            errorTitleCreate(it)
                        }
                    } else {
                        //正常ケース
//                        val freeWordEdit = root.findViewById<EditText>(R.id.fixFreeWordValue)
//                        freeWord = freeWordEdit.text.toString()
                        startSearch()
                    }
                }
            }else{
                throw Exception("Failed to receive internal data ")
            }
        }catch (e:Exception){
            val errorMessage = msg.createErrorMessage("getFail")
            Toast.makeText(activity, errorMessage, toastErrorLen).show()
            btnSearch.setOnClickListener {
                AlertDialog.Builder(activity)
                    .setTitle("メッセージ")
                    .setMessage("内部データの取得に失敗しました。検索を実行できません")
                    .setPositiveButton("YES") { dialog, which ->
                        //TODO YESを押したときの処理
                    }
                    .show()

            }
        }

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        scrollView.hideKeyboardWhenTouch(this)
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


        //val intent = Intent(activity, FixtureSearchResultActivity::class.java)
        var bundle = Bundle()
        bundle.putString("token",token)
        bundle.putString("project_id",projectId)
        bundle.putString("freeWord",searchValueMap["freeWord"].toString())
        bundle.putString("serialNumber",searchValueMap["serialNumber"].toString())
        bundle.putString("dataId",searchValueMap["dataId"].toString())
        bundle.putString("kenpinOrg",searchValueMap["kenpinOrg"].toString())
        bundle.putString("kenpinUser",searchValueMap["kenpinUser"].toString())
        bundle.putString("kenpinDayMin",searchValueMap["kenpinDayMin"].toString())
        bundle.putString("kenpinDayMax",searchValueMap["kenpinDayMax"].toString())
        bundle.putString("takeOutOrg",searchValueMap["takeOutOrg"].toString())
        bundle.putString("takeOutUser",searchValueMap["takeOutUser"].toString())
        bundle.putString("takeOutDayMin",searchValueMap["takeOutDayMin"].toString())
        bundle.putString("takeOutDayMax",searchValueMap["takeOutDayMax"].toString())
        bundle.putString("returnOrg",searchValueMap["returnOrg"].toString())
        bundle.putString("returnUser",searchValueMap["returnUser"].toString())
        bundle.putString("returnDayMin",searchValueMap["returnDayMin"].toString())
        bundle.putString("returnDayMax",searchValueMap["returnDayMax"].toString())
        bundle.putString("itemOrg",searchValueMap["itemOrg"].toString())
        bundle.putString("itemUser",searchValueMap["itemUser"].toString())
        bundle.putString("itemDayMin",searchValueMap["itemDayMin"].toString())
        bundle.putString("itemDayMax",searchValueMap["itemDayMax"].toString())
        bundle.putString("status",searchValueMap["status"].toString())

        //startActivity(intent)

        bundle.putString("token", token)
        bundle.putString("project_id", projectId)
        bundle.putString("project_name", projectName)

        parentFragmentManager.beginTransaction()
            .replace(R.id.nav_host_fragment_fixture, FixtureViewFragment.newInstance().apply { arguments = bundle})
            .commitNow()

    }


    /**
     * タップ時の処理
     */
    private fun setDateTimeAction(Day:EditText) {
        Day.setOnClickListener {
            val dtp =
                DatePickerDialog(
                    requireContext(),
                    DatePickerDialog.OnDateSetListener { view, y, m, d ->
                        //フォーマットを作成
                        Day.setText(String.format("%d-%02d-%02d", y, m + 1, d))
                    },
                    year,
                    month,
                    day
                )
            dtp.show()
        }
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
                //半角
                '[', ']', '(', ')', '\\', '{', '}',
                '*', '+', '.', '$', '^', '|', ':', '!',
                //全角
                '［','］','（','）','￥','｛','｝',
                '＊','＋','＿','＄','＾','｜','＝','！'
                ->{
                    chk = true
                    break@loop
                }
            }
        }
        return chk
    }

    /**
     * 検索に使用不可文字を使っていた場合、タイトルにエラー表示をし、タイトル色を変更する
     */
    private fun errorTitleCreate(idx:String){
        val title = titlePicker(idx)
        if(title != null) {
            val titleText = title.text.toString()
            val newTitleText = "${titleText}${FieldType.SEARCH_ERROR}"
            title.setText(newTitleText)
            title.setTextColor(Color.RED)
        }
    }

    /**
     * 修復するタイトルを取得し、元に戻す
     */
    private fun titleRecover(idx: String){
        val title = titlePicker(idx)
        if(title != null) {
            val delNum = FieldType.SEARCH_ERROR.length
            val newTitleText = title.text.toString().dropLast(delNum)
            title.setText(newTitleText)
            title.setTextColor(Color.DKGRAY)
        }
    }

    /**
     * タイトルを取得する関数
     */
    private fun titlePicker(idx: String): TextView? {
        val title :TextView ?
        when(idx) {
//            "freeWord" -> { title = root.findViewById<TextView>(R.id.fixFreeWord) }
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
