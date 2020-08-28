package com.v3.basis.blas.blasclass.app

import android.app.Application
import android.content.Context
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics


class BlasApp : Application() {

    override fun onCreate() {
        super.onCreate()
        firebaseAnalytics = FirebaseAnalytics.getInstance(this)

        // Setup handler for uncaught exceptions.
//        Thread.setDefaultUncaughtExceptionHandler { thread, e ->
//            FirebaseCrashlytics.getInstance().recordException(e)
//            FirebaseCrashlytics.getInstance().sendUnsentReports()
//            Thread.setDefaultUncaughtExceptionHandler(null)
//            throw e
//        }
    }

    init {
        instance = this
    }

    companion object {
        private var instance: BlasApp? = null
        private lateinit var firebaseAnalytics: FirebaseAnalytics
        var token: String? = null
        var userId : Int? = 0
        var password : String? = null

        fun applicationContext() : Context {
            return instance!!.applicationContext
        }

        fun analytics(): FirebaseAnalytics {
            return firebaseAnalytics
        }
    }
}
