package com.v3.basis.blas.blasclass.db.data

data class Images(
    var image_id: Long? = 0,
    var project_id: Int? = 0,
    var project_image_id: Int? = 0,
    var item_id: Long? = 0,
    var filename: String? = null,
    var hash: String? = null,
    var moved: Int? = 0,
    var create_date: String? = null,
    var sync_status: Int? = 0,
    var error_msg:String? = null
)