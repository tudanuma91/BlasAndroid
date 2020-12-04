package com.v3.basis.blas.ui.fixture.fixture_config

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.lifecycle.MutableLiveData
import com.v3.basis.blas.R
import com.v3.basis.blas.databinding.ListBarcodeItemBinding
import kotlinx.android.synthetic.main.list_barcode_item.view.*
//ObservableField<MutableList<BarcodeItem>>()
class BarcodeListAdapter(val con: Context, val resource:Int, val items:List<BarcodeItem>) : BaseAdapter() {
    private val inflater = LayoutInflater.from(con)

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var binding: ListBarcodeItemBinding?

        if(convertView != null) {
            binding = convertView.tag as ListBarcodeItemBinding
        }
        else {
            binding = ListBarcodeItemBinding.inflate(inflater, parent,false)
            binding.root.tag = binding
        }

        //var liveData = getItem(position) as BarcodeItem
        binding?.barcodeitem = getItem(position) as BarcodeItem
        //binding?.barcodeitem = getItem(position) as BarcodeItem


        return binding?.root
    }

    override fun getItem(position: Int): Any {
        return items[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return items.count()
    }
}