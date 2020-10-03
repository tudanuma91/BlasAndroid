package com.v3.basis.blas.ui.item.item_drawing_search

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.v3.basis.blas.blasclass.component.DrawingImageComponent
import com.v3.basis.blas.blasclass.db.drawing.DrawingsController
import com.v3.basis.blas.blasclass.rest.BlasRestDrawing


class DrawingSearchViewModelFactory(
    private val context: Context,
    private val token: String,
    private val projectId: String
): ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T = DrawingSearchViewModel(
        context,
        token,
        projectId
    ) as T
}

class DrawingSearchViewModel(
    var context: Context,
    var token: String,
    var projectId: String
) : ViewModel() {
    private val TAG = "DrawingSearchViewModel"
    private val unselectedString = "----------"

    private val drawingsController: DrawingsController = DrawingsController(context, projectId)
    private val selectedCategory = MutableLiveData<DrawingCategory>()
    private val selectedSubCategory = MutableLiveData<DrawingSubCategory>()
    private val selectedDrawing = MutableLiveData<Drawing>()

    private val categories: MutableLiveData<List<DrawingCategory>> by lazy {
        MutableLiveData<List<DrawingCategory>>().also {
            loadCategories(it)
        }
    }

    private val subCategories: MutableLiveData<List<DrawingSubCategory>> by lazy {
        MutableLiveData<List<DrawingSubCategory>>().also {
            loadSubCategories(it)
        }
    }

    private val drawings: MutableLiveData<List<Drawing>> by lazy {
        MutableLiveData<List<Drawing>>().also {
            loadDrawings(it)
        }
    }

    private val drawingImage: MutableLiveData<DrawingImage> by lazy {
        MutableLiveData<DrawingImage>().also {
            loadDrawingImage(it)
        }
    }

    fun getCategories(): LiveData<List<DrawingCategory>> {
        return categories
    }

    fun getSubCategories(): LiveData<List<DrawingSubCategory>> {
        return subCategories
    }

    fun getDrawings(): LiveData<List<Drawing>> {
        return drawings
    }

    fun getDrawingImage(): LiveData<DrawingImage> {
        return drawingImage
    }

    fun selectCategory(category: DrawingCategory?) {
        selectedCategory.value = category
        selectedSubCategory.value = null
        selectedDrawing.value = null

        loadSubCategories(subCategories)
        loadDrawings(drawings)
    }

    fun selectSubCategory(subCategory: DrawingSubCategory?) {
        selectedSubCategory.value = subCategory
        selectedDrawing.value = null

        loadDrawings(drawings)
    }

    fun selectDrawing(drawing: Drawing?) {
        selectedDrawing.value = drawing;

        loadDrawingImage(drawingImage)
    }

    private fun loadCategories(data: MutableLiveData<List<DrawingCategory>>) {
        Log.d(TAG, "loadCategories: ")
        var list = arrayListOf<DrawingCategory>()
        // カテゴリ無しを追加
        list.plusAssign(DrawingCategory(0, unselectedString))
        // DBからカテゴリをロード
        drawingsController.getCategories().forEach {
            Log.d(TAG, "loadCategories: ${it.toString()}")
            val id = it.get("drawing_category_id")?.toInt() ?: 0
            val name = it.get("name")?.toString() ?: ""
            list.plusAssign(DrawingCategory(id, name))
        }
        data.postValue(list.toList())
    }

    private fun loadSubCategories(data: MutableLiveData<List<DrawingSubCategory>>) {
        Log.d(TAG, "loadSubCategories: ")
        var list = arrayListOf<DrawingSubCategory>()
        // サブカテゴリ無しを追加
        list.plusAssign(DrawingSubCategory(0, unselectedString))
        // DBからサブカテゴリをロード
        selectedCategory.value?.let { drawingCategory ->
            drawingsController.getSubCategories(drawingCategory.id).forEach {
                Log.d(TAG, "loadSubCategories: ${it.toString()}")
                val id = it.get("drawing_sub_category_id")?.toInt() ?: 0
                val name = it.get("name")?.toString() ?: ""
                list.plusAssign(DrawingSubCategory(id, name))
            }
        }
        data.postValue(list.toList())
    }

    private fun loadDrawings(data: MutableLiveData<List<Drawing>>) {
        Log.d(TAG, "loadDrawings: ")
        var list = arrayListOf<Drawing>()
        // 図面無しを追加
        list.plusAssign(Drawing(0, unselectedString, "", 0))
        // DBから図面をロード
        drawingsController.getDrawings(
            selectedCategory.value?.id ?: 0,
            selectedSubCategory.value?.id ?: 0
        ).forEach {
            Log.d(TAG, "loadDrawings: ${it.toString()}")
            val id = it.get("drawing_id")?.toInt() ?: 0
            val name = it.get("name")?.toString() ?: ""
            val drawingFile = it.get("filename")?.toString() ?: ""
            list.plusAssign(Drawing(id, name, drawingFile, 0))
        }
        data.postValue(list.toList())
    }

    private fun loadDrawingImage(data: MutableLiveData<DrawingImage>) {
        selectedDrawing.value?.let {
            val spots = getSpots()
            // 対象のデータがローカルにあるか確認
            val bmp = DrawingImageComponent().readBmpFromLocal(
                context,
                it.id.toString(),
                it.drawingFile
            )
            if (bmp != null) {
                Log.d(
                    TAG,
                    "loadDrawingImage: load success from LOCAL(projectID=${it.id} filename=${it.drawingFile})"
                )
                data.postValue(DrawingImage(bmp,spots))
                return@let
            }
            val payload = mapOf("token" to token, "drawing_id" to it.id.toString())
            BlasRestDrawing(
                "view",
                payload,
                funcSuccess = { drawingResponse ->
                    Log.d(TAG, "loadDrawingImage: REMOTE SUCCESS")
                    val decodedString: ByteArray = Base64.decode(drawingResponse.records[0].Drawing.image, Base64.DEFAULT)
                    val bmp =
                        BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
                    data.postValue(DrawingImage(bmp,spots))
                },
                funcError = { i: Int, i1: Int ->
                    Log.d(TAG, "loadDrawingImage: REMOTE ERROR")
                }
            ).execute()
        }
    }

    private fun getSpots(): List<DrawingSpot> {
        Log.d(TAG, "loadSpots: ")
        var list = arrayListOf<DrawingSpot>()
        // DBからラベルをロード
        drawingsController.getSpots(
            selectedDrawing.value?.id ?: 0
        ).forEach {
            Log.d(TAG, "loadSpots: ${it.toString()}")
            val name = it.get("name")?.toString() ?: ""
            val color = it.get("shape_color")?.toString() ?: ""
            val x = it.get("abscissa")?.toFloat() ?: 0.0f
            val y = it.get("ordinate")?.toFloat() ?: 0.0f
            list.plusAssign(DrawingSpot(name, color, x.toInt(), y.toInt()))
        }
        return list
    }

    override fun onCleared() {
        super.onCleared()
    }
}
