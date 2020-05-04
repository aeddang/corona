package com.ironleft.corona.store
import android.content.Context
import com.google.android.gms.maps.model.Marker
import com.ironleft.corona.R
import com.ironleft.corona.api.ApiConst
import com.ironleft.corona.api.ApiRequest
import com.ironleft.corona.model.*


import com.skeleton.module.network.NetworkFactory
import com.skeleton.module.network.RxObservableConverter
import com.skeleton.view.alert.CustomToast
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import java.util.*
import kotlin.math.max


class Repository(
    val ctx: Context,
    val setting:SettingPreference,
    networkFactory: NetworkFactory
) {

    private val appTag = "Repository"
    private val disposables = CompositeDisposable()
    private val api = networkFactory.getRetrofit(ApiConst.API_PATH).create(ApiRequest::class.java)

    var selectedCountry : CountryData? = null
        set(value) {
            if(field == value) return
            field = value
            setting.putSelectedCountry(field?.title ?: "")
            selectedCountryObservable.onNext(field ?: CountryData( ctx.getString(R.string.cp_tab_title)) )
            clearGraphs()
        }
    val selectedCountryObservable = PublishSubject.create<CountryData>()

    var virusConfirmedDatas: List<VirusConfirmedData>? = null ; private set
    var virusConfirmedData: VirusConfirmedData? = null ; private set
    var graphIncresedMax = 0L ; private set
    var graphIncresedConfirmedMax = 0L ; private set
    var graphIncresedDeathsMax = 0L ; private set
    var graphIncresedRecoveredMax = 0L ; private set
    var graphDatas :List<GraphData>? = null ; private set
    var newsDatas:ArrayList<NewsData>? = null ; private set
    var noticesDatas:ArrayList<NewsData>? = null ; private set

    val virusConfirmedDataObservable = PublishSubject.create<VirusConfirmedData>()
    val virusConfirmedCountryDataObservable = PublishSubject.create<VirusConfirmedData>()
    val graphDatasObservable = PublishSubject.create<List<GraphData>>()
    val newsDatasObservable = PublishSubject.create<ArrayList<NewsData>>()
    val noticesDatasObservable = PublishSubject.create<ArrayList<NewsData>>()
    fun clearGraphs(){
        graphDatas = null
        graphIncresedMax = 0L
        graphIncresedConfirmedMax = 0L
        graphIncresedDeathsMax = 0L
        graphIncresedRecoveredMax = 0L
    }
    fun getGraphs(){
        if(graphDatas != null){
            graphDatasObservable.onNext(graphDatas!!)
            return
        }
        val smode = if(selectedCountry == null) ApiConst.API_SMODE_GRAPH_TOTAL else ApiConst.API_SMODE_GRAPH
        RxObservableConverter.forNetwork( api.getGraphDatas(smode, selectedCountry?.title , null) ).subscribe(
            {result->
                var datas = result.map { GraphData(UUID.randomUUID().toString()).setData(it) }
                if(selectedCountry != null) datas = datas.reversed()
                var prevData:GraphData? = null
                datas.forEach {
                    graphIncresedMax = max(graphIncresedMax, it.setDiffData(prevData))
                    graphIncresedConfirmedMax = max(graphIncresedConfirmedMax, it.diffConfirmed)
                    graphIncresedDeathsMax = max(graphIncresedDeathsMax, it.diffDeaths)
                    graphIncresedRecoveredMax = max(graphIncresedRecoveredMax, it.diffRecovered)
                    prevData = it
                }
                graphDatas = datas
                graphDatasObservable.onNext(graphDatas!!)
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

    fun clearNotices(){ noticesDatas = null }
    fun getNotices(page:Int = 0, perPage:Int = 100){
        if( noticesDatas == null ) noticesDatas = arrayListOf()
        RxObservableConverter.forNetwork( api.getNews(ApiConst.API_SMODE_NOTICES, page+1, perPage) ).subscribe(
            {result->
                val addDatas = result.list.map { NewsData(UUID.randomUUID().toString()).setData(it) }
                noticesDatas?.addAll(addDatas)
                noticesDatasObservable.onNext(noticesDatas!!)
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
                val selectedTitle = setting.getSelectedCountry() ?: ""
                virusConfirmedDatas = result.map { VirusConfirmedData(UUID.randomUUID().toString()).setData(it) }
                val sumData = VirusConfirmedData(UUID.randomUUID().toString())
                virusConfirmedDatas?.forEach {
                    it.map?.let { m-> if(selectedTitle == m.title) selectedCountry = CountryData(selectedTitle, m.latLng )}
                    sumData.sum(it)
                }
                virusConfirmedData = sumData
                virusConfirmedDataObservable.onNext(virusConfirmedData!!)

            },{
                CustomToast.makeToast(ctx, R.string.error_data_loading).show()
            }
        ).apply { disposables.add(this) }
    }


    fun getVirusConfirmedCountry(){
        if(virusConfirmedDatas != null){
            val find = virusConfirmedDatas!!.find { it.map?.title == selectedCountry?.title }
            if( find != null ) {
                virusConfirmedCountryDataObservable.onNext(find)
                return
            }
        }

        RxObservableConverter.forNetwork( api.getDatas(ApiConst.API_SMODE_COUNTRY , selectedCountry?.title, null) ).subscribe(
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