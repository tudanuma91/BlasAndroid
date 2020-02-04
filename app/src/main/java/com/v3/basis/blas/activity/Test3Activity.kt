package com.v3.basis.blas.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.v3.basis.blas.R
import com.v3.basis.blas.Test3RecyclerviewFragment
import com.v3.basis.blas.ui.test1.Test1Fragment
import kotlinx.android.synthetic.main.fragment_test3.*

class Test3Activity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test3)

         if(savedInstanceState == null){
            /*login_test1.setOnClickListener{
                Log.d("【test1】","test1がクリックされました")
                val fragmentManager = supportFragmentManager
                val fragmentTransaction = fragmentManager.beginTransaction()
                // BackStackを設定
                fragmentTransaction.addToBackStack(null)
                // パラメータを設定
                fragmentTransaction.replace(
                    R.id.container,
                    Test1Fragment.newInstance("Fragment1")
                )
                fragmentTransaction.commit()
            }
            login_test2.setOnClickListener{
                Log.d("【test2】","test2がクリックされました")
            }
            login_test3.setOnClickListener{
                Log.d("【test3】","test3がクリック
                        */

        }

    }
}
