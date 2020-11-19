package com.v3.basis.blas.activity

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.gson.Gson
import com.v3.basis.blas.BuildConfig
import com.v3.basis.blas.R
import com.v3.basis.blas.blasclass.controller.ImageControllerException
import com.v3.basis.blas.blasclass.controller.ImagesController
import com.v3.basis.blas.blasclass.controller.ImagesController.Companion.BIG_IMAGE
import com.v3.basis.blas.blasclass.controller.ImagesController.Companion.SMALL_IMAGE
import com.v3.basis.blas.blasclass.db.BaseController
import com.v3.basis.blas.blasclass.db.BaseController.Companion.SYNC_STATUS_NEW
import com.v3.basis.blas.blasclass.db.BlasSQLDataBase.Companion.context
import com.v3.basis.blas.blasclass.ldb.LdbImageRecord
import com.v3.basis.blas.blasclass.ldb.LdbItemImageRecord
import com.v3.basis.blas.blasclass.rest.SyncBlasRestImage
import com.v3.basis.blas.blasclass.service.BlasSyncMessenger
import com.v3.basis.blas.blasclass.service.BlasSyncService
import com.v3.basis.blas.blasclass.service.SenderHandler
import com.v3.basis.blas.databinding.ActivityItemImageZoomBinding
import com.v3.basis.blas.ui.ext.rotateLeft
import com.v3.basis.blas.ui.ext.rotateRight
import com.v3.basis.blas.ui.item.item_image.model.ItemImageWithLink
import com.v3.basis.blas.ui.item.item_image_zoom.custom_view.CustomImageView
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import java.lang.Exception
import kotlin.concurrent.withLock

class ItemImageZoomActivity : AppCompatActivity() {
    companion object {
        const val ITEM_ID = "item_id"
        const val PROJECT_ID = "project_id"
        const val PROJECT_IMG_ID = "project_img_id"
        const val IMG_ID = "img_id"
        const val TITLE = "title"
        const val TOKEN = "token"

    }
    private val projectImgId: String
        get() = intent.extras?.getString(PROJECT_IMG_ID) ?: ""

    private var imageId: String = ""
        get() = intent.extras?.getString(IMG_ID) ?: ""

    private val projectId: String
        get() = intent.extras?.getString(PROJECT_ID) ?: ""

    private val itemId: String
        get() = intent.extras?.getString(ITEM_ID) ?: ""

    private val title: String
        get() = intent.extras?.getString(TITLE) ?: ""

    private val token: String
        get() = intent.extras?.getString(TOKEN) ?: ""

    private var disposable = CompositeDisposable()
    private lateinit var  mImageCustomView : CustomImageView
    private lateinit var bind: ActivityItemImageZoomBinding
    private lateinit var imageController: ImagesController


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_item_image_zoom)

        imageController = ImagesController(applicationContext, projectId)

        bind = DataBindingUtil.setContentView(this, R.layout.activity_item_image_zoom)
        bind.loading = true
        bind.activity = this

        supportActionBar?.title = title
        //上の矢印表示
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        //layoutに埋め込んだCustomImageViewを取得する
        mImageCustomView = findViewById(R.id.customView)
        //画像の取得処理
        Single.fromCallable {
            SenderHandler.lock.withLock {
                //通信エラーのテストが不十分
                this.fetchImage()
            }
        }.subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onSuccess = {
                    mImageCustomView.setBitMap(it)
                    mImageCustomView.invalidate()
                    bind.loading = false
                },
                onError = {
                    Toast.makeText(context, "画像のダウンロードに失敗しました", Toast.LENGTH_LONG).show()
                }
            )
            .addTo(disposable)
    }

    fun fetchImage():Bitmap {
        //大きな画像はあるか？
        val ret = imageController.getImage(token, itemId, projectImgId, BIG_IMAGE)
        val bmp = ret.first
        imageId = ret.second.toString()
        if(bmp != null) {
            //imageController.saveBitmap(bmp, itemId, projectImgId, BIG_IMAGE)
            val imageRecord = LdbImageRecord()
            imageRecord.image_id = imageId.toLong()
            imageRecord.project_id = projectId.toInt()
            imageRecord.item_id = itemId.toLong()
            imageRecord.project_image_id = projectImgId.toInt()
            //ダウンロードしただけなので、sync済みとする
            imageRecord.sync_status = BaseController.SYNC_STATUS_SYNC
            if (imageRecord != null) {
                /*
                    レコードを保存するとき、仮IDで保存してしまうのはおかしい…。
                 */
                val ret = imageController.save2LDB(imageRecord)
                if (!ret.first) {
                    Log.d("konishi", "画像の保存に失敗しました")
                    Toast.makeText(context, "画像の取得に失敗しました", Toast.LENGTH_SHORT).show()
                }
            }
        }
        else {
            throw ImageControllerException(3, "画像の読み込みに失敗しました")
        }
        return bmp
    }


    //画像を右回転する
    fun rightRotate() {
        //右回転ボタンを押したとき
        SenderHandler.lock.withLock {
            //小さな画像を読み込んで回転して保存する
            var rminiBmp = imageController?.getCacheBitmap(itemId, projectImgId, SMALL_IMAGE)
            rminiBmp = rminiBmp?.rotateRight()
            if (rminiBmp != null) {
                imageController?.saveBitmap(rminiBmp, itemId, projectImgId, SMALL_IMAGE)
            }

            //大きな画像を回転して保存する
            var rbigBmp = imageController?.getCacheBitmap( itemId, projectImgId, BIG_IMAGE)
            rbigBmp = rbigBmp?.rotateRight()
            if (rbigBmp != null) {
                imageController?.saveBitmap(rbigBmp, itemId, projectImgId, BIG_IMAGE)
            }

            //表示用画像を回転する
            if (rbigBmp != null) {

                val imageRecord = LdbImageRecord()
                imageRecord.image_id = imageId.toLong()
                imageRecord.project_id = projectId.toInt()
                imageRecord.item_id = itemId.toLong()
                imageRecord.project_image_id = projectImgId.toInt()
                imageRecord.sync_status = SYNC_STATUS_NEW
                imageController?.save2LDB(imageRecord)

                //画像表示の更新
                mImageCustomView.setBitMap(rbigBmp)
                mImageCustomView.invalidate()
                //再送信のイベントを送る
                BlasSyncMessenger.notifyBlasImages(token, projectId)
            }
        }
    }

    //  画像を左回転する
    fun leftRotate() {
        //右回転ボタンを押したとき
        SenderHandler.lock.withLock {
            //小さな画像を読み込んで回転して保存する
            var rminiBmp = imageController?.getCacheBitmap(itemId, projectImgId, SMALL_IMAGE)
            rminiBmp = rminiBmp?.rotateLeft()
            if (rminiBmp != null) {
                imageController?.saveBitmap(rminiBmp, itemId, projectImgId, SMALL_IMAGE)
            }

            //大きな画像を回転して保存する
            var rbigBmp = imageController?.getCacheBitmap( itemId, projectImgId, BIG_IMAGE)
            rbigBmp = rbigBmp?.rotateLeft()
            if (rbigBmp != null) {
                imageController?.saveBitmap(rbigBmp, itemId, projectImgId, BIG_IMAGE)
            }

            //表示用画像を回転する
            if (rbigBmp != null) {

                val imageRecord = LdbImageRecord()
                imageRecord.image_id = imageId.toLong()
                imageRecord.project_id = projectId.toInt()
                imageRecord.item_id = itemId.toLong()
                imageRecord.project_image_id = projectImgId.toInt()
                imageRecord.sync_status = SYNC_STATUS_NEW
                imageController?.save2LDB(imageRecord)

                //画像表示の更新
                mImageCustomView.setBitMap(rbigBmp)
                mImageCustomView.invalidate()
                //再送信のイベントを送る
                BlasSyncMessenger.notifyBlasImages(token, projectId)
            }
        }
    }


    //矢印ボタンで戻るを実行する処理
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == android.R.id.home){
            finish()
        }
        return super.onOptionsItemSelected(item)
    }
}
