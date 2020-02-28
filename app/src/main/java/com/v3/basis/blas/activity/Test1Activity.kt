package com.v3.basis.blas.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.v3.basis.blas.R
import com.v3.basis.blas.blasclass.controller.QueueController

/**
 * 小西用実験コード
 */
class Test1Activity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test1)
    }

    override fun onStart() {
        super.onStart()
        QueueController.start()
    }

    override fun onStop() {
        super.onStop()
        QueueController.stop()
    }

    override fun onResume() {
        super.onResume()
    }
}
