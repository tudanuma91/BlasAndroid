package com.v3.basis.blas.ui.ext

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.v3.basis.blas.R


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


