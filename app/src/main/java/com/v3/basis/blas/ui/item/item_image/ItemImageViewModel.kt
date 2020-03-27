package com.v3.basis.blas.ui.item.item_image

import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Base64
import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.v3.basis.blas.blasclass.rest.BlasRestImage
import com.v3.basis.blas.blasclass.rest.BlasRestImageField
import com.v3.basis.blas.ui.ext.translateToBitmap
import com.v3.basis.blas.ui.item.item_image.model.ImageFieldModel
import com.v3.basis.blas.ui.item.item_image.model.ItemImage
import com.v3.basis.blas.ui.item.item_image.model.ItemImageModel
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import org.json.JSONObject
import java.io.ByteArrayOutputStream


class ItemImageViewModel : ViewModel() {

    val delete: PublishSubject<Int> = PublishSubject.create()
    val rightRotate: PublishSubject<Int> = PublishSubject.create()
    val leftRotate: PublishSubject<Int> = PublishSubject.create()
    val fetchSuccess: PublishSubject<ItemImage> = PublishSubject.create()
    val fetchEmpty: PublishSubject<String> = PublishSubject.create()
    val fetchError: PublishSubject<Int> = PublishSubject.create()
    val imageFieldUpdated: PublishSubject<ImageFieldModel> = PublishSubject.create()
    val successUploaded: PublishSubject<String> = PublishSubject.create()
    val errorUploaded: PublishSubject<String> = PublishSubject.create()
    val fileSelect: PublishSubject<String> = PublishSubject.create()

    private lateinit var token: String
    private lateinit var projectId: String
    private lateinit var itemId: String

    private lateinit var imageField: ImageFieldModel
    private lateinit var images: ItemImageModel
    private var disposable = CompositeDisposable()

    fun setup(token: String, projectId: String, itemId: String) {

        this.token = token
        this.projectId = projectId
        this.itemId = itemId

        val payload2 = mapOf("token" to token, "project_id" to projectId)
        BlasRestImageField(payload2, ::imageFieldSuccess, ::error).execute()
    }

    fun fetchImage(projectImageId: String) {

        Log.d("fetchImage", "fetch id = $projectImageId")
        val payload = mapOf("token" to token, "item_id" to itemId, "project_image_id" to projectImageId)

        val fetchError: (errorCode: Int) -> Unit = { errorCode ->
            if (errorCode == 200) {
                fetchEmpty.onNext(projectImageId)
                Log.d("fetch error", "no column")
            } else {
                fetchError.onNext(errorCode)
                Log.d("fetch error", "error $errorCode")
            }
        }

        BlasRestImage("download", payload, ::success, fetchError).execute()
    }

    fun deleteClick(item: ItemImageCellItem) {

        item.loading.set(false)
        fun success(json: JSONObject) {
            item.loading.set(false)
            item.empty.set(true)
        }

        fun error(errorCode: Int) {
            item.loading.set(false)
            item.empty.set(false)
        }

        val payload = mapOf("token" to token, "image_id" to item.imageId)
        BlasRestImage("delete", payload, ::success, ::error).execute()
    }

    private fun rotateBitmap(source: Bitmap, angle: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(angle)
        return Bitmap.createBitmap(
            source,
            0,
            0,
            source.width,
            source.height,
            matrix,
            true
        )
    }

    fun rightRotate(item: ItemImageCellItem) {

        item.image.get()?.let { rotateBitmap(it, 90.0f) }.apply {
            item.image.set(this)
        }
        //todo
    }

    fun leftRotate(item: ItemImageCellItem) {

        item.image.get()?.let { rotateBitmap(it, 270.0f) }.apply {
            item.image.set(this)
        }
        //todo
    }

    fun selectFile(id: String) {
        fileSelect.onNext(id)
    }

    fun upload(bitmap: Bitmap, mime: String, projectImageId: String) {

        val format = FileExtensions.matchExtension(mime)

        Single.create<String> { it.onSuccess(encode(bitmap, format)) }
            .subscribeOn(Schedulers.newThread())
            .observeOn(Schedulers.newThread())
            .subscribeBy {

                val success: (jsonObject: JSONObject) -> Unit = {
                    successUploaded.onNext(projectImageId)
                }
                val error: (errorCode: Int) -> Unit = {
                    errorUploaded.onNext(projectImageId)
                }

                val payload = mapOf(
                    "token" to token,
                    "project_id" to projectId,
                    "project_image_id" to projectImageId,
                    "item_id" to itemId,
                    "image" to it,
                    "image_type" to format.restImageType)

                BlasRestImage("upload", payload, success, error).execute()
            }
            .addTo(disposable)
    }

    private fun encode(bitmap: Bitmap, ext: FileExtensions) : String {

        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(ext.compressFormat, 90, byteArrayOutputStream)
        val byteArray: ByteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    private fun decode(json: JSONObject) : ItemImage {
        images = Gson().fromJson(json.toString(), ItemImageModel::class.java)
        val item = images.records?.map { it.Image }?.first()?.let {
            it.apply {
                bitmap = Base64.decode(image, Base64.DEFAULT).translateToBitmap()
                Log.d("fetch image", "file = ${it.filename}")
            }
        }
        return item ?: throw IllegalStateException("BlasRestImage:failed to json convert")
    }

    private fun success(json: JSONObject) {
        Single.create<ItemImage> { it.onSuccess(decode(json)) }
            .subscribeOn(Schedulers.newThread())
            .observeOn(Schedulers.newThread())
            .subscribeBy {
                fetchSuccess.onNext(it)
            }
            .addTo(disposable)
    }

    private fun imageFieldSuccess(json: JSONObject) {

        Log.d("image field","${json}")
        json.toString().also {
            imageField = Gson().fromJson(it, ImageFieldModel::class.java)
            imageFieldUpdated.onNext(imageField)
        }
    }

    /**
     * フィールド取得失敗時
     */
    private fun error(errorCode: Int) {
        if (errorCode != 200)
            fetchError.onNext(errorCode)
    }
}
