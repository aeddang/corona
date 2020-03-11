package com.ironleft.corona.component

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorRes

import com.ironleft.corona.R
import com.ironleft.corona.model.GraphData
import com.jakewharton.rxbinding3.view.scrollChangeEvents
import com.lib.util.Log
import com.lib.util.animateAlpha
import com.lib.util.decimalFormat
import com.skeleton.rx.RxFrameLayout
import com.skeleton.view.alert.CustomToast
import com.skeleton.view.graph.Graph
import com.skeleton.view.graph.GraphBar
import com.skeleton.view.graph.GraphBuilder
import com.skeleton.view.item.ListItem
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.cp_graph_box.view.*
import kotlinx.android.synthetic.main.item_graph_bar.view.*
import java.util.concurrent.TimeUnit
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.min
import kotlin.math.round




class GraphBox : RxFrameLayout {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context,attrs)
    override fun getLayoutResId(): Int { return R.layout.cp_graph_box }
    private val appTag = javaClass.simpleName

    enum class BoxType{
        Confirmed, Deaths, Recovered, All
    }
    enum class GraphType{
        Confirmed, Deaths, Recovered
    }
    var graphIncreasedMax:Long = 0
    var graphDatas:List<GraphData> = listOf()
        set(value) {
            field = value
            if(field.isEmpty()) {
                return
            }
            initGraph(value)
            if(type == BoxType.All || type == BoxType.Confirmed ) updateGraph(GraphType.Confirmed, value, type != BoxType.All)
            if(type == BoxType.All || type == BoxType.Deaths ) updateGraph(GraphType.Deaths, value, type != BoxType.All)
            if(type == BoxType.All || type == BoxType.Recovered ) updateGraph(GraphType.Recovered, value, true)
        }


    override fun onSubscribe() {
        super.onSubscribe()
        graphsScroll.scrollChangeEvents().subscribe {
            val scrollIdx = round( it.scrollX / scrollRange)
            currnentScrollIdx = scrollIdx.toInt()
            modifyScrollPos()
        }.apply { disposables?.add(this) }


        currnentScrollIdxObservable.observeOn(AndroidSchedulers.mainThread()).subscribe {
            updateDate()
        }.apply { disposables?.add(this) }
    }
    var type = BoxType.All
    private var lines = ArrayList<Item>()
    private var graphs = ArrayList<ItemGraph>()
    var lineNumMax = 21
    private var lineNum = 30
    private var graphMagin = 6.0f
    private var maxValue:Long = 0
    private var scrollRange:Float = 0.0f
    private var currnentScrollIdx:Int = 0
        set(value) {
            if(field == value) return
            field = value
            currnentScrollIdxObservable.onNext(value)
            currentGraphData?.let {
                textConfirmed.text = getGraphAmount(it)
            }
            startGraphData?.let {
                textStartConfirmed.text = getGraphAmount(it)
            }

            if(graphDatas.isEmpty()) return

            graphs.forEachIndexed{index, graph->
                val n = currnentScrollIdx + index
                if(n >= graphDatas.size) return@forEachIndexed
                val data = graphDatas[n]
                val v = when(type){
                    BoxType.Deaths -> data.diffDeaths
                    BoxType.Recovered -> data.diffRecovered
                    else -> data.diffConfirmed
                }
                val pct = v / graphIncreasedMax.toDouble()
                graph.setData(pct)
            }

        }
    private fun getGraphAmount(data:GraphData):String{
        val v = when(type){
            BoxType.Deaths -> data.deaths
            BoxType.Recovered -> data.recovered
            else -> data.confirmed
        }
        val d = when(type){
            BoxType.Deaths -> data.diffDeaths
            BoxType.Recovered -> data.diffRecovered
            else -> data.diffConfirmed
        }
        return "${v.toString().decimalFormat()} ( +$d )"
    }


    val currnentScrollIdxObservable = PublishSubject.create<Int>()
    val currentGraphData:GraphData?
        get(){
            if(graphDatas.isEmpty()) return null
            val n = min( currnentScrollIdx + lineNum , graphDatas.size-1)
            return graphDatas[n]
        }
    val startGraphData:GraphData?
        get(){
            if(graphDatas.isEmpty()) return null
            val n = currnentScrollIdx
            return graphDatas[n]
        }

    private var modifyScrollPosDisposable:Disposable? = null
    private fun modifyScrollPos(isSmooth:Boolean = true){
        modifyScrollPosDisposable?.dispose()
        modifyScrollPosDisposable = Observable.interval(300, TimeUnit.MILLISECONDS)
            .take(1)
            .observeOn(AndroidSchedulers.mainThread()).subscribe {
                val tx = currnentScrollIdx * scrollRange
                if(isSmooth) graphsScroll.smoothScrollTo(tx.toInt(), 0)
                else graphsScroll.scrollTo(tx.toInt(), 0)
            }

    }

    fun reflashGraph(){
        loadingBar.visibility = View.VISIBLE
    }
    fun reflashEmptyGraph(){
        loadingBar.visibility = View.GONE
        if(type == BoxType.All || type == BoxType.Confirmed ) graphConfirmed.resetValues()
        if(type == BoxType.All || type == BoxType.Deaths ) graphDeaths.resetValues()
        if(type == BoxType.All || type == BoxType.Recovered ) graphRecovered.resetValues()
        graphs.forEach { it.setData(0.0, 0) }
    }


    private fun initGraph(datas:List<GraphData>){
        val dataNum = datas.size-1
        lineNum = min(dataNum-1,lineNumMax)

        updateDate()
        val margin = graphMagin.toInt()
        graphConfirmed.graphMargin = margin
        graphDeaths.graphMargin = margin
        graphRecovered.graphMargin = margin



        val lineWidth = margin * 2
        val lineRange = round((graphsScroll.width - (lineWidth)).toFloat() / lineNum.toFloat() )
        val layoutWid = round(lineRange).toInt() * dataNum + lineWidth
        val max = when(type){
            BoxType.Deaths -> datas.last().deaths
            BoxType.Recovered -> datas.last().recovered
            else -> datas.last().confirmed
        }
        maxValue = ceil(max* 1.2 ).toLong()
        if(type == BoxType.All || type == BoxType.Confirmed ) setupGrapeArea(graphConfirmed, layoutWid)
        if(type == BoxType.All || type == BoxType.Deaths ) setupGrapeArea(graphDeaths, layoutWid)
        if(type == BoxType.All || type == BoxType.Recovered ) setupGrapeArea(graphRecovered, layoutWid)

        scrollRange = lineRange
        clearGraph()

        for ( i in 0..lineNum ) {
            creatGrapeLine(lineWidth, lineRange)
            creatGrapeBar(lineRange.toInt())
        }
        loadingBar.visibility = View.GONE
    }
    private fun creatGrapeLine(width:Int, lineRange:Float){
        val line = Item(context)
        graphLines.addView(line)
        val layout = line.layoutParams as ViewGroup.MarginLayoutParams
        layout.width = width
        layout.rightMargin = lineRange.toInt() - width
        lines.add(line)
        line.layoutParams = layout

    }

    private fun clearGraph(){
        graphs.forEach { graphBars.removeView(it) }
        graphs.clear()
        lines.forEach { graphLines.removeView(it) }
        lines.clear()
    }

    private fun creatGrapeBar(width:Int){
        val graph = ItemGraph(context)
        graphBars.addView(graph)
        val layout = graph.layoutParams as ViewGroup.MarginLayoutParams
        layout.width = width
        graphs.add(graph)
        graph.layoutParams = layout

    }

    private fun updateDate(){
        if(currnentScrollIdx < 0) return
        val max = graphDatas.size -1
        if(currnentScrollIdx > max) return
        val startData = graphDatas[currnentScrollIdx]
        val endIdx = min(currnentScrollIdx + lineNum, max)
        val endData = graphDatas[endIdx ]
        textStartDate.text = startData.viewDate
        textEndDate.text = endData.viewDate
    }

    private fun setupGrapeArea(graph:Graph, graphBodyWidth:Int){
        val layout = graph.layoutParams
        layout.width = graphBodyWidth
        graph.layoutParams = layout
    }



    private fun updateGraph(graph:GraphType, datas:List<GraphData>, isLast:Boolean = false){
        val stroke = 2.0f
        if( isLast ) {
            currnentScrollIdx = datas.size - lineNum
            modifyScrollPos(false)
        }
        val graphLine = when(graph){
            GraphType.Confirmed -> graphConfirmed
            GraphType.Deaths -> graphDeaths
            GraphType.Recovered -> graphRecovered
        }
        val amounts = when(graph){
            GraphType.Confirmed -> datas.map { it.confirmed.toDouble() }
            GraphType.Deaths -> datas.map { it.deaths.toDouble() }
            GraphType.Recovered -> datas.map { it.recovered.toDouble() }
        }
        val graphColor = when(graph){
            GraphType.Confirmed -> R.color.color_white
            GraphType.Deaths -> R.color.color_red
            GraphType.Recovered -> R.color.color_green
        }
        GraphBuilder(graphLine).setColor(graphColor).setRange(maxValue.toDouble()).setStroke(stroke)
            .show(amounts, 500)

    }


    override fun onCreatedView() {
        loadingBar.visibility = View.VISIBLE
        graphMagin *= context.resources.displayMetrics.density
        graphMagin = round(graphMagin)
    }

    override fun onDestroyedView() {
        modifyScrollPosDisposable?.dispose()
        modifyScrollPosDisposable = null
    }

    inner class Item(context: Context) : ListItem(context) {
        override fun getLayoutResId(): Int {
            return R.layout.item_graph_line
        }

        override fun onCreatedView() {
            super.onCreatedView()
            alpha = 0.0f
            animateAlpha(1.0f)
        }
    }

    inner class ItemGraph(context: Context) : ListItem(context) {
        private var builder:GraphBuilder? = null
        override fun getLayoutResId(): Int {
            return R.layout.item_graph_bar
        }

        override fun onCreatedView() {
            super.onCreatedView()
            builder = GraphBuilder(graph).setColor(R.color.color_gray).setRange(1.0)
        }

        override fun setData(data: Any?, idx: Int) {
            super.setData(data, idx)
            val pct = data as Double? ?: 0.0
            builder?.show(pct, 500)

        }
    }

}

