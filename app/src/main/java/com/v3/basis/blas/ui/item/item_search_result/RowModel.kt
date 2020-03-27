package com.v3.basis.blas.ui.item.item_search_result

import android.widget.ImageButton

class RowModel {
    var title: String = ""
    var detail: String = ""
    var token : String? = null
    var projectId : String? = null
    var itemList: MutableList<MutableMap<String, String?>> = mutableListOf()
    var image: ImageButton? = null
    var image2 : ImageButton? = null
}
