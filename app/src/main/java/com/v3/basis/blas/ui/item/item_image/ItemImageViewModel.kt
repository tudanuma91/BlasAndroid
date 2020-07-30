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
import com.v3.basis.blas.blasclass.controller.ImagesController
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
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.lang.Exception


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
     * ローカルから画像を取得する
     */
    private fun fetchImageFromLocal(item: ItemImageCellItem) {
        val projectImageId = item.id
        val imageController = ImagesController(context, projectId)

        // ローカルから画像ファイルを取得する
        Single.fromCallable { imageController.searchFromLocal(context, itemId, projectImageId) }
            .subscribeOn(AndroidSchedulers.mainThread())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnError {
                Log.d("konishi", "エラー発生")
            }
            .subscribeBy{
                //Toast.makeText(context,it,Toast.LENGTH_LONG)
                    item.image.set(it.first)
                    item.empty.set(false)
                    item.loading.set(false)
                    item.imageId = it.second.toString()
                    //item.ext = it.ext
            }.addTo(disposable)
    }

    /**
     * リモートから画像をダウンロードする
     */
    private fun fetchImageFromRemote(item: ItemImageCellItem) {
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
                        Log.d("fetchImage", "failed to decode image")
                    },
                    onSuccess = {
                        item.image.set(it.bitmap)
                        item.empty.set(false)
                        item.loading.set(false)
                        item.imageId = it.image_id
                        item.ext = it.ext
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


    fun fetchImage(item: ItemImageCellItem) {
        //konishi 今ここいじり中

        try{
            //ローカルから画像を取得する
            fetchImageFromLocal(item)
        }
        catch(e: Exception){
            //リモートから画像を取得する
            //fetchImageFromRemote(item)
        }

      /*
        val projectImageId = item.id
        val imageController = ImagesController(context, projectId)
        try{
            // ローカルから画像ファイルを取得する
            Single.fromCallable { imageController.searchFromLocal(context, itemId, projectImageId) }
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy{
                    //Toast.makeText(context,it,Toast.LENGTH_LONG)
                    item.image.set(it.first)
                    item.empty.set(false)
                    item.loading.set(false)
                    item.imageId = it.second.toString()
                    //item.ext = it.ext
                }.addTo(disposable)
        }
        catch(e:Exception) {
            //ローカルに画像がないので、リモートから取得する
            item.loading.set(false)
            item.empty.set(true)
            e.printStackTrace()
        }


            .subscribeBy {
                if (it.isNotEmpty()) {

                    // TODO:一覧画面を表示したい。なにすればよい？
                    itemList.clear()
                    jsonItemList.clear()
                    //jsonItemList.set("1",result)

                    setAdapter()
                } else {
                    throw Exception()
                }
            }
            .addTo(disposables)*/
/*
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
                        Log.d("fetchImage", "failed to decode image")
                    },
                    onSuccess = {
                        item.image.set(it.bitmap)
                        item.empty.set(false)
                        item.loading.set(false)
                        item.imageId = it.image_id
                        item.ext = it.ext
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

        BlasRestImage("download", payload, ::success, ::error).execute()*/
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
