package com.ironleft.corona.page

import android.Manifest
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.maps.SupportMapFragment
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
import kotlinx.android.synthetic.main.page_map.*
import javax.inject.Inject



class PageMap  : RxPageFragment(){

    private val appTag = javaClass.simpleName
    override fun getLayoutResId() = R.layout.page_map

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
                    if(!resultAll){
                        context?.let { CustomToast.makeToast(it, R.string.notice_need_permission).show() }
                        return
                    }
                    try {
                        val map = childFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment?
                        map?.let { it.getMapAsync { gm-> mapBox.googleMap = gm } }
                    } catch (e:Exception){
                        context?.let { CustomToast.makeToast(it, R.string.error_google_map).show() }
                    }
                }
            })

    }
    override fun onTransactionCompleted() {
        super.onTransactionCompleted()
        viewModel.repo.getVirusConfirmed()
    }



    override fun onSubscribe() {
        super.onSubscribe()
        btnBack.clicks().subscribe {
            PagePresenter.getInstance<PageID>().goBack()
        }.apply { disposables.add(this) }
        viewModel.repo.virusConfirmedDataObservable.subscribe {
            mapBox.virusConfirmedDatas = viewModel.repo.virusConfirmedDatas
            mapBox.initLocation = viewModel.repo.selectedCountry?.latLng
        }.apply { disposables.add(this) }

        mapBox.selectedLocationObservable.subscribe {
            viewModel.repo.selectedCountry = CountryData(it.title, it.position)
            PagePresenter.getInstance<PageID>().pageChange(PageID.GRAPH)
        }.apply { disposables.add(this) }
    }



    override fun onDestroyView() {
        super.onDestroyView()
    }


}

