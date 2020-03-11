package com.ironleft.corona.page

import android.Manifest
import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.Marker
import com.ironleft.corona.PageID
import com.ironleft.corona.R
import com.ironleft.corona.component.CountryBox
import com.ironleft.corona.component.GraphBox
import com.ironleft.corona.model.CountryData
import com.ironleft.corona.page.viewmodel.ViewModelData
import com.jakewharton.rxbinding3.view.clicks
import com.lib.page.PagePresenter
import com.lib.page.PageRequestPermission
import com.lib.util.decimalFormat
import com.lib.util.toPercent
import com.skeleton.module.ViewModelFactory
import com.skeleton.rx.RxPageFragment
import com.skeleton.view.alert.CustomToast
import com.skeleton.view.graph.GraphBuilder
import dagger.android.support.AndroidSupportInjection
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.page_graph.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject



class PageGraph  : RxPageFragment(){

    private val appTag = javaClass.simpleName
    override fun getLayoutResId() = R.layout.page_graph

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
        graphConfirmedBox.type = GraphBox.BoxType.Confirmed
        graphDeathsBox.type = GraphBox.BoxType.Deaths
        graphRecoveredBox.type = GraphBox.BoxType.Recovered
    }
    override fun onTransactionCompleted() {
        super.onTransactionCompleted()
        viewModel.repo.getVirusConfirmed()
        textTitle.text = viewModel.repo.selectedCountry?.title ?: (context?.getString(R.string.cp_tab_title))
     }


    override fun onSubscribe() {
        super.onSubscribe()
        btnBack.clicks().subscribe {
            PagePresenter.getInstance<PageID>().goBack()
        }.apply { disposables.add(this) }

        viewModel.repo.selectedCountryObservable.subscribe {
            textTitle.text = it.title
        }.apply { disposables.add(this) }

        viewModel.repo.virusConfirmedDataObservable.subscribe {
            viewModel.repo.getGraphs()
            countryBox.datas = viewModel.repo.virusConfirmedDatas
            updateCountryBox()

        }.apply { disposables.add(this) }

        viewModel.repo.graphDatasObservable.subscribe { graphs->
            if(graphs.isEmpty()) {
                graphConfirmedBox.reflashEmptyGraph()
                graphDeathsBox.reflashEmptyGraph()
                graphRecoveredBox.reflashEmptyGraph()
                context?.let { CustomToast.makeToast(it, R.string.notice_empty_data).show() }
                return@subscribe
            }
            val data = graphs.last()
            textConfirmed.text = getTitleString(R.string.cp_data_box_title, data.confirmed)
            textDeaths.text = getTitleString(R.string.cp_data_box_deaths, data.deaths, data.deathsRatio)
            textRecovered.text = getTitleString(R.string.cp_data_box_recovered, data.recovered, data.recoveredRatio)
            graphConfirmedBox.graphDatas = graphs
            graphConfirmedBox.graphIncreasedMax = viewModel.repo.graphIncresedConfirmedMax
            graphDeathsBox.graphDatas = graphs
            graphDeathsBox.graphIncreasedMax = viewModel.repo.graphIncresedDeathsMax
            graphRecoveredBox.graphDatas = graphs
            graphRecoveredBox.graphIncreasedMax = viewModel.repo.graphIncresedRecoveredMax


        }.apply { disposables.add(this) }

        countryBox.selectLocationObservable.subscribe {
            loadCountry(it)
        }.apply { disposables.add(this) }
    }

    private fun updateCountryBox(){
        Observable.interval(1000, TimeUnit.MILLISECONDS)
            .take(1)
            .observeOn(AndroidSchedulers.mainThread()).subscribe {
                //scrollView.smoothScrollTo(0, scrollView.bottom)
                countryBox.type = CountryBox.SortType.Confirmed
            }.apply { disposables.add(this) }
    }

    private fun getTitleString(@StringRes titleId:Int, num:Long, pct:Double = -1.0):SpannableString{
        context ?: ""
        val title = context!!.getString(titleId)
        val text =
            if(pct >= 0.0)  SpannableString("$title ${num.toString().decimalFormat()} ( ${pct.toPercent()}% )")
            else SpannableString("$title ${num.toString().decimalFormat()}")
        text.setSpan(ForegroundColorSpan(context!!.resources.getColor(R.color.color_gray)), 0, title.length, 0)
        return text
    }

    private fun loadCountry(countryData:CountryData){
        graphConfirmedBox.reflashGraph()
        graphDeathsBox.reflashGraph()
        graphRecoveredBox.reflashGraph()
        viewModel.repo.selectedCountry = countryData
        viewModel.repo.clearGraphs()
        viewModel.repo.getGraphs()
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }


}

