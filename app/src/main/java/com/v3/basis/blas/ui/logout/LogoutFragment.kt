package com.v3.basis.blas.ui.logout

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.v3.basis.blas.R

class LogoutFragment : Fragment() {

    private lateinit var notificationsViewModel: LogoutViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AlertDialog.Builder(activity)
            .setTitle("メッセージ")
            .setMessage("ログアウトしますか？")
            .setPositiveButton("YES",{dialog, which ->
                //TODO YESを押したときの処理
            })
            .setNegativeButton("NO", { dialog, which ->
                //TODO NOを押したときの処理
            })
            .show()

    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        notificationsViewModel =
            ViewModelProviders.of(this).get(LogoutViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_logout, container, false)
        val textView: TextView = root.findViewById(R.id.logout)
        notificationsViewModel.text.observe(this, Observer {
            textView.text = it
        })

        return root
    }
}