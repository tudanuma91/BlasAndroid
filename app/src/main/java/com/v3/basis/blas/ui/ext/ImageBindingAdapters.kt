package com.v3.basis.blas.ui.ext

import android.graphics.Bitmap
import android.util.Base64
import android.widget.ImageView
import androidx.databinding.BindingAdapter


@BindingAdapter("convertBase64")
fun ImageView.setEncodedImage(encodedImage: String?) {

    encodedImage?.also {
        setImageBitmap(Base64.decode(encodedImage, Base64.DEFAULT).translateToBitmap())
    }
}

@BindingAdapter("image")
fun ImageView.setImage(image: Bitmap?) {

    image?.also { setImageBitmap(it) } ?: setImageDrawable(null)
}