package com.v3.basis.blas.blasclass.app

import android.app.Application
import android.content.Context


class BlasApp : Application() {

    override fun onCreate() {
        super.onCreate()
    }

    init {
        instance = this
    }

    companion object {
        private var instance: BlasApp? = null

        fun applicationContext() : Context {
            return instance!!.applicationContext
        }
    }
}