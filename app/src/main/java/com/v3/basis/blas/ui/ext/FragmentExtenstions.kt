package com.v3.basis.blas.ui.ext

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.work.WorkInfo
import com.v3.basis.blas.R
import com.v3.basis.blas.activity.QRActivity
import com.v3.basis.blas.blasclass.db.fixture.FixtureController
import com.v3.basis.blas.blasclass.worker.DownloadWorker
import com.v3.basis.blas.blasclass.worker.WorkerHelper
import com.v3.basis.blas.ui.terminal.common.DownloadModel
import com.v3.basis.blas.ui.terminal.common.DownloadViewModel
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.*
import kotlin.reflect.full.memberProperties


fun Fragment.getStringExtra(key: String) : String? {
    return requireActivity().intent?.extras?.getString(key)
}

fun Fragment.addTitle(extraName: String) {

    (requireActivity() as AppCompatActivity).supportActionBar?.also {
        getStringExtra(extraName)?.run {
            it.customView.findViewById<TextView>(R.id.title)?.text = it.title
            it.customView.findViewById<TextView>(R.id.projectName)?.text = this
        }
    }
}

fun Fragment.setViewTitle(title: String) {

    (requireActivity() as AppCompatActivity).supportActionBar?.also {
        it.customView.findViewById<TextView>(R.id.title)?.text = title
    }
}

fun Fragment.getCustomActionTitle(): String {
    (requireActivity() as AppCompatActivity).supportActionBar?.also {
        return it.customView.findViewById<TextView>(R.id.title)?.text.toString()
    }
    return ""
}

fun Fragment.hideViewForCustomActionBar(targets: Array<Int>) {

    (requireActivity() as AppCompatActivity).supportActionBar?.also {
        targets.forEach { id ->
            it.customView.findViewById<View>(id)?.visibility = View.INVISIBLE
        }
    }
}

fun Fragment.showViewForCustomActionBar(targets: Array<Int>) {

    (requireActivity() as AppCompatActivity).supportActionBar?.also {
        targets.forEach { id ->
            it.customView.findViewById<View>(id)?.visibility = View.VISIBLE
        }
    }
}

/**
 * パーミッションをとる関数。
 */
fun FragmentActivity.checkPermissions() {//許可取り
    // すでに許可をしていた場合はスキップする
    if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 0)
    }

    if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.CAMERA)) {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 999)
    }
}

/**
 * パーミッションのチェック
 */
fun FragmentActivity.permissionChk(): Boolean {

    val isReadPermissionGranted = (let {
        PermissionChecker.checkSelfPermission(it, Manifest.permission.READ_EXTERNAL_STORAGE)
    } == PackageManager.PERMISSION_GRANTED)

    val isWritePermissionGranted = (let {
        PermissionChecker.checkSelfPermission(
            it,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    } == PackageManager.PERMISSION_GRANTED)
    val isCameraPermissionGranted = (let {
        PermissionChecker.checkSelfPermission(
            it,
            Manifest.permission.CAMERA
        )
    } == PackageManager.PERMISSION_GRANTED)
    //上三つの変数すべてtrueの時、trueを返す。それ以外はfalse
    return isReadPermissionGranted && isWritePermissionGranted && isCameraPermissionGranted
}

fun Fragment.closeSoftKeyboard() {

    val imm: InputMethodManager = requireActivity().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    val view: View = requireActivity().window.decorView
    imm.hideSoftInputFromWindow(view.windowToken, 0)
}

/**
 * バックグラウンドでダウンロードを実行するタスクを、キューに追加します。
 * [引数]　
 * vm: DownloadViewModel　ダウンロードステータスが変更になった際に利用。
 * item: DownloadItem　　ダウンロード対象UIの表示状態を変更。
 * [ジェネリクス]
 * [例外]
 * 明示的なthrowなし。
 * [戻り値]
 * なし。
 * [その他]
 * [特記事項]
 * ダウンロードボタンが押されたときに実行することを想定。
 * [作成者]
 * fukuda
 */
fun Fragment.addDownloadTask(vm: DownloadViewModel, model: DownloadModel, unzipPath: String, token: String, projectId: String) {

    var once = true
    WorkerHelper.addDownloadTask<DownloadWorker>(this, token, projectId, model.savePath, unzipPath, projectId) { state, progress, id ->

        when (state) {
            WorkInfo.State.BLOCKED,
            WorkInfo.State.ENQUEUED -> {
                vm.preDownloading(model, id)
            }
            WorkInfo.State.RUNNING -> {
                if (once) {
                    vm.downloading(model)
                    once = false
                }
                Log.d("foreground worker", "running: $id, progress: $progress")
            }
            WorkInfo.State.SUCCEEDED -> {
                vm.setFinishDownloading(model)
                /**
                //テスト
                Completable
                    .fromAction {
                        val ctl = FixtureController(requireContext(), projectId)
                        val records = ctl.searchDisp()

                        records.forEach { rec ->
                            rec::class.memberProperties.forEach {prop ->
                                Log.d("prop:",prop.name + ":" + prop.call(rec).toString())
                            }
                        }

//                        val _join = ctl.joinTest()
//                        Log.d("FixtureController", "list:" + _join.first { it.username != null })
//                        val join = UsersController(requireContext(), projectId).joinTest()
//                        Log.d("JoinTest", "list:" + join.toString())
                    }
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe()
                **/
            }
            WorkInfo.State.FAILED -> {
                WorkerHelper.stopDownload(requireContext(), id)
                vm.cancelDownloading(model, id)
            }
        }
    }
}

/**
 * ダウンロード実行中のタスクの状態を監視します
 * [引数]　
 * vm: DownloadViewModel　ダウンロードステータスが変更になった際に利用。
 * item: DownloadItem　　ダウンロード対象UIの表示状態を変更。
 * [ジェネリクス]
 * [例外]
 * 明示的なthrowなし。
 * [戻り値]
 * なし。
 * [その他]
 * [特記事項]
 * ダウンロード状態を表示するListViewが生成されたタイミングで実行することを想定
 * [作成者]
 * fukuda
 */
fun Fragment.continueDownloadTask(vm: DownloadViewModel, model: DownloadModel) {

    var once = true
    WorkerHelper.observe(this, UUID.fromString(model.uuid)) { state, progress, id ->

        when (state) {
            WorkInfo.State.BLOCKED,
            WorkInfo.State.ENQUEUED -> {
                vm.preDownloading(model, id)
            }
            WorkInfo.State.RUNNING -> {
                if (once) {
                    vm.downloading(model)
                    once = false
                }
                Log.d("foreground worker", "running $id, progress: $progress")
            }
            WorkInfo.State.SUCCEEDED -> {
                vm.setFinishDownloading(model)
            }
        }
    }
}

fun Fragment.setBackPressedEvent(action: (callback: OnBackPressedCallback) -> Unit) {
    requireActivity().onBackPressedDispatcher.addCallback(
        this,
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                action.invoke(this)
            }
        })
}

fun Fragment.startActivityWithResult(launchClass: Class<*>, requestCode: Int, extra: Pair<String, String>, callBack: (result: ActivityResult) -> Unit): ActivityResultLauncher<Intent> {

    val registry = requireActivity().activityResultRegistry
    val observer = registry.register(requestCode.toString(),
        viewLifecycleOwner,
        ActivityResultContracts.StartActivityForResult(),
        ActivityResultCallback {
            Log.d("result", "fragment: ${it.data?.getStringExtra("result")}")
            callBack.invoke(it)
        })
    val i = Intent(requireContext(), launchClass)
    i.putExtra(extra.first, extra.second)
    observer.launch(i)
    return observer
}

fun Fragment.requestPermissions(permissions: Array<String>, resultCallBacks: (Map<String, Boolean>) -> Unit) {
    val launcher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        resultCallBacks.invoke(it)
    }
    launcher.launch(permissions)
}
