package com.ironleft.corona.component

import android.Manifest
import android.content.Context
import android.location.Location

import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import com.ironleft.corona.PageID
import com.ironleft.corona.R
import com.ironleft.corona.model.MapData
import com.ironleft.corona.model.VirusConfirmedData
import com.lib.page.PagePresenter
import com.lib.util.Log
import com.skeleton.rx.RxFrameLayout
import com.skeleton.view.item.ListItem
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.cp_map_box.view.*
import kotlinx.android.synthetic.main.item_map_snippet.view.*
import kotlin.math.floor


class MapBox: RxFrameLayout {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context,attrs)
    override fun getLayoutResId(): Int { return R.layout.cp_map_box }
    private val appTag = javaClass.simpleName

    val selectedLocationObservable = PublishSubject.create<Marker>()
    val selectLocationObservable = PublishSubject.create<Marker>()
    private val meLocationObservable = PublishSubject.create<Location>()
    private var meLocation: Location? = null
        set(value) {
            field = value
            value ?: return
            meLocationObservable.onNext(value)

        }
    var virusConfirmedDatas: List<VirusConfirmedData>? = null
        set(value) {
            field = value
            value ?: return
            onMapUpdate()
        }
    var initLocation: LatLng? = null
        set(value) {
            field = value
            if(field == null) findMe()
            else googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(field!!, defaultZoom))
        }

    var googleMap: GoogleMap? = null
        set(value) {
            field = value
            value ?: return
            googleMap ?: return
            googleMap?.setMinZoomPreference(minZoom)
            googleMap?.setMaxZoomPreference(maxZoom)
            googleMap?.setMapStyle( MapStyleOptions.loadRawResourceStyle(context, R.raw.style_json) )
            googleMap?.isMyLocationEnabled = true
            googleMap?.setInfoWindowAdapter(object :GoogleMap.InfoWindowAdapter{
                override fun getInfoContents(marker: Marker?): View? {
                    context ?: return null
                    val info = Item(context!!)
                    info.setData(marker)
                    return info
                }
                override fun getInfoWindow(marker: Marker?): View? {
                    context ?: return null
                    val info = Item(context!!)
                    info.setData(marker)
                    return info
                }

            })
            googleMap?.setOnInfoWindowClickListener {
                selectedLocationObservable.onNext(it)
            }
            googleMap?.setOnMarkerClickListener {
                selectLocationObservable.onNext(it)
                false
            }
            onMapUpdate()
            if(initLocation == null) findMe()
        }


    override fun onCreatedView() {
        loadingBar.visibility = View.VISIBLE
    }

    override fun onDestroyedView() {
        googleMap = null
        virusConfirmedDatas = null
    }


    private val colorLevels = listOf(
        R.color.color_red_opacity_10,
        R.color.color_red_opacity_20,
        R.color.color_red_opacity_30,
        R.color.color_red_opacity_40,
        R.color.color_red_opacity_50,
        R.color.color_red_opacity_60)

    inner class Item(context: Context) : ListItem(context) {
        override fun getLayoutResId(): Int  = R.layout.item_map_snippet
        private var currentData: Marker? = null
            set(value) {
                field = value
                value?.let { data->
                    textTitle.text = data.title
                    val datas = data.snippet.split("/")
                    textConfirmed.text = datas[0]
                    textDeaths.text = datas[1]
                    textRecovered.text = datas[2]
                }
            }

        override fun setData(data: Any?, idx:Int){
            super.setData(data, idx)
            currentData = data as? Marker?
        }


    }

    private val defaultZoom = 4.0f
    private val maxZoom = 6.0f
    private val minZoom = 3.0f


    private fun findMe(){
        googleMap ?: return
        context ?: return
        if( !PagePresenter.getInstance<PageID>().hasPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)) ) return
        LocationServices.getFusedLocationProviderClient(context!!).lastLocation.addOnSuccessListener { location->
            if (location != null) {
                meLocation = location
                meLocation?.let { googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(it.latitude, it.longitude), defaultZoom)) }
            }
        }
    }


    private fun onMapUpdate() {
        googleMap ?: return

        loadingBar.visibility = View.GONE

        googleMap?.let { gmap ->
            gmap.clear()
            //gmap.setOnCircleClickListener { Log.i(appTag, "on Circle click") }
            context?.let { ctx->

                var initData: MapData? = null
                virusConfirmedDatas?.forEach {data->
                    if(initData == null) initData = data.map
                    val map = data.map
                    map ?: return
                    val cl = floor((colorLevels.size-1) * map.colorLevel).toInt()
                    val markerOption = MarkerOptions()
                        .position(map.latLng)
                        .title(map.title)
                        .snippet(data.infoDatas)
                    val r =  map.circleRadius
                    val marker = gmap.addMarker(markerOption)
                    val circleOptions = CircleOptions()
                        .clickable(true)
                        .center(map.latLng)
                        .radius(r)
                        .strokeWidth(1.0f)
                        .strokeColor(ContextCompat.getColor(ctx, R.color.color_white))
                        .fillColor(ContextCompat.getColor(ctx, colorLevels[cl]))
                    gmap.addCircle(circleOptions)
                }
                //initData?.let {  gmap.moveCamera(CameraUpdateFactory.newLatLng(it.latLng)) }
            }


        }
    }

}