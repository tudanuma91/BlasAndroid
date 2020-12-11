package com.v3.basis.blas.ui.item.item_image


import android.Manifest
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
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.v3.basis.blas.R
import com.v3.basis.blas.activity.ItemImageZoomActivity
import com.v3.basis.blas.blasclass.controller.ImagesController
import com.v3.basis.blas.blasclass.controller.ImagesController.Companion.SMALL_IMAGE
import com.v3.basis.blas.blasclass.controller.ImagesController.Companion.BIG_IMAGE
import com.v3.basis.blas.blasclass.db.BaseController.Companion.SYNC_STATUS_NEW
import com.v3.basis.blas.blasclass.db.BlasSQLDataBase
import com.v3.basis.blas.blasclass.ldb.LdbItemImageRecord
import com.v3.basis.blas.blasclass.service.BlasSyncMessenger
import com.v3.basis.blas.blasclass.service.SenderHandler
import com.v3.basis.blas.ui.ext.rotateLeft
import com.v3.basis.blas.ui.ext.rotateRight
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.SingleOnSubscribe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_item_image.*
import org.reactivestreams.Subscriber
import org.reactivestreams.Subscription
import java.io.File
import java.lang.Exception
import kotlin.concurrent.withLock

/**
 * 画像を受信したら、notifyを通知するだけのサブスクライバ。
 * スレッドで実装してしまうと、notifyDataSetChangedはメインスレッド以外から
 * コールできないため、エラーになる。
 */
class ImageSubscriber<LdbItemImageRecord>(val count:Long, val recycleView:RecyclerView) : Subscriber<LdbItemImageRecord> {
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

    override fun onNext(t: LdbItemImageRecord) {
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
    private var imageFields: MutableList<LdbItemImageRecord>? = null

    var imagesController:ImagesController? = null
    var imageSubscriber:ImageSubscriber<LdbItemImageRecord>? = null
    //filechooserで渡す引数
    var imageUri:Uri?=null
    var fcItem:LdbItemImageRecord? = null
    private var disposable = CompositeDisposable()


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
    private inner class RecyclerListAdapter(private val listData:MutableList<LdbItemImageRecord>):
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
                        val newStatus = imagesController?.getImageStatus(item.image_id.toString())
                        if (newStatus != null) {
                            item.sync_status = newStatus
                        }
                        fcItem = item
                        //ファイルダイアログを開く
                        startFileChoicer()
                    }
                }
            }

            holder.imageView.setOnLongClickListener {
                //まず、画像があるのかないのかチェックする
                val fileName = imagesController?.getFileName(item.item_id.toString(), item.project_image_id.toString())
                if(File(fileName).exists()) {
                    //画像を拡大表示する
                    val intent = Intent(context, ItemImageZoomActivity::class.java)
                    //パラメータ―の設定
                    intent.putExtra(ItemImageZoomActivity.ITEM_ID, item.item_id.toString())
                    intent.putExtra(ItemImageZoomActivity.PROJECT_ID, item.project_id.toString())
                    intent.putExtra(ItemImageZoomActivity.PROJECT_IMG_ID, item.project_image_id.toString())
                    intent.putExtra(ItemImageZoomActivity.IMG_ID, item.image_id.toString())
                    intent.putExtra(ItemImageZoomActivity.TITLE, item.name)
                    intent.putExtra(ItemImageZoomActivity.TOKEN, token)
                    fcItem = item
                    startActivityForResult(intent, REQUEST_ZOOM)
                }

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
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        //layoutManagerをgridにすることで横にも表示可能にしている
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2) as RecyclerView.LayoutManager?

        //ビューモデル生成
        viewModel = ViewModelProvider(this).get(ItemImageViewModel::class.java)

        //LDBのimagesテーブルにアクセスするためのコントローラー生成
        imagesController = context?.let { ImagesController(it, projectId) }

        /* 画像のフィールド項目と画像名を取得する */
        imageFields = imagesController?.getItemImages(itemId)

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
            imageSubscriber = ImageSubscriber<LdbItemImageRecord>(imageFieldNum.toLong(), recyclerView)
            //画像を別スレッドでダウンロードする
            val flow = Flowable.create<LdbItemImageRecord>(imageDownLoader, BackpressureStrategy.BUFFER)
            //ダウンロード開始
            flow.observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.newThread())
                .subscribe(imageSubscriber)
        }
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        //サーバーに画像送信のイベントを送信する
        BlasSyncMessenger.notifyBlasImages(token, projectId)
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

    /**
     * 写真撮影した場合に呼ばれる。
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        var ret = false
        var bmp: Bitmap? = null
        if (requestCode == REQUEST_CHOOSER && resultCode == Activity.RESULT_OK) {
            /* タップしてファイルダイアログを開いた場合 */
            val uri = data?.data ?: imageUri
            if(uri != null){
                //画像の取得
                // ギャラリーへスキャンを促す
                MediaScannerConnection.scanFile(
                    requireContext(),
                    arrayOf(uri.path),
                    arrayOf("image/jpeg"),
                    null
                )
                val resolver = requireContext().contentResolver

                //bitmap取得
                if (Build.VERSION.SDK_INT < 28) {
                    bmp = MediaStore.Images.Media.getBitmap(resolver, uri)
                } else {
                    val source = ImageDecoder.createSource(resolver, uri)
                    bmp = ImageDecoder.decodeBitmap(source)
                }
            }

            //どの画像が更新されたかを調べる
            val item = imageFields?.firstOrNull { it == fcItem }

            //画面にはあたかも撮影出来たかのように見せかける
            val fakeBmp = bmp?.let {
                saveImage(
                    it,
                    item?.item_id.toString(),
                    item?.project_image_id.toString(),
                    230.0f, SMALL_IMAGE)
            }
            item?.bitmap = fakeBmp

            Log.d("send", "itemImageFragment ロック開始")

            Single.fromCallable {
                if ((item != null) && (bmp != null)) {
                    SenderHandler.lock.withLock {
                        //画像を保存して、LDBにレコードを書き込む
                        sendToService(bmp, item)
                    }
                }
            }.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                    onSuccess = {
                        recyclerView.adapter?.notifyDataSetChanged()
                        BlasSyncMessenger.notifyBlasImages(token, projectId)
                    },
                    onError = {
                        Toast.makeText(BlasSQLDataBase.context, "画像の更新に失敗しました", Toast.LENGTH_LONG).show()
                    }
                ).addTo(disposable)
        }
        else if(requestCode == REQUEST_ZOOM) {
            /* 画像の拡大表示が終わった後 */
            val item = imageFields?.firstOrNull { it == fcItem }
            if(item != null) {

                imagesController = context?.let { ImagesController(it, projectId) }

                item?.bitmap = imagesController?.getCacheBitmap(
                                                 item?.item_id.toString(),
                                                 item?.project_image_id.toString())

                recyclerView.adapter?.notifyDataSetChanged()
            }
        }
    }

    private fun sendToService(bmp:Bitmap, item:LdbItemImageRecord ) {
        SenderHandler.lock.withLock {
            Log.d("send", "itemImageFragment ロック通過")

            //画像を保存する
            if ((bmp != null) && (item != null)) {
                //表示用(幅230)の小さいアイコンを作成して保存
                val smallBmp = saveImage(
                    bmp,
                    item?.item_id.toString(),
                    item?.project_image_id.toString(),
                    230.0f, SMALL_IMAGE)

                item?.bitmap = smallBmp

                //1080の大きな画像を保存する
                saveImage(
                    bmp,
                    item?.item_id.toString(),
                    item?.project_image_id.toString(),
                    1080.0f, BIG_IMAGE
                )

                //未送信フラグセット
                item.sync_status = SYNC_STATUS_NEW
                item.error_msg = "送信待ちです"
                //保存
                Log.d("send", item.toString())
                var ret:Pair<Boolean, Long>? = null

                ret = imagesController?.save2LDB(item) //戻り値はPair型。(ステータス,保存時のＩＤ)
                if(ret?.first == true) {
                    val status = ret?.first
                    val imageId = ret?.second
                    if (status) {
                        item.image_id = imageId
                        //画面を更新する

                    }
                }
                else {
                    Log.d("send", "データベースの保存に失敗しました")
                }
            }
        }
    }

    /**
     * 引数widthに指定した幅の画像を保存する。
     * @params
     * bmp:圧縮していない画像
     * itemId:item_id
     * projectImageId:project_image_id
     * width:画像の幅
     * sizeType:小さな画像として保存する場合、0,拡大用の画像として保存する場合、1
     */
    private fun saveImage(bmp:Bitmap, itemId:String, projectImageId:String, width:Float, sizeType:Int):Bitmap? {
        var rate: Float
        var rtnBmp:Bitmap? = null
        try {
            //オリジナルサイズの画像を保存する
            rate = (bmp.width / width)
            var dstWidth = (bmp.width / rate).toInt()
            var dstHeight = (bmp.height / rate).toInt()
            rtnBmp = Bitmap.createScaledBitmap(bmp, dstWidth, dstHeight, true)
            imagesController?.saveBitmap(rtnBmp,
                                         itemId,
                                         projectImageId,
                                         sizeType)
        }
        catch(e:Exception) {
            e.printStackTrace()
        }
        return rtnBmp
    }
}
