package com.v3.basis.blas.ui.terminal.fixture

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.ObservableBoolean
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.v3.basis.blas.R
import com.v3.basis.blas.activity.FixtureActivity
import com.v3.basis.blas.activity.ItemActivity
import com.v3.basis.blas.activity.TerminalActivity
import com.v3.basis.blas.blasclass.app.BlasMsg
import com.v3.basis.blas.blasclass.helper.RestHelper
import com.v3.basis.blas.blasclass.rest.BlasRestProject
import com.v3.basis.blas.ui.ext.addDownloadTask
import com.v3.basis.blas.ui.ext.getCustomActionTitle
import com.v3.basis.blas.ui.terminal.TerminalViewModel
import com.v3.basis.blas.ui.terminal.adapter.ProjectListCellItem
import com.v3.basis.blas.ui.terminal.common.DownloadModel
import com.v3.basis.blas.ui.terminal.common.DownloadViewModel
import com.v3.basis.blas.ui.terminal.fixture.project_list_view.RowModel
import com.v3.basis.blas.ui.terminal.fixture.project_list_view.ViewAdapterAdapter
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.databinding.GroupieViewHolder
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.fragment_project.*
import org.json.JSONObject
import java.io.File

/**
 * [説明]
 * データ管理・機器管理のプロジェクトダウンロードに関する画面
 */
open class FixtureFragment : Fragment() {

    private lateinit var fixtureViewModel: FixtureViewModel
    lateinit var token:String
    private var msg = BlasMsg()
    private val toastErrorLen = Toast.LENGTH_LONG
    private var toastSuccessLen = Toast.LENGTH_SHORT

    private val viewModel: DownloadViewModel by activityViewModels()
    private val terminalViewModel: TerminalViewModel by activityViewModels()
    private val disposables = CompositeDisposable()

    private var filterWord: String = ""
    private var beforeFilterWord: String = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        fixtureViewModel = ViewModelProviders.of(this).get(FixtureViewModel::class.java)
//        viewModel = ViewModelProviders.of(this).get(DownloadViewModel::class.java)

        val extras = activity?.intent?.extras
        if(extras?.getString("token") != null ) {
            token = extras.getString("token").toString() //トークンの値を取得
        }

        return inflater.inflate(R.layout.fragment_fixture, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        try {
            if(token != null) {
                makeProjectList()
            }else{
                throw Exception("Failed to receive internal data ")
            }
        }catch (e:Exception){
            val errorMessage = msg.createErrorMessage("getFail")
            Toast.makeText(activity, errorMessage, toastErrorLen).show()
        }

        viewModel.startDownload
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                val unzipPath = requireContext().dataDir.path + "/databases/" + it.projectId
                addDownloadTask(viewModel, it, unzipPath, token, it.projectId)
            }
            .addTo(disposables)

        terminalViewModel.filterEvent
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                filterWord = it
                makeProjectList(true)
            }
            .addTo(disposables)

        terminalViewModel.refreshEvent
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                makeProjectList(false)
            }
            .addTo(disposables)
    }

    /**
     * [説明]
     * BLASからプロジェクト一覧を取得する
     */
    private fun makeProjectList(update: Boolean = false) {
        if (update) {
            if (beforeFilterWord != filterWord) {
                beforeFilterWord = filterWord
                val payload = mapOf("token" to token)
                BlasRestProject(payload, ::projectSearchSuccess, ::projectSearchError).execute()
            }
        } else {
            val payload = mapOf("token" to token)
            BlasRestProject(payload, ::projectSearchSuccess, ::projectSearchError).execute()
        }
    }

    /**
     * [説明]
     * プロジェクト選択時にデータ管理画面、または機器管理画面を呼び出す。
     * データ管理画面、機器管理画面のどちらを呼び出すかは、画面のタイトルで判定する
     */
    open fun clickCell(rowModel: RowModel, model: DownloadModel) {

        if (model.doneDownloaded.get()) {
            val title = getCustomActionTitle()
            val title2 = requireContext().resources.getString(R.string.navi_title_terminal_item)
            if (title == title2) {
                //データ管理の画面を呼び出す
                Log.d(
                    "DataManagement",
                    "click_NAME => ${rowModel.title}/click_ID => ${rowModel.detail}"
                )
                val intent = Intent(activity, ItemActivity::class.java)
                intent.putExtra("token", token)
                intent.putExtra("project_id", rowModel.detail)
                intent.putExtra("projectName", rowModel.title)
                startActivity(intent)
            } else {
                //機器管理の画面を呼び出す
                Log.d(
                    "DataManagement",
                    "click_NAME => ${rowModel.title}/click_ID => ${rowModel.detail}"
                )
                val intent = Intent(activity, FixtureActivity::class.java)
                intent.putExtra("token", token)
                intent.putExtra("project_id", rowModel.detail)
                intent.putExtra("project_name", rowModel.title)
                startActivity(intent)
            }
        } else {
            if (model.downloading.get().not()) {
                viewModel.clickDownload(model)
            }
        }
    }

    private fun projectSearchSuccess(result:JSONObject) {
        val newMap = RestHelper().createProjectList(result)
        val projectList = createProjectList(newMap)
        val listener = object : ViewAdapterAdapter.ListListener {
            override fun onClickRow(tappedView: View, model: DownloadModel, rowModel: RowModel) {
                clickCell(rowModel, model)
            }
        }
        projectList.forEach { it.listener = listener }

        val gAdapter = GroupAdapter<GroupieViewHolder<*>>()
        gAdapter.update(projectList)

        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.adapter = gAdapter
    }


    /**
     * プロジェクト取得失敗時
     * @param  error_code 失敗した要因コード
     */
    private fun projectSearchError(error_code: Int, aplCode:Int) {
        var message: String? = null

        message = BlasMsg().getMessage(error_code,aplCode)
        Toast.makeText(activity, message, toastErrorLen).show()

        val intent = Intent(activity, TerminalActivity::class.java)
        //intent.putExtra("token",token)
        startActivity(intent)
    }

    /**
     * マップ形式からリスト形式に変換する
     * @param projectのマップ形式のデータ
     * @return プロジェクトのリスト
     */
    private fun createProjectList(from: MutableMap<String,MutableMap<String, String>>): List<ProjectListCellItem> {
        val dataList = mutableListOf<ProjectListCellItem>()

        val dir = requireContext().cacheDir.path + "/download_zip"
        val directory = File(dir)
        directory.mkdirs()

        from.forEach{
            val project_name = it.value["project_name"].toString()
            if (filterWord.isBlank() || project_name.contains(filterWord)) {
                val project_id = it.value["project_id"].toString()
                val item = DownloadModel(project_id, dir)
                viewModel.setupItem(this, item, token)
                val data = ProjectListCellItem(project_name, project_id, viewModel, item)
                dataList.add(data)
            }
        }
        return dataList
    }

    override fun onDestroyView() {
        recyclerView.adapter = null
        super.onDestroyView()
    }
}
