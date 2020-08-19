package com.v3.basis.blas.activity

import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.databinding.DataBindingUtil
import androidx.databinding.ObservableField
import com.v3.basis.blas.R
import com.v3.basis.blas.blasclass.controller.ImageControllerException
import com.v3.basis.blas.blasclass.controller.ImagesController
import com.v3.basis.blas.blasclass.db.BlasSQLDataBase.Companion.context
import com.v3.basis.blas.ui.item.item_image.ItemImageCellItem
import com.v3.basis.blas.ui.item.item_image_zoom.ItemImage
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

    }
    private val projectImgId: String
        get() = intent.extras?.getString(PROJECT_IMG_ID) ?: ""

    private val projectId: String
        get() = intent.extras?.getString(PROJECT_ID) ?: ""

    private val itemId: String
        get() = intent.extras?.getString(ITEM_ID) ?: ""

    private var disposable = CompositeDisposable()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_item_image_zoom)


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
                            Log.d("[木島テスト]","分岐=>1")
                        } else if (it.errorCode == 2) {
                            //リモート問い合わせ
                            Log.d("[木島テスト]","分岐=>2")
                        }
                        else {
                            //その他エラー
                            Log.d("[木島テスト]","分岐=>3")
                        }
                    } else {
                        //想定外のエラー
                        Log.d("[木島テスト]","分岐=>4")
                    }
                },
                onSuccess = {
                    //searchFromLocalで取得した画像を表示する
                    try {
                        findViewById<ImageView>(R.id.image).setImageBitmap(it.first)
                        Log.d("[木島テスト]", "分岐=>5")
                    }catch (e:Exception){
                        Log.d("[木島テスト]","怖いからcatchする")
                    }
                }
            ).addTo(disposable)
    }


}