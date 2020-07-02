package com.v3.basis.blas.ui.fixture.fixture_view

import com.v3.basis.blas.R
import com.v3.basis.blas.databinding.ListFixtureBinding
import com.xwray.groupie.databinding.BindableItem

class FixtureListCell(private val viewModel: FixtureListViewModel, val model: FixtureCellModel) : BindableItem<ListFixtureBinding>() {

    override fun getLayout(): Int = R.layout.list_fixture

    override fun bind(viewBinding: ListFixtureBinding, position: Int) {
        viewBinding.vm = viewModel
        viewBinding.model = model
    }
}
