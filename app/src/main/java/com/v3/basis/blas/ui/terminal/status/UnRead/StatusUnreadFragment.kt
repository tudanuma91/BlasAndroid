package com.v3.basis.blas.ui.terminal.status.UnRead


import android.app.AlertDialog
import android.database.Cursor
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

import com.v3.basis.blas.R
import com.v3.basis.blas.blasclass.db.BlasSQLDataBase
import com.v3.basis.blas.ui.terminal.status.StatusFragment
import com.v3.basis.blas.ui.viewparts.CardRecycler.CardRecyclerStatusUnread
import kotlinx.android.synthetic.main.list_status_unread.*

/**
 * A simple [Fragment] subclass.
 */
class StatusUnreadFragment : Fragment() {

    private val db = BlasSQLDataBase()
    private lateinit var root :View
    private val dataList = mutableListOf<UnReadRowModel>()
    private lateinit var  cursor :Cursor
    private lateinit var recyclerView:RecyclerView
    private lateinit var partRecyclerUnread:CardRecyclerStatusUnread
    private val adapterUnRead :UnReadViewAdapter = UnReadViewAdapter(dataList,object : UnReadViewAdapter.ListListener{
        override fun onClickRow(tappedView: View, unReadRowModel: UnReadRowModel) {
            //Log.d("test","testtest")
        }

        override fun onClickChangeAlready(id: String) {
            Log.d("Hello","say Hello${id}")
            val flg = db.upDateStatus(id)
            if(flg){
                AlertDialog.Builder(activity)
                    .setTitle("メッセージ")
                    .setMessage("既読処理が完了しました。")
                    .setPositiveButton("YES") { dialog, which ->
                        //TODO YESを押したときの処理
                    }
                    .show()
            }else{
                AlertDialog.Builder(activity)
                    .setTitle("メッセージ")
                    .setMessage("既読処理に失敗しました。")
                    .setPositiveButton("YES") { dialog, which ->
                        //TODO YESを押したときの処理
                    }
                    .show()
            }
        }

    })

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        root = inflater.inflate(R.layout.fragment_status_unread, container, false)

        //DBから値を取得
        cursor = db.getRecordUnRead()

        //cardの準備
        val protoRecyclerView = root.findViewById<RecyclerView>(R.id.unread_recyclerView)
        partRecyclerUnread = CardRecyclerStatusUnread(activity, protoRecyclerView, adapterUnRead, cursor)
        recyclerView =partRecyclerUnread.createRecyclerView()
        //データの作成およびモデルへのデータ格納・反映
        dataList.addAll(partRecyclerUnread.createStatusList())
        adapterUnRead.notifyItemInserted(0)


        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

}
