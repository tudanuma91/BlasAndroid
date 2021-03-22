package com.v3.basis.blas.ui.item.item_view

import android.graphics.ImageDecoder
import android.graphics.drawable.AnimatedImageDrawable
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.v3.basis.blas.R
import com.v3.basis.blas.blasclass.app.BlasApp
import com.v3.basis.blas.blasclass.config.FieldType
import com.v3.basis.blas.blasclass.controller.FixtureController
import com.v3.basis.blas.blasclass.db.BlasSQLDataBase.Companion.context
import com.v3.basis.blas.blasclass.db.data.ItemsController
import com.v3.basis.blas.blasclass.ldb.LdbFieldRecord
import com.v3.basis.blas.blasclass.log.BlasLog
import com.v3.basis.blas.blasclass.rest.BlasRestEvent
import com.v3.basis.blas.blasclass.rest.SyncBlasRestEvent
import com.v3.basis.blas.blasclass.service.BlasSyncMessenger
import com.v3.basis.blas.blasclass.service.SenderHandler
import com.v3.basis.blas.databinding.ActivitySplashBinding
import com.v3.basis.blas.databinding.InputField23Binding
import com.v3.basis.blas.databinding.InputField23BtnBinding
import com.v3.basis.blas.databinding.ListItemBinding
import com.v3.basis.blas.ui.item.common.FieldText
import com.xwray.groupie.databinding.BindableItem
import io.reactivex.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.input_field23_btn.view.*
import org.json.JSONObject
import org.reactivestreams.Subscriber
import org.reactivestreams.Subscription
import java.lang.Exception
import java.util.logging.Handler
import kotlin.concurrent.withLock

class ItemsListCell(private val viewModel: ItemsListViewModel, val model: ItemsCellModel, val fields:List<LdbFieldRecord>) : BindableItem<ListItemBinding>() {

    var eventLayout:InputField23BtnBinding? = null

    override fun getLayout(): Int = R.layout.list_item

    @RequiresApi(Build.VERSION_CODES.P)//GIFアニメーションを表示するのに必要
    override fun bind(viewBinding: ListItemBinding, position: Int) {
        viewBinding.vm = viewModel
        viewBinding.model = model

        fields.forEach {field->
            if(field.type == FieldType.EVENT_FIELD.toInt()) {
                eventLayout = DataBindingUtil.inflate(
                    LayoutInflater.from(context),
                    R.layout.input_field23_btn,
                    null,
                    false
                )
                eventLayout?.let {binding->
                    //ボタンの表示テキストに項目名を指定する
                    binding.button.text = field.name

                    //疎通確認ボタンがクリックされたとき
                    binding.button.setOnClickListener { buttonView->
                        //LDBのイベントを処理中に更新する
                        val itemController = ItemsController(context, model.project_id.toString())
                        val record = itemController.findByItemId(model.item_id.toString())
                        val eventFld = "fld${field.col.toString()}"
                        record[eventFld] = "処理中"
                        //LDBを更新する
                        itemController.updateToLDB(record)

                        Thread(Runnable {
                            //イベント発行のデータを更新する
                            SenderHandler.lock.withLock {
                                itemController.updateToLDB(record)
                            }
                            //BLASにデータ送信の合図を送る
                            BlasSyncMessenger.notifyBlasItems(model.token, model.project_id.toString())
                        }).start()

                        //ここでLDBの監視を開始させる
                        /*
                        //データ管理の更新用データ作成
                        val payload = mutableMapOf<String, String>()
                        payload["token"] = model.token
                        payload["item_id"] = model.item_id.toString()
                        val fldIndex = "fld${field.col.toString()}"
                        payload[fldIndex] = "処理中"


                        //TODO LDBも更新しないと、表示で不整合起きる
                        //BLASを2次開通にしたけど、データ管理で表示したら処理中になるとかがありえる

                        //非同期でイベント型のデータを更新する
                        Single.fromCallable {
                            SyncBlasRestEvent("update").request(payload)
                        }.subscribeOn(Schedulers.newThread())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeBy(
                                onSuccess = {jsonObj->
                                    //BLASからupdateのレスポンスを受信
                                    val errorCode = jsonObj.getInt("error_code")
                                    if(errorCode == 0) {
                                        //BLASのレコードを更新できたので、状態を監視する
                                        val eventPayload = mapOf("token" to model.token,
                                                                 "project_id" to model.project_id.toString(),
                                                                 "item_id" to model.item_id.toString())
                                        //観測者作成
                                        val evobs = EventObserver(eventPayload, fldIndex, 10*1000)
                                        //イベント受信者(購読者)作成
                                        val subsc = EventSubScriber<String>(buttonView as Button)

                                        //観測開始
                                        Flowable.create<String>(evobs, BackpressureStrategy.BUFFER)
                                            .subscribeOn(Schedulers.newThread())
                                            .subscribe(subsc)

                                        //gifアニメーションを再生する
                                        val drawable = getGifAnimationDrawable()
                                        binding.imageView.setImageDrawable(drawable)
                                        drawable.start()

                                        //疎通確認ボタンを非表示にする
                                        buttonView.visibility = View.INVISIBLE
                                        //処理中の画面を表示する
                                        binding.imageView.visibility = View.VISIBLE
                                    }
                                    else {
                                        //更新に失敗
                                        val message = jsonObj.getString("message")
                                        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                                    }
                                },
                                onError = {
                                    //通信エラーが発生した場合
                                    binding.imageView.visibility = View.INVISIBLE
                                    buttonView.visibility = View.INVISIBLE
                                    Toast.makeText(context, "通信に失敗しました。電波のよいところで実行してください", Toast.LENGTH_LONG).show()
                                }
                            ).addTo(CompositeDisposable())*/

                    }

                    viewBinding.eventLayout.addView(binding.root)
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun getGifAnimationDrawable():AnimatedImageDrawable{
        //画像ソースを取得(assets直下)
        val source = ImageDecoder.createSource(context.assets,"run.gif" )
        return ImageDecoder.decodeDrawable(source) as? AnimatedImageDrawable
            ?: throw ClassCastException()
    }


    //gifを表示する処理＋動かす処理
    private fun animationStart(binding: InputField23BtnBinding){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            //pie以降はこっちの処理を使用する
            val drawable = getGifAnimationDrawable()
            binding.imageView.setImageDrawable(drawable)
            drawable.start()
        }else {
            //pieより前はこっちの処理を使用する
            //後々ここの処理は削除したい。
            Glide.with(context).load(R.drawable.run).into(binding.imageView)
        }
    }

    /**
     * イベントの状態を取得するクラス
     */
    class EventSubScriber<String>(val button:Button): Subscriber<String> {
        var subscription:Subscription? = null

        override fun onComplete() {
            TODO("Not yet implemented")
        }

        override fun onSubscribe(s: Subscription?) {
            s?.request(1)
            subscription = s
        }

        override fun onNext(t: String) {
            if(t == "処理中") {
                //何もしない
                subscription?.request(1)
            }
            else {
                button.text = t.toString()
                //処理中以外のレコードが返ってきたので終了する
                this.dispose()
            }
        }

        override fun onError(t: Throwable?) {
            button.text = t?.message
            this.dispose()
        }

        fun dispose() {
            subscription?.cancel()
        }

    }

}

/**
 * イベント観測クラス。指定した引数の値が更新されていないか監視する
 */
class EventObserver(val payload:Map<String, String>, val fldIndex:String, val interval:Long): FlowableOnSubscribe<String> {

    override fun subscribe(emitter: FlowableEmitter<String>) {
        while(true){
            val jsonObj = SyncBlasRestEvent("search").request(payload)
            val errorCode = jsonObj.getInt("error_code")
            if(errorCode == 0) {
                //BLASのイベント状態を取得できた
                val data = jsonObj.getJSONArray("records")
                    .getJSONObject(0)
                    .getJSONObject("Item")
                    .getString(fldIndex)

                emitter.onNext(data)
            }
            else {
                //BLASに通信できたけど、レコードの更新ができなかったとき
                val message = jsonObj.getString("message")
                throw Exception(message)
            }
            Thread.sleep(interval)
        }
    }

}