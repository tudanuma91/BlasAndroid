package com.v3.basis.blas.activity

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.v3.basis.blas.R
import com.v3.basis.blas.blasclass.log.BlasLog
import com.v3.basis.blas.databinding.ActivityDrawingBinding
import com.v3.basis.blas.databinding.ViewLabelBinding
import com.v3.basis.blas.ui.item.item_drawing_search.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.activity_drawing.*
import kotlin.math.abs
import kotlin.math.min

/**
 * 図面検索のアクティビティクラス
 */
class DrawingSearchActivity : AppCompatActivity() {
    companion object {
        const val SEARCH_FREEWORD: String = "search_freeword"
    }

    private lateinit var bind: ActivityDrawingBinding
    private lateinit var progressBar: ProgressBar

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

        // プログレスバー
        progressBar = findViewById<ProgressBar>(R.id.progressBarLoadDrawing)
        progressBar.visibility = android.widget.ProgressBar.INVISIBLE

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

        bind.photoView.setOnSingleFlingListener { _, _, velocityX, velocityY ->
            val nextPos = drawingSpinner.selectedItemPosition + 1
            val prevPos = drawingSpinner.selectedItemPosition - 1
            val left = abs(velocityX) > abs(velocityY) && velocityX > 0
            val right = abs(velocityX) > abs(velocityY) && velocityX <= 0
            if (right && nextPos < drawings.size) {
                drawingSpinner.setSelection(nextPos)
            } else if (left && prevPos >= 0) {
                drawingSpinner.setSelection(prevPos)
            }
            false
        }

        //  スピナーの初期化と項目選択時のイベント設定
        setSpinners()

        //  カテゴリー取得コールバック
        categoryEvent
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy { list ->
                BlasLog.trace("I", "categoryEvent: start ")
                categories.clear()
                categories.addAll(list)
                categorySpinner.adapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, list.map { it.name })

                // 画面回転時の選択状態をViewModelから取得する
                val selectedCategory = mViewModel.selectedCategory.value
                selectedCategory?.let {selected ->
                    categories.forEachIndexed { index, category ->
                        if (selected.id == category.id) {
                            categorySpinner.setSelection(index)
                        }
                    }
                }
                BlasLog.trace("I", "categoryEvent: end ")
            }
            .addTo(disposables)

        //  サブカテゴリー取得コールバック
        subCategoryEvent
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy { list ->
                BlasLog.trace("I", "subCategoryEvent: start ")
                subCategories.clear()
                subCategories.addAll(list)
                subCategorySpinner.adapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, list.map { it.name })

                // 画面回転時の選択状態をViewModelから取得する
                val selectedSubCategory = mViewModel.selectedSubCategory.value
                selectedSubCategory?.let {selected ->
                    subCategories.forEachIndexed { index, subCategory  ->
                        if (selected.id == subCategory.id) {
                            subCategorySpinner.setSelection(index)
                        }
                    }
                }
                BlasLog.trace("I", "subCategoryEvent: end ")
            }
            .addTo(disposables)

        //  図面取得コールバック
        drawingsEvent
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy { list ->
                BlasLog.trace("I", "drawingsEvent: start ")
                drawings.clear()
                drawings.addAll(list)
                drawingSpinner.adapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, list.map { it.name })

                // 画面回転時の選択状態をViewModelから取得する
                val selectedDrawing = mViewModel.selectedDrawing.value
                selectedDrawing?.let {selected ->
                    drawings.forEachIndexed { index, drawing ->
                        if (selected.id == drawing.id) {
                            drawingSpinner.setSelection(index)
                        }
                    }
                }
                BlasLog.trace("I", "drawingsEvent: end ")
            }
            .addTo(disposables)

        //  図面画像取得コールバック
        drawingImageEvent
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy { drawingImage ->
                BlasLog.trace("I", "drawingImageEvent: start ")
                progressBar.visibility = android.widget.ProgressBar.INVISIBLE
                // ラベルデータのセット
                // 端末上に表示されるサイズとビットマップのサイズからラベルに適するスケールを計算
                val scaleOfOriginalImage = min(bind.photoView.width.toFloat() / drawingImage.bitmap.width, bind.photoView.height.toFloat() / drawingImage.bitmap.height)
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

                    if (it.color == "yellow") {
                        // 黄色のラベルの文字色は黒色
                        txt.label.setTextColor(Color.BLACK)
                    }
                    txt.label.maxEms = 10   // 最大10文字まで表示
                    txt.label.ellipsize = TextUtils.TruncateAt.END  // ラベルが11文字以上の時は末尾に「...」を表示
                    txt.label.setSingleLine()   // 1行表示に指定

                    it.layout = txt.parent
                    bind.labelContainer.addView(txt.root)
                }
                // 画像データのセット
                bind.photoView.setImageBitmap(drawingImage.bitmap)
                BlasLog.trace("I", "drawingImageEvent: end ")
            }
            .addTo(disposables)

        // ViewModelのカテゴリに対する監視
        mViewModel.getCategories().observe(this, androidx.lifecycle.Observer { categories ->
            categoryEvent.onNext(categories)
        })

        // ViewModelのサブカテゴリに対する監視
        mViewModel.getSubCategories().observe(this, androidx.lifecycle.Observer { subcategories ->
            subCategoryEvent.onNext(subcategories)
        })

        // ViewModelの図面に対する監視
        mViewModel.getDrawings().observe(this, androidx.lifecycle.Observer { drawings ->
            drawingsEvent.onNext(drawings)
        })

        // ViewModelのエラーメッセージに対する監視
        mViewModel.getError().observe(this, androidx.lifecycle.Observer { error ->
            progressBar.visibility = android.widget.ProgressBar.INVISIBLE
            Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
        })

        // ViewModelの図面画像に対する監視
        mViewModel.getDrawingImage().observe(this, androidx.lifecycle.Observer { drawingImage ->
            if (drawingImage == null) {
                BlasLog.trace("I", "Erase labels and image ")
                progressBar.visibility = android.widget.ProgressBar.INVISIBLE
                this.labels.clear()
                bind.labelContainer.removeAllViews()
                bind.photoView.setImageBitmap(null)
            } else {
                drawingImageEvent.onNext(drawingImage)
            }
        })
    }

    /**
     * カテゴリー、サブカテゴリー、図面名をスピナーにセットする
     */
    private fun setSpinners() {

        //  都道府県
        categorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                BlasLog.trace("I", "Category selected: position=$position ")
                mViewModel.selectCategory(categories[position])
            }
        }

        //  施設名、病院名
        subCategorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                BlasLog.trace("I", "SubCategory selected: position=$position ")
                mViewModel.selectSubCategory(subCategories[position])
            }
        }

        //  図面名
        drawingSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                BlasLog.trace("I", "Drawing selected: position=$position ")
                progressBar.visibility = android.widget.ProgressBar.VISIBLE
                val drawing = drawings[position]
                mViewModel.selectDrawing(drawing)
            }
        }
    }

    /**
     * 選択したラベルの情報を呼び出し元のアクティビティに渡し、このアクティビティを終了する。
     *
     * @param model ユーザーに選択されたラベルモデルオブジェクト
     */
    fun clickLabel(model: LabelModel) {
        BlasLog.trace("I", "Label selected: name=${model.name} ")
        val intent = Intent()
        intent.putExtra(SEARCH_FREEWORD, model.name)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    /**
     * ラベルモデルクラス
     */
    inner class LabelModel {
        var layout: View? = null
        var name: String = ""
        var color: String = ""
        var x: Int = 0
        var y: Int = 0
    }
}


