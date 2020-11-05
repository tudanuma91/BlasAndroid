package com.v3.basis.blas.ui.fixture.fixture_kenpin_multi

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.v3.basis.blas.R
import com.v3.basis.blas.blasclass.controller.FixtureController
import java.text.SimpleDateFormat

/**
 * リサイクラーに表示するデータ形式
 */
data class BarCodeItem(val code: String, val status: String, val dateTime:String) {
    override fun toString(): String = code + " ${dateTime}"
}

/**
 * A fragment representing a list of Items.
 */
class FixtureKenpinItemsFragment : Fragment() {

    private var adapter: FixtureItemRecyclerViewAdapter? = null
    val items = mutableListOf<BarCodeItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_fixture_kenpin_items_list, container, false)
        val recyclerListView = view.findViewById<RecyclerView>(R.id.list)
        recyclerListView.layoutManager = LinearLayoutManager(context)
        adapter = FixtureItemRecyclerViewAdapter(items)
        recyclerListView.adapter = adapter

        view.findViewById<TextView>(R.id.item_total).text = "読み取り完了数:${items.size}件"
        return view
    }


    public fun setItems(code:String, status:Int) {
        val dateTime:String = SimpleDateFormat("HH:mm:ss").format(java.util.Date())
        var statusMsg:String = ""
        when(status){
            FixtureController.NORMAL->{
                statusMsg = "OK"
            }
            FixtureController.ALREADY_ENTRY->{
                statusMsg = "検品済み"
            }
            FixtureController.INSERT_ERROR->{
                statusMsg = "追加に失敗しました"
            }
            FixtureController.UPDATE_ERROR->{
                statusMsg = "更新に失敗しました"
            }
        }

        //同じIDは破棄する
        if(items.find { it.code == code } == null) {
            items.add(BarCodeItem(code, statusMsg, dateTime))
            adapter?.notifyDataSetChanged()
            view?.findViewById<TextView>(R.id.item_total)?.text = "読み取り完了数:${items.size}件"
        }
    }

    companion object {

        // TODO: Customize parameter argument names
        const val ARG_COLUMN_COUNT = "column-count"

        // TODO: Customize parameter initialization
        @JvmStatic
        fun newInstance() =
            FixtureKenpinItemsFragment().apply {
                arguments = Bundle().apply {
                    //putInt(ARG_COLUMN_COUNT, columnCount)
                }
            }
    }
}