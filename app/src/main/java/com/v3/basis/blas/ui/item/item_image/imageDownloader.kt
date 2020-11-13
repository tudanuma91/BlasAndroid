package com.v3.basis.blas.ui.item.item_image

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.v3.basis.blas.R
import com.v3.basis.blas.blasclass.controller.ImagesController
import com.v3.basis.blas.blasclass.db.BaseController
import io.reactivex.FlowableEmitter
import com.v3.basis.blas.blasclass.db.BlasSQLDataBase.Companion.context
import com.v3.basis.blas.blasclass.ldb.LdbItemImageRecord
import io.reactivex.FlowableOnSubscribe


class ImageDownLoader(val r: Resources,
                      val token:String,
                      val projectId:String,
                      val item_id:String,
                      val imageList: MutableList<LdbItemImageRecord>) : FlowableOnSubscribe<LdbItemImageRecord> {
    val controller = ImagesController(context, projectId)
    val downloadQueue = mutableListOf<Thread>()

    fun stop() {
        downloadQueue.forEach {th->
            th.interrupt()
        }
    }

    override fun subscribe(emitter: FlowableEmitter<LdbItemImageRecord>) {
        //ここに画像ダウンロードの処理を書く
        val MAX_THREAD_NUM=4
        try {
            imageList.forEach { itemImage ->
                //MAX_THREAD_NUMスレッドずつダウンロードする
                if (emitter.isCancelled) {
                    //これでは後ろの処理が走っているときに止められない
                    return
                }

                val thread = imageDownloader(controller, token, item_id, itemImage)
                //max_thread_numを超えるまでは、キューに貯める
                downloadQueue.add(thread)
                if (downloadQueue.size == MAX_THREAD_NUM) {
                    downloadQueue.forEach {
                        //max_thread_numスレッドだけ画像ダウンロード実行
                        it.start()
                    }

                    downloadQueue.forEach { th ->
                        //ここで待ち
                        th.join()
                        emitter.onNext(itemImage)
                    }
                    downloadQueue.clear()
                }
            }
            //最後のQueue実行（最後が5未満の場合)
            downloadQueue.forEach { th ->
                th.start()
            }

            //待ち
            downloadQueue.forEach { th ->
                th.join()
            }

            emitter.onComplete()
        }
        catch(e:Exception) {
        }
    }


    inner class imageDownloader(val controller: ImagesController,
                                val token:String,
                                val item_id:String,
                                val itemImage: LdbItemImageRecord): Thread() {

        override fun run() {
            super.run()
            var bitmap: Bitmap? = null
            try {
                for (i in 0 until 3) {//リトライループ
                    try {
                        bitmap =
                            controller.getSmallImage(
                                token,
                                item_id,
                                itemImage.project_image_id.toString()
                            )
                        break
                    } catch (e: java.lang.Exception) {
                        //1秒間隔で3回リトライ
                        Thread.sleep(1 * 1000)
                    }
                    catch(e: InterruptedException) {
                        break
                    }
                }

                if(bitmap != null) {
                    itemImage.bitmap = bitmap
                }
                else {
                    itemImage.bitmap = BitmapFactory.decodeResource(r, R.drawable.imageselect)
                }
                /*
                if(bitmap != null) {
                    itemImage.bitmap = bitmap
                    //ローカルDBにダウンロード済みとして保存
                    val projectId = itemImage.project_id
                    val controller = ImagesController(context, projectId.toString())
                    try{
                        itemImage.sync_status = BaseController.SYNC_STATUS_SYNC
                        controller?.save2LDB(itemImage)
                    }
                    catch(e: java.lang.Exception) {
                        e.printStackTrace()
                    }
                }
                else {
                    itemImage.bitmap = BitmapFactory.decodeResource(r, R.drawable.imageselect)
                }*/

                itemImage.downloadProgress = false
            }
            catch(e: InterruptedException) {
                Log.d("konishi", "InterruptedException happen!!")
            }
        }
    }
}


