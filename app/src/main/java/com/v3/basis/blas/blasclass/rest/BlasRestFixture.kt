package com.v3.basis.blas.blasclass.rest

open class BlasRestFixture : BlasRest() {


    val CREATE_FIXTURE_URL  = BlasRest.URL +"fixtures/create"
    val GET_FIXTURE_URL = BlasRest.URL +"fixtures/search"
    val UPDATE_FIXTURE_URL = BlasRest.URL +"fixtures/update"
    val DELETE_FIXTURE_URL = BlasRest.URL +"fixtures/delete"
    val KENPIN_FIXTURE_URL = BlasRest.URL +"fixtures/kenpin"
    val TOUROKU_FIXTURE_URL = BlasRest.URL +"fixtures/takeout"
    val RETURN_FIXTURE_URL = BlasRest.URL +"fixtures/trn"

    override fun doInBackground(vararg params: String?): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}