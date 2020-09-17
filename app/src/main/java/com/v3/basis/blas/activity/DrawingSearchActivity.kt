package com.v3.basis.blas.activity

import android.graphics.RectF
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ObservableField
import androidx.databinding.ObservableInt
import com.v3.basis.blas.R
import com.v3.basis.blas.databinding.ActivityDrawingBinding
import kotlinx.android.synthetic.main.activity_drawing.*


//ログイン画面を表示する処理
class DrawingSearchActivity : AppCompatActivity() {

    private lateinit var bind: ActivityDrawingBinding
    private val topMargin = 200
    private val leftMargin = 300
    private var leftDiff: Double = 0.0
    private var topDiff: Double = 0.0
    var correction: Double = 0.0

    private var scale: Float = 0.0f

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
//            change(r)
        }
//        photoView.setOnSingleFlingListener { e1, e2, velocityX, velocityY ->
//            Log.d("photoview", "Filing")
//            false
//        }
//        photoView.setOnViewDragListener { dx, dy ->
//            Log.d("photoviewXY", "$dx $dy")
//            Log.d("photoviewXY", "${photoView.attacher.displayRect}")
//            val r = photoView.attacher.displayRect
////            changeHV(r)
//        }
//        val correction = 1.0 / resources.displayMetrics.density
        photoView.setOnMatrixChangeListener {

//            if (scale != photoView.attacher.scale) {
//
//                scale = photoView.attacher.scale
//                val params = bind.label1.layoutParams as? ViewGroup.MarginLayoutParams
//                params?.topMargin = (topMargin * scale + it.top).toInt()
//                params?.leftMargin = (leftMargin * scale + it.left).toInt()
//                bind.label1.layoutParams = params
//            }
            scale = photoView.attacher.scale
            val params = bind.label1.layoutParams as? ViewGroup.MarginLayoutParams
            params?.topMargin = (topMargin * scale + it.top).toInt()
            params?.leftMargin = (leftMargin * scale + it.left).toInt()
            bind.label1.layoutParams = params

//            if (scale != photoView.attacher.scale) {
//                change(it)
//                scale = photoView.attacher.scale
//            } else {
//                changeHV(it)
//            }
//            Log.d("photoview", "Matrix: $it")
//            val params = bind.label1.layoutParams as? ViewGroup.MarginLayoutParams
//            params?.topMargin = topMargin + (it.top * correction).toInt()
//            params?.leftMargin = leftMargin + (it.left * correction).toInt()
//            bind.label1.layoutParams = params
        }

        scale = photoView.attacher.scale

//        changeHV(photoView.attacher.displayRect)

        correction = 1.0 / resources.displayMetrics.density

        backButton.setOnClickListener {
            finish()
        }

        val tdata = arrayOf("和歌山", "大阪", "東京")
        categorySpinner.adapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, tdata)
        categorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val tdata = arrayOf("病院１", "病院２", "病院３")
                subCategorySpinner.adapter = ArrayAdapter<String>(this@DrawingSearchActivity, android.R.layout.simple_spinner_item, tdata)
            }
        }
        subCategorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val tdata = arrayOf("図面１", "図面２", "図面３")
                drawingSpinner.adapter = ArrayAdapter<String>(this@DrawingSearchActivity, android.R.layout.simple_spinner_item, tdata)
            }
        }
        drawingSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                //TODO　図面更新
            }
        }
        label1.setOnClickListener {
            finish()
        }
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

    inner class LabelModel {
        val name: ObservableField<String> = ObservableField("")
        val color: ObservableInt = ObservableInt(0)
        var topMargin: Int = 0
        var leftMargin: Int = 0
    }
}


