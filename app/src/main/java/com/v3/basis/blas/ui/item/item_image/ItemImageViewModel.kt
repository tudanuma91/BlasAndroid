package com.v3.basis.blas.ui.item.item_image

import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.v3.basis.blas.blasclass.rest.BlasRestImage
import com.v3.basis.blas.blasclass.rest.BlasRestImageField
import com.v3.basis.blas.ui.ext.rotateLeft
import com.v3.basis.blas.ui.ext.rotateRight
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

    val errorAPI: PublishSubject<Int> = PublishSubject.create()
    val receiveImageFields: PublishSubject<ImageFieldModel> = PublishSubject.create()
    val uploadAction: PublishSubject<String> = PublishSubject.create()

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

        fun imageFieldSuccess(json: JSONObject) {

            Log.d("image field","${json}")
            json.toString().also {
                imageField = Gson().fromJson(it, ImageFieldModel::class.java)
                receiveImageFields.onNext(imageField)
            }
        }

        fun error(errorCode: Int ,aplCode:Int) {
            errorAPI.onNext(errorCode)
        }

        val payload2 = mapOf("token" to token, "project_id" to projectId)
        BlasRestImageField(payload2, ::imageFieldSuccess, ::error).execute()
    }

    fun fetchImage(item: ItemImageCellItem) {

        val projectImageId = item.id
        Log.d("fetchImage", "fetch id = $projectImageId")
        val payload = mapOf("token" to token, "item_id" to itemId, "project_image_id" to projectImageId)

        fun success(json: JSONObject) {

            Single.just(decode(json))
                .subscribeOn(Schedulers.newThread())
                .observeOn(Schedulers.newThread())
                .subscribeBy {
                    item.image.set(it.bitmap)
                    item.empty.set(false)
                    item.loading.set(false)
                    item.imageId = it.image_id
                    item.ext = it.ext
                }
                .addTo(disposable)
        }

        fun error(errorCode: Int, aplCode:Int) {
            item.loading.set(false)
            item.empty.set(true)
            if (errorCode == 200) { Log.d("fetch error", "no column") }
            else { Log.d("fetch error", "error $errorCode") }
        }

        BlasRestImage("download", payload, ::success, ::error).execute()
    }

    fun deleteClick(item: ItemImageCellItem) {

        item.loading.set(false)
        fun success(json: JSONObject) {
            item.loading.set(false)
            item.empty.set(true)
        }

        fun error(errorCode: Int, aplCode:Int) {
            item.loading.set(false)
            item.empty.set(false)
        }

        val payload = mapOf("token" to token, "image_id" to item.imageId)
        BlasRestImage("delete", payload, ::success, ::error).execute()
    }

    fun rightRotate(item: ItemImageCellItem) {

        val error: (errorCode: Int, aplCode:Int) -> Unit = { i: Int, i1: Int ->
            item.loading.set(false)
            item.image.get()?.rotateLeft()
        }
        item.image.get()?.rotateRight()?.apply { updateCellItem(item, this, error) }
    }

    fun leftRotate(item: ItemImageCellItem) {

        val error: (errorCode: Int, aplCode:Int) -> Unit = { i: Int, i1: Int ->
            item.loading.set(false)
            item.image.get()?.rotateRight()
        }
        item.image.get()?.rotateLeft()?.apply { updateCellItem(item, this, error) }
    }

    private fun updateCellItem(item: ItemImageCellItem, bitmap: Bitmap, error: (errorCode: Int,aplCode:Int) -> Unit) {
        item.image.set(bitmap)
        upload(bitmap, item.ext, item, error)
        item.loading.set(true)
    }

    fun selectFile(id: String) {
        uploadAction.onNext(id)
    }

    fun upload(bitmap: Bitmap, mime: String, item: ItemImageCellItem, error: (errorCode: Int, aplCode:Int) -> Unit) {

        val format = FileExtensions.matchExtension(mime)

        Single.create<String> { it.onSuccess(encode(bitmap, format)) }
            .subscribeOn(Schedulers.newThread())
            .observeOn(Schedulers.newThread())
            .subscribeBy {

                val success: (jsonObject: JSONObject) -> Unit = {
                    item.loading.set(false)
                }

                val payload = mapOf(
                    "token" to token,
                    "project_id" to projectId,
                    "project_image_id" to item.id,
                    "item_id" to itemId,
                    "image" to it,
                    "image_type" to format.restImageType)

                BlasRestImage("upload", payload, success, error).execute()
            }
            .addTo(disposable)
    }

    private fun encode(bitmap: Bitmap, ext: FileExtensions) : String {

        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(ext.compressFormat, 100, byteArrayOutputStream)
        val byteArray: ByteArray = byteArrayOutputStream.toByteArray()

        val flag = Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING
        return Base64.encodeToString(byteArray, flag)
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
}
