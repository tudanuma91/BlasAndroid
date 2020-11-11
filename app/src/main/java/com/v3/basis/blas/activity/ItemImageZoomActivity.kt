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
import com.v3.basis.blas.blasclass.db.BaseController
import com.v3.basis.blas.blasclass.db.BlasSQLDataBase.Companion.context
import com.v3.basis.blas.blasclass.rest.SyncBlasRestImage
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

    private val imageId: String
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
        bind.loading = false
        bind.activity = this

        supportActionBar?.title = title
        //上の矢印表示
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        //layoutに埋め込んだCustomImageViewを取得する
        mImageCustomView = findViewById(R.id.customView)
        //画像の取得処理
        fetchImage()

    }

    //画像取得処理
    fun fetchImage() {
        val imageController = ImagesController(context, projectId)
        // ローカルから画像ファイルを取得する
        Single.fromCallable { imageController.searchFromLocal(context, itemId, projectImgId) }
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .subscribeBy(
                onError = {
                    if (it is ImageControllerException) {
                        if (it.errorCode == 1) {
                            //ローカルにもリモートにも画像なし
                            createToast()
                            Log.d("ImageZoom[fetchImage()]","errorCode = 1")
                        } else if (it.errorCode == 2) {
                            //リモート問い合わせ
                            getImageUrl()
                            createToast()
                            Log.d("ImageZoom[fetchImage()]","errorCode = 2")
                        }
                        else {
                            //その他エラー
                            createToast()
                            Log.d("ImageZoom[fetchImage()]","errorCode = 3")
                        }
                    } else {
                        //想定外のエラー
                        createToast()
                        Log.d("ImageZoom[fetchImage()]","errorCode = 4")
                    }
                },
                onSuccess = {
                    //searchFromLocalで取得した画像を表示する
                    try {
                        //findViewById<ImageView>(R.id.image).setImageBitmap(it.first)
                        if (it.first.width <= 230) {
                            getImageUrl()
                            Log.d("ImageZoom[fetchImage()]","サムネイル画像しかないため、リモートから取り直し")
                        } else {
                            mImageCustomView.setBitMap(it.first)
                            mImageCustomView.invalidate()
                            Log.d("ImageZoom[fetchImage()]","画像取得成功")
                        }
                    }catch (e:Exception){
                        createToast()
                        Log.d("ImageZoom[fetchImage()]","ローカルで取得失敗")
                    }
                }
            ).addTo(disposable)
    }

    //  画像URLをリモートから取得する
    private fun getImageUrl() {

        val payload = mapOf("token" to token, "item_id" to itemId, "project_image_id" to projectImgId)
        Single
            .fromCallable {
                val json = SyncBlasRestImage().getUrl(payload)
                Gson().fromJson(json.toString(), ItemImageWithLink::class.java)
            }
            .subscribeOn(Schedulers.newThread())
            .doOnError {
                //とりあえず呼び出し側に通知
            }
            .doOnSuccess {
                setImage( it.records.firstOrNull()?.Image?.image )
            }
            .subscribe()
            .addTo(disposable)
    }

    //  URLから画像を取得して、Viewにセットする
    private fun setImage(url: String?) {
        /*
        if (url.isNullOrBlank().not()) {
            Glide.with(this)
                .asBitmap()
                .load(BuildConfig.HOST + url)
                .into(object : CustomTarget<Bitmap>(){
                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                        mImageCustomView.setBitMap(resource)
                        mImageCustomView.invalidate()
                        updateLocalImage(resource)
                        Log.d("ImageZoom[fetchImage()]","リモートから画像取得成功")
                    }
                    override fun onLoadCleared(placeholder: Drawable?) {
                    }
                })
        }*/
    }

    //画像を右回転する
    fun rightRotate() {
        /*
        if (bind.loading == true || mImageCustomView.mBitmap == null) {
            return
        }

        bind.loading = true
        Single.fromCallable { mImageCustomView.mBitmap!!.rotateRight() }
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onError = { bind.loading = false },
                onSuccess = {
                    updateLocalImage(it)
                    setResult(Activity.RESULT_OK)
                }
            )
            .addTo(disposable)*/
    }

    //  画像を左回転する
    fun leftRotate() {
        /*
        if (bind.loading == true || mImageCustomView.mBitmap == null) {
            return
        }

        bind.loading = true
        Single.fromCallable { mImageCustomView.mBitmap!!.rotateLeft() }
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onError = { bind.loading = false },
                onSuccess = {
                    updateLocalImage(it)
                    setResult(Activity.RESULT_OK)
                }
            )
            .addTo(disposable)*/
    }

    //  画像を左回転
    /*
    private fun updateLocalImage(bitmap: Bitmap) {
        Single
            .fromCallable {
                //リモートから画像をダウンロードできているので、imageIdは必ずある。
                //リモートからダウンロードした画像は本登録する。
                val itemRecord = ItemImageModel(
                    image_id=imageId,
                    item_id = itemId,
                    moved="0",
                    project_id=projectId,
                    project_image_id = projectImgId)

                itemRecord.bitmap = bitmap
                save2DB(itemRecord, BaseController.SYNC_STATUS_NEW)
                bitmap
            }
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onError = { bind.loading = false },
                onSuccess = {
                    bind.loading = false
                    mImageCustomView.setBitMap(it)
                    mImageCustomView.invalidate()
                }
            )
            .addTo(disposable)
    }*/

    //ローカルに画像を保存する（デカイ画像）
    /*
    private fun save2DB(record: ItemImageModel, status: Int) {
        Completable
            .fromAction {
             //   imageController.save2LDB(record, status)
            }
            .subscribeOn(Schedulers.io())
            .subscribe()
            .addTo(disposable)
    }*/

    //矢印ボタンで戻るを実行する処理
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == android.R.id.home){
            finish()
        }
        return super.onOptionsItemSelected(item)
    }


    //エラー時のトースト作成処理
    private fun createToast(){
        val text = getString(R.string.error_image_get)
        Toast.makeText(context,text,Toast.LENGTH_SHORT).show()
    }


}
