package com.v3.basis.blas.blasclass.rest

class BlasRestData : BlasRest() {
    val GET_DATAFIELD_URL = BlasRest().URL + "project_fields/search"
    val CREATE_ITEM_URL = BlasRest().URL + "items/create"
    val ITEM_URL  = BlasRest().URL +"items"

}