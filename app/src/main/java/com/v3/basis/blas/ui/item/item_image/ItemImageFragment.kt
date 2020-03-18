package com.v3.basis.blas.ui.item.item_image


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.v3.basis.blas.R
import com.v3.basis.blas.blasclass.rest.BlasRestImage
import com.v3.basis.blas.ui.item.item_image.adapter.ImageAdapterItem
import com.v3.basis.blas.ui.logout.LogoutViewModel
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.databinding.GroupieViewHolder
import kotlinx.android.synthetic.main.fragment_item_image.*

/**
 * A simple [Fragment] subclass.
 */
class ItemImageFragment : Fragment() {

    companion object {
        const val TOKEN = "token"
        const val ITEM_ID = "item_id"

        fun newInstance(token: String?, itemId: String?) : Fragment {
            val f = ItemImageFragment()
            f.arguments = Bundle().apply {
                putString(TOKEN, token)
                putString(ITEM_ID, itemId)
            }
            return f
        }
    }

    private val token:String?
        get() = arguments?.getString(TOKEN)

    private val itemId: String?
        get() = arguments?.getString(ITEM_ID)

    private lateinit var viewModel: ItemImageViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.fragment_item_image, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        viewModel = ViewModelProviders.of(this).get(ItemImageViewModel::class.java)
        viewModel.fetchSuccess.observe(this, Observer {
            val items = it.records?.map { records ->
                ImageAdapterItem(viewModel, records.Image.mapToItemImageCellItem())
            }
            items?.also { hash ->
                val gAdapter = GroupAdapter<GroupieViewHolder<*>>()
                recyclerView.adapter = gAdapter
                gAdapter.update(hash.toList())
            }
        })
        viewModel.fetchError.observe(this, Observer {

        })

        viewModel.fetch(token, itemId)

        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
    }
}
