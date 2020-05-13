package com.v3.basis.blas.ui.terminal.status.AlreadyRead


import android.database.Cursor
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.v3.basis.blas.R
import com.v3.basis.blas.blasclass.db.BlasSQLDataBase
import com.v3.basis.blas.blasclass.helper.RestHelper
import com.v3.basis.blas.blasclass.rest.BlasRestProject
import com.v3.basis.blas.ui.ext.getStringExtra
import com.v3.basis.blas.ui.terminal.status.UnRead.AlreadyRowModel
import com.v3.basis.blas.ui.terminal.status.UnRead.AlreadyViewAdapter
import com.v3.basis.blas.ui.viewparts.CardRecycler.CardRecyclerStatusAlready
import org.json.JSONObject

/**
 * A simple [Fragment] subclass.
 */
class StatusAlreadyFragment : Fragment() {

    private val db = BlasSQLDataBase()
    private var token: String? = null
    private lateinit var  cursor : Cursor
    private lateinit var root : View
    private lateinit var recyclerView:RecyclerView
    private lateinit var partRecyclerAlready:CardRecyclerStatusAlready
    private val dataList = mutableListOf<AlreadyRowModel>()

     private val adapterAlready : AlreadyViewAdapter = AlreadyViewAdapter(dataList,object : AlreadyViewAdapter.ListListener{
        override fun onClickRow(tappedView: View, unReadRowModel: AlreadyRowModel) {
           // Log.d("test","testtesrt")
        }
    })



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        root = inflater.inflate(R.layout.fragment_status_already, container, false)

        //DBから値を取得
        cursor = db.getRecordAlreadyRead()
        if(getStringExtra("token") != null){
            token = getStringExtra("token")
        }

        /* while (cursor.moveToNext()) {
             cursor.columnNames.forEach {
                 val idxNote = cursor.getColumnIndex(it)
             }
         }*/

        val payload = mapOf("token" to token)
        BlasRestProject(payload, ::projectSearchSuccess, ::projectSearchError).execute()

        return root
    }

    private fun projectSearchSuccess(result: JSONObject){
        //jsonからプロジェクト名を取得
        val newMap = RestHelper().createProjectList(result)
        val projectMap:MutableMap<String,String> = mutableMapOf()
        newMap.forEach{
            projectMap.set(key = it.value["project_id"].toString(),value = it.value["project_name"].toString())
        }

        //cardの準備
        val protoRecyclerView = root.findViewById<RecyclerView>(R.id.recyclerView)
        partRecyclerAlready = CardRecyclerStatusAlready(activity, protoRecyclerView, adapterAlready, cursor,projectMap)
        recyclerView = partRecyclerAlready.createRecyclerView()

        //値の取得と反映
        dataList.addAll(partRecyclerAlready.createStatusList())
        adapterAlready.notifyItemInserted(0)

    }


    private fun projectSearchError(errorCord:Int,aplCode:Int){

    }

    override fun onDestroyView() {
        recyclerView.adapter = null
        super.onDestroyView()
    }

}
