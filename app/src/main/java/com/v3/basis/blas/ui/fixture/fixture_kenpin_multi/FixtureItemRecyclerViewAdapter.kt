package com.v3.basis.blas.ui.fixture.fixture_kenpin_multi

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.v3.basis.blas.R

/**
 * [RecyclerView.Adapter] that can display a [DummyItem].
 * TODO: Replace the implementation with code for your data type.
 */
class FixtureItemRecyclerViewAdapter(
    private var values: MutableList<BarCodeItem>
) : RecyclerView.Adapter<FixtureItemRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_fixture_kenpin_items, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = values[position]
        holder.dateTimeView.text = item.dateTime
        holder.contentView.text = item.code
        holder.statusView.text = item.status
    }

    override fun getItemCount(): Int = values.size


    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val dateTimeView: TextView = view.findViewById(R.id.item_datetime)
        val contentView: TextView = view.findViewById(R.id.content)
        val statusView: TextView = view.findViewById(R.id.item_status)

        override fun toString(): String {
            return super.toString() + " '" + contentView.text + "'"
        }
    }
}