package com.v3.basis.blas.ui.item.item_image


import android.Manifest.permission.*
import android.app.Activity
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.ImageDecoder
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.*
import android.widget.AdapterView.AdapterContextMenuInfo
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.v3.basis.blas.R
import com.v3.basis.blas.activity.ItemActivity
import com.v3.basis.blas.activity.ItemEditActivity
import com.v3.basis.blas.activity.ItemImageZoomActivity
import com.v3.basis.blas.blasclass.app.BlasDef.Companion.APL_OK
import com.v3.basis.blas.blasclass.app.BlasMsg
import com.v3.basis.blas.blasclass.controller.ImagesController
import com.v3.basis.blas.blasclass.db.BaseController
import com.v3.basis.blas.ui.item.item_image.adapter.AdapterCellItem
import com.v3.basis.blas.ui.item.item_image.model.ImageFieldModel
import com.v3.basis.blas.ui.item.item_image.model.ItemImage
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.OnItemClickListener
import com.xwray.groupie.OnItemLongClickListener
import com.xwray.groupie.databinding.GroupieViewHolder
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import kotlinx.android.synthetic.main.fragment_item_image.*
import java.text.SimpleDateFormat
import java.util.*


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

    private var deniedPermission = false

    private val disposables: CompositeDisposable = CompositeDisposable()
    private var message:String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_item_image, container, false)
        val button = root.findViewById<FloatingActionButton>(R.id.img_fab)

        button.setOnClickListener { view ->
            //setup2を元に戻してからpushすること
            viewModel.setup(requireContext(), token, projectId, itemId)
            AlertDialog.Builder(activity)
                .setTitle("メッセージ")
                .setMessage("リロードしました")
                .setPositiveButton("YES") { dialog, which ->
                    //TODO YESを押したときの処理
                }
                .show()
        }


        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        //layoutManagerをgridにすることで横にも表示可能にしている
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2) as RecyclerView.LayoutManager?

        viewModel = ViewModelProviders.of(this).get(ItemImageViewModel::class.java)
        //画像の取得処理
        viewModel.setup(requireContext(), token, projectId, itemId)


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

        viewModel.deleteAction
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy {
                androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle("画像削除")
                    .setMessage("本当に削除しても良いですか？")
                    .setNegativeButton("キャンセル", null)
                    .setPositiveButton("削除する") { _, _ ->
                        viewModel.deleteItem(it)
                    }
                    .show()
            }
            .addTo(disposables)

        viewModel.errorAPI
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy {

                message = BlasMsg().getMessage(it,APL_OK)

                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
                //Toast.makeText(requireContext(), "API Error ($it)", Toast.LENGTH_LONG).show()
            }
            .addTo(disposables)

        //長押し時の処理
        viewModel.zoomAction
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy {
                Log.d("[木島テスト]","テスト")
                Log.d("[木島テスト]","${it.title}")
                val intent = Intent(requireContext(),ItemImageZoomActivity::class.java)
                intent.putExtra("project_img_id",it.id)
                intent.putExtra("project_id",projectId)
                intent.putExtra("item_id",itemId)
                /*
                intent.putExtra("item_id", "${it.item_id}")
                intent.putExtra("token", token)
                intent.putExtra("project_id", projectId)
                intent.putExtra("value_list", it.valueList)
                */
                requireActivity().startActivity(intent)

            }
            .addTo(disposables)
    }


    private fun createAdapter(field: ImageFieldModel) {
        val list = field.records.map { records -> records.ProjectImage }.map {
            AdapterCellItem(viewModel, it.mapToItemImageCellItem()).apply {
                this.item.imageId=""
                viewModel.fetchImage(this.item)
            }
        }
        adapterCellItems.clear()
        adapterCellItems.addAll(list)

        val gAdapter = GroupAdapter<GroupieViewHolder<*>>()
        recyclerView.adapter = gAdapter
        gAdapter.update(adapterCellItems.toList())
        recyclerView.hasPendingAdapterUpdates()
    }




    private fun initCameraPermission(): Boolean {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return true

        val permissions = listOf(READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE, CAMERA)
        if (permissions.all { ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED}) {
            startFileChoicer()
        } else {
            requestPermissions(arrayOf(CAMERA,WRITE_EXTERNAL_STORAGE), REQUEST_CAMERA_PERMISSION)
        }

        return false
    }

    /**
     * [説明]
     * 画像を新規追加、または更新する場合に呼ばれる。
     */
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
            REQUEST_CAMERA_PERMISSION -> {

                permissions.forEach {
                    val notGranted = ContextCompat.checkSelfPermission(requireContext(), it) != PackageManager.PERMISSION_GRANTED
                    if (notGranted && !ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), it)) {
                        if ( !deniedPermission ) {
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                            val uri: Uri = Uri.fromParts("package", requireContext().packageName, null)
                            intent.data = uri
                            startActivityForResult(intent, REQUEST_CAMERA_PERMISSION)
                            deniedPermission = true
                            return
                        }
                    }
                }

                val allGranted = grantResults.firstOrNull { it == -1 }?.let { false } ?: true
                if (allGranted) {
                    startFileChoicer()
                }
            }
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
                    message = BlasMsg().getMessage(i, i1)
                    Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
                    Log.d("upload", "upload error")
                }

                //ここが画像の保存パス！！！
                val imgCon =
                    ImagesController(
                        requireContext(),
                        projectId
                    )
                //ここでbmpにファイル名を付けてキャッシュディレクトリに保存する
                val itemRecord = ItemImage(
                    create_date= SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date()),
                    ext = mime,
                    image_id=item.imageId,
                    item_id = itemId,
                    moved="0",
                    project_id=projectId,
                    project_image_id = item.id)
                itemRecord.bitmap = bmp
                //仮登録で保存する
                imgCon.save2LDB(itemRecord, BaseController.SYNC_STATUS_NEW)
                //本登録のときに画像を送信するように変更
                viewModel.fetchImage(item)
                //viewModel.upload(bmp, mime, item, error)
            }
        }
    }

    override fun onStop() {
        ItemActivity.setRestartFlag()
        super.onStop()
    }

    override fun onDestroyView() {
        recyclerView.adapter = null
        //画面破棄のときにItemActivityをリロードする
        ItemActivity.setRestartFlag()
        requireActivity().finish()
        disposables.dispose()
        super.onDestroyView()
    }

}
