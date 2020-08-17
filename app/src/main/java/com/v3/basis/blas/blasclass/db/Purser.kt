package com.v3.basis.blas.blasclass.db

abstract  class PurserBase {

    abstract fun encode( serialNumber : String) : MutableList<String>

}


class Purser : PurserBase() {
    override fun encode(serialNumber : String ) : MutableList<String> {
        val list = serialNumber.split(",")
        val newSerialNumber = list[0].replace("\n","")
        return mutableListOf(newSerialNumber)
    }
}


class PurserNCU : PurserBase() {
    override fun encode(serialNumber: String): MutableList<String> {
        var serials = mutableListOf<String>()
        var fixtureType = 0

        //スペースでトークンを分解
        // var qr_code = qr_code.replace("\n", "")
        val tokens = serialNumber.split("\n", " ")
        var i = 0
        while(i < tokens.count()) {
            if(tokens[i] == "BOX") {
                fixtureType = 1
                while(!tokens[i].endsWith("UNITS")) {
                    i++
                    if(i >= tokens.count()){
                        break
                    }
                }
            }
            else if(tokens[i] == "PACKAGE") {
                while(!tokens[i].endsWith("UNITS")) {
                    i++
                    if(i >= tokens.count()){
                        break
                    }
                }
            }
            else {
                if(tokens[i] != "") {
                    serials.add(tokens[i])
                }
            }
            i++
        }

        if(fixtureType == 0) {
            serials.clear()
            val lastIndex = tokens.count() - 1
            serials.add(tokens[lastIndex])
        }

        //箱を読んだときは、10個連番で読み取る
        if(serials.count() == 1) {
            val originalSerial = serials[0]
            var tail = originalSerial.length
            val lastChar = originalSerial[tail - 1]

            if(lastChar == 'S') {
                val len = originalSerial.length
                var serial = originalSerial.substring(0, len - 4)
                var suffix = originalSerial.substring(len - 4, len-1)
                serials.clear()
                for(i in 0 until 10){
                    var intSerial = serial.toInt() + i
                    serials.add("${intSerial}${suffix}")
                }
            }
        }

        return serials
    }
}

class PurserCSVFirst : PurserBase() {
    override fun encode(serialNumber: String): MutableList<String> {
        var serials = mutableListOf<String>()
        val tokens = serialNumber.split(",")
        serials.add(tokens[0])
        return serials
    }
}

class PurserCSV : PurserBase() {
    override fun encode(serialNumber: String): MutableList<String> {
        var serials = mutableListOf<String>()
        val tokens = serialNumber.split(",", "\n")
        tokens.forEach{
            serials.add(it)
        }
        return serials
    }
}