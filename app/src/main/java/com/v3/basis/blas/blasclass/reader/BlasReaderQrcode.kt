package com.v3.basis.blas.blasclass.reader

/**
 * QRコードを読み取る処理を書くクラス
 */
class BlasReaderQrcode : BlasReader() {
    val aaa =BlasReader().test

    open fun create(){
        //TODO:リーダーの初期する処理。最初に行う
    }

    open fun scan(){
        //TODO:バーコードを読み取る処理
    }

    open fun decode(){
        //TODO:読み取ったバーコードのでコードをする処理
    }
}