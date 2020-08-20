package com.v3.basis.blas.activity

import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.databinding.ObservableField
import com.v3.basis.blas.R
import com.v3.basis.blas.blasclass.controller.ImageControllerException
import com.v3.basis.blas.blasclass.controller.ImagesController
import com.v3.basis.blas.blasclass.db.BlasSQLDataBase.Companion.context
import com.v3.basis.blas.ui.item.item_image.ItemImageCellItem
import com.v3.basis.blas.ui.item.item_image_zoom.custom_view.CustomImageView
import io.reactivex.Single
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
        const val TITLE = "title"

    }
    private val projectImgId: String
        get() = intent.extras?.getString(PROJECT_IMG_ID) ?: ""

    private val projectId: String
        get() = intent.extras?.getString(PROJECT_ID) ?: ""

    private val itemId: String
        get() = intent.extras?.getString(ITEM_ID) ?: ""

    private val title: String
        get() = intent.extras?.getString(TITLE) ?: ""

    private var disposable = CompositeDisposable()
    private lateinit var  mImageCustomView : CustomImageView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_item_image_zoom)
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
                        mImageCustomView.setBitMap(it.first)
                        Log.d("ImageZoom[fetchImage()]","画像取得成功")
                    }catch (e:Exception){
                        createToast()
                        Log.d("ImageZoom[fetchImage()]","ローカルで取得失敗")
                    }
                }
            ).addTo(disposable)
    }

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