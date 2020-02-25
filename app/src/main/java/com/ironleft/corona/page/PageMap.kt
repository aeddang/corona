package com.ironleft.corona.page

import android.Manifest
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.maps.SupportMapFragment
import com.ironleft.corona.PageID
import com.ironleft.corona.R
import com.ironleft.corona.page.viewmodel.ViewModelMap
import com.jakewharton.rxbinding3.view.clicks
import com.lib.page.PagePresenter
import com.lib.page.PageRequestPermission
import com.skeleton.module.ViewModelFactory
import com.skeleton.rx.RxPageFragment
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.page_map.*
import javax.inject.Inject



class PageMap  : RxPageFragment(){

    private val appTag = javaClass.simpleName
    override fun getLayoutResId() = R.layout.page_map

    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    private lateinit var viewModel: ViewModelMap


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidSupportInjection.inject(this)
        viewModel = ViewModelProvider(this, viewModelFactory).get(ViewModelMap::class.java)
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

        viewModel.repo.getVirusConfirmed()
        viewModel.repo.getGraphs()
    }



    override fun onSubscribe() {
        super.onSubscribe()
        btnBack.clicks().subscribe {
            PagePresenter.getInstance<PageID>().goBack()
        }.apply { disposables.add(this) }
        viewModel.repo.virusConfirmedDataObservable.subscribe {
            mapBox.virusConfirmedDatas = viewModel.repo.virusConfirmedDatas
        }.apply { disposables.add(this) }
    }



    override fun onDestroyView() {
        super.onDestroyView()
    }


}

