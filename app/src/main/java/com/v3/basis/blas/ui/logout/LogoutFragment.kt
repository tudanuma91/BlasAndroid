package com.v3.basis.blas.ui.logout

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.v3.basis.blas.R
import com.v3.basis.blas.blasclass.rest.SyncBlasRestInquiry
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
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
                .setPositiveButton("YES") { dialog, which ->
                    //TODO YESを押したときの処理
                    requireActivity().finish()
                }
                .setNegativeButton("NO") { dialog, which ->
                    //TODO NOを押したときの処理
                }
                .show()
        }

        btn_inquiry.setOnClickListener {

            AlertDialog.Builder(activity)
                .setTitle("メッセージ")
                .setMessage("問合せのためデータを送信します")
                .setPositiveButton("YES") { dialog,which ->

                    Single.fromCallable {
                        SyncBlasRestInquiry().execute()
                    }.subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeBy (

                            onSuccess = {
                                Log.d("inquiry","onSuccess")
                            },
                            onError = {
                                Log.d("inquiry","onError!!!!!!!!!!")
                            }

                        ).addTo(CompositeDisposable())


                }
                .setNegativeButton("NO") { dialog,which ->
                    // TODO:???
                }
                .show()

        }

    }
}
