package com.ironleft.corona.component

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.ironleft.corona.PageID
import com.ironleft.corona.R
import com.ironleft.corona.model.VirusConfirmedData
import com.jakewharton.rxbinding3.view.clicks
import com.lib.page.PagePresenter
import com.lib.util.decimalFormat
import com.lib.util.toPercent
import com.skeleton.rx.RxFrameLayout
import kotlinx.android.synthetic.main.cp_data_box.view.*



class DataBox : RxFrameLayout {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context,attrs)
    override fun getLayoutResId(): Int { return R.layout.cp_data_box }
    private val appTag = javaClass.simpleName


    var data:VirusConfirmedData? = null
        set(value) {
            field = value
            value ?: return
            loadingBar.visibility = View.GONE
            val pct = value.increase.toFloat()/(value.confirmed-value.increase).toFloat()
            textComfirmed.text = "${value.confirmed}".decimalFormat()
            textIncrease.text = "+${value.increase}".decimalFormat()
            textIncreasePct.text = "+${pct.toPercent()}%"
            textDeaths.text = "${value.deaths}".decimalFormat()
            textRecovered.text = "${value.recovered}".decimalFormat()
        }


    override fun onCreatedView() {
        loadingBar.visibility = View.VISIBLE
    }

    override fun onDestroyedView() {

    }

    override fun onSubscribe() {
        super.onSubscribe()
        btnDetail.clicks().subscribe {
            PagePresenter.getInstance<PageID>().pageChange(PageID.GRAPH)
        }.apply { disposables?.add(this) }
    }

}