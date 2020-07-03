package com.v3.basis.blas.blasclass.db

abstract  class PurserBase {

    abstract fun encode( serialNumber : String) : String

}


class Purser : PurserBase() {
    override fun encode(serialNumber : String ) : String {
        val list = serialNumber.split(",")
        val newSerialNumber = list[0].replace("\n","")
        return newSerialNumber
    }
}


class PurserNCU : PurserBase() {
    override fun encode(serialNumber: String): String {
        // TODO:未

        return serialNumber
    }

}
