package com.v3.basis.blas.ui.item.item_image.model

import io.reactivex.Single
import org.json.JSONObject

data class BlasRestCallable(
    val single: Single<JSONObject>,
    var next: BlasRestCallable?
)
