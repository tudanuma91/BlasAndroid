package com.v3.basis.blas.activity

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.media.AudioManager
import android.media.ToneGenerator
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.view.MenuItem
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.fragment.app.FragmentActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI.setupWithNavController
import com.v3.basis.blas.R
import kotlinx.android.synthetic.main.activity_fixture.*
import androidx.navigation.ui.setupActionBarWithNavController
import com.google.zxing.ResultPoint
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.CompoundBarcodeView
import com.v3.basis.blas.blasclass.rest.BlasRestFixture
import com.v3.basis.blas.ui.ext.showBackKeyForActionBar
import com.v3.basis.blas.ui.fixture.fixture_kenpin.FixtureKenpinFragment
import com.v3.basis.blas.ui.fixture.fixture_return.FixtureReturnFragment
import com.v3.basis.blas.ui.fixture.fixture_takeout.FixtureTakeOutFragment
import org.json.JSONObject

class FixtureActivity : AppCompatActivity() {
    private var messageText:TextView? = null
    private var oldResult:String? =null
    private var vibrator:Vibrator? = null
    private var vibrationEffect = VibrationEffect.createOneShot(300,
        VibrationEffect.DEFAULT_AMPLITUDE
    )
    private var tone: ToneGenerator = ToneGenerator(AudioManager.STREAM_SYSTEM, ToneGenerator.MAX_VOLUME)

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
                if(result.toString() != oldResult) {
                    if (result != null) {
                        //初期化と変数宣言
                        messageText = null
                        messageText = message_text

                        //読み取りを伝えるバイブレーション
                        vibrator = fragm!!.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

                        //Thread.sleep(500)
                        Log.d("QRCode", "処理実行！！")
                        result_text.text = "$result"


                        //ひとつ前のQRコードをこのQRコードにする。連続読み取りを避けるため。
                        oldResult = result.toString()

                        var payload2 = mapOf(
                            "token" to token,
                            "project_id" to projectId,
                            "serial_number" to "${result}"
                        )

                        //restで更新する処理
                        when(type){
                            "kenpin"->{
                                FixtureKenpinFragment().callOnPouse()
                                BlasRestFixture("kenpin", payload2, ::success, ::error).execute()
                            }
                            "takeout"->{
                                FixtureTakeOutFragment().callOnPouse()
                                BlasRestFixture("takeout",payload2, ::success, ::error).execute()
                            }
                            "return"->{
                                FixtureReturnFragment().callOnPouse()
                                BlasRestFixture("rtn",payload2, ::success, ::error).execute()
                            }
                        }

                        Log.d("OK", "終了")
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
        vibrationEffect = VibrationEffect.createOneShot(300, VibrationEffect.DEFAULT_AMPLITUDE)
        vibrator!!.vibrate(vibrationEffect)
        tone.startTone(ToneGenerator.TONE_DTMF_S,200)
        Log.d("OK","作成完了")
        messageText!!.setTextColor(Color.GREEN)
        messageText!!.text = "QRコードを読み取りました"
    }

    /**
     * rest失敗時の処理
     */
    fun error(errorCode: Int){
        vibrationEffect = VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE)
        vibrator!!.vibrate(vibrationEffect)
        tone.startTone(ToneGenerator.TONE_CDMA_ONE_MIN_BEEP,200)
        Log.d("NG","作成失敗")
        Log.d("errorCorde","${errorCode}")
        messageText!!.setTextColor(Color.RED)
        messageText!!.text = "すでに登録済のQRコードです"
    }

}
