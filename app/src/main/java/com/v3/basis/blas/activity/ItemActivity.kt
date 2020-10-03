package com.v3.basis.blas.activity

import android.Manifest
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.PopupMenu
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.NavigationUI.setupWithNavController
import androidx.navigation.ui.setupActionBarWithNavController
import com.v3.basis.blas.R
import com.v3.basis.blas.ui.ext.setBlasCustomView
import com.v3.basis.blas.ui.ext.showBackKeyForActionBar
import kotlinx.android.synthetic.main.activity_item.*
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Handler
import android.os.Looper
import android.os.ResultReceiver
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.v3.basis.blas.blasclass.service.FetchAddressIntentService
import com.v3.basis.blas.blasclass.service.LocationConstants
import com.v3.basis.blas.ui.terminal.BottomNavButton
import kotlinx.android.synthetic.main.fragment_terminal.view.*


/**
 * データ管理画面クラス
 */
class ItemActivity : AppCompatActivity() {

    companion object {
        var reloard: Boolean = false
        var searchFreeWord: String? = null
        fun setRestartFlag() {
            reloard = true
        }
    }

    private val REQUESTCODE_TEST = 1

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var lastLocation: Location? = null
    private lateinit var resultReceiver: AddressResultReceiver
    private var callBack: ((address: String) -> Unit)? = null
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    data class formType(var type: String?,
                        var title: String?,
                        var field_col: String?,
                        var choiceValue: List<String?>?,
                        var require:String?,
                        var unique:String?)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_item)

        val navController = findNavController(R.id.nav_host_fragment)
        setupWithNavController(item_list_bottom_navigation, navController)
        fusedLocationClient = FusedLocationProviderClient(this)

        locationRequest = LocationRequest().apply {
            // 精度重視(電力大)と省電力重視(精度低)を両立するため2種類の更新間隔を指定
            // 今回は公式のサンプル通りにする。
            interval = 10000                                   // 最遅の更新間隔(但し正確ではない。)
            fastestInterval = 5000                             // 最短の更新間隔
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY  // 精度重視
        }

        // コールバック
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                // 更新直後の位置が格納されているはず
                lastLocation = locationResult?.lastLocation ?: return
                startIntentService()
                fusedLocationClient.removeLocationUpdates(this)
//                Toast.makeText(this@ItemActivity,
//                    "緯度:${location.latitude}, 経度:${location.longitude}", Toast.LENGTH_LONG).show()
            }
        }

        resultReceiver = AddressResultReceiver(Handler())

        //タイトルバーの名称を変更する処理
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navi_item_view, //データ一覧
                R.id.navi_item_create, //データ新規作成
                R.id.navi_item_seach, //データ検索
                R.id.navi_item_drawing_seach
               // R.id.navi_item_back
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)

        /**
         * 戻るボタンが非表示になる問題の修正
         * [setupActionBarWithNavController]内部で[setDisplayHomeAsUpEnabled]をfalseにする処理が走るため、
         * リスナーにて再度[setDisplayHomeAsUpEnabled]trueとする。
         */
        navController.addOnDestinationChangedListener{ _, destination, _ ->
            showBackKeyForActionBar()
            supportActionBar?.title = destination.label
        }

        setBlasCustomView()

        navi_item_drawing_seach.setOnClickListener {
            val intent = Intent(this, DrawingSearchActivity::class.java)
            val token:String = this.intent.extras?.get("token") as String? ?: ""
            intent.putExtra("token", token)
            val projectId:String = this.intent.extras?.get("project_id") as String? ?: ""
            intent.putExtra("project_id", projectId)
            startActivity(intent)
        }
    }

    /**
     * [説明]
     * データ一覧画面で画面上部の←ボタンを押したときにコールされる。
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        // 検索ワードを消去
        searchFreeWord = null

        when (item.itemId) {
            android.R.id.home -> {
                val navController = findNavController(R.id.nav_host_fragment)

                if (navController.currentDestination?.id == R.id.navi_item_view) {
                    this.finish()
                    return true
                } else {
                    //  ルート以外の画面なら、ルート画面に遷移させる！
                    transitionItemListScreen()
                }
            }
            else ->
                return super.onOptionsItemSelected(item)
        }
        return true
    }

    fun transitionItemListScreen() {
        val navController = findNavController(R.id.nav_host_fragment)
        val menu = PopupMenu(this, null).menu
        menuInflater.inflate(R.menu.bottom_navigation_menu_item, menu)
        NavigationUI.onNavDestinationSelected(menu.get(0), navController)
    }


    fun deleteFragment(fragment: Fragment){
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.remove(fragment)
        fragmentTransaction.commit()
    }

    override fun onRestart() {
        super.onRestart()
        if (reloard) {
            reloard = false
            reloard()
        }
    }

    fun reloard(){
        val intent = intent
        overridePendingTransition(0, 0)
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
        finish()
        overridePendingTransition(0, 0)
        startActivity(intent)
    }

    fun fetchLocationAddress(callBack: (String) -> Unit) {
        this.callBack = callBack
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper())
    }

    private fun startIntentService() {
        val intent = Intent(this, FetchAddressIntentService::class.java).apply {
            putExtra(LocationConstants.RECEIVER, resultReceiver)
            putExtra(LocationConstants.LOCATION_DATA_EXTRA, lastLocation)
        }
        startService(intent)
    }

    internal inner class AddressResultReceiver(handler: Handler) : ResultReceiver(handler) {

        override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {

            // Display the address string
            // or an error message sent from the intent service.
            val addressOutput = resultData?.getString(LocationConstants.RESULT_DATA_KEY) ?: ""
            callBack?.invoke(addressOutput)
        }
    }

}
