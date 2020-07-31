package com.v3.basis.blas.blasclass.service

import android.app.IntentService
import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.os.ResultReceiver
import android.util.Log
import java.io.IOException
import java.util.*

/**
 * 緯度経度から住所を取得するService
 * １：緯度経度を取得する
 * ２：ジオコーディングで緯度経度を住所に変換する
 */
class FetchAddressIntentService: IntentService("FetchAddressIntentService") {

    private var receiver: ResultReceiver? = null
    private val TAG = ""

    override fun onHandleIntent(intent: Intent?) {
        intent ?: return

        var errorMessage = ""

        // Get the location passed to this service through an extra.
        val location: Location = intent.getParcelableExtra(LocationConstants.LOCATION_DATA_EXTRA)
        val geocoder = Geocoder(this, Locale.getDefault())
        var addresses: List<Address> = emptyList()

        try {
            addresses = geocoder.getFromLocation(
                location.latitude,
                location.longitude,
                // In this sample, we get just a single address.
                1)
        } catch (ioException: IOException) {
            // Catch network or other I/O problems.
            errorMessage = "Catch network or other I/O problems"
            Log.e(TAG, errorMessage, ioException)
        } catch (illegalArgumentException: IllegalArgumentException) {
            // Catch invalid latitude or longitude values.
            errorMessage = "Catch invalid latitude or longitude values."
            Log.e(TAG, "$errorMessage. Latitude = $location.latitude , " +
                    "Longitude =  $location.longitude", illegalArgumentException)
        }

        // Handle case where no address was found.
        if (addresses.isEmpty()) {
            if (errorMessage.isEmpty()) {
                errorMessage = "No address found"
                Log.e(TAG, errorMessage)
            }
            deliverResultToReceiver(LocationConstants.FAILURE_RESULT, errorMessage)
        } else {
            val address = addresses[0]
            // Fetch the address lines using getAddressLine,
            // join them, and send them to the thread.
            val addressFragments = with(address) {
                (0..maxAddressLineIndex).map { getAddressLine(it) }
            }
            deliverResultToReceiver(LocationConstants.SUCCESS_RESULT,
                addressFragments.joinToString(separator = "\n"))
        }
    }

    private fun deliverResultToReceiver(resultCode: Int, message: String) {
        val bundle = Bundle().apply { putString(LocationConstants.RESULT_DATA_KEY, message) }
        receiver?.send(resultCode, bundle)
    }
}
