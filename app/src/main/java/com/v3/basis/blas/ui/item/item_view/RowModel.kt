package com.v3.basis.blas.ui.item.item_view


class RowModel {
    var title: String = ""
    var detail: String = ""
    var token : String? = null
    var projectId : String? = null
    var itemList: MutableList<MutableMap<String, String?>> = mutableListOf()
    var itemId: String? = null
    var projectNames : String? = null
    var errMsg : String? = null
}
