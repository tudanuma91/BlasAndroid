package com.v3.basis.blas.ui.terminal.adapter

import com.v3.basis.blas.R
import com.v3.basis.blas.databinding.ListProjectBinding
import com.v3.basis.blas.ui.terminal.common.DownloadModel
import com.v3.basis.blas.ui.terminal.common.DownloadViewModel
import com.v3.basis.blas.ui.terminal.fixture.project_list_view.RowModel
import com.v3.basis.blas.ui.terminal.fixture.project_list_view.ViewAdapterAdapter
import com.xwray.groupie.databinding.BindableItem

/**
 * リストビューライブラリのGroupieのBindableItemを継承したクラス。
 * [ジェネリクス]
 * ListProjectBindingを指定
 * [引数]　
 * title: String　プロジェクトタイトル
 * detail: String　プロジェクトID
 * viewModel: DownloadViewModel　list_projectの内部レイアウトに渡すために必要
 * item: DownloadItem　list_projectの内部レイアウトに渡すために必要
 * listener: ViewAdapterAdapter.ListListener?　リストビューがクリックされたときのリスナー
 * [特記事項]
 * list_project.xmlをもとに自動生成されたListProjectBindingをBindableItemに指定する
 * [作成者]
 * fukuda
 */
class ProjectListCellItem(
    val title: String,
    val detail: String,
    private val viewModel: DownloadViewModel,
    val model: DownloadModel,
    var listener: ViewAdapterAdapter.ListListener? = null) : BindableItem<ListProjectBinding>() {

    override fun getLayout(): Int = R.layout.list_project

    override fun bind(viewBinding: ListProjectBinding, position: Int) {
        viewBinding.vm = viewModel
        viewBinding.item = model
        viewBinding.rowTitle.text = title
        viewBinding.rowDetail.text = detail
        viewBinding.root.setOnClickListener {
            listener?.onClickRow(it, model, RowModel().apply {
                this.detail = this@ProjectListCellItem.detail
                this.title = this@ProjectListCellItem.title
            })
        }
    }
}
