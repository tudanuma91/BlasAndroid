package com.v3.basis.blas.ui.fixture.fixture_config

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.v3.basis.blas.R
import com.v3.basis.blas.databinding.FragmentFixtureConfigBinding
import com.v3.basis.blas.ui.ext.addTitleWithProjectName
import com.v3.basis.blas.ui.common.FixtureBaseFragment
import kotlinx.android.synthetic.main.fragment_fixture_config.*


class FixtureConfigFragment : FixtureBaseFragment() {

    companion object {
        fun newInstance() = FixtureConfigFragment()
    }

    private lateinit var viewModel: FixtureConfigViewModel
    private lateinit var binding: FragmentFixtureConfigBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //viewモデルの作成
        viewModel = ViewModelProvider(this).get(FixtureConfigViewModel::class.java)
        viewModel?.apply {
            context?.let {
                this.loadViewModel(it)
            }
        }

        //アクションバーにプロジェクト名を表示する(あとで直す。addTitleでproject_nameは意味がわからない)
        addTitleWithProjectName("機器管理設定画面")

        //「QRコード/バーコードを一つずつ読む」に変更があった際のコールバック登録
        val isCheckedSingleObserver = Observer<Boolean> {
            //radioSingleCamera.isChecked = it
            Log.d("konishi", "observer called single")
            context?.let {
                viewModel.saveViewModel(it)

                //一つずつ読むがしっかり生成されている時
                if(viewModel.isCheckedSingle.value != null){
                    //値によって処理を変更
                    if(viewModel.isCheckedSingle.value!!){
                        //一つずつ読むチェック時=>リスト非表示
                        barcode_list_view?.isVisible = false
                    }else{
                        //一つずつ読む非チェック時=>リスト表示
                        barcode_list_view?.isVisible = true
                    }
                }

            }
        }
        viewModel.isCheckedSingle.observe(this, isCheckedSingleObserver)

        //「QRコード/バーコードを一括で読む」に変更があった際のコールバック登録
        val isCheckedMultiObserver = Observer<Boolean> {
           // radioMultiCamera.isChecked = it
            Log.d("konishi", "observer called multi")
            context?.let {
                viewModel.saveViewModel(it)
            }
        }
        viewModel.isCheckedMulti.observe(this, isCheckedMultiObserver)

        //バーコード一覧のチェックボックスに変更があった際のコールバック登録
        val switchObserver = Observer<Boolean> {
            //双方向バインディングしているため、viewModelに変更は自動的に反映されている
            Log.d("konishi", "swich callled")
            context?.let {
                viewModel.saveViewModel(it)
            }

        }
        viewModel.listItems.forEach{
            //リストの一行一行に変更監視を追加する
            it.useFlg.observe(this, switchObserver)
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //データバインディングの設定
        binding = FragmentFixtureConfigBinding.inflate(inflater, container, false)
        binding.viewmodel = viewModel

        //rootを返すのに注意。
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //バーコードのリストビューにデータを追加する
        val adapter = viewModel.listItems?.let {
            BarcodeListAdapter(this.requireContext(), R.layout.list_barcode_item,
                it
            )
        }

        //barcode_list_view?.isVisible = false
        barcode_list_view?.adapter = adapter
    }
}