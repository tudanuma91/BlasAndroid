package com.v3.basis.blas.ui.item.item_image

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.tonyodev.fetch2.*
import com.tonyodev.fetch2core.Func
import com.v3.basis.blas.BuildConfig
import com.v3.basis.blas.blasclass.controller.ImageControllerException
import com.v3.basis.blas.blasclass.controller.ImagesController
import com.v3.basis.blas.blasclass.db.BaseController
import com.v3.basis.blas.blasclass.rest.BlasRestImage
import com.v3.basis.blas.blasclass.rest.BlasRestImageField
import com.v3.basis.blas.blasclass.rest.SyncBlasRestImage
import com.v3.basis.blas.ui.ext.rotateLeft
import com.v3.basis.blas.ui.ext.rotateRight
import com.v3.basis.blas.ui.item.item_image.model.ImageFieldModel
import com.v3.basis.blas.ui.item.item_image.model.ItemImage
import com.v3.basis.blas.ui.item.item_image.model.ItemImageWithLink
import com.v3.basis.blas.ui.item.item_image.model.ItemImageWithLinkImage
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.AsyncSubject
import io.reactivex.subjects.PublishSubject
import org.json.JSONObject
import java.io.ByteArrayOutputStream


class ItemImageViewModel() : ViewModel() {

    val errorAPI: PublishSubject<Int> = PublishSubject.create()
    val receiveImageFields: AsyncSubject<ImageFieldModel> = AsyncSubject.create()
    val uploadAction: PublishSubject<String> = PublishSubject.create()
    val deleteAction: PublishSubject<ItemImageCellItem> = PublishSubject.create()
    val zoomAction: PublishSubject<ItemImageCellItem> = PublishSubject.create()

    private lateinit var token: String
    private lateinit var projectId: String
    private lateinit var itemId: String

    private lateinit var imageField: ImageFieldModel
    private var disposable = CompositeDisposable()

    private lateinit var context: Context
    private lateinit var imageController: ImagesController
    private val imageItems: MutableList<ItemImageWithLinkImage> = mutableListOf()

    fun setup(context:Context, token: String, projectId: String, itemId: String) {

        this.token = token
        this.projectId = projectId
        this.itemId = itemId
        this.context = context

        fun imageFieldSuccess(json: JSONObject) {

            Log.d("image field","${json}")
            json.toString().also {
                imageField = Gson().fromJson(it, ImageFieldModel::class.java)
                Single.create<ImagesController> { it.onSuccess(ImagesController(context, projectId)) }
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(Schedulers.newThread())
                    .subscribeBy {
                        imageController = it
                        receiveImageFields.onNext(imageField)
                        getImageUrls()
                    }
                    .addTo(disposable)
            }
        }

        fun error(errorCode: Int ,aplCode:Int) {
            errorAPI.onNext(errorCode)
            Log.d("取得失敗","画像の取得失敗")
        }

        val payload2 = mapOf("token" to token, "project_id" to projectId)
        BlasRestImageField(payload2, ::imageFieldSuccess, ::error).execute()
    }

    private fun getImageUrls() {

        val payload = mapOf("token" to token, "item_id" to itemId)
        Single
            .fromCallable {
                val json = SyncBlasRestImage().getUrl(payload)
                Gson().fromJson(json.toString(), ItemImageWithLink::class.java)
            }
            .subscribeOn(Schedulers.newThread())
            .doOnError {
                //とりあえず呼び出し側に通知
                receiveImageFields.onComplete()
            }
            .doOnSuccess {
                imageItems.addAll(it.records.map { it.Image })
                receiveImageFields.onComplete()
            }
            .subscribe()
            .addTo(disposable)
    }

    private fun save2DB(record: ItemImage, status: Int) {
        Completable
            .fromAction {
                imageController.save2LDB(record, status)
            }
            .subscribeOn(Schedulers.io())
            .subscribe()
            .addTo(disposable)
    }

    /**
     * [説明]
     * リモートから画像をダウンロードする。
     * [コール条件]
     * 本関数が呼ばれるのは、ローカルに画像がない場合だけである。
     */
    private fun fetchImageFromRemote(item: ItemImageCellItem){

        Single
            .create<ItemImageWithLinkImage> {emitter ->
                imageItems.firstOrNull { it.project_image_id == item.id }?.also { emitter.onSuccess(it) }
                    ?: emitter.onError(NullPointerException())
            }
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onError = {
                    item.loading.set(false)
                    item.empty.set(true)
                    item.imageId = ""
                },
                onSuccess = {
                    item.url.set(BuildConfig.HOST + it.small_image)
                    item.urlBig.set(BuildConfig.HOST + it.image)
                    item.empty.set(false)
                    item.ext = it.ext
                    item.imageId = it.image_id
                    item.bitmapEvent.observeOn(Schedulers.io())
                        .subscribeBy {
                            val itemRecord = ItemImage(
                                item_id = itemId,
                                project_id = projectId,
                                project_image_id = item.id
                            )
                            itemRecord.bitmap = it
                            save2DB(itemRecord, BaseController.SYNC_STATUS_SYNC)
                        }
                        .addTo(disposable)
                }
            )
            .addTo(disposable)
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

        // ローカルから画像ファイルを取得する
        Single.fromCallable { imageController.searchFromLocal(context, itemId, projectImageId) }
            .subscribeOn(Schedulers.newThread())
            .observeOn(Schedulers.newThread())
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

        if (item.loading.get() == true) {
            return
        }

        /* ここを改良する Single.fromCallble使う */
        Single.fromCallable {
            item.loading.set(true)
            imageController.reserveDeleteImg(item.imageId.toLong())
        }.subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onError = {
                    item.loading.set(false)
                    Toast.makeText(context, "画像の削除予約に失敗しました", Toast.LENGTH_SHORT).show()
                },
                onSuccess = {
                    item.loading.set(false)
                    item.image.set(null)
                    item.empty.set(true)
                }
            ).addTo(CompositeDisposable())
    }

    fun selectFile(id: String) {
        uploadAction.onNext(id)
    }
    fun selectFile2(item: ItemImageCellItem): Boolean {
        zoomAction.onNext(item)
        return true
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

//    private fun decode(json: JSONObject) : Single<ItemImageWithLinkImage> {
//        return Single.create<ItemImageWithLinkImage> { emitter ->
//            images = Gson().fromJson(json.toString(), ItemImageWithLink::class.java)
//            val item = images.records.map { it.Image }.first()
//            item.also { emitter.onSuccess(it) }
//                ?: emitter.onError(IllegalStateException("BlasRestImage:failed to json convert"))
//        }
//    }

    private fun decode(json: JSONObject) : ItemImageWithLinkImage? {
        try {
            val images = Gson().fromJson(json.toString(), ItemImageWithLink::class.java)
            return images.records.map { it.Image }.first()
        } catch (e: Exception) {
            return null
        }
    }

    override fun onCleared() {
        super.onCleared()
        disposable.dispose()
    }
}
