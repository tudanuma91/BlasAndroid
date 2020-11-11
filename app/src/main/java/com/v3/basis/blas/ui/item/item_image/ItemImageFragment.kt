package com.v3.basis.blas.ui.item.item_image


import android.Manifest
import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
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
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.v3.basis.blas.R
import com.v3.basis.blas.blasclass.controller.ImagesController
import com.v3.basis.blas.blasclass.db.data.ItemImage
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.cell_item_image.view.*
import kotlinx.android.synthetic.main.fragment_item_image.*
import org.reactivestreams.Subscriber
import org.reactivestreams.Subscription
import java.lang.Exception

/**
 * 画像を受信したら、notifyを通知するだけのサブスクライバ。
 * スレッドで実装してしまうと、notifyDataSetChangedはメインスレッド以外から
 * コールできないため、エラーになる。
 */
class ImageSubscriber<ItemImage>(val count:Long, val recycleView:RecyclerView) : Subscriber<ItemImage> {
    var subscription:Subscription? = null
    override fun onComplete() {
        //notifyDataSetChangedはメインスレッドから呼ぶ必要がある
        recycleView.adapter?.notifyDataSetChanged()
        subscription?.cancel()
    }

    override fun onSubscribe(s: Subscription?) {
        subscription = s
        subscription?.request(count)
    }

    override fun onNext(t: ItemImage) {
        //notifyDataSetChangedはメインスレッドから呼ぶ必要がある
        recycleView.adapter?.notifyDataSetChanged()
    }

    override fun onError(t: Throwable?) {
        //notifyDataSetChangedはメインスレッドから呼ぶ必要がある
        recycleView.adapter?.notifyDataSetChanged()
    }

    fun dispose() {
        subscription?.cancel()
    }
}
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
        const val REQUEST_ZOOM = 3

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
    private var imageFields: MutableList<ItemImage>? = null

    var controller:ImagesController? = null
    var imageSubscriber:ImageSubscriber<ItemImage>? = null
    //filechooserで渡す引数
    var imageUri:Uri?=null
    var fcItem:ItemImage? = null


    protected val PERMISSIONS_REQUEST_CODE = 1234
    protected val PERMISSIONS_REQUIRED = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE
    )

    /**
     * 権限チェック　関数に関数を代入する。
     */
    protected fun hasPermissions(context: Context) = PERMISSIONS_REQUIRED.all {
        ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }

    /* ビューホルダクラス */
    private inner class RecyclerListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var imageFieldText : TextView
        var progressBar : ProgressBar
        var imageView: ImageView
        init {
            imageFieldText = itemView.findViewById(R.id.image_name)
            progressBar = itemView.findViewById(R.id.progressbar)
            imageView = itemView.findViewById(R.id.image)
        }
    }

    /* アダプタクラス */
    private inner class RecyclerListAdapter(private val listData:MutableList<ItemImage>):
        RecyclerView.Adapter<RecyclerListViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerListViewHolder {
            val inflater = LayoutInflater.from(context)
            //cell_item_image.xml取得
            val view = inflater.inflate(R.layout.cell_item_image, parent, false)
            val holder = RecyclerListViewHolder(view)
            return holder
        }

        override fun onBindViewHolder(holder: RecyclerListViewHolder, position: Int) {
            val item = listData[position]
            holder.imageFieldText.text = item.name
            holder.progressBar.isVisible = item.downloadProgress //グルグル表示
            if(item.bitmap != null) {
                holder.imageView.setImageBitmap(item.bitmap)
            }


            holder.imageView.setOnClickListener {
                //画像ファイルをタップされたら、画像ダイアログを開く。
                context?.let {
                    if (!hasPermissions(it)) {
                        //権限がないので要求する
                        requestPermissions(PERMISSIONS_REQUIRED, PERMISSIONS_REQUEST_CODE)
                    }
                    else {
                        fcItem = item
                        startFileChoicer()
                    }
                }
            }

            holder.imageView.setOnLongClickListener {
                Toast.makeText(context, "Long呼ばれました",Toast.LENGTH_SHORT).show()
                //画像を拡大表示する
                true
            }
        }

        override fun getItemCount(): Int {
            return listData.size
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

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_item_image, container, false)
        val button = root.findViewById<FloatingActionButton>(R.id.img_fab)

        /*
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
        }*/

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        //layoutManagerをgridにすることで横にも表示可能にしている
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2) as RecyclerView.LayoutManager?

        //ビューモデル生成
        viewModel = ViewModelProvider(this).get(ItemImageViewModel::class.java)

        //LDBのimagesテーブルにアクセスするためのコントローラー生成
        controller = context?.let { ImagesController(it, projectId) }

        /* 画像のフィールド項目と画像名を取得する */
        imageFields = controller?.getItemImages(itemId)

        imageFields?.forEach {
            //画像がないレコードはitem_idがないので補完する
            it.item_id = itemId.toLong()
        }

        /* 空のフィールドを一旦アダプターに渡す */
        val adapter = imageFields?.let { RecyclerListAdapter(it) }
        recyclerView.adapter = adapter

        //画像の取得処理
        //imageDownLoaderは、画像をダウンロードするデータ提供者。
        val imageDownLoader = imageFields?.let { ImageDownLoader(resources, token, projectId, itemId, it) }
        val imageFieldNum: Int? = imageFields?.size
        if(imageFieldNum != null) {
            //購読者の作成（画像がダウンロードされるたびに呼ばれる)
            imageSubscriber = ImageSubscriber<ItemImage>(imageFieldNum.toLong(), recyclerView)
            //画像を別スレッドでダウンロードする
            val flow = Flowable.create<ItemImage>(imageDownLoader, BackpressureStrategy.BUFFER)
            //ダウンロード開始
            flow.observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.newThread())
                .subscribe(imageSubscriber)
        }
    }

    /**
     * 画面終了時
     */
    override fun onPause() {
        super.onPause()
        imageSubscriber?.dispose()
    }

    /**
     * 権限要求に対するレスポンス
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        var chkPermission = true

        grantResults.forEach {
            if(it != 0) {
                chkPermission = false
            }
        }
        if(!chkPermission){
            Toast.makeText(getActivity(), "アクセス権限がありません。写真を撮影できません。", Toast.LENGTH_LONG).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        var ret:Boolean? = false

        if (requestCode == REQUEST_CHOOSER && resultCode == Activity.RESULT_OK) {
            /* タップしてファイルダイアログを開いた場合 */
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
                var bmp: Bitmap? = null
                //bitmap取得
                if (Build.VERSION.SDK_INT < 28) {
                    bmp = MediaStore.Images.Media.getBitmap(resolver, it)
                } else {
                    val source = ImageDecoder.createSource(resolver, it)
                    bmp = ImageDecoder.decodeBitmap(source)
                }

                //どの画像が更新されたかを調べる
                val item = imageFields?.firstOrNull { it == fcItem }

                //画像を保存する
                if (bmp != null) {
                    try {
                        //オリジナルサイズの画像を保存する
                        controller?.saveBitmap(bmp,
                                               item?.item_id.toString(),
                                               item?.project_image_id.toString(),
                                               ImagesController.ORIGINAL_IMAGE)

                        //小さいファイルに圧縮する
                        var rate = (bmp.width / 230).toInt()
                        if(rate == 0) {
                            rate = 1
                        }
                        val dstWidth = (bmp.width / rate).toInt()
                        val dstHeight = (bmp.height / rate).toInt()
                        val smallBmp = Bitmap.createScaledBitmap(bmp, dstWidth, dstHeight, true)
                        controller?.saveBitmap(smallBmp,
                                               item?.item_id.toString(),
                                               item?.project_image_id.toString(),
                                               ImagesController.MINI_IMAGE)

                        item?.bitmap = smallBmp

                    }
                    catch(e:Exception) {
                        e.printStackTrace()
                        return
                    }
                }

                //レコードを保存する
                if (item != null) {
                    ret = controller?.save2LDB(item)
                    if(ret == true) {

                    }
                }

                //画面を更新する
                recyclerView.adapter?.notifyDataSetChanged()

                //サーバーにイベントを送信する

            }

        }
    }
    /**
     * カメラが起動した後にコールバックされる
     */
    /*
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (requestCode == REQUEST_CHOOSER && resultCode == Activity.RESULT_OK) {
            /* タップしてファイルダイアログを開いた場合 */
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
    }*/
    /*
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

        if (requestCode == REQUEST_ZOOM && resultCode == Activity.RESULT_OK) {
            //画像の再取得処理
            viewModel.setup(requireContext(), token, projectId, itemId)
        } else if (requestCode == REQUEST_CHOOSER && resultCode == Activity.RESULT_OK) {

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
*/
}
