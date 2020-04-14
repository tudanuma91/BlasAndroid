package com.v3.basis.blas.ui.logout

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.v3.basis.blas.R
import kotlinx.android.synthetic.main.fragment_logout.*

class LogoutFragment : Fragment() {

    private lateinit var notificationsViewModel: LogoutViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        notificationsViewModel =
            ViewModelProviders.of(this).get(LogoutViewModel::class.java)

        return inflater.inflate(R.layout.fragment_logout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btn_logout.setOnClickListener {

            AlertDialog.Builder(activity)
                .setTitle("メッセージ")
                .setMessage("ログアウトしますか？")
                .setPositiveButton("YES",{dialog, which ->
                    //TODO YESを押したときの処理
                    requireActivity().finish()
                })
                .setNegativeButton("NO", { dialog, which ->
                    //TODO NOを押したときの処理
                })
                .show()
        }
    }
}
