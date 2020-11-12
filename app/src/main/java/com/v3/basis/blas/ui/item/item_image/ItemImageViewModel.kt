package com.v3.basis.blas.ui.item.item_image

import androidx.lifecycle.ViewModel
import com.v3.basis.blas.blasclass.ldb.LdbItemImageRecord

class ItemImageViewModel(): ViewModel() {
    var itemImages:MutableList<LdbItemImageRecord> = mutableListOf()
}
