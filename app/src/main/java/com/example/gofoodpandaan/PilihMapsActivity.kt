package com.example.gofoodpandaan

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.alfanshter.udinlelangfix.Session.SessionManager
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.mancj.materialsearchbar.MaterialSearchBar
import com.mancj.materialsearchbar.adapter.SuggestionsAdapter
import com.skyfishjy.library.RippleBackground
import kotlinx.android.synthetic.main.content_map.*
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast
import java.util.*

class PilihMapsActivity : AppCompatActivity(), OnMapReadyCallback {
    private var mFusedLocationProviderClient: FusedLocationProviderClient? = null
    private var placesClient: PlacesClient? = null
    lateinit var predictionList : List<AutocompletePrediction>
    private var mMap: GoogleMap? = null

    private var rippleBg: RippleBackground? = null
    lateinit var sessionManager: SessionManager
    private var locationCallback: LocationCallback? = null

    private var mLastKnownLocation: Location? = null
   companion object{
       private const val DEFAULT_ZOOM = 15

   }

    private var mapView: View? = null
    lateinit var materialSearchBar: MaterialSearchBar
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pilih_maps)
        sessionManager = SessionManager(this)

        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment!!.getMapAsync(this)
        materialSearchBar = findViewById(R.id.searchBar)
        rippleBg = findViewById(R.id.ripple_bg)

        mapView = mapFragment.view
        mFusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(this@PilihMapsActivity)
        Places.initialize(this@PilihMapsActivity, getString(R.string.google_maps_key))
        placesClient = Places.createClient(this)
        val token = AutocompleteSessionToken.newInstance()

        materialSearchBar.setOnSearchActionListener(object :
            MaterialSearchBar.OnSearchActionListener {
            override fun onSearchStateChanged(enabled: Boolean) {}
            override fun onSearchConfirmed(text: CharSequence) {
                startSearch(text.toString(), true, null, true)
            }

            override fun onButtonClicked(buttonCode: Int) {
                if (buttonCode == MaterialSearchBar.BUTTON_NAVIGATION) {
                    //opening or closing a navigation drawer
                } else if (buttonCode == MaterialSearchBar.BUTTON_BACK) {
                    materialSearchBar.disableSearch()
                }
            }
        })

        materialSearchBar.addTextChangeListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence,
                start: Int,
                before: Int,
                count: Int
            ) {
                val predictionsRequest =
                    FindAutocompletePredictionsRequest.builder()
                        .setTypeFilter(TypeFilter.ADDRESS)
                        .setSessionToken(token)
                        .setQuery(s.toString())
                        .build()
                placesClient!!.findAutocompletePredictions(predictionsRequest)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val predictionsResponse =
                                task.result
                            if (predictionsResponse != null) {
                                predictionList =
                                    predictionsResponse.autocompletePredictions
                                val suggestionsList: MutableList<String?> =
                                    ArrayList()
                                for (i in predictionList.indices) {
                                    val prediction: AutocompletePrediction =
                                        predictionList.get(i)
                                    suggestionsList.add(prediction.getFullText(null).toString())
                                }
                                materialSearchBar.updateLastSuggestions(suggestionsList)
                                if (!materialSearchBar.isSuggestionsVisible) {
                                    materialSearchBar.showSuggestionsList()
                                }
                            }
                        } else {
                            Log.i("mytag", "prediction fetching task unsuccessful")
                        }
                    }
            }

            override fun afterTextChanged(s: Editable) {}
        })

        materialSearchBar.setSuggstionsClickListener(object :
            SuggestionsAdapter.OnItemViewClickListener {
            override fun OnItemClickListener(position: Int, v: View) {
                if (position >= predictionList.size) {
                    return
                }
                val selectedPrediction = predictionList[position]
                val suggestion =
                    materialSearchBar.lastSuggestions[position].toString()
                materialSearchBar.text = suggestion
                Handler().postDelayed({ materialSearchBar.clearSuggestions() }, 1000)
                val imm =
                    getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm?.hideSoftInputFromWindow(
                    materialSearchBar.windowToken,
                    InputMethodManager.HIDE_IMPLICIT_ONLY
                )
                val placeId = selectedPrediction.placeId
                val placeFields =
                    Arrays.asList(Place.Field.LAT_LNG)
                val fetchPlaceRequest =
                    FetchPlaceRequest.builder(placeId, placeFields).build()
                placesClient!!.fetchPlace(fetchPlaceRequest)
                    .addOnSuccessListener { fetchPlaceResponse ->
                        val place = fetchPlaceResponse.place
                        Log.i("mytag", "Place found: " + place.name)
                        val latLngOfPlace = place.latLng
                        if (latLngOfPlace != null) {
                            mMap!!.moveCamera(
                                CameraUpdateFactory.newLatLngZoom(
                                    latLngOfPlace,
                                    DEFAULT_ZOOM.toFloat()
                                )
                            )
                        }
                    }.addOnFailureListener { e ->
                        if (e is ApiException) {
                            val apiException = e as ApiException
                            apiException.printStackTrace()
                            val statusCode = apiException.statusCode
                            Log.i("mytag", "place not found: " + e.message)
                            Log.i("mytag", "status code: $statusCode")
                        }
                    }
            }

            override fun OnItemDeleteListener(position: Int, v: View) {}
        })

        btn_accept.setOnClickListener(View.OnClickListener {
            val latitude = mMap!!.cameraPosition.target.latitude
            val longitude = mMap!!.cameraPosition.target.longitude
            rippleBg!!.startRippleAnimation()
                IkiOjekActivity.latitude = latitude.toString()
                IkiOjekActivity.longitude = longitude.toString()

            startActivity<IkiOjekActivity>()
            Handler().postDelayed({ rippleBg!!.stopRippleAnimation() }, 3000)
        })
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 51) {
            if (resultCode == RESULT_OK) {
                getDeviceLocation()

            }
        }
    }

    fun showMainMarker(lat: Double, lon: Double, msg: String) {

        val coordinate = LatLng(lat, lon)
        IkiOjekActivity.peta?.addMarker(
            MarkerOptions().position(coordinate).title(msg)
                .icon(BitmapDescriptorFactory.defaultMarker())
                .draggable(true)
        )
        val cameraPosition =
            CameraPosition.Builder().target(LatLng(lat, lon)).zoom(17f).build()
        IkiOjekActivity.peta?.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
    }



    override fun onMapReady(googleMap: GoogleMap?) {
        mMap = googleMap
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
        mMap!!.isMyLocationEnabled = true
        mMap!!.uiSettings.isMyLocationButtonEnabled = true

        if (mapView != null && mapView!!.findViewById<View?>("1".toInt()) != null) {
            val locationButton =
                (mapView!!.findViewById<View>("1".toInt())
                    .parent as View).findViewById<View>("2".toInt())
            val layoutParams =
                locationButton.layoutParams as RelativeLayout.LayoutParams
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0)
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE)
            layoutParams.setMargins(0, 0, 40, 180)
        }

        //check if gps is enabled or not and then request user to enable it

        //check if gps is enabled or not and then request user to enable it
        val locationRequest = LocationRequest.create()
        locationRequest.interval = 10000
        locationRequest.fastestInterval = 5000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        val builder =
            LocationSettingsRequest.Builder().addLocationRequest(locationRequest)

        val settingsClient = LocationServices.getSettingsClient(this@PilihMapsActivity)
        val task =
            settingsClient.checkLocationSettings(builder.build())

        task.addOnSuccessListener(
            this@PilihMapsActivity,
            OnSuccessListener<LocationSettingsResponse?> { getDeviceLocation() })

        task.addOnFailureListener(this@PilihMapsActivity,
            OnFailureListener { e ->
                if (e is ResolvableApiException) {
                    try {
                        e.startResolutionForResult(this@PilihMapsActivity, 51)
                    } catch (e1: IntentSender.SendIntentException) {
                        e1.printStackTrace()
                    }
                }
            })

        mMap!!.setOnMyLocationButtonClickListener {
            if (materialSearchBar.isSuggestionsVisible) materialSearchBar.clearSuggestions()
            if (materialSearchBar.isSearchEnabled) materialSearchBar.disableSearch()
            false
        }
        getDeviceLocation()

    }

    @SuppressLint("MissingPermission")
    private fun getDeviceLocation() {
        mFusedLocationProviderClient!!.lastLocation
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    mLastKnownLocation = task.result
                    if (mLastKnownLocation != null) {
                        mMap!!.moveCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                LatLng(
                                    mLastKnownLocation!!.getLatitude(),
                                    mLastKnownLocation!!.getLongitude()
                                ), DEFAULT_ZOOM.toFloat()
                            )
                        )
                    } else {
                        val locationRequest = LocationRequest.create()
                        locationRequest.interval = 10000
                        locationRequest.fastestInterval = 5000
                        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                        locationCallback =
                            object : LocationCallback() {
                                override fun onLocationResult(locationResult: LocationResult) {
                                    super.onLocationResult(locationResult)
                                    if (locationResult == null) {
                                        return
                                    }
                                    mLastKnownLocation = locationResult.lastLocation
                                    mMap!!.moveCamera(
                                        CameraUpdateFactory.newLatLngZoom(
                                            LatLng(
                                                mLastKnownLocation!!.getLatitude(),
                                                mLastKnownLocation!!.getLongitude()
                                            ), DEFAULT_ZOOM.toFloat()
                                        )
                                    )
                                    mFusedLocationProviderClient!!.removeLocationUpdates(
                                        locationCallback
                                    )
                                }
                            }
                        mFusedLocationProviderClient!!.requestLocationUpdates(
                            locationRequest,
                            locationCallback,
                            null
                        )
                    }
                } else {
                    Toast.makeText(
                        this@PilihMapsActivity,
                        "unable to get last location",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

}