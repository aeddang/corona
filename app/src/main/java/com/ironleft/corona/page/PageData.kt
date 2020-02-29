package com.ironleft.corona.page

import android.Manifest
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.Marker
import com.ironleft.corona.PageID
import com.ironleft.corona.R
import com.ironleft.corona.model.VirusConfirmedData
import com.ironleft.corona.page.viewmodel.ViewModelData
import com.jakewharton.rxbinding3.view.clicks
import com.lib.page.PagePresenter
import com.lib.page.PageRequestPermission
import com.skeleton.module.ViewModelFactory
import com.skeleton.rx.RxPageFragment
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.page_data.*
import java.lang.Exception
import java.util.*
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
        PagePresenter.getInstance<PageID>().requestPermission(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
            object : PageRequestPermission {
                override fun onRequestPermissionResult(
                    resultAll: Boolean,
                    permissions: List<Boolean>?
                ) {
                    val map = childFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment?
                    map?.let { it.getMapAsync { gm-> mapBox.googleMap = gm } }
                }
            })

    }
    override fun onTransactionCompleted() {
        super.onTransactionCompleted()
        loadAllCountry()
        if(viewModel.repo.selectedMarker != null) loadCountry( viewModel.repo.selectedMarker!! )
    }


    override fun onSubscribe() {
        super.onSubscribe()
        btnMapDetail.clicks().subscribe {
            PagePresenter.getInstance<PageID>().pageChange(PageID.MAP)
        }.apply { disposables.add(this) }
        viewModel.repo.virusConfirmedDataObservable.subscribe {
            if(viewModel.repo.selectedMarker == null)  dataBox.data = it
            mapBox.virusConfirmedDatas = viewModel.repo.virusConfirmedDatas
        }.apply { disposables.add(this) }

        viewModel.repo.virusConfirmedCountryDataObservable.subscribe {
            dataBox.data = it
        }.apply { disposables.add(this) }

        viewModel.repo.graphDatasObservable.subscribe {
            graphBox.graphDatas = it
        }.apply { disposables.add(this) }

        mapBox.selectLocationObservable.subscribe {
            loadCountry(it)
        }.apply { disposables.add(this) }

    }

    override fun onPageReload() {
        super.onPageReload()
        viewModel.repo.selectedMarker = null
        loadAllCountry()
    }

    private fun loadAllCountry(){
        viewModel.repo.clearVirusConfirmed()
        viewModel.repo.clearGraphs()
        viewModel.repo.getVirusConfirmed()
        viewModel.repo.getGraphs()
    }

    private fun loadCountry(marker:Marker){
        viewModel.repo.selectedMarker = marker
        viewModel.repo.clearGraphs()
        viewModel.repo.getVirusConfirmedCountry()
        viewModel.repo.getGraphs()
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }


}

