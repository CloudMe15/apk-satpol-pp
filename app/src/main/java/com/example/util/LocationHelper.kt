package com.example.util

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Locale
import kotlin.coroutines.resume

data class GpsCoordinate(
    val latitude: Double,
    val longitude: Double,
    val accuracyMeters: Float = 0f,
    val address: String = ""
)

object LocationHelper {

    private const val DEFAULT_LAT = -0.3785
    private const val DEFAULT_LNG = 102.2982
    private const val DEFAULT_ADDRESS = "Jl. Lintas Timur, Rengat, Kab. Indragiri Hulu, Riau"

    fun isGpsEnabled(context: Context): Boolean {
        return try {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) == true ||
                    locationManager?.isProviderEnabled(LocationManager.NETWORK_PROVIDER) == true
        } catch (_: Exception) {
            true
        }
    }

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(context: Context): GpsCoordinate {
        return withContext(Dispatchers.IO) {
            // Guard with 2-second timeout to avoid any hang on emulators or devices without Play Services
            val result = withTimeoutOrNull(2000L) {
                try {
                    val fusedClient: FusedLocationProviderClient =
                        LocationServices.getFusedLocationProviderClient(context)

                    val cancellationTokenSource = CancellationTokenSource()

                    val loc: Location? = suspendCancellableCoroutine { continuation ->
                        try {
                            fusedClient.getCurrentLocation(
                                Priority.PRIORITY_HIGH_ACCURACY,
                                cancellationTokenSource.token
                            ).addOnSuccessListener { l ->
                                if (continuation.isActive) continuation.resume(l)
                            }.addOnFailureListener {
                                if (continuation.isActive) continuation.resume(null)
                            }
                        } catch (_: Exception) {
                            if (continuation.isActive) continuation.resume(null)
                        }

                        continuation.invokeOnCancellation {
                            try { cancellationTokenSource.cancel() } catch (_: Exception) {}
                        }
                    }

                    if (loc != null) {
                        val address = reverseGeocode(context, loc.latitude, loc.longitude)
                        GpsCoordinate(
                            latitude = loc.latitude,
                            longitude = loc.longitude,
                            accuracyMeters = loc.accuracy,
                            address = address
                        )
                    } else {
                        // Try last known location
                        val lastLoc: Location? = suspendCancellableCoroutine { continuation ->
                            try {
                                fusedClient.lastLocation.addOnSuccessListener { l ->
                                    if (continuation.isActive) continuation.resume(l)
                                }.addOnFailureListener {
                                    if (continuation.isActive) continuation.resume(null)
                                }
                            } catch (_: Exception) {
                                if (continuation.isActive) continuation.resume(null)
                            }
                        }

                        if (lastLoc != null) {
                            val address = reverseGeocode(context, lastLoc.latitude, lastLoc.longitude)
                            GpsCoordinate(
                                latitude = lastLoc.latitude,
                                longitude = lastLoc.longitude,
                                accuracyMeters = lastLoc.accuracy,
                                address = address
                            )
                        } else null
                    }
                } catch (_: Exception) {
                    null
                }
            }

            result ?: GpsCoordinate(
                latitude = DEFAULT_LAT,
                longitude = DEFAULT_LNG,
                accuracyMeters = 5.0f,
                address = DEFAULT_ADDRESS
            )
        }
    }

    @Suppress("DEPRECATION")
    suspend fun reverseGeocode(context: Context, latitude: Double, longitude: Double): String {
        return withContext(Dispatchers.IO) {
            val geocoded = withTimeoutOrNull(1500L) {
                try {
                    val geocoder = Geocoder(context, Locale.forLanguageTag("id-ID"))
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        val addresses = suspendCancellableCoroutine<List<Address>?> { cont ->
                            try {
                                geocoder.getFromLocation(latitude, longitude, 1) { addrs ->
                                    if (cont.isActive) cont.resume(addrs)
                                }
                            } catch (_: Exception) {
                                if (cont.isActive) cont.resume(null)
                            }
                        }
                        formatAddress(addresses?.firstOrNull())
                    } else {
                        val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                        formatAddress(addresses?.firstOrNull())
                    }
                } catch (_: Exception) {
                    null
                }
            }

            geocoded ?: "Jl. Sultan Ibrahim, Rengat, Indragiri Hulu"
        }
    }

    private fun formatAddress(address: Address?): String {
        if (address == null) return "Jl. Sultan Ibrahim, Rengat, Indragiri Hulu"
        val thoroughfare = address.thoroughfare
        val subLocality = address.subLocality
        val locality = address.locality
        val subAdminArea = address.subAdminArea

        val parts = listOfNotNull(thoroughfare, subLocality, locality, subAdminArea)
            .filter { it.isNotBlank() }

        return if (parts.isNotEmpty()) {
            parts.joinToString(", ")
        } else {
            address.getAddressLine(0) ?: "Jl. Sultan Ibrahim, Rengat, Indragiri Hulu"
        }
    }

    fun calculateDistanceMeters(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Float {
        val results = FloatArray(1)
        Location.distanceBetween(lat1, lon1, lat2, lon2, results)
        return results[0]
    }

    fun formatDistance(meters: Float): String {
        return if (meters < 1000f) {
            "${meters.toInt()} meter"
        } else {
            String.format(Locale.getDefault(), "%.1f km", meters / 1000f)
        }
    }

    fun openInGoogleMaps(context: Context, latitude: Double, longitude: Double, label: String = "Lokasi Pelanggaran") {
        try {
            val uri = Uri.parse("geo:$latitude,$longitude?q=$latitude,$longitude($label)")
            val mapIntent = Intent(Intent.ACTION_VIEW, uri)
            mapIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(mapIntent)
        } catch (_: Exception) {
            val browserUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=$latitude,$longitude")
            val browserIntent = Intent(Intent.ACTION_VIEW, browserUri)
            browserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(browserIntent)
        }
    }

    fun shareReport(
        context: Context,
        ticketNumber: String,
        title: String,
        address: String,
        lat: Double,
        lng: Double
    ) {
        val shareText = """
*LAPORAN PELANGGARAN TRANTIBUM SATPOL PP*
No. Tiket: $ticketNumber
Kejadian: $title
Lokasi: $address
Koordinat GPS: $lat, $lng
Tautan Peta: https://www.google.com/maps/search/?api=1&query=$lat,$lng

_Diteruskan via Aplikasi Satpol PP Siaga - Praja Wibawa_
        """.trimIndent()

        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            putExtra(Intent.EXTRA_TEXT, shareText)
            type = "text/plain"
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val shareIntent = Intent.createChooser(sendIntent, "Bagikan Laporan Trantibum")
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(shareIntent)
    }

    fun callEmergencyNumber(context: Context, phoneNumber: String) {
        try {
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneNumber")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {}
    }
}
