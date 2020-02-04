package com.v3.basis.blas.blasclass.queuing

/**
 * キューイングクラスの親クラス
 */
abstract class BlasQueing {
    companion object {
        const val test = "test1"
    }

    open fun create(){
        //TODO:キューイング作成の処理を書く
    }

    open fun enque(){
        //TODO:エンキューの処理を書く
    }

    open fun deque(){
        //TODO:デキューの処理を書く
    }
}