package com.v3.basis.blas.ui.item.item_drawing_search

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.v3.basis.blas.blasclass.db.drawing.DrawingsController
import com.v3.basis.blas.blasclass.log.BlasLog
import com.v3.basis.blas.blasclass.rest.BlasRestDrawing

/**
 * DrawingSearchViewModelのカスタムファクトリークラス。
 * このクラスがないとカスタムコンストラクタをもつビューモデルのインスタンスが作れない。
 * @param context コンテキスト
 * @param token 認証トークン
 * @param projectId プロジェクトID
 */
@Suppress("UNCHECKED_CAST")
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

/**
 * 図面検索にかかわるデータの取得・保持を行うビューモデルクラス。
 * 図面検索のアクティビティから参照される。
 * @param context コンテキスト
 * @param token 認証トークン
 * @param projectId プロジェクトID
 */
class DrawingSearchViewModel(
    var context: Context,
    var token: String,
    var projectId: String
) : ViewModel() {
    private val unselectedString = "----------"

    // 図面検索のデータベースコントローラー
    private val drawingsController: DrawingsController = DrawingsController(context, projectId)

    // 選択中の図面カテゴリーを保持
    val selectedCategory = MutableLiveData<DrawingCategory?>()
    // 選択中の図面サブカテゴリーを保持
    val selectedSubCategory = MutableLiveData<DrawingSubCategory>()
    // 選択中の図面を保持
    val selectedDrawing = MutableLiveData<Drawing>()

    // 図面カテゴリ一覧を保持
    private val categories: MutableLiveData<List<DrawingCategory>> by lazy {
        MutableLiveData<List<DrawingCategory>>().also {
            loadCategories(it)
        }
    }

    // 図面サブカテゴリーを保持
    private val subCategories: MutableLiveData<List<DrawingSubCategory>> by lazy {
        MutableLiveData<List<DrawingSubCategory>>().also {
            loadSubCategories(it)
        }
    }

    // 図面一覧を保持
    private val drawings: MutableLiveData<List<Drawing>> by lazy {
        MutableLiveData<List<Drawing>>().also {
            loadDrawings(it)
        }
    }

    // 図面画像を保持
    private val drawingImage: MutableLiveData<DrawingImage> by lazy {
        MutableLiveData<DrawingImage>().also {
            loadDrawingImage(it)
        }
    }

    private val error: MutableLiveData<String> = MutableLiveData<String>()

    /**
     * 図面カテゴリー一覧のデータホルダーを取得する。
     */
    fun getCategories(): LiveData<List<DrawingCategory>> {
        return categories
    }

    /**
     * 図面サブカテゴリー一覧のデータホルダーを取得する。
     */
    fun getSubCategories(): LiveData<List<DrawingSubCategory>> {
        return subCategories
    }

    /**
     * 図面一覧のデータホルダーを取得する。
     */
    fun getDrawings(): LiveData<List<Drawing>> {
        return drawings
    }

    /**
     * 図面画像のデータホルダーを取得する。
     */
    fun getDrawingImage(): LiveData<DrawingImage> {
        return drawingImage
    }

    /**
     * エラーメッセージのデータホルダーを取得する。
     */
    fun getError(): LiveData<String> {
        return error
    }

    /**
     * 図面カテゴリーを選択する。
     * 選択した図面カテゴリーに紐づくサブカテゴリー一覧および
     * 図面一覧が再設定される。
     * @param category 選択する図面カテゴリー
     */
    fun selectCategory(category: DrawingCategory?) {
        selectedCategory.value = category
        selectedSubCategory.value = null
        selectedDrawing.value = null

        loadSubCategories(subCategories)
        loadDrawings(drawings)
    }

    /**
     * 図面サブカテゴリーを選択する。
     * 選択した図面サブカテゴリーに紐づく図面一覧が再設定される。
     * @param subCategory 選択する図面サブカテゴリー
     */
    fun selectSubCategory(subCategory: DrawingSubCategory?) {
        selectedSubCategory.value = subCategory
        selectedDrawing.value = null

        loadDrawings(drawings)
    }

    /**
     * 図面を選択する。
     * 選択した図面に紐づく図面画像が再設定される。
     * @param drawing 選択する図面
     */
    fun selectDrawing(drawing: Drawing?) {
        selectedDrawing.value = drawing

        loadDrawingImage(drawingImage)
    }

    /**
     * 図面カテゴリー一覧をデータベースから読み込み、データホルダーにロードする。
     * @param data ロード先のデータホルダー
     */
    private fun loadCategories(data: MutableLiveData<List<DrawingCategory>>) {
        BlasLog.trace("I", "loadCategories: start ")
        val list = arrayListOf<DrawingCategory>()
        // カテゴリ無しを追加
        list.plusAssign(DrawingCategory(0, unselectedString))
        // DBからカテゴリー一覧を読み込み
        drawingsController.getCategories().forEach {
            BlasLog.trace("I", "Category got: $it ")
            val id = it["drawing_category_id"]?.toInt() ?: 0
            val name = it["name"] ?: ""
            list.plusAssign(DrawingCategory(id, name))
        }
        data.postValue(list.toList())
        BlasLog.trace("I", "loadCategories: end ")
    }

    /**
     * 図面サブカテゴリー一覧をデータベースから読み込み、データホルダーにロードする。
     * @param data ロード先のデータホルダー
     */
    private fun loadSubCategories(data: MutableLiveData<List<DrawingSubCategory>>) {
        BlasLog.trace("I", "loadSubCategories: start ")
        val list = arrayListOf<DrawingSubCategory>()
        // サブカテゴリ無しを追加
        list.plusAssign(DrawingSubCategory(0, unselectedString))
        // DBからサブカテゴリー一覧を読み込み
        selectedCategory.value?.let { drawingCategory ->
            drawingsController.getSubCategories(drawingCategory.id).forEach {
                BlasLog.trace("I", "SubCategory got: $it ")
                val id = it["drawing_sub_category_id"]?.toInt() ?: 0
                val name = it["name"] ?: ""
                list.plusAssign(DrawingSubCategory(id, name))
            }
        }
        data.postValue(list.toList())
        BlasLog.trace("I", "loadSubCategories: end ")
    }

    /**
     * 図面一覧をデータベースから読み込み、データホルダーにロードする。
     * @param data ロード先のデータホルダー
     */
    private fun loadDrawings(data: MutableLiveData<List<Drawing>>) {
        BlasLog.trace("I", "loadDrawings: start ")
        val list = arrayListOf<Drawing>()
        // 図面無しを追加
        list.plusAssign(Drawing(0, unselectedString, "", 0))
        // DBから図面一覧を読み込み
        drawingsController.getDrawings(
            selectedCategory.value?.id ?: 0,
            selectedSubCategory.value?.id ?: 0
        ).forEach {
            BlasLog.trace("I", "Drawing got: $it ")
            val id = it["drawing_id"]?.toInt() ?: 0
            val name = it["name"] ?: ""
            val drawingFile = it["filename"] ?: ""
            list.plusAssign(Drawing(id, name, drawingFile, 0))
        }
        data.postValue(list.toList())
        BlasLog.trace("I", "loadDrawings: end ")
    }

    /**
     * 図面画像をデータベースまたはリモートサーバーから取得し、データホルダーにロードする。
     * まず、データベースに対象のデータがあるか確認する。データがある場合は、そのデータをロードする。
     * データがない場合は、BLASサーバーにリクエストしてデータを取得する。
     * BLASサーバーからデータが取得できた場合は、データベースにも保存し、次回から再利用する。
     * @param data ロード先のデータホルダー
     */
    private fun loadDrawingImage(data: MutableLiveData<DrawingImage>) {
        BlasLog.trace("I", "loadDrawingImage: start")
        selectedDrawing.value?.let {
            if (it.id == 0) {
                BlasLog.trace("I", "No drawing selected ")
                data.postValue(null)
                return@let // 図面無しを選択している場合は処理終了
            }
            // 図面に設定されている設置箇所データを取得する
            val spots = getSpots()
            // 対象のデータがローカルにあるか確認
            val localBmp:Bitmap? = drawingsController.loadImageFromLocal( it.drawingFile )
            if (localBmp != null) {
                BlasLog.trace("I",
                    "The data was successfully read from the local: projectID=${it.id}, filename=${it.drawingFile} "
                )
                data.postValue(DrawingImage(localBmp,spots))
                return@let
            }
            val payload = mapOf("token" to token, "drawing_id" to it.id.toString())
            BlasRestDrawing(
                "view",
                payload,
                funcSuccess = { drawingResponse ->
                    BlasLog.trace("I", "The data was successfully read from the remote server ")
                    val decodedString: ByteArray = Base64.decode(drawingResponse.records[0].Drawing.image, Base64.DEFAULT)
                    val remoteBmp:Bitmap =
                        BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
                    data.postValue(DrawingImage(remoteBmp,spots))
                    // 画像データをローカルストレージに保存する
                    drawingsController.saveImageToLocal(it.drawingFile ,remoteBmp)
                },
                funcError = { _: Int, _: Int ->
                    BlasLog.trace("W", "Failed to read data from remote server ")
                    error.postValue("図面のダウンロードに失敗しました")
                }
            ).execute()
        }
        BlasLog.trace("I", "loadDrawingImage: end")
    }

    /**
     * 現在選択されている図面に紐づく設置箇所を取得する。
     * @return 設置箇所一覧
     */
    private fun getSpots(): List<DrawingSpot> {
        BlasLog.trace("I", "getSpots: start ")
        val list = arrayListOf<DrawingSpot>()
        // DBからラベルをロード
        drawingsController.getSpots(
            selectedDrawing.value?.id ?: 0
        ).forEach {
            BlasLog.trace("I", "Spot got: $it ")
            val name = it["name"] ?: ""
            val color = it["shape_color"] ?: ""
            val x :Float = if (it["abscissa"]?.isNotBlank()!!) {
                it["abscissa"]?.toFloat()!!
            } else {
                // 設置箇所未定（Int.MIN_VALUE）
                Int.MIN_VALUE.toFloat()
            }
            val y :Float = if (it["ordinate"]?.isNotBlank()!!) {
                it["ordinate"]?.toFloat()!!
            } else {
                // 設置箇所未定（Int.MIN_VALUE）
                Int.MIN_VALUE.toFloat()
            }
            list.plusAssign(DrawingSpot(name, color, x.toInt(), y.toInt()))
        }
        BlasLog.trace("I", "getSpots: end ")
        return list
    }

}
