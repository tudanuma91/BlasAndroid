package com.v3.basis.blas.ui.item.item_view

import android.graphics.ImageDecoder
import android.graphics.drawable.AnimatedImageDrawable
import android.os.Build
import android.os.Handler
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
import com.v3.basis.blas.blasclass.service.EventListener
import com.v3.basis.blas.blasclass.service.SenderHandler
import com.v3.basis.blas.databinding.ActivitySplashBinding
import com.v3.basis.blas.databinding.InputField23BtnBinding
import com.v3.basis.blas.databinding.ListItemBinding
import com.v3.basis.blas.ui.item.common.FieldText
import com.xwray.groupie.databinding.BindableItem
import com.xwray.groupie.databinding.GroupieViewHolder
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

import kotlin.concurrent.withLock

class ItemsListCell(private val viewModel: ItemsListViewModel, val model: ItemsCellModel, val fields:List<LdbFieldRecord>) : BindableItem<ListItemBinding>() {

    //var eventLayout:InputField23BtnBinding? = null

    override fun getLayout(): Int = R.layout.list_item

    override fun createViewHolder(itemView: View): GroupieViewHolder<ListItemBinding> {
        val holder = super.createViewHolder(itemView)
        //addPingForm(holder.binding)
        return holder
    }

    @RequiresApi(Build.VERSION_CODES.P)//GIFアニメーションを表示するのに必要
    override fun bind(viewBinding: ListItemBinding, position: Int) {
        viewBinding.vm = viewModel
        viewBinding.model = model
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

    fun addPingForm(viewBinding: ListItemBinding) {

        fields.forEach {field->
            if(field.type == FieldType.EVENT_FIELD.toInt()) {
                //レイアウトをバインドする
                val eventLayout:InputField23BtnBinding = DataBindingUtil.inflate(
                    LayoutInflater.from(context),
                    R.layout.input_field23_btn,
                    null,
                    false
                )

                val itemController = ItemsController(context, model.project_id.toString())
                val itemRecord = itemController.findByItemId(model.item_id.toString())
                val eventFld = "fld${field.col.toString()}"

                eventLayout?.let {binding->
                    //LDBの監視を登録する
                    var label:String = ""
                    field.name?.let {
                        label = it
                    }
                    //LDBの監視開始
                    val ev = ItemEventListener(model.item_id, eventFld, label, binding)
                    SenderHandler.eventCallbackList.add(ev)
                    //カラム名設定
                    binding.colname.text = field.name
                    //ボタンのテキスト設定
                    binding.button.text = field.name
                    //GIFアニメーション開始
                    animationStart(binding)

                    if(itemRecord[eventFld] == "処理中") {
                        //処理中のGIF表示
                        binding.imageView.visibility = View.VISIBLE
                        //ボタンの非表示
                        binding.button.visibility = View.GONE
                    }
                    else {
                        //処理中のGIF非表示
                        binding.imageView.visibility = View.GONE
                    //ボタンの表示
                        binding.button.visibility = View.VISIBLE
                        //イベント型の値を表示
                        binding.status.text = itemRecord[eventFld]
                    }

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
                            //イベントの監視の合図を送る
                            BlasSyncMessenger.notifyBlasEvents(model.token, model.project_id.toString())
                        }).start()

                    }

                    viewBinding.eventLayout.addView(binding.root)
                }
            }
        }
    }
}

/**
 * イベント監視クラス
 */
class ItemEventListener(override val itemId: Long, val fieldIndex:String, val label:String, val binding: InputField23BtnBinding): EventListener {

    val guiHandler = Handler()

    override fun callBack(itemRecord: MutableMap<String, String>) {
        //ここがスレッドからの呼び出しになるため、エラー…。
        //rxkotlinに切り替えを検討するか、はてさて…
        /*BlasLog.trace("I","callbackが呼ばれました")
        BlasLog.trace("I", itemRecord.toString())
        guiHandler.post{
            if(itemRecord[fieldIndex] != "処理中") {
                binding.button.text = label
                binding.button.visibility = View.VISIBLE
                binding.imageView.visibility = View.GONE
                binding.status.visibility = View.VISIBLE
                binding.status.text = itemRecord[fieldIndex]
            }
            else {
                binding.button.visibility = View.GONE
                binding.imageView.visibility = View.VISIBLE
                binding.status.visibility = View.GONE
            }
        }*/
    }
}

