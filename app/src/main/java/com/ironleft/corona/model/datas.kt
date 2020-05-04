package com.ironleft.corona.model

import com.google.android.gms.maps.model.LatLng
import com.ironleft.corona.api.DataCountry
import com.ironleft.corona.api.DataGraph
import com.ironleft.corona.api.DataNews
import com.lib.util.Log
import kotlin.math.min

data class CountryData(val title:String, val latLng: LatLng = LatLng(0.0,0.0)){}

data class VirusConfirmedData(val id:String){

    var confirmed:Long = 0 ; private set
    var increase:Long = 0 ; private set
    var deaths:Long = 0 ; private set
    var recovered:Long = 0 ; private set
    var map :MapData? = null ; private set
    fun setData(data:DataCountry):VirusConfirmedData{
        confirmed = data.confirmed?.toLongOrNull() ?: 0
        increase = data.gap_confirmed?.toLongOrNull() ?: 0
        deaths = data.deaths?.toLongOrNull() ?: 0
        recovered = data.recovered?.toLongOrNull() ?: 0
        map = MapData(id).setData(data, confirmed)
        return this
    }
    fun setData(prev:GraphData?, current:GraphData):VirusConfirmedData{
        confirmed = current.confirmed
        increase = if(prev == null) 0 else current.confirmed - prev.confirmed
        deaths = current.deaths
        recovered =current.recovered
        return this
    }
    fun sum(sumData:VirusConfirmedData){
        confirmed += sumData.confirmed
        increase += sumData.increase
        deaths += sumData.deaths
        recovered += sumData.recovered

    }

    val deathsRatio:Double
        get() {
            if(confirmed <= 0) return 0.0
            return deaths.toDouble() / confirmed.toDouble()
        }
    val recoveredRatio:Double
        get() {
            if(confirmed <= 0) return 0.0
            return recovered.toDouble() / confirmed.toDouble()
        }

    val infoDatas:String
        get() {
            return "confirmed : $confirmed/deaths : $deaths/recovered : $recovered"
        }

}

data class MapData(val id:String){

    var title:String = "" ; private set
    var latLng: LatLng = LatLng(0.0,0.0) ; private set
    var confirmed:Long = 0 ; private set
    var lastUpdate:String = ""
    val unit = 500.0
    val limited:Long = 1000
    val colorLevel:Double
        get() {
            if(confirmed >= limited) return 1.0
            return confirmed.toDouble()/limited
        }
    val circleRadius:Double
        get() {
            val m = min(confirmed , limited)
            return m * unit
        }


    fun setData(data:DataCountry, confirmed:Long):MapData{
        title = data.country ?: ""
        lastUpdate = data.last_update ?: ""
        this.confirmed = confirmed
        val latitude = data.lati?.toDoubleOrNull() ?: 0.0
        val longitude = data.longi?.toDoubleOrNull() ?: 0.0
        latLng = LatLng(latitude,longitude)
        return this
    }
}



data class GraphData( val id:String){
    var confirmed:Long = 0 ; private set
    var deaths:Long = 0 ; private set
    var recovered:Long = 0 ; private set

    var diffConfirmed:Long = 0 ; private set
    var diffDeaths:Long = 0 ; private set
    var diffRecovered:Long = 0 ; private set

    var viewDate:String = "" ; private set
    fun setData(data:DataGraph):GraphData{
        confirmed = data.confirmed?.toLongOrNull() ?: 0
        deaths = data.deaths?.toLongOrNull() ?: 0
        recovered = data.recovered?.toLongOrNull() ?: 0
        viewDate = data.ymd ?: ""
        return this
    }

    fun setDiffData(prevData:GraphData? = null):Long{
        diffConfirmed = confirmed - (prevData?.confirmed ?: 0)
        diffDeaths = deaths - (prevData?.deaths ?: 0)
        diffRecovered = recovered - (prevData?.recovered ?: 0)
        return maxOf(diffConfirmed,diffDeaths,diffRecovered)
    }

    val deathsRatio:Double
        get() {
            if(confirmed <= 0) return 0.0
            return deaths.toDouble() / confirmed.toDouble()
        }
    val recoveredRatio:Double
        get() {
            if(confirmed <= 0) return 0.0
            return recovered.toDouble() / confirmed.toDouble()
        }
}



data class NewsData(val id:String){
    var desc:String = "" ; private set
    var date:String = "" ; private set
    var pageContent:String = "" ; private set
    var pageUrl:String = "" ; private set
    var isOpen = false
    fun setData(data:DataNews):NewsData{
        desc = data.title ?: ""
        date = data.post_datetime ?: ""
        pageUrl = data.url ?: ""
        pageContent = data.content ?: ""
        return this
    }
}