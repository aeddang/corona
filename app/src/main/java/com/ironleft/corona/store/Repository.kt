package com.ironleft.corona.store
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.maps.model.Marker
import com.ironleft.corona.R
import com.ironleft.corona.api.ApiConst
import com.ironleft.corona.api.ApiRequest
import com.ironleft.corona.api.DataCountry
import com.ironleft.corona.model.*


import com.skeleton.module.network.NetworkFactory
import com.skeleton.module.network.RxObservableConverter
import com.skeleton.view.alert.CustomAlert
import com.skeleton.view.alert.CustomToast
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import java.util.*


class Repository(
    val ctx: Context,
    val setting:SettingPreference,
    networkFactory: NetworkFactory
) {

    private val appTag = "Repository"
    private val disposables = CompositeDisposable()
    private val api = networkFactory.getRetrofit(ApiConst.API_PATH).create(ApiRequest::class.java)

    var selectedMarker : Marker? = null
        set(value) {
            field = value
            titleObservable.onNext(field?.title ?: ctx.getString(R.string.cp_tab_title))
        }
    val titleObservable = PublishSubject.create<String>()

    var virusConfirmedDatas: List<VirusConfirmedData>? = null ; private set
    var virusConfirmedData: VirusConfirmedData? = null
        private set(value) {
            field = value
            value ?: return
            virusConfirmedDataObservable.onNext(value)
        }


    var graphDatas :List<GraphData>? = null
        private set(value) {
            field = value
            value ?: return
            graphDatasObservable.onNext(value)
        }

    var newsDatas:ArrayList<NewsData>? = null
        private set(value) {
            field = value
            value ?: return

        }

    val virusConfirmedDataObservable = PublishSubject.create<VirusConfirmedData>()
    val virusConfirmedCountryDataObservable = PublishSubject.create<VirusConfirmedData>()
    val graphDatasObservable = PublishSubject.create<List<GraphData>>()
    val newsDatasObservable = PublishSubject.create<ArrayList<NewsData>>()

    fun clearGraphs(){
        graphDatas = null
    }
    fun getGraphs(){
        val smode = if(selectedMarker == null) ApiConst.API_SMODE_GRAPH_TOTAL else ApiConst.API_SMODE_GRAPH
        RxObservableConverter.forNetwork( api.getGraphDatas(smode, selectedMarker?.title , null) ).subscribe(
            {result->
                graphDatas = result.map { GraphData(UUID.randomUUID().toString()).setData(it) }
                if(selectedMarker != null) graphDatas = graphDatas?.reversed()
            },{
                CustomToast.makeToast(ctx, R.string.error_data_loading).show()
            }
        ).apply { disposables.add(this) }
    }


    fun clearNews(){ newsDatas = null }
    fun getNews(page:Int = 0, perPage:Int = 20){
        if( newsDatas == null ) newsDatas = arrayListOf()
        RxObservableConverter.forNetwork( api.getNews(ApiConst.API_SMODE_NEWS, page+1, perPage) ).subscribe(
            {result->
                val addDatas = result.list.map { NewsData(UUID.randomUUID().toString()).setData(it) }
                newsDatas?.addAll(addDatas)
                newsDatasObservable.onNext(newsDatas!!)
            },{
                CustomToast.makeToast(ctx, R.string.error_data_loading).show()
            }
        ).apply { disposables.add(this) }
    }

    fun clearVirusConfirmed(){
        virusConfirmedDatas = null
        virusConfirmedData = null
    }
    fun getVirusConfirmed(){
        if(virusConfirmedData != null){
            virusConfirmedDataObservable.onNext(virusConfirmedData!!)
            return
        }
        RxObservableConverter.forNetwork( api.getAllDatas(ApiConst.API_SMODE_COUNTRY_TOTAL) ).subscribe(
            {result->

                virusConfirmedDatas = result.map { VirusConfirmedData(UUID.randomUUID().toString()).setData(it) }
                val sumData = VirusConfirmedData(UUID.randomUUID().toString())
                virusConfirmedDatas?.forEach { sumData.sum(it) }
                virusConfirmedData = sumData


            },{
                CustomToast.makeToast(ctx, R.string.error_data_loading).show()
            }
        ).apply { disposables.add(this) }
    }


    fun getVirusConfirmedCountry(){
        RxObservableConverter.forNetwork( api.getDatas(ApiConst.API_SMODE_COUNTRY , selectedMarker?.title, null) ).subscribe(
            {result->
                val datas = result.map { VirusConfirmedData(UUID.randomUUID().toString()).setData(it) }
                val sumData = VirusConfirmedData(UUID.randomUUID().toString())
                datas.forEach { sumData.sum(it) }
                virusConfirmedCountryDataObservable.onNext(sumData)

            },{
                CustomToast.makeToast(ctx, R.string.error_data_loading).show()
            }
        ).apply { disposables.add(this) }
    }


}