package com.v3.basis.blas.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ObservableField
import com.v3.basis.blas.R
import com.v3.basis.blas.databinding.ActivityDrawingBinding
import com.v3.basis.blas.databinding.ViewLabelBinding
import com.v3.basis.blas.ui.item.item_drawing_search.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.activity_drawing.*


class DrawingSearchActivity : AppCompatActivity() {
    companion object {
        val SEARCH_FREEWORD: String = "search_freeword"
    }

    private lateinit var bind: ActivityDrawingBinding
    private val topMargin = 200
    private val leftMargin = 300

    private var scale: Float = 0.0f // 図面のズームインアウトに使用するスケール変数
    private val disposables: CompositeDisposable = CompositeDisposable()

    private val categoryEvent: PublishSubject<List<DrawingCategory>> = PublishSubject.create()
    private val subCategoryEvent: PublishSubject<List<DrawingSubCategory>> = PublishSubject.create()
    private val drawingsEvent: PublishSubject<List<Drawing>> = PublishSubject.create()
    private val drawingImageEvent: PublishSubject<DrawingImage> = PublishSubject.create()

    private val categories: MutableList<DrawingCategory> = mutableListOf()
    private val subCategories: MutableList<DrawingSubCategory> = mutableListOf()
    private val drawings: MutableList<Drawing> = mutableListOf()
    private val labels: MutableList<LabelModel> = mutableListOf()

    private val mViewModel: DrawingSearchViewModel by viewModels { DrawingSearchViewModelFactory(
        this, intent.extras?.getString("token") ?: "",intent.extras?.getString("project_id") ?: "") }

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

                // 画面回転時の選択状態をViewModelから取得する
                val selectedCategory = mViewModel.selectedCategory.value
                selectedCategory?.let {selected ->
                    categories.forEachIndexed { index, category ->
                        if (selected.id == category.id) {
                            categorySpinner.setSelection(index)
                        }
                    }
                }
            }
            .addTo(disposables)

        //  サブカテゴリー取得コールバック
        subCategoryEvent
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy {
                subCategories.clear()
                subCategories.addAll(it)
                subCategorySpinner.adapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, it.map { it.name })

                // 画面回転時の選択状態をViewModelから取得する
                val selectedSubCategory = mViewModel.selectedSubCategory.value
                selectedSubCategory?.let {selected ->
                    subCategories.forEachIndexed { index, subCategory  ->
                        if (selected.id == subCategory.id) {
                            subCategorySpinner.setSelection(index)
                        }
                    }
                }
            }
            .addTo(disposables)

        //  図面取得コールバック
        drawingsEvent
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy {
                drawings.clear()
                drawings.addAll(it)
                drawingSpinner.adapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, it.map { it.name })

                // 画面回転時の選択状態をViewModelから取得する
                val selectedDrawing = mViewModel.selectedDrawing.value
                selectedDrawing?.let {selected ->
                    drawings.forEachIndexed { index, drawing ->
                        if (selected.id == drawing.id) {
                            drawingSpinner.setSelection(index)
                        }
                    }
                }
            }
            .addTo(disposables)

        //  図面画像取得コールバック
        drawingImageEvent
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy { drawingImage ->
                // ラベルデータのセット
                val scaleOfOriginalImage = bind.photoView.width.toFloat() / drawingImage.bitmap.width
                Log.d("DEBUG", "drawingImageEvent: $scaleOfOriginalImage ")
                bind.labelContainer.removeAllViews()

                val labels = drawingImage.spots.map {
                    LabelModel().apply {
                        this.name = it.name
                        this.color = it.color
                        this.x = (it.x * scaleOfOriginalImage).toInt()
                        this.y = (it.y * scaleOfOriginalImage).toInt()
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
                // 画像データのセット
                bind.photoView.setImageBitmap(drawingImage.bitmap)
            }
            .addTo(disposables)

        mViewModel.getCategories().observe(this, androidx.lifecycle.Observer { categories ->
            Log.d("DEBUG", "onCreate: mViewModel Update UI")
            categoryEvent.onNext(categories)
        })

        mViewModel.getSubCategories().observe(this, androidx.lifecycle.Observer { subcategories ->
            Log.d("DEBUG", "onCreate: mViewModel Update UI")
            subCategoryEvent.onNext(subcategories)
        })

        mViewModel.getDrawings().observe(this, androidx.lifecycle.Observer { drawings ->
            Log.d("DEBUG", "onCreate: mViewModel Update UI")
            drawingsEvent.onNext(drawings)
        })

        mViewModel.getDrawingImage().observe(this, androidx.lifecycle.Observer { drawingImage ->
            Log.d("DEBUG", "onCreate: mViewModel Image Load")
            drawingImageEvent.onNext(drawingImage)
        })
    }

    //  カテゴリー、サブカテゴリー、図面名をスピナーにセットする
    private fun setSpinners() {

        //  都道府県
        categorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                mViewModel.selectCategory(categories[position])
            }
        }

        //  施設名、病院名
        subCategorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                mViewModel.selectSubCategory(subCategories[position])
            }
        }

        //  図面名
        drawingSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val drawing = drawings[position]
                mViewModel.selectDrawing(drawing)
            }
        }
    }

    fun clickLabel(model: LabelModel) {
        val intent = Intent()
        intent.putExtra(SEARCH_FREEWORD, model.name)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    inner class LabelModel {
        var layout: View? = null
        var name: String = ""
        var color: String = ""
        var x: Int = 0
        var y: Int = 0
    }
}


