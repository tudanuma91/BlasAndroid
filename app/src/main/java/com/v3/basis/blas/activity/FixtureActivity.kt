package com.v3.basis.blas.activity

import android.content.Context
import android.content.Intent
import android.media.ToneGenerator
import android.os.*
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.v3.basis.blas.R
import com.v3.basis.blas.ui.ext.setBlasCustomView
import com.v3.basis.blas.ui.ext.showBackKeyForActionBar
import com.v3.basis.blas.ui.common.ARG_PROJECT_ID
import com.v3.basis.blas.ui.common.ARG_PROJECT_NAME
import com.v3.basis.blas.ui.common.ARG_TOKEN
import com.v3.basis.blas.ui.fixture.fixture_config.FixtureConfigFragment
import com.v3.basis.blas.ui.fixture.fixture_kenpin.FixtureKenpinFragment
import com.v3.basis.blas.ui.fixture.fixture_kenpin_multi.FixtureKenpinMultiFragment
import com.v3.basis.blas.ui.fixture.fixture_return.FixtureReturnFragment
import com.v3.basis.blas.ui.fixture.fixture_search.FixtureSearchFragment
import com.v3.basis.blas.ui.fixture.fixture_takeout.FixtureTakeOutFragment
import com.v3.basis.blas.ui.fixture.fixture_view.FixtureViewFragment
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_fixture.*

class FixtureActivity : AppCompatActivity() {
    private lateinit var messageText:TextView
    private var oldResult:String? =null
    private lateinit var vibrator:Vibrator
    private var vibrationEffect = VibrationEffect.createOneShot(300,
        VibrationEffect.DEFAULT_AMPLITUDE
    )
    private var tone: ToneGenerator? = null
    private var realTime = false
    private var prevFragmentId:Int = R.id.navi_fixture_view

    private val disposable = CompositeDisposable()

    private lateinit var argToken:String
    private lateinit var argProjectId:String
    private lateinit var argProjectName:String
    val bundle = Bundle()

   /* QRコード読を読み取りました</string>
    <string name="error_create_qr">すでに登録済のQRコードです</string>
    <string name="error_session_qr">通信エラーのため、機器情報を登録できません</string>*/


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val extras = this.intent?.extras
        if (extras?.getString("token") != null) {
            argToken = extras.getString(ARG_TOKEN).toString()
        }
        if (extras?.getString("project_id") != null) {
            argProjectId = extras.getString(ARG_PROJECT_ID).toString()
        }
        if (extras?.getString("project_name") != null) {
            argProjectName = extras.getString(ARG_PROJECT_NAME).toString()
        }
        //フラグメントに渡す引数
        bundle.putString("token", argToken)
        bundle.putString("project_id", argProjectId)
        bundle.putString("project_name", argProjectName)

        setContentView(R.layout.activity_fixture)

        //フラグメントが使用する画面遷移(ナビゲーション)の定義を持ったコントローラーを取得する
        //val navController = findNavController(R.id.nav_host_fragment_fixture)

        //ボトムナビゲーションにもフラグメントが使用するナビゲーションのコントローラーをボトムにも設定する
        //setupWithNavController(bottom_navigation_fixture, navController)

        bottom_navigation_fixture.setOnNavigationItemSelectedListener {
            //ボトムナビゲーションをクリックしたときのフラグメントの切り替え
            ChangeFragment(it.itemId)
            true//戻り値
        }
        //戻るボタン表示
        showBackKeyForActionBar()

        setBlasCustomView()
    }

    override fun onStart() {
        super.onStart()
        supportFragmentManager.beginTransaction()
            .add(R.id.nav_host_fragment_fixture, FixtureViewFragment.newInstance().apply { arguments = bundle})
            .commitNow()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                //←ボタン押下時
                if(prevFragmentId == R.id.navi_fixture_config) {
                    ChangeFragment(R.id.navi_fixture_kenpin)
                    prevFragmentId = R.id.navi_fixture_kenpin
                }
                else {
                    this.finish()
                    return true
                }
            }
            R.id.menu_fixture_config-> {
                //検品の設定画面を開く
                prevFragmentId = R.id.navi_fixture_config

                supportFragmentManager.beginTransaction()
                    .replace(R.id.nav_host_fragment_fixture, FixtureConfigFragment.newInstance().apply { arguments = bundle })
                    .commitNow()
            }
            R.id.menu_fixture_search -> {
                //検索押下時
                supportFragmentManager.beginTransaction()
                    .replace(R.id.nav_host_fragment_fixture, FixtureSearchFragment.newInstance().apply { arguments = bundle})
                    .commitNow()
            }

            else -> return super.onOptionsItemSelected(item)
        }
        return false
    }

    /*
    アクションバー上部のオプションメニューの設定(3点リーダーのアイコン)
     */
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        super.onCreateOptionsMenu(menu)

        val inflater = menuInflater
        //メニューのリソース選択
        inflater.inflate(R.menu.config_menu_fixture, menu)
        return true
    }

    private fun ChangeFragment(naviId:Int) {

        when(naviId) {
            R.id.navi_fixture_kenpin-> {
                //検品ボタン押下時
                if(isKenpinSingle()) {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.nav_host_fragment_fixture, FixtureKenpinFragment.newInstance().apply { arguments = bundle})
                        .commitNow()
                }
                else {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.nav_host_fragment_fixture, FixtureKenpinMultiFragment.newInstance().apply { arguments = bundle})
                        .commitNow()
                }
            }

            R.id.navi_fixture_motidasi -> {
                //持ち出し押下時
                supportFragmentManager.beginTransaction()
                    .replace(R.id.nav_host_fragment_fixture, FixtureTakeOutFragment.newInstance().apply { arguments = bundle})
                    .commitNow()
            }

            R.id.navi_fixture_return -> {
                //返却押下時
                supportFragmentManager.beginTransaction()
                    .replace(R.id.nav_host_fragment_fixture, FixtureReturnFragment.newInstance().apply { arguments = bundle})
                    .commitNow()
            }

            R.id.navi_fixture_view -> {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.nav_host_fragment_fixture, FixtureViewFragment.newInstance().apply { arguments = bundle})
                    .commitNow()
            }
        }
    }

    /**
     * trueを返したときはシングルの検品
     * falseを返したときはマルチの検品
     */
    public fun isKenpinSingle(): Boolean {

        var prefs = applicationContext?.getSharedPreferences(applicationContext.getString(R.string.BlasAppConfig), Context.MODE_PRIVATE)
        var isSingle = true
        if(prefs != null) {
            isSingle = prefs.getBoolean("singleCamera", true)
        }
        return isSingle
    }

    override fun onRestart() {
        super.onRestart()
       // reloard()
    }

    //検索結果から戻った時に走る処理
    fun reloard(){
        val intent = intent
        overridePendingTransition(0, 0)
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
        finish()

        overridePendingTransition(0, 0)
        startActivity(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable.dispose()
    }

}
