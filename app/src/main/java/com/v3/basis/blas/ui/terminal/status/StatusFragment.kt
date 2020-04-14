package com.v3.basis.blas.ui.terminal.status


import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button

import com.v3.basis.blas.R
import com.v3.basis.blas.ui.ext.getStringExtra
import com.v3.basis.blas.ui.terminal.status.AlreadyRead.StatusAlreadyFragment
import com.v3.basis.blas.ui.terminal.status.UnRead.StatusUnreadFragment

/**
 * A simple [Fragment] subclass.
 */
class StatusFragment : Fragment() {

    private var token: String? = null
    private lateinit var root:View
    private lateinit var alreadyButton:Button
    private lateinit var unReadButton:Button


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        token = getStringExtra("token") //トークンの値を取得
        root = inflater.inflate(R.layout.fragment_status, container, false)
        alreadyButton = root.findViewById<Button>(R.id.btn1)
        unReadButton = root.findViewById<Button>(R.id.btn2)

        displayManager(0)

        return root

    }

    private fun displayManager(number:Int){
        when(number){
            0->{
                //ボタンにフラグメントを設定
                alreadyButton.setBackgroundColor(Color.rgb(255,239,255))
                alreadyButton.setOnClickListener{
                    val firstFragment =
                        StatusUnreadFragment()
                    replaceFragment(firstFragment)
                    alreadyButton.setBackgroundColor(Color.rgb(255,239,255))
                    unReadButton.setBackgroundColor(Color.rgb(255,255,255))
                }
                //ボタンにフラグメントを設定
                unReadButton.setOnClickListener{
                    val secondFragment =
                        StatusAlreadyFragment()
                    replaceFragment(secondFragment)
                    alreadyButton.setBackgroundColor(Color.rgb(255,255,255))
                    unReadButton.setBackgroundColor(Color.rgb(255,239,239))
                }

                val firstFragment =
                    StatusUnreadFragment()
                replaceFragment(firstFragment)

            }
            1->{
                val firstFragment =
                    StatusUnreadFragment()
                replaceFragment(firstFragment)

            }
            2->{
                val secondFragment =
                    StatusAlreadyFragment()
                replaceFragment(secondFragment)
            }
        }
    }

    fun replaceFragment(fragment: Fragment) {
        val fragmentManager = activity!!.supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.cont2, fragment)
        fragmentTransaction.commit()
    }





}
