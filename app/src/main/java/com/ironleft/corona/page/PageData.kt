package com.ironleft.corona.page

import android.Manifest
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.Marker
import com.ironleft.corona.PageID
import com.ironleft.corona.R
import com.ironleft.corona.model.CountryData
import com.ironleft.corona.page.viewmodel.ViewModelData
import com.jakewharton.rxbinding3.view.clicks
import com.lib.page.PagePresenter
import com.lib.page.PageRequestPermission
import com.skeleton.module.ViewModelFactory
import com.skeleton.rx.RxPageFragment
import com.skeleton.view.alert.CustomToast
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.page_data.*
import javax.inject.Inject



class PageData  : RxPageFragment(){

    private val appTag = javaClass.simpleName
    override fun getLayoutResId() = R.layout.page_data

    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    private lateinit var viewModel: ViewModelData


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidSupportInjection.inject(this)
        viewModel = ViewModelProvider(this, viewModelFactory).get(ViewModelData::class.java)
    }

    override fun onCreatedView() {
        super.onCreatedView()


    }
    override fun onTransactionCompleted() {
        super.onTransactionCompleted()
        PagePresenter.getInstance<PageID>().requestPermission(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
            object : PageRequestPermission {
                override fun onRequestPermissionResult(
                    resultAll: Boolean,
                    permissions: List<Boolean>?
                ) {
                    if(!resultAll){
                        context?.let { CustomToast.makeToast(it, R.string.notice_need_permission).show() }
                        loadAllCountry()
                        return
                    }
                    val map = childFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment?
                    map?.let { it.getMapAsync { gm-> mapBox.googleMap = gm } }
                    loadAllCountry()
                }
            })

    }


    override fun onSubscribe() {
        super.onSubscribe()
        btnMapDetail.clicks().subscribe {
            PagePresenter.getInstance<PageID>().pageChange(PageID.MAP)
        }.apply { disposables.add(this) }
        viewModel.repo.virusConfirmedDataObservable.subscribe {
            if(viewModel.repo.selectedCountry == null)  {
                dataBox.data = it
                viewModel.repo.getGraphs()
            }
            else loadCountry( viewModel.repo.selectedCountry!! )
            mapBox.virusConfirmedDatas = viewModel.repo.virusConfirmedDatas
            mapBox.initLocation = viewModel.repo.selectedCountry?.latLng
        }.apply { disposables.add(this) }

        viewModel.repo.virusConfirmedCountryDataObservable.subscribe {
            dataBox.data = it
        }.apply { disposables.add(this) }

        viewModel.repo.graphDatasObservable.subscribe {graphs->
            if(graphs.isEmpty()) {
                graphBox.reflashEmptyGraph()
                context?.let { CustomToast.makeToast(it, R.string.notice_empty_data).show() }
                return@subscribe
            }
            graphBox.graphIncreasedMax = viewModel.repo.graphIncresedMax
            graphBox.graphDatas = graphs
        }.apply { disposables.add(this) }

        mapBox.selectLocationObservable.subscribe {
            loadCountry(CountryData(it.title, it.position))
        }.apply { disposables.add(this) }

        mapBox.selectedLocationObservable.subscribe {
            PagePresenter.getInstance<PageID>().pageChange(PageID.GRAPH)
        }.apply { disposables.add(this) }

    }

    override fun onPageReload() {
        super.onPageReload()
        viewModel.repo.selectedCountry = null
        loadAllCountry()
    }

    private fun loadAllCountry(){
        viewModel.repo.clearVirusConfirmed()
        viewModel.repo.clearGraphs()
        viewModel.repo.getVirusConfirmed()

    }

    private fun loadCountry(countryData:CountryData){
        viewModel.repo.selectedCountry = countryData
        graphBox.reflashGraph()
        viewModel.repo.clearGraphs()
        viewModel.repo.getVirusConfirmedCountry()
        viewModel.repo.getGraphs()
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }


}

