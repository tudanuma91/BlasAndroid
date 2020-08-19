package com.v3.basis.blas.ui.item.item_image

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.v3.basis.blas.blasclass.component.ImageComponent
import com.v3.basis.blas.blasclass.controller.ImageControllerException
import com.v3.basis.blas.blasclass.controller.ImagesController
import com.v3.basis.blas.blasclass.db.BaseController
import com.v3.basis.blas.blasclass.rest.BlasRest
import com.v3.basis.blas.blasclass.rest.BlasRestImage
import com.v3.basis.blas.blasclass.rest.BlasRestImageField
import com.v3.basis.blas.ui.ext.rotateLeft
import com.v3.basis.blas.ui.ext.rotateRight
import com.v3.basis.blas.ui.ext.translateToBitmap
import com.v3.basis.blas.ui.item.item_image.model.ImageFieldModel
import com.v3.basis.blas.ui.item.item_image.model.ItemImage
import com.v3.basis.blas.ui.item.item_image.model.ItemImageModel
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.internal.util.HalfSerializer.onError
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*


class ItemImageViewModel() : ViewModel() {

    val errorAPI: PublishSubject<Int> = PublishSubject.create()
    val receiveImageFields: PublishSubject<ImageFieldModel> = PublishSubject.create()
    val uploadAction: PublishSubject<String> = PublishSubject.create()
    val deleteAction: PublishSubject<ItemImageCellItem> = PublishSubject.create()

    private lateinit var token: String
    private lateinit var projectId: String
    private lateinit var itemId: String

    private lateinit var imageField: ImageFieldModel
    private lateinit var images: ItemImageModel
    private var disposable = CompositeDisposable()

    private lateinit var context: Context

    fun setup(context:Context, token: String, projectId: String, itemId: String) {

        this.token = token
        this.projectId = projectId
        this.itemId = itemId
        this.context = context

        fun imageFieldSuccess(json: JSONObject) {

            Log.d("image field","${json}")
            json.toString().also {
                imageField = Gson().fromJson(it, ImageFieldModel::class.java)
                receiveImageFields.onNext(imageField)
            }
        }

        fun error(errorCode: Int ,aplCode:Int) {
            errorAPI.onNext(errorCode)
            Log.d("取得失敗","画像の取得失敗")
        }

        val payload2 = mapOf("token" to token, "project_id" to projectId)
        BlasRestImageField(payload2, ::imageFieldSuccess, ::error).execute()
    }

    /**
     * [説明]
     * リモートから画像をダウンロードする。
     * [コール条件]
     * 本関数が呼ばれるのは、ローカルに画像がない場合だけである。
     */
    private fun fetchImageFromRemote(item: ItemImageCellItem){
        val projectImageId = item.id
        Log.d("fetchImage", "fetch id = $projectImageId")
        val payload = mapOf("token" to token, "item_id" to itemId, "project_image_id" to projectImageId)

        fun success(json: JSONObject) {

            decode(json)
                .subscribeOn(Schedulers.newThread())
                .observeOn(Schedulers.newThread())
                .subscribeBy( //asynctaskのexecute
                    onError = {
                        item.loading.set(false)
                        item.empty.set(true)
                        item.imageId = ""
                        Log.d("fetchImage", "failed to decode image")
                    },
                    onSuccess = {
                        item.image.set(it.bitmap)
                        item.empty.set(false)
                        item.loading.set(false)
                        item.imageId = it.image_id
                        item.ext = it.ext

                        //LDBの更新
                        val imageController = ImagesController(context, projectId)
                        //リモートから画像をダウンロードできているので、imageIdは必ずある。
                        //リモートからダウンロードした画像は本登録する。
                        imageController.save2LDB(it, BaseController.SYNC_STATUS_SYNC)
                    }
                )
                .addTo(disposable)//disposableは使い捨ての意味

        }

        fun error(errorCode: Int, aplCode:Int) {
            item.loading.set(false)
            item.empty.set(true)
            if (errorCode == 200) { Log.d("fetch error", "no column") }
            else { Log.d("fetch error", "error $errorCode") }
        }

        BlasRestImage("download", payload, ::success, ::error).execute()
    }

    /**
     * [説明]
     * 画像表示。
     * imagesテーブルに仮登録のローカル画像があれば、ローカル画像を返す
     * 無い場合はリモートから画像をダウンロードして、ローカルのimagesテーブルに本登録して
     * 表示する。
     */
    fun fetchImage(item: ItemImageCellItem) {
        val projectImageId = item.id
        val imageController = ImagesController(context, projectId)

        // ローカルから画像ファイルを取得する
        Single.fromCallable { imageController.searchFromLocal(context, itemId, projectImageId) }
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .subscribeBy(
                onError = {
                    if (it is ImageControllerException) {
                        if (it.errorCode == 1) {
                            //ローカルにもリモートにも画像なし
                            item.loading.set(false)
                            item.empty.set(true)
                            item.imageId = ""
                        } else if (it.errorCode == 2) {
                            //リモート問い合わせ
                            fetchImageFromRemote(item)
                        }
                        else {
                            item.loading.set(false)
                            item.empty.set(true)
                            item.imageId = ""
                        }
                    } else {
                        //想定外のエラー
                        item.loading.set(false)
                        item.empty.set(true)
                        item.imageId = ""
                    }
                },
                onSuccess = {
                    //searchFromLocalで取得した画像を表示する
                    item.image.set(it.first)
                    item.empty.set(false)
                    item.loading.set(false)
                    item.imageId = it.second.toString()
                    //item.ext = it.ext
                }
            ).addTo(disposable)
    }

    fun deleteClick(item: ItemImageCellItem) = deleteAction.onNext(item)
    fun deleteItem(item: ItemImageCellItem) {

        item.loading.set(false)
        fun success(json: JSONObject) {
            item.loading.set(false)
            item.empty.set(true)
        }

        fun error(errorCode: Int, aplCode:Int) {
            item.loading.set(false)
            item.empty.set(false)
        }

        /* ここを改良する Single.fromCallble使う */
        val error: (errorCode: Int, aplCode:Int) -> Unit = { i: Int, i1: Int ->
            item.loading.set(false)
        }
        val imgCon = ImagesController(context, projectId)
        imgCon.deleteImageLocal(item.imageId.toLong())
        //updateCellItem(item, null, error)
        //val payload = mapOf("token" to token, "image_id" to item.imageId)
        //BlasRestImage("delete", payload, ::success, ::error).execute()
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
        item.loading.set(true)
        item.image.set(bitmap)
        //upload(bitmap, item.ext, item, error)
        val imageController = ImagesController(context, projectId)
        //リモートから画像をダウンロードできているので、imageIdは必ずある。
        //リモートからダウンロードした画像は本登録する。
        val itemRecord = ItemImage(
            image_id=item.imageId,
            item_id = itemId,
            moved="0",
            project_id=projectId,
            project_image_id = item.id)

        itemRecord.bitmap = bitmap
        imageController.save2LDB(itemRecord, BaseController.SYNC_STATUS_NEW)
        item.loading.set(false)
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
                    fetchImage(item)
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

    private fun decode(json: JSONObject) : Single<ItemImage> {
        return Single.create<ItemImage> { emitter ->
            images = Gson().fromJson(json.toString(), ItemImageModel::class.java)
            val item = images.records?.map { it.Image }?.first()?.let {
                it.apply {
                    try {
                        bitmap = Base64.decode(image, Base64.DEFAULT).translateToBitmap()
                        Log.d("fetch image", "file = ${it.filename}")
                    } catch (t :Throwable) {
                        emitter.onError(t)
                    }
                }
            }
            item?.also { emitter.onSuccess(it) }
                ?: emitter.onError(IllegalStateException("BlasRestImage:failed to json convert"))
        }
    }

    override fun onCleared() {
        super.onCleared()
        disposable.dispose()
    }
}
