package com.v3.basis.blas.ui.ext

import android.Manifest
import android.content.ContentResolver
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.v3.basis.blas.R

fun AppCompatActivity.setActionBarTitle(resId: Int) {
    this.supportActionBar?.title = getString(resId)
}

fun Fragment.getStringExtra(key: String) : String? {
    return requireActivity().intent?.extras?.getString(key)
}

fun AppCompatActivity.showBackKeyForActionBar() {
    supportActionBar?.apply {
        setDisplayHomeAsUpEnabled(true)
        setHomeButtonEnabled(true)
    }
}

fun Fragment.addTitle(extraName: String) {

    (requireActivity() as AppCompatActivity).supportActionBar?.also {
        getStringExtra(extraName)?.run {
            it.customView.findViewById<TextView>(R.id.title)?.text = it.title
            it.customView.findViewById<TextView>(R.id.projectName)?.text = this
        }
    }
}

fun AppCompatActivity.setBlasCustomView() {

    supportActionBar?.apply {
        setCustomView(R.layout.view_blas_actionbar)
        setDisplayShowCustomEnabled(true)
        setDisplayShowTitleEnabled(true)
        setDisplayShowHomeEnabled(true)
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
        ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.CAMERA), 999)
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
