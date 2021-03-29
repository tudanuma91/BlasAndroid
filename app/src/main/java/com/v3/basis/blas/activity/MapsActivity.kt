package com.v3.basis.blas.activity

import android.location.Geocoder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.v3.basis.blas.R
import com.v3.basis.blas.ui.common.ARG_PROJECT_ID
import com.v3.basis.blas.ui.common.ARG_PROJECT_NAME
import com.v3.basis.blas.ui.common.ARG_TOKEN
import java.util.*

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private var address:String = ""
    private var title:String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val extras = this.intent?.extras

        if (extras?.getString("address") != null) {
            address = extras.getString("address").toString()
        }
        if (extras?.getString("title") != null) {
            title = extras.getString("title").toString()
        }

        //タイトルに住所を表示
        this.setTitle(address)
        //戻るボタンを作成
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        this.finish()
        return super.onOptionsItemSelected(item)
    }
    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isScrollGesturesEnabled = true
        mMap.uiSettings.isZoomGesturesEnabled = true
        mMap.uiSettings.isMapToolbarEnabled = true
        mMap.uiSettings.isCompassEnabled = true
        mMap.uiSettings.isMyLocationButtonEnabled = true

        val geoCorder = Geocoder(this, Locale.getDefault())
        val addrList = geoCorder.getFromLocationName(address, 1)
        if(!addrList.isEmpty()) {
            val address = addrList.get(0)
            // Add a marker in Sydney and move the camera
            val latlng = LatLng(address.latitude, address.longitude)
            //mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(sydney, 1.0f))
            mMap.addMarker(MarkerOptions().position(latlng).title(title))
            val zoomRate = 15.0f//mMap.maxZoomLevel
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, zoomRate))
        }
        else {
            Toast.makeText(applicationContext, "地図の情報を取得できませんでした", Toast.LENGTH_LONG).show()
            finish()
        }
    }
}