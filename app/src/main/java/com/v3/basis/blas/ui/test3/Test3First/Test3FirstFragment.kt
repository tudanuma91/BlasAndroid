package com.v3.basis.blas.ui.test3.Test3First


import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.v3.basis.blas.R
import kotlinx.android.synthetic.main.fragment_test3_first.*

/**
 * A simple [Fragment] subclass.
 */
class ActivityFirstFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_test3_first, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("lifeCycle", "onViewCreated")
        val recyclerView = recycler_list
        val adapter = ViewAdapter(createDataList(), object : ViewAdapter.ListListener {
            override fun onClickRow(tappedView: View, rowModel: RowModel) {
                Log.d("test","testtest")
                Toast.makeText(activity, rowModel.title, Toast.LENGTH_LONG).show()
                Log.d(
                    "DataManagement",
                    "click_NAME => ${rowModel.title}/click_ID => ${rowModel.detail}"
                )
            }
        })
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.adapter = adapter
    }


    private fun createDataList(): List<RowModel> {

        val dataList = mutableListOf<RowModel>()
        for (i in 0..49) {
            val data: RowModel = RowModel().also {
                it.title = "タイトル" + i + "だよ"
                it.detail = "詳細" + i + "個目だよ"
            }
            dataList.add(data)
        }
        return dataList
    }


    fun onClickRow(tappedView: View, rowModel: RowModel) {
        Snackbar.make(tappedView, "Replace with your own action tapped ${rowModel.title}", Snackbar.LENGTH_LONG)
            .setAction("Action", null).show()
    }


}
