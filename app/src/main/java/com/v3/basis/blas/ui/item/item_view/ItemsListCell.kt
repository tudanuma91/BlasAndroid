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

class ItemsListCell(
    private val viewModel: ItemsListViewModel
    , val model: ItemsCellModel
    , val fields:List<LdbFieldRecord>
    , val editEnabled:Boolean = false
) : BindableItem<ListItemBinding>() {

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

        if( !editEnabled ) {
            viewBinding.editButton.visibility = View.GONE
        }
    }
}


