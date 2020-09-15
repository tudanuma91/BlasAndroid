package com.v3.basis.blas.activity

import android.graphics.RectF
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.v3.basis.blas.R
import com.v3.basis.blas.databinding.ActivityDrawingBinding
import kotlinx.android.synthetic.main.activity_drawing.*


//ログイン画面を表示する処理
class DrawingSearchActivity : AppCompatActivity() {

    private lateinit var bind: ActivityDrawingBinding
    private val topMargin = 200
    private val leftMargin = 300
    private var _topMargin = 200
    private var _leftMargin = 300
    private var leftDiff: Double = 0.0
    private var topDiff: Double = 0.0
    var correction: Double = 0.0
    var once = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_drawing)

        bind = DataBindingUtil.setContentView(this, R.layout.activity_drawing)
//        bind.topMargin = topMargin
//        bind.leftMargin = leftMargin

        bind.photoView.setImageResource(R.drawable.drawing_sample2)
        bind.photoView.attacher.maximumScale = 16.0f
        photoView.setOnScaleChangeListener { scaleFactor, focusX, focusY ->
            Log.d("photoview", "ScaleChanged: scaleFactor = $scaleFactor")
            Log.d("photoview", "Scale: ${photoView.attacher.scale}")
            val r = photoView.attacher.displayRect
            change(r)
        }
//        photoView.setOnSingleFlingListener { e1, e2, velocityX, velocityY ->
//            Log.d("photoview", "Filing")
//            false
//        }
        photoView.setOnViewDragListener { dx, dy ->
            Log.d("photoviewXY", "$dx $dy")
            Log.d("photoviewXY", "${photoView.attacher.displayRect}")
            val r = photoView.attacher.displayRect
            changeHV(r)
        }
//        val correction = 1.0 / resources.displayMetrics.density
        photoView.setOnMatrixChangeListener {
            //TODO  動きがおかしいので、Matrixイベントで拡大縮小、移動のイベント処理する。前回の縮尺を記憶しておいて、比較してロジック分ける
//            Log.d("photoview", "Matrix: $it")
//            val params = bind.label1.layoutParams as? ViewGroup.MarginLayoutParams
//            params?.topMargin = topMargin + (it.top * correction).toInt()
//            params?.leftMargin = leftMargin + (it.left * correction).toInt()
//            bind.label1.layoutParams = params
        }

        changeHV(photoView.attacher.displayRect)

        correction = 1.0 / resources.displayMetrics.density
    }

    private fun change(r: RectF) {

        val top = r.top * correction
        val left = r.left * correction
        Log.d("photoviewXY", "top: $top, left: $left")

        topDiff = r.top - top
        leftDiff = r.left - left

        val params = bind.label1.layoutParams as? ViewGroup.MarginLayoutParams
        params?.topMargin = topMargin + top.toInt()
        params?.leftMargin = leftMargin + left.toInt()
        bind.label1.layoutParams = params
    }

    private fun changeHV(r: RectF) {

        val params = bind.label1.layoutParams as? ViewGroup.MarginLayoutParams
        params?.topMargin = (topMargin + r.top - topDiff).toInt()
        params?.leftMargin = (leftMargin + r.left - leftDiff).toInt()
//        _topMargin = (_topMargin + dx).toInt()
//        _leftMargin = (_leftMargin + dy).toInt()
//        params?.topMargin = topMargin + _topMargin
//        params?.leftMargin = leftMargin + _leftMargin
        bind.label1.layoutParams = params
    }
}


