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
import com.v3.basis.blas.blasclass.db.BlasSQLDataBase
import com.v3.basis.blas.blasclass.db.BlasSQLDataBase.Companion.context
import com.v3.basis.blas.blasclass.ldb.LdbImageRecord
import com.v3.basis.blas.blasclass.ldb.LdbItemImageRecord
import com.v3.basis.blas.blasclass.log.BlasLog
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
import kotlinx.android.synthetic.main.fragment_item_image.*
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
        //??????????????????
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        //layout??????????????????CustomImageView???????????????
        mImageCustomView = findViewById(R.id.customView)
        //?????????????????????
        Single.fromCallable {
            SenderHandler.lock.withLock {
                //???????????????????????????????????????
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
                    Toast.makeText(context, "????????????????????????????????????????????????", Toast.LENGTH_LONG).show()
                }
            )
            .addTo(disposable)
    }

    fun fetchImage():Bitmap {
        //??????????????????????????????
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
            //??????????????????????????????????????????sync???????????????
            imageRecord.sync_status = BaseController.SYNC_STATUS_SYNC
            if (imageRecord != null) {
                /*
                    ???????????????????????????????????????ID????????????????????????????????????????????????
                 */
                val ret = imageController.save2LDB(imageRecord)
                if (!ret.first) {
                    Log.d("konishi", "????????????????????????????????????")
                    Toast.makeText(context, "????????????????????????????????????", Toast.LENGTH_SHORT).show()
                }
            }
        }
        else {
            throw ImageControllerException(3, "??????????????????????????????????????????")
        }
        return bmp
    }


    //????????????????????????
    fun rightRotate() {
        Completable.fromAction {
            SenderHandler.lock.withLock {
                //????????????????????????LDB??????????????????????????????
                rotate(1)
            }
        }.subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onComplete = {
                    BlasLog.trace("I", "??????????????????????????????")
                },
                onError = {
                }
            ).addTo(disposable)
    }

    //  ????????????????????????
    fun leftRotate() {
        Completable.fromAction {
                SenderHandler.lock.withLock {
                    //????????????????????????LDB??????????????????????????????
                    rotate(0)
                }
        }.subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onComplete = {
                    BlasLog.trace("I", "??????????????????????????????")
                },
                onError = {
                }
            ).addTo(disposable)
    }

    fun rotate(leftOrRight:Int) {
        //?????????????????????????????????????????????????????????
        var rminiBmp = imageController?.getCacheBitmap(itemId, projectImgId, SMALL_IMAGE)
        if(leftOrRight == 0) {
            rminiBmp = rminiBmp?.rotateLeft()
        }
        else {
            rminiBmp = rminiBmp?.rotateRight()
        }

        if (rminiBmp != null) {
            imageController?.saveBitmap(rminiBmp, itemId, projectImgId, SMALL_IMAGE)
        }

        //??????????????????????????????????????????
        var rbigBmp = imageController?.getCacheBitmap( itemId, projectImgId, BIG_IMAGE)
        if(leftOrRight == 0) {
            rbigBmp = rbigBmp?.rotateLeft()
        }
        else {
            rbigBmp = rbigBmp?.rotateRight()
        }

        if (rbigBmp != null) {
            imageController?.saveBitmap(rbigBmp, itemId, projectImgId, BIG_IMAGE)
        }

        //??????????????????????????????
        if (rbigBmp != null) {
            val imageRecord = LdbImageRecord()
            imageRecord.image_id = imageId.toLong()
            imageRecord.project_id = projectId.toInt()
            imageRecord.item_id = itemId.toLong()
            imageRecord.project_image_id = projectImgId.toInt()
            imageRecord.sync_status = SYNC_STATUS_NEW
            imageRecord.error_msg = "??????????????????"
            imageController?.save2LDB(imageRecord)

            //?????????????????????
            mImageCustomView.setBitMap(rbigBmp)
            mImageCustomView.invalidate()
            //?????????????????????????????????
            BlasSyncMessenger.notifyBlasImages(token, projectId)
        }
    }

    //?????????????????????????????????????????????
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == android.R.id.home){
            finish()
        }
        return super.onOptionsItemSelected(item)
    }
}
