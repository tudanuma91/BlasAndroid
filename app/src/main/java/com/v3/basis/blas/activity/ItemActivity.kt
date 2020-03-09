package com.v3.basis.blas.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI.setupWithNavController
import androidx.navigation.ui.setupActionBarWithNavController
import com.v3.basis.blas.R
import com.v3.basis.blas.blasclass.config.FieldType
import kotlinx.android.synthetic.main.activity_item.*

class ItemActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_item)

        val navController = findNavController(R.id.nav_host_fragment)
        setupWithNavController(item_list_bottom_navigation, navController)

        //タイトルバーの名称を変更する処理
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navi_item_view,
                R.id.navi_item_create,
                R.id.navi_item_seach
               // R.id.navi_item_back
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)

        /**
         * 戻るボタンが非表示になる問題の修正
         * [setupActionBarWithNavController]内部で[setDisplayHomeAsUpEnabled]をfalseにする処理が走るため、
         * リスナーにて再度[setDisplayHomeAsUpEnabled]trueとする。
         */
        navController.addOnDestinationChangedListener{ _, destination, _ ->
            supportActionBar?.apply {
                title = destination.label
                setDisplayHomeAsUpEnabled(true)
                setHomeButtonEnabled(true)
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                //Write your logic here
                this.finish()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    fun replaceFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frame_item, fragment)
        fragmentTransaction.commit()
        Log.d("title","呼ばれた")
    }

    fun deleteFragment(fragment: Fragment){
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.remove(fragment)
        fragmentTransaction.commit()
    }


    /**
     * 取得したアイテムフィールドから項目のタイプ・名前・選択肢を返す
     * [引数]
     * list => 配列。項目の情報が入っている
     *
     * [返り値]
     * rtnType => 項目のタイプを返す。
     *            それ以外 => 項目名を返す。
     *            listがnull => nullを返す。
     * rtnTitle => 項目の名称を返す。
     *             それ以外 => 項目名を返す
     *             listがnull => nullを返す。
     * choiceValue => 項目の選択肢を返す。
     *                typeがSINGLE_SELECTION(単一選択) => 値を返す
     *                typeがMULTIPLE_SELECTION(複数選択) => 値を返す
     *                それ以外 => nullを返す
     */
    fun typeCheck(list:MutableMap<String, String?>?): Triple<String?, String? ,MutableMap<String, String?>?> {
        val choiceValue:MutableMap<String, String?>? = null
        var rtnType:String? =null
        var rtnTitle :String?= null
        if(list!=null){
            val chkType = list.get("type")
            when(chkType){
                FieldType.TEXT_FIELD->{
                    rtnType =  FieldType.TEXT_FIELD
                    rtnTitle = list.get("name")
                }
            }
        }
        return Triple(rtnType,rtnTitle,choiceValue)
    }


    /**
     * 取得した項目名をフォームの項目のタイトルにセットする。
     * [引数]
     * title => string。取得したタイトルを指定
     * params => LinearLayout.params。表示するviewの設定を指定
     * act => FragmentActivity。この操作を行うactivity指定
     *
     * [返り値]
     * view => paramsの設定を反映したにtextViewを作成。titleをタイトルに反映して返す。
     *         titleがnullの時 => タイトルnullのまま作成
     *         それ以外 => titleの値をタイトルに反映する
     */
    fun createFormSectionTitle(title:String?, params:LinearLayout.LayoutParams?, act:FragmentActivity?): TextView {
        val view = TextView(act)
        val formTitle = if(title != null){"${title}"}else{" "}
        view.setText("${formTitle}")
        view.setLayoutParams(params)
        return view
    }

    /**
     * テキストフィールドを作成する関数。
     * [引数]
     * params => LinearLayoutに設定したパラメータ
     * act => 操作をするFragmentActivity
     * cnt => 作成しているフォームは何行目かを示す
     *
     * []
     */
    fun createTextField(params:LinearLayout.LayoutParams?, act:FragmentActivity?,cnt:Int): EditText {
        val edit = EditText(act)
        edit.setText("")
        edit.inputType =1
        edit.setLayoutParams(params)
        edit.id = cnt
        return edit
    }

}
