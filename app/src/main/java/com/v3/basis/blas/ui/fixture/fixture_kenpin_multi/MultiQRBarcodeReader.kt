package com.v3.basis.blas.ui.fixture.fixture_kenpin_multi

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.media.AudioManager
import android.media.Image
import android.media.ToneGenerator
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.util.Size
import android.view.Surface
import android.widget.ImageView
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.core.Camera
import androidx.camera.core.TorchState.OFF
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.util.valueIterator
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import com.google.android.gms.vision.Frame
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import com.v3.basis.blas.R
import io.reactivex.FlowableEmitter
import io.reactivex.FlowableOnSubscribe
import java.io.ByteArrayOutputStream
import java.lang.RuntimeException
import java.util.concurrent.Executor

class MultiQrBarcodeReader(val activity: FragmentActivity, var preview1: androidx.camera.view.PreviewView, val imageView: ImageView?): FlowableOnSubscribe<String> {
    private var preview: Preview? = null
    private var imageCapture: ImageCapture? = null
    private var camera: Camera? = null
    private var imageAnalyzer: ImageAnalysis? = null
    private var barcodeValues: MutableMap<String, Int> = mutableMapOf()
    private var detector: BarcodeDetector
    private var appExecutor: Executor
    lateinit var vibrationEffect : VibrationEffect
    private var tone : ToneGenerator? = null
    private var vibrator : Vibrator? = null
    private var barCodeCount = 0
    //RXKotlin対応
    private lateinit var flowableEmitter: FlowableEmitter<String>

    private val imageAnalizer = object : ImageAnalysis.Analyzer {
        @SuppressLint("UnsafeExperimentalUsageError")
        override fun analyze(imageProxy: ImageProxy) {
            //imageをビットマップに変換する
            var dispBitmap = imageProxy.image?.let { toBitmap(it) }

            //ここでbitmapに何か書き込みたい
            var paint = Paint()
            paint.setColor(Color.RED)
            paint.style = Paint.Style.STROKE

            dispBitmap?.let {
                val r = barcodeDetect(it)
                imageView?.setImageBitmap(it)
            }

            imageProxy.close()   //closeを呼び出すと次のフレームを取得する
        }
    }

    init {
        var barCodeType = loadConfig(activity.applicationContext)
        detector = BarcodeDetector.Builder(activity.applicationContext)
            .setBarcodeFormats(barCodeType)
            .build()

        appExecutor = ContextCompat.getMainExecutor(activity.applicationContext)
        //バイブレーションの設定
        try{
            vibrator = activity.applicationContext.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibrationEffect = VibrationEffect.createOneShot(30, VibrationEffect.DEFAULT_AMPLITUDE)
        }
        catch(e: RuntimeException) {
            vibrator = null
            Toast.makeText(activity.applicationContext, "バイブレーションを使用できません", Toast.LENGTH_SHORT).show()
        }

        //ビープ音の設定
        try{
            tone = ToneGenerator(AudioManager.STREAM_SYSTEM, ToneGenerator.MAX_VOLUME)
        }
        catch(e: RuntimeException) {
            tone = null
            Toast.makeText(activity.applicationContext, "ビープ音を使用できません", Toast.LENGTH_SHORT).show()
        }

        bindCameraUseCases()
    }

    override fun subscribe(emitter: FlowableEmitter<String>) {
        flowableEmitter = emitter
    }

    private fun toBitmap(image: Image): Bitmap {
        val planes = image.planes
        val yBuffer = planes[0].buffer
        val uBuffer = planes[1].buffer
        val vBuffer = planes[2].buffer

        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()

        var nv21 = ByteArray(ySize + uSize + vSize)

        yBuffer.get(nv21, 0, ySize)
        vBuffer.get(nv21, ySize, vSize)
        uBuffer.get(nv21, ySize + vSize, uSize)

        val yuvImage = YuvImage(nv21, ImageFormat.NV21, image.width, image.height, null)
        val out = ByteArrayOutputStream()

        yuvImage.compressToJpeg(Rect(0, 0, yuvImage.width, yuvImage.height), 100, out)
        val imageBytes = out.toByteArray()
        var bmp = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)

        var mat: Matrix = Matrix()
        mat.postRotate(90.0f)
        return Bitmap.createBitmap(bmp, 0, 0, bmp.width, bmp.height, mat, true)
    }


    private fun bindCameraUseCases() {
        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build()

        val cameraProvider = activity?.applicationContext?.let {context->
            ProcessCameraProvider.getInstance(context)
        }

        cameraProvider?.addListener(Runnable {
            val cameraProvider =
                cameraProvider.get()

            val size: Size = Size(720, 1280)
            preview = Preview.Builder()
                //.setTargetAspectRatio(AspectRatio.RATIO_16_9)
                .setTargetRotation(Surface.ROTATION_0)
                .setTargetResolution(size)
                .build()

            preview?.setSurfaceProvider(preview1.createSurfaceProvider(camera?.cameraInfo))

            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                //.setTargetAspectRatio(AspectRatio.RATIO_16_9)
                .setTargetResolution(size)
                .setTargetRotation(Surface.ROTATION_0)
                .build()

            imageAnalyzer = ImageAnalysis.Builder()
                //.setTargetAspectRatio(AspectRatio.RATIO_16_9)
                .setTargetRotation(Surface.ROTATION_0)
                .setTargetResolution(size)
                .build()

            imageAnalyzer?.setAnalyzer(appExecutor, imageAnalizer)


            cameraProvider.unbindAll()

            try {
                camera = cameraProvider.bindToLifecycle(
                    activity as LifecycleOwner, cameraSelector,
                    preview, imageCapture, imageAnalyzer
                )
            } catch(exc: Exception) {
                Log.d("konishi", exc.message)
            }
        }, appExecutor)
    }

    /**
     * 設定画面の値を読み込む
     */
    fun loadConfig(context: Context?):Int {
        var barCodeType = 0
        var prefs = context?.getSharedPreferences(context.getString(R.string.BlasAppConfig), Context.MODE_PRIVATE)

        if(prefs != null) {
            if (prefs.getBoolean("QRCode", false)) {
                barCodeType = barCodeType or Barcode.QR_CODE
            }

            if (prefs.getBoolean("EAN", true)) {
                barCodeType = barCodeType or Barcode.EAN_8 or Barcode.EAN_13
            }

            if (prefs.getBoolean("CODE", true)) {
                barCodeType = barCodeType + Barcode.CODE_39 or Barcode.CODE_93 or Barcode.CODE_128
            }

            if (prefs.getBoolean("ITF", true)) {
                barCodeType = barCodeType or Barcode.ITF
            }

            if (prefs.getBoolean("UPC", false)) {
                barCodeType = barCodeType or Barcode.UPC_A or Barcode.UPC_E
            }

            if (prefs.getBoolean("PDF417", false)) {
                barCodeType = barCodeType or Barcode.PDF417
            }

            if (prefs.getBoolean("AZTEC", false)) {
                barCodeType = barCodeType or Barcode.AZTEC
            }
            if (prefs.getBoolean("CODABAR", true)) {
                barCodeType = barCodeType or Barcode.CODABAR
            }
        }
        else {
            //デフォルトの設定
            barCodeType = Barcode.EAN_13 or
                    Barcode.EAN_8 or
                    Barcode.ITF or
                    Barcode.CODE_39 or
                    Barcode.CODE_93 or
                    Barcode.CODE_128 or
                    Barcode.CODABAR
        }

        return barCodeType
    }

    private fun barcodeDetect(bitmap: Bitmap): Bitmap? {
        val frame = Frame.Builder().setBitmap(bitmap).build()
        val barcodes = detector.detect(frame)

        var paint = Paint()
        paint.strokeWidth = 6.0f
        paint.style = Paint.Style.STROKE

        var fontPaint = Paint()
        fontPaint.strokeWidth = 1.0f
        fontPaint.textSize = 40.0f

        val c = Canvas(bitmap)

        for (r in barcodes.valueIterator()) {
            val rect = r.boundingBox
            val value = r.rawValue
            if(barcodeValues.contains(value)) {
                // 既に存在している
                var v = 0
                barcodeValues[value]?.let {
                    v = it
                }
                barcodeValues[value] = v + 1
            }
            else {
                paint.setColor(Color.argb(255, 255, 0, 0))
                fontPaint.setColor(Color.argb(255, 255, 0, 0))
                barcodeValues[value] = 1
            }
            var barcodeCount = barcodeValues[value]
            var color:Int = Color.argb(255, 0, 255, 0)
            barcodeCount?.let {
                if(it == 1) {
                    color = Color.argb(255, 255, 0, 0)
                }
                else if(it < 5) {
                    color = Color.argb(255, 255, 255, 0)
                }
                else if(it == 5) {
                    try {
                        //5回同じ値が読めたら、誤検出ではないと判断して保存する。
                        //insertSerial(value)
                        barCodeCount++
                        flowableEmitter.onNext(value)
                    }
                    catch (e:java.lang.Exception) {
                        Log.d("konishi", e.message)
                    }
                    tone?.startTone(ToneGenerator.TONE_PROP_BEEP)
                    vibrator?.vibrate(vibrationEffect)
                }
            }

            paint.setColor(color)
            fontPaint.setColor(color)
            c.drawRect(rect, paint)
            c.drawText("${value}", rect.left.toFloat(), rect.top.toFloat(), fontPaint)
        }

        fontPaint.setColor(Color.argb(255, 255, 0, 255))
        c.drawText("読み込んだ数:${barCodeCount}", 10.0f, 60.0f, fontPaint)

        return bitmap
    }

    fun unbindAll() {
        val cameraProvider = activity.applicationContext.let {context->
            ProcessCameraProvider.getInstance(context)
        }
        cameraProvider?.get()?.unbindAll()
    }

    fun enableTorch() {
        if(camera?.cameraInfo?.torchState?.value == OFF) {
            camera?.cameraControl?.enableTorch(true)
        }
        else {
            camera?.cameraControl?.enableTorch(false)
        }

    }
}