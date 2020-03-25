package com.v3.basis.blas.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.AudioManager
import android.media.ToneGenerator
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import com.google.zxing.ResultPoint
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.CompoundBarcodeView
import com.v3.basis.blas.R
import com.v3.basis.blas.blasclass.rest.BlasRestFixture
import com.v3.basis.blas.ui.fixture.fixture_kenpin.FixtureKenpinFragment
import com.v3.basis.blas.ui.fixture.fixture_return.FixtureReturnFragment
import com.v3.basis.blas.ui.fixture.fixture_takeout.FixtureTakeOutFragment
import com.v3.basis.blas.ui.qr.QrFragment
import org.json.JSONObject

class QRActivity : AppCompatActivity() {
    private var messageText:TextView? = null
    private var oldResult:String? =null
    private var vibrator:Vibrator? = null
    private var vibrationEffect = VibrationEffect.createOneShot(300,
        VibrationEffect.DEFAULT_AMPLITUDE
    )
    private var tone: ToneGenerator = ToneGenerator(AudioManager.STREAM_SYSTEM, ToneGenerator.MAX_VOLUME)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qr)
    }


}
