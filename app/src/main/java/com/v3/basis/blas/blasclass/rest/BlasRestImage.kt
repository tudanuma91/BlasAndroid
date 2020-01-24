package com.v3.basis.blas.blasclass.rest

class BlasRestImage : BlasRest() {
    val GET_IMAGEFIELD_URL = BlasRest().URL + "project_images/search"
    val UPLOAD_IMAGE_URL = BlasRest().URL +"images/upload"
    val DOWNLOAD_IMAGE_URL = BlasRest().URL +"images/download"
    val DELETE_IMAGE_URL = BlasRest().URL +"images/delete"
}