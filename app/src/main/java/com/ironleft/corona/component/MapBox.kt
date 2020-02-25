package com.ironleft.corona.component

import android.content.Context
import android.location.Location

import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import com.ironleft.corona.R
import com.ironleft.corona.model.MapData
import com.ironleft.corona.model.VirusConfirmedData
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

    var googleMap: GoogleMap? = null
        set(value) {
            field = value
            value ?: return
            findMe()
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
            onMapUpdate()
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
        context ?: return
        LocationServices.getFusedLocationProviderClient(context!!).lastLocation.addOnSuccessListener { location->
            if (location != null) meLocation = location
        }
    }

    private fun onMe(){
        val zoomLevel = defaultZoom // 2~21
        meLocation?.let { googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(it.latitude, it.longitude), zoomLevel)) }
    }

    private fun onMapUpdate() {
        googleMap ?: return

        loadingBar.visibility = View.GONE

        googleMap?.let { gmap ->

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

                    gmap.addMarker(markerOption)
                    val circleOptions = CircleOptions()
                        .center(map.latLng)
                        .radius(r)
                        .strokeWidth(1.0f)
                        .strokeColor(ContextCompat.getColor(ctx, R.color.color_white))
                        .fillColor(ContextCompat.getColor(ctx, colorLevels[cl]))
                    gmap.addCircle(circleOptions)

                }
                onMe()
                //initData?.let {  gmap.moveCamera(CameraUpdateFactory.newLatLng(it.latLng)) }
            }


        }
    }

}