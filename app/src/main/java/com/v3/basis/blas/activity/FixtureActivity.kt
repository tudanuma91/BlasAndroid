package com.v3.basis.blas.activity

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.*
import android.util.Log
import android.view.MenuItem
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.get
import androidx.fragment.app.FragmentActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.NavigationUI.setupWithNavController
import androidx.navigation.ui.setupActionBarWithNavController
import com.google.zxing.ResultPoint
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.CompoundBarcodeView
import com.v3.basis.blas.R
import com.v3.basis.blas.blasclass.app.BlasApp
import com.v3.basis.blas.blasclass.app.BlasDef
import com.v3.basis.blas.blasclass.app.BlasMsg
import com.v3.basis.blas.blasclass.db.fixture.FixtureController
import com.v3.basis.blas.blasclass.rest.BlasRestErrCode
import com.v3.basis.blas.blasclass.rest.BlasRestFixture
import com.v3.basis.blas.ui.ext.setBlasCustomView
import com.v3.basis.blas.ui.ext.showBackKeyForActionBar
import com.v3.basis.blas.ui.fixture.fixture_kenpin.FixtureKenpinFragment
import com.v3.basis.blas.ui.fixture.fixture_return.FixtureReturnFragment
import com.v3.basis.blas.ui.fixture.fixture_takeout.FixtureTakeOutFragment
import kotlinx.android.synthetic.main.activity_fixture.*
import org.json.JSONObject

class FixtureActivity : AppCompatActivity() {
    private lateinit var messageText:TextView
    private var oldResult:String? =null
    private lateinit var vibrator:Vibrator
    private var vibrationEffect = VibrationEffect.createOneShot(300,
        VibrationEffect.DEFAULT_AMPLITUDE
    )
    private var tone: ToneGenerator? = null
    private var realTime = false

   /* QRコード読を読み取りました</string>
    <string name="error_create_qr">すでに登録済のQRコードです</string>
    <string name="error_session_qr">通信エラーのため、機器情報を登録できません</string>*/


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fixture)
        val navController = findNavController(R.id.nav_host_fragment_fixture)
        setupWithNavController(bottom_navigation_fixture, navController)

        //タイトルバーの名称を変更する処理
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navi_fixture_view,
                R.id.navi_fixture_kenpin,
                R.id.navi_fixture_motidasi,
                R.id.navi_fixture_return,
                R.id.navi_fixture_search
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)

        /**
         * 戻るボタンが非表示になる問題の修正
         * [setupActionBarWithNavController]内部で[setDisplayHomeAsUpEnabled]をfalseにする処理が走るため、
         * リスナーにて再度[setDisplayHomeAsUpEnabled]trueとする。
         */
        navController.addOnDestinationChangedListener{ _, destination, _ ->
            showBackKeyForActionBar()
            supportActionBar?.title = destination.label
        }

        setBlasCustomView()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_fixture)
        when (item.itemId) {
            android.R.id.home -> {

                if (navController.currentDestination?.id == R.id.navi_fixture_view) {
                    this.finish()
                    return true
                } else {
                    //  ルート以外の画面なら、ルート画面に遷移させる！
                    val menu = PopupMenu(this, null).menu
                    menuInflater.inflate(R.menu.bottom_navigation_menu_fixture, menu)
                    NavigationUI.onNavDestinationSelected(menu.get(0), navController)
                }
            }
            else -> return super.onOptionsItemSelected(item)
        }
        return false
    }

    /**
     * QRカメラを起動する関数
     * type => return/kenpin/takeoutのどれかが入る
     * qr_view => QRカメラ起動時に設定を入れる
     * fragm => fragmentを入れる。
     * token => トークンを入れる
     * projectId => プロジェクトIDを入れる
     * result_text => 読みとったQRコードの値を出力する場所
     * message_text => restを使用した結果を出力する場所
     *
     */
    fun openQRCamera(
        type:String,
        qr_view : CompoundBarcodeView,
        fragm: FragmentActivity?,
        token: String?,
        projectId:String?,
        result_text:TextView ,
        message_text:TextView

    ) {
        qr_view.decodeContinuous(object : BarcodeCallback {
            override fun barcodeResult(result: BarcodeResult?) {//QRコードを読み取った時の処理
                Log.d("CL_0001_4", "画面起動")
                if(result.toString() != oldResult) {
                    if (result != null) {
                        //初期化と変数宣言
                        messageText = message_text

                        //読み取りを伝えるバイブレーション
                        if(fragm != null){
                            Log.d("CL_0001_5", "読み取り時震動")
                            vibrator = fragm.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                        }

                        //Thread.sleep(500)
                        Log.d("QRCode", "処理実行！！")
                        Log.d("CL_0001_6", "結果を表示")
                        result_text.text = "$result"


                        //ひとつ前のQRコードをこのQRコードにする。連続読み取りを避けるため。
                        oldResult = result.toString()

                      //  realTime = true

                        var payload2 = mapOf(
                            "token" to token,
                            "project_id" to projectId,
                            "serial_number" to "${result}"
                        )

                        //restで更新する処理
                        when(type){
                            "kenpin"->{
                                Log.d("CL_0001_7", "検品開始")
                                // onResumeをオーバーライドしたので，手動呼び出しは禁止
                                // FixtureKenpinFragment().callOnPouse()
                                // ROOMテストインサート
                                if (FixtureController(this@FixtureActivity, projectId!!).kenpin(result.toString()).not()) {
                                    Log.d("FixtureViewText", "INSERT失敗。projectId: $projectId, ")
                                }
//                                BlasRestFixture("kenpin", payload2, ::success, ::error).execute()
                            }
                            "takeout"->{
                                // onResumeをオーバーライドしたので，手動呼び出しは禁止
                                // FixtureTakeOutFragment().callOnPouse()
                                if (FixtureController(this@FixtureActivity, projectId!!).takeout(result.toString()).not()) {
                                    Log.d("FixtureViewText", "IUPDATE失敗。projectId: $projectId, ")
                                }
//                                BlasRestFixture("takeout",payload2, ::success, ::error).execute()
                            }
                            "return"->{
                                // onResumeをオーバーライドしたので，手動呼び出しは禁止
                                // FixtureReturnFragment().callOnPouse()
                                if (FixtureController(this@FixtureActivity, projectId!!).rtn(result.toString()).not()) {
                                    Log.d("FixtureViewText", "UPDATE失敗。projectId: $projectId, ")
                                }
//                                BlasRestFixture("rtn",payload2, ::success, ::error).execute()
                            }
                        }

                        //この時、エラーが帰ってきたら逃がす処理を追加する。
                    }

                }
            }
            override fun possibleResultPoints(resultPoints: MutableList<ResultPoint>?) { }
        })
    }

    /**
     * rest成功時の処理
     *
     */
    fun success(result: JSONObject){
        //if(realTime) {
            vibrationEffect =
                VibrationEffect.createOneShot(300, VibrationEffect.DEFAULT_AMPLITUDE)
            vibrator.vibrate(vibrationEffect)
            // tone.startTone(ToneGenerator.TONE_DTMF_S,200)
            //tone.startTone(ToneGenerator.TONE_CDMA_ANSWER,200)
            playTone(ToneGenerator.TONE_CDMA_ANSWER)
            Log.d("OK", "作成完了")
            messageText.setTextColor(Color.GREEN)
            messageText.text = "QRコードを読み取りました"
            realTime = false
       // }

    }

    /**
     * rest失敗時の処理
     */
    fun error(errorCode: Int, aplCode :Int){
       // if(realTime) {
            vibrationEffect =
                VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE)
            vibrator.vibrate(vibrationEffect)
            // tone.startTone(ToneGenerator.TONE_CDMA_HIGH_PBX_S_X4,200)
            playTone(ToneGenerator.TONE_CDMA_HIGH_PBX_S_X4)
            Log.d("NG", "作成失敗")
            Log.d("errorCorde", "${errorCode}")
            var message: String? = ""

            message = BlasMsg().getMessage(errorCode, aplCode)
            messageText.setTextColor(Color.RED)
            messageText.text = message
          //  realTime = false
        //}
    }

    private fun playTone(mediaFileRawId: Int) {

        try {
            if (tone == null) {
                tone = ToneGenerator(AudioManager.STREAM_SYSTEM, ToneGenerator.MAX_VOLUME)
            }
            tone?.also {
                it.startTone(mediaFileRawId, 200)
                val handler = Handler(Looper.getMainLooper())
                handler.postDelayed({
                    if (it != null) {
                        Log.d("FixtureActivity", "ToneGenerator released")
                        it.release()
                        tone = null
                    }
                }, 200)
            }
        } catch (e: Exception) {
            Log.d("FixtureActivity", "Exception while playing sound:$e")
        }
    }

    override fun onRestart() {
        super.onRestart()
        reloard()
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
    /*
    val intent = intent
        overridePendingTransition(0, 0)
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
        //前回の状態をセーブしているから呼び出す！
        intent.putExtra(BEFORE_FRAGMENT, beforeSelectedNavButton)
        finish()

        overridePendingTransition(0, 0)
        startActivity(intent)
     */


}
