package com.ironleft.corona.component

import android.content.Context
import android.graphics.Color
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.StringRes

import com.ironleft.corona.R
import com.ironleft.corona.model.CountryData
import com.ironleft.corona.model.VirusConfirmedData
import com.jakewharton.rxbinding3.view.clicks
import com.lib.util.decimalFormat
import com.lib.util.toPercent
import com.lib.view.adapter.SingleAdapter
import com.skeleton.rx.RxFrameLayout
import com.skeleton.view.graph.GraphBuilder
import com.skeleton.view.graph.draw
import com.skeleton.view.item.ListItem
import com.skeleton.view.item.VerticalLinearLayoutManager
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.cp_country_box.view.*
import kotlinx.android.synthetic.main.item_country.view.*
import kotlinx.android.synthetic.main.item_country_text.view.*
import kotlin.math.round


class CountryBox : RxFrameLayout {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context,attrs)
    override fun getLayoutResId(): Int { return R.layout.cp_country_box }
    private val appTag = javaClass.simpleName

    enum class SortType{
        None,Confirmed, Deaths, Recovered
    }
    val selectLocationObservable = PublishSubject.create<CountryData>()
    var datas:List<VirusConfirmedData>? = null
    private val adapter = ListAdapter()

    private val colors = arrayOf("#fa0505","#fa7705", "#fad105", "#eefa05", "#acfa05", "#1efa05", "#05fae6", "#0584fa", "#050dfa", "#7305fa", "#666666")
    private var sum:Long = 0
    var type:SortType = SortType.None
    set(value) {
        field = value
        datas ?: return
        val sortDatas = when(field){
            SortType.Deaths -> {
                sum = datas!!.map { it.deaths }.sum()
                datas!!.sortedByDescending { it.deaths }
            }
            SortType.Recovered -> {
                sum = datas!!.map { it.recovered }.sum()
                datas!!.sortedByDescending { it.recovered }
            }
            else ->{
                sum = datas!!.map { it.confirmed }.sum()
                datas!!.sortedByDescending { it.confirmed }
            }
        }
        val ranks = sortDatas.subList(0, colors.size-1).map {
            when(field) {
                SortType.Deaths -> it.deaths.toDouble()
                SortType.Recovered -> it.recovered.toDouble()
                else -> it.confirmed.toDouble()
            }
        }
        val newRanks = ranks.toMutableList()
        newRanks.add(sum - ranks.sum())
        builder.setRange(sum.toDouble()).setRange(sum.toDouble()).show( newRanks )
        adapter.setDataArray(sortDatas.toTypedArray())
    }

    private lateinit var builder:GraphBuilder
    override fun onCreatedView() {
        builder = GraphBuilder(graphCircle).setColor(colors)
        context?.let {
            recyclerView.adapter = adapter
            recyclerView.layoutManager = VerticalLinearLayoutManager(it)
        }
    }

    override fun onSubscribe() {
        super.onSubscribe()
        graphCircle.draw().subscribe {pos->
            val last = pos.size - 1
            pos.forEachIndexed {index , p->
                if( p.first < 15.0) return@forEachIndexed
                val text = ItemText(context)
                val isLast = index == last
                val pct = (p.first/360.0).toPercent()
                val title = if(isLast) "etc" else adapter.getData(index).map?.title ?: ""
                text.setText( "$title $pct%" )
                graphArea.addView(text)
                val layout = text.layoutParams as MarginLayoutParams
                layout.leftMargin = p.second.x - (title.length * 10).toInt()
                layout.topMargin = p.second.y - 16
                text.layoutParams = layout
            }
        }.apply { disposables?.add(this) }
    }

    override fun onDestroyedView() {
        datas = null
    }

    inner class ItemText(context: Context) : ListItem(context) {
        override fun getLayoutResId(): Int = R.layout.item_country_text
        fun setText(title:String){
            textGraphTitle.text = title
        }
    }


    inner class ListAdapter : SingleAdapter<VirusConfirmedData>(false){
        override fun getListCell(parent: ViewGroup): ListItem {
            return Item(context!!)
        }

    }

    inner class Item(context: Context) : ListItem(context) {
        override fun getLayoutResId(): Int = R.layout.item_country

        private var currentData: VirusConfirmedData? = null
            set(value) {
                field = value
                value?.let { data ->
                    textConfirmed.text = getTitleString(R.string.cp_data_box_title, data.confirmed)
                    textDeaths.text = getTitleString(R.string.cp_data_box_deaths, data.deaths)
                    textRecovered.text = getTitleString(R.string.cp_data_box_recovered, data.recovered)

                    val amount = when(type){
                        SortType.Recovered -> data.recovered
                        SortType.Deaths -> data.deaths
                        else -> data.confirmed
                    }
                    val pct = (amount.toDouble()/sum.toDouble()).toPercent()
                    textTitle.text = "${data.map?.title}"
                    textPct.text = "$pct%"
                }
            }

        private fun getTitleString(@StringRes titleId:Int, num:Long): SpannableString {
            context ?: ""
            val title = context!!.getString(titleId)
            val text =  SpannableString("$title ${num.toString().decimalFormat()}")
            text.setSpan(ForegroundColorSpan(context.resources.getColor(R.color.color_gray)), 0, title.length, 0)
            return text
        }


        override fun onAttachedToWindow() {
            super.onAttachedToWindow()
            currentData?.let { data ->
                clicks().subscribe {
                    data.map ?: return@subscribe
                    selectLocationObservable.onNext(
                        CountryData(
                            data.map!!.title,
                            data.map!!.latLng
                        )
                    )
                }.apply { disposables?.add(this) }
            }
        }

        override fun setData(data: Any?, idx: Int) {
            super.setData(data, idx)
            currentData = data as VirusConfirmedData?
            val c =
                if (idx < colors.size-1) Color.parseColor(colors[idx])
                else context.resources.getColor(R.color.color_white)
            textTitle.setTextColor(c)
            textPct.setTextColor(c)
        }
    }
}