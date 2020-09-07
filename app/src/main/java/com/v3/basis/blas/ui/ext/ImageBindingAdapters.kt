package com.v3.basis.blas.ui.ext

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.util.Base64
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.module.GlideModule
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.v3.basis.blas.ui.item.item_image.ItemImageCellItem
import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit


@BindingAdapter("convertBase64")
fun ImageView.setEncodedImage(encodedImage: String?) {

    encodedImage?.also {
        setImageBitmap(Base64.decode(encodedImage, Base64.DEFAULT).translateToBitmap())
    }
}

@BindingAdapter("image")
fun ImageView.setImage(image: Bitmap?) {

    image?.also {
        setImageBitmap(it)
    } ?: setImageDrawable(null)
}

@BindingAdapter("url")
fun ImageView.setImage(image: String?) {

    if (image.isNullOrBlank().not()) {
        Glide.with(this).load(image).into(this)
    }
}

@BindingAdapter(value = ["decodeImage", "decodeModel"], requireAll = false)
fun ImageView.decodeImage(image: String?, model: ItemImageCellItem) {

    if (image.isNullOrBlank().not()) {
        Glide.with(this)
            .asBitmap()
            .load(image)
            .into(object : CustomTarget<Bitmap>(){
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    model.loading.set(false)
                    model.image.set(resource)
                    model.bitmapEvent.onNext(resource)
                }
                override fun onLoadCleared(placeholder: Drawable?) {
                }
            })
    }
}
