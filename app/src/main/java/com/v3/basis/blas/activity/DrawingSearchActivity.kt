package com.v3.basis.blas.activity

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toBitmap
import androidx.databinding.DataBindingUtil
import androidx.databinding.ObservableField
import com.v3.basis.blas.R
import com.v3.basis.blas.databinding.ActivityDrawingBinding
import com.v3.basis.blas.databinding.ViewLabelBinding
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.activity_drawing.*


class DrawingSearchActivity : AppCompatActivity() {

    private lateinit var bind: ActivityDrawingBinding
    private val topMargin = 200
    private val leftMargin = 300

    private var scale: Float = 0.0f
    private val disposables: CompositeDisposable = CompositeDisposable()

    data class DrawingCategory(val id: Int, val name: String)
    data class DrawingSubCategory(val id: Int, val name: String)
    data class Drawings(val id: Int, val name: String, val drawingFile: String, val resId: Int/* <- テスト用 */)
    data class DrawingSpots(val name: String, val color: String, val x: Int, val y: Int)

    private val categoryEvent: PublishSubject<List<DrawingCategory>> = PublishSubject.create()
    private val subCategoryEvent: PublishSubject<List<DrawingSubCategory>> = PublishSubject.create()
    private val drawingsEvent: PublishSubject<List<Drawings>> = PublishSubject.create()
    private val spotsEvent: PublishSubject<List<DrawingSpots>> = PublishSubject.create()

    private val categories: MutableList<DrawingCategory> = mutableListOf()
    private val subCategories: MutableList<DrawingSubCategory> = mutableListOf()
    private val drawings: MutableList<Drawings> = mutableListOf()
    private val spots: MutableList<DrawingSpots> = mutableListOf()
    private val labels: MutableList<LabelModel> = mutableListOf()

    private var currentDrawing: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_drawing)

        bind = DataBindingUtil.setContentView(this, R.layout.activity_drawing)
        //  最大１６倍！！
        bind.photoView.attacher.maximumScale = 16.0f
        //  初期倍率　= 1.0
        scale = bind.photoView.attacher.scale

        //　左上の戻るボタン
        backButton.setOnClickListener {
            finish()
        }

        //  画像の大きさが変わったら呼ばれるイベント
        bind.photoView.setOnMatrixChangeListener { rect ->

            scale = photoView.attacher.scale
            labels.forEach { model ->
                model.layout?.also { textView ->
                    val params = textView.layoutParams as? ViewGroup.MarginLayoutParams
                    params?.topMargin = (model.y * scale + rect.top).toInt()
                    params?.leftMargin = (model.x * scale + rect.left).toInt()
                    textView.layoutParams = params
                }
            }
        }

        bind.photoView.setOnSingleFlingListener { e1, e2, velocityX, velocityY ->
            val nextPos = drawingSpinner.selectedItemPosition + 1
            val prevPos = drawingSpinner.selectedItemPosition - 1
            val left = Math.abs(velocityX) > Math.abs(velocityY) && velocityX > 0
            val right = Math.abs(velocityX) > Math.abs(velocityY) && velocityX <= 0
            if (right && nextPos < drawings.size) {
                drawingSpinner.setSelection(nextPos)
            } else if (left && prevPos >= 0) {
                drawingSpinner.setSelection(prevPos)
            }
            false
        }

        //  スピナーの項目選択時のイベント設定
        setSpinners()

        //  カテゴリー取得コールバック
        categoryEvent
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy {
                categories.clear()
                categories.addAll(it)
                categorySpinner.adapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, it.map { it.name })
            }
            .addTo(disposables)

        //  サブカテゴリー取得コールバック
        subCategoryEvent
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy {
                subCategories.clear()
                subCategories.addAll(it)
                subCategorySpinner.adapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, it.map { it.name })
            }
            .addTo(disposables)

        //  図面取得コールバック
        drawingsEvent
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy {
                drawings.clear()
                drawings.addAll(it)
                drawingSpinner.adapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, it.map { it.name })
            }
            .addTo(disposables)

        //  ラベル取得コールバック
        spotsEvent
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy {
                bind.labelContainer.removeAllViews()

                val labels = it.map {
                    LabelModel().apply {
                        this.name.set(it.name)
                        this.color.set(it.color)
                        this.x = it.x
                        this.y = it.y
                    }
                }
                this.labels.clear()
                this.labels.addAll(labels)

                labels.forEach {
                    val txt = DataBindingUtil.inflate<ViewLabelBinding>(layoutInflater, R.layout.view_label, null, false)
                    txt.activity = this
                    txt.model = it
                    it.layout = txt.parent
                    bind.labelContainer.addView(txt.root)
                }
                bind.photoView.setImageBitmap(currentDrawing)
            }
            .addTo(disposables)

        fetchCategory(0)
    }

    //  カテゴリー、サブカテゴリー、図面名をスピナーにセットする
    fun setSpinners() {

        //  都道府県
        categorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                fetchSubCategory(categories[position].id)
            }
        }

        //  施設名、病院名
        subCategorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                fetchDrawings(0, 0, subCategories[position].id)
            }
        }

        //  図面名
        drawingSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

                val item = drawings.get(position)
                fetchSpots(item.id)
                setDrawingImage(item)
            }
        }
    }

    fun clickLabel(model: LabelModel) {
        //TODO ラベル名で検索
        val data = Intent()
        data.putExtra("drawing", model.name)
        setResult(Activity.RESULT_OK, data)
        finish()
    }

    //  カテゴリーを取得
    fun fetchCategory(projectId: Int) {

        //TODO API呼び出しに置き換える"大阪", "東京"
        categoryEvent.onNext(listOf(
            //   テストデータ　↓
            DrawingCategory(1, "東京")
        ))
    }

    //  サブカテゴリーを取得
    fun fetchSubCategory(drawingCategoryId: Int) {

        //TODO API呼び出しに置き換える
        subCategoryEvent.onNext(
            //   テストデータ　↓
            when (drawingCategoryId) {
                1 -> listOf(
                    DrawingSubCategory(1, "徳洲会"),
                    DrawingSubCategory(2, "徳洲会２"),
                    DrawingSubCategory(3, "徳洲会３")
                )
                else -> listOf()
            }
        )
    }

    //  図面を取得
    fun fetchDrawings(projectId: Int, drawingCategoryId: Int, drawingSubCategoryId: Int) {

        //TODO API呼び出しに置き換える
        drawingsEvent.onNext(
            //   テストデータ　↓
            when (drawingSubCategoryId) {
                1 -> listOf(
                    Drawings(1, "1F", "", R.drawable.drawing_sample)
                )
                2 -> listOf(
                    Drawings(2, "1F", "", R.drawable.drawing_sample2),
                    Drawings(4, "2F", "", R.drawable.drawing_sample)
                )
                3 -> listOf(
                    Drawings(3, "1F", "", R.drawable.sample3)
                )
                else -> listOf()
            }
        )
    }

    //  ラベルを取得
    fun fetchSpots(drawingId: Int) {

        //TODO API呼び出しに置き換える
        spotsEvent.onNext(
            //   テストデータ　↓
            when (drawingId) {
                1 -> listOf(
                    DrawingSpots("Label1", "red", 100, 200),
                    DrawingSpots("Label2", "blue", 200, 100)
                )
                2 -> listOf(
                    DrawingSpots("Label10", "green", 300, 150)
                )
                3 -> listOf(
                    DrawingSpots("Label20", "yellow", 300, 180)
                )
                4 -> listOf(
                    DrawingSpots("Label20", "yellow", 300, 180),
                    DrawingSpots("Label1", "red", 100, 200),
                    DrawingSpots("Label2", "blue", 200, 100)
                )
                else -> listOf()
            }
        )
    }

    private fun setDrawingImage(item: Drawings) {

        currentDrawing = applicationContext.resources.getDrawable(item.resId, null).toBitmap()
        //  URL使用時は↓
//                Glide.with(this@DrawingSearchActivity)
//                    .asBitmap()
//                    .load(item.drawingFile)
//                    .into(object : CustomTarget<Bitmap>() {
//                        override fun onLoadCleared(placeholder: Drawable?) {}
//                        override fun onResourceReady(
//                            resource: Bitmap,
//                            transition: Transition<in Bitmap>?
//                        ) {
//                            currentDrawing = resource
//                        }
//                    })
    }

    inner class LabelModel {
        var layout: View? = null
        val name: ObservableField<String> = ObservableField("")
        val color: ObservableField<String> = ObservableField("")
        var x: Int = 0
        var y: Int = 0
    }
}


