package com.v3.basis.blas.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.fragment.app.Fragment
import com.v3.basis.blas.R
import com.v3.basis.blas.ui.test3.Test3First.Test3FirstFragment
import com.v3.basis.blas.ui.test3.Test3Second.Test3Second2Fragment
import com.v3.basis.blas.ui.test3.Test3Second.Test3SecondFragment

class Test3Activity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        displayManager(1)

        /* val layout = LinearLayout(this)
        setContentView(layout)

        val button = Button(this)
        button.text = "send"
        layout.addView(button)*/


    }
     //R.id.containerに引数で渡されたフラグメントを入れる。
    fun replaceFragment(fragment: Fragment) {
         val fragmentManager = supportFragmentManager
         val fragmentTransaction = fragmentManager.beginTransaction()
         fragmentTransaction.replace(R.id.container, fragment)
         fragmentTransaction.commit()
         Log.d("title","呼ばれた")
     }

    fun displayManager(number : Int){
        when(number){
            1 ->{//テスト画面へ遷移
                setContentView(R.layout.activity_test3)
                val firstbutton = findViewById<Button>(R.id.firstButton)
                val secondButton = findViewById<Button>(R.id.secondButton)

                //FirstFragmentActivityクラスをインスタンス化その下も同様。
                val firstFragment = Test3FirstFragment()
                val secondFragment = Test3SecondFragment()
                //buttonをクリックしたときにreplaceFragmentメソッドを実行
                firstbutton.setOnClickListener {
                    replaceFragment(firstFragment)
                }
                secondButton.setOnClickListener {
                    replaceFragment(secondFragment)
                }
            }
            2->{//テスト画面へ遷移
                setContentView(R.layout.activity_test2)
                Log.d("aaa","aaaaaaa")
            }
        }
    }

}