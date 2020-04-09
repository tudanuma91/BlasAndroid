package com.v3.basis.blas.ui.item.item_image


import android.Manifest.permission.*
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.ImageDecoder
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.PermissionChecker.checkSelfPermission
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.v3.basis.blas.R
import com.v3.basis.blas.ui.ext.rotateRight
import com.v3.basis.blas.ui.item.item_image.adapter.AdapterCellItem
import com.v3.basis.blas.ui.item.item_image.model.ImageFieldModel
import com.v3.basis.blas.ui.item.item_image.model.ItemImage
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.databinding.GroupieViewHolder
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import kotlinx.android.synthetic.main.fragment_item_image.*


/**
 * A simple [Fragment] subclass.
 */
class ItemImageFragment : Fragment() {

    companion object {
        const val TOKEN = "token"
        const val PROJECT_ID = "project_id"
        const val ITEM_ID = "item_id"
        const val REQUEST_CAMERA_PERMISSION:Int = 1
        const val REQUEST_CHOOSER = 2

        fun newInstance(token: String, projectId: String, itemId: String) : Fragment {
            val f = ItemImageFragment()
            f.arguments = Bundle().apply {
                putString(TOKEN, token)
                putString(PROJECT_ID, projectId)
                putString(ITEM_ID, itemId)
            }
            return f
        }
    }

    private val token:String
        get() = arguments?.getString(TOKEN) ?: ""

    private val projectId: String
        get() = arguments?.getString(PROJECT_ID) ?: ""

    private val itemId: String
        get() = arguments?.getString(ITEM_ID) ?: ""

    private lateinit var viewModel: ItemImageViewModel

    private var adapterCellItems: MutableList<AdapterCellItem> = mutableListOf()
    private var imageUri: Uri? = null
    private var uploadId: String = ""

    private val disposables: CompositeDisposable = CompositeDisposable()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.fragment_item_image, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2) as RecyclerView.LayoutManager?

        viewModel = ViewModelProviders.of(this).get(ItemImageViewModel::class.java)
        viewModel.setup(token, projectId, itemId)

        viewModel.receiveImageFields
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy {
                createAdapter(it)
            }
            .addTo(disposables)

        viewModel.uploadAction
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy {
                uploadId = it
                if (initCameraPermission()) {
                    startFileChoicer()
                }
            }
            .addTo(disposables)

        viewModel.errorAPI
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy {
                Toast.makeText(requireContext(), "API Error ($it)", Toast.LENGTH_LONG).show()
            }
            .addTo(disposables)
    }

    private fun createAdapter(field: ImageFieldModel) {

        val list = field.records.map { records -> records.ProjectImage }.map {
            AdapterCellItem(viewModel, it.mapToItemImageCellItem()).apply {
                viewModel.fetchImage(this.item)
            }
        }
        adapterCellItems.clear()
        adapterCellItems.addAll(list)

        val gAdapter = GroupAdapter<GroupieViewHolder<*>>()
        recyclerView.adapter = gAdapter
        gAdapter.update(adapterCellItems.toList())
    }

    private fun initCameraPermission(): Boolean {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return true

        val isReadPermissionGranted = (activity?.let { checkSelfPermission(it,READ_EXTERNAL_STORAGE) } == PackageManager.PERMISSION_GRANTED)
        val isWritePermissionGranted = (activity?.let { checkSelfPermission(it,WRITE_EXTERNAL_STORAGE) } == PackageManager.PERMISSION_GRANTED)
        val isCameraPermissionGranted = (activity?.let { checkSelfPermission(it,CAMERA) } == PackageManager.PERMISSION_GRANTED)

        if (isReadPermissionGranted && isWritePermissionGranted && isCameraPermissionGranted) {
            startFileChoicer()
        } else {
            requestPermissions(arrayOf(CAMERA,WRITE_EXTERNAL_STORAGE), REQUEST_CAMERA_PERMISSION)
        }
        return false
    }

    private fun startFileChoicer() {

        //カメラの起動Intentの用意
        val photoName = System.currentTimeMillis().toString() + ".jpg"
        val contentValues = ContentValues()
        contentValues.put(MediaStore.Images.Media.TITLE, photoName)
        contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        imageUri = requireActivity().contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        val intentCamera = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intentCamera.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)

        // ギャラリー用のIntent作成
        val intentGallery: Intent
        if (Build.VERSION.SDK_INT < 19) {
            intentGallery = Intent(Intent.ACTION_GET_CONTENT)
            intentGallery.type = "image/*"
        } else {
            intentGallery = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intentGallery.addCategory(Intent.CATEGORY_OPENABLE)
            intentGallery.type = "*/*"
            intentGallery.putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/jpeg", "image/png"))
        }
        val intent = Intent.createChooser(intentCamera, "画像選択")
        intent.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(intentGallery))
        startActivityForResult(intent, REQUEST_CHOOSER)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode) {
            REQUEST_CAMERA_PERMISSION -> { initCameraPermission() }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (requestCode == REQUEST_CHOOSER && resultCode == Activity.RESULT_OK) {

            val uri = data?.data ?: imageUri
            uri?.also {

                // ギャラリーへスキャンを促す
                MediaScannerConnection.scanFile(
                    requireContext(),
                    arrayOf(it.path),
                    arrayOf("image/jpeg"),
                    null
                )

                val resolver = requireContext().contentResolver
                val bmp = if (Build.VERSION.SDK_INT < 28) {
                    MediaStore.Images.Media.getBitmap(resolver, it)
                } else {
                    val source = ImageDecoder.createSource(resolver, it)
                    ImageDecoder.decodeBitmap(source)
                }

                val mime = resolver.getType(it) ?: ""
                val item = adapterCellItems.first { it.item.id == uploadId }.item
                item.image.set(bmp)
                item.loading.set(true)
                item.empty.set(false)
                item.ext = mime
                val error: (errorCode: Int, aplCode:Int) -> Unit = { i: Int, i1: Int ->
                    item.loading.set(false)
                    item.image.set(null)
                    item.empty.set(true)
                    Log.d("upload", "upload error")
                }
                viewModel.upload(bmp, mime, item, error)
            }
        }
    }
}
