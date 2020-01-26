package com.v3.basis.blas.blasclass.rest

open class BlasRestData : BlasRest() {
    val GET_DATAFIELD_URL = BlasRest.URL + "project_fields/search"
    val CREATE_ITEM_URL = BlasRest.URL + "items/create"
    val ITEM_URL  = BlasRest.URL +"items"

    override fun doInBackground(vararg params: String?): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}