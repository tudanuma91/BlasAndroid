package com.v3.basis.blas.blasclass.app

import android.util.Log
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

class BlasCom {

}

    /**
     * オブジェクトをJSON文字列に変換するメソッド
     * [引数]
     * stream(オブジェクト) :　restful通信にて取得したデータ。
     *
     * [返り値]
     * sb.toString(文字列) : streamを文字列にして返す。
     * */
    fun is2String(stream: InputStream): String{
        val sb = StringBuilder()
        val reader = BufferedReader(InputStreamReader(stream,"UTF-8"))
        Log.d("[rest/BlasRest]","{$reader}")
        var line = reader.readLine()
        if(line != null){
            sb.append(line)
            line = reader.readLine()
        }
        reader.close()
        return sb.toString()
    }

