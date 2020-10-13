package com.example.gofoodpandaan

import android.app.Activity
import android.app.Dialog
import android.app.DialogFragment
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions


class ConfirmAddress : DialogFragment(),
    View.OnClickListener,
    OnMapReadyCallback {
    var c: Activity? = null
    var d: Dialog? = null
    var yes: Button? = null
    var no: Button? = null
    private var mMap: GoogleMap? = null
    var mapView: MapView? = null
    var Lat: Double? = null
    var Long: Double? = null
    var Address: String? = null
    var myAddress: TextView? = null
    var SelectBtn: Button? = null
    var ChangeBtn: Button? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Lat = arguments.getDouble("lat")
        Long = arguments.getDouble("long")
        Address = arguments.getString("address")
    }

    var mapFragment: MapFragment? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle
    ): View? {
        val v: View =
            inflater.inflate(R.layout.fragment_customplacepicker, container, false)
        myAddress = v.findViewById<View>(R.id.myAddress) as TextView
        SelectBtn = v.findViewById<View>(R.id.btn_Select) as Button
        ChangeBtn = v.findViewById<View>(R.id.btn_Change) as Button
        mapFragment = fragmentManager.findFragmentById(R.id.mapp) as MapFragment
        mapFragment!!.getMapAsync(this)
        // Toast.makeText(getActivity(),mNum,Toast.LENGTH_LONG).show();
        SelectBtn!!.setOnClickListener {
            Toast.makeText(activity, myAddress!!.text.toString(), Toast.LENGTH_LONG)
                .show()
            fragmentManager.beginTransaction().remove(mapFragment).commit()
            dismiss()
        }
        ChangeBtn!!.setOnClickListener {
            fragmentManager.beginTransaction().remove(mapFragment).commit()
            dismiss()
        }
        dialog.setCanceledOnTouchOutside(true)
        return v
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        fragmentManager.beginTransaction().remove(mapFragment).commit()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        dismiss()
    }

    override fun onClick(v: View) {}
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        myAddress!!.text = Address
        mMap!!.uiSettings.isMyLocationButtonEnabled = false
        val markerOptions = MarkerOptions()
        markerOptions.position(LatLng(Lat!!, Long!!))
        markerOptions.title(Address)
        mMap!!.clear()
        val location = CameraUpdateFactory.newLatLngZoom(
            LatLng(Lat!!, Long!!), 16f
        )
        mMap!!.animateCamera(location)
        mMap!!.addMarker(markerOptions)
        Log.d("status", "success")
    }
}