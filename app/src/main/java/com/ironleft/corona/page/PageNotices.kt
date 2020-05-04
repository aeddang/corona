package com.ironleft.corona.page

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.ironleft.corona.PageID
import com.ironleft.corona.R
import com.ironleft.corona.model.NewsData
import com.ironleft.corona.page.viewmodel.ViewModelNotices
import com.jakewharton.rxbinding3.view.clicks
import com.lib.page.PagePresenter
import com.lib.view.adapter.SingleAdapter
import com.skeleton.module.ViewModelFactory
import com.skeleton.rx.RxPageFragment
import com.skeleton.view.item.ListItem
import com.skeleton.view.item.VerticalLinearLayoutManager
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.item_notice.view.*
import kotlinx.android.synthetic.main.page_notices.*
import javax.inject.Inject




class PageNotices  : RxPageFragment() {

    private val appTag = javaClass.simpleName
    override fun getLayoutResId() = R.layout.page_notices

    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    private lateinit var viewModel: ViewModelNotices
    private val pageSize = 100
    private val adapter = ListAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidSupportInjection.inject(this)
        viewModel = ViewModelProvider(this, viewModelFactory).get(ViewModelNotices::class.java)
    }

    override fun onCreatedView() {
        super.onCreatedView()
        context?.let {
            recyclerView.adapter = adapter
            recyclerView.layoutManager = VerticalLinearLayoutManager(it)
        }
    }

    override fun onTransactionCompleted() {
        super.onTransactionCompleted()
        viewModel.repo.clearNotices()
        viewModel.repo.getNotices(0, pageSize)
    }

    
    override fun onDestroyView() {
        viewModel.repo.clearNotices()
        super.onDestroyView()
    }

    override fun onSubscribe() {
        super.onSubscribe()
        btnBack.clicks().subscribe {
            PagePresenter.getInstance<PageID>().goBack()
        }.apply { disposables?.add(this) }
        viewModel.repo.noticesDatasObservable.subscribe { datas ->
            loadingBar.visibility = View.GONE
            if(datas.isNotEmpty()) datas.first().isOpen = true
            adapter.setDataArray(datas.toTypedArray())
        }.apply { disposables.add(this) }
    }


    inner class ListAdapter : SingleAdapter<NewsData>(false, pageSize){
        override fun getListCell(parent: ViewGroup): ListItem {
            return Item(context!!)
        }

    }

    inner class Item(context: Context) : ListItem(context) {
        override fun getLayoutResId(): Int  = R.layout.item_notice

        private var currentData:NewsData? = null
            set(value) {
                field = value
                value?.let { data->
                    textTitle.text = data.desc
                    textDesc.text = data.pageContent
                    textDate.text = data.date
                    btnLink.visibility = if (data.pageUrl == "") View.GONE else View.VISIBLE
                    btnOpen.visibility = if (data.pageContent == "") View.GONE else View.VISIBLE
                    if ( data.isOpen ) openContentBox() else closeContentBox()
                }
            }


        override fun onAttachedToWindow() {
            super.onAttachedToWindow()
            currentData?.let {data->
                clicks().subscribe {
                    if ( data.isOpen ) closeContentBox() else openContentBox()
                }.apply { disposables?.add(this) }

                btnLink.clicks().subscribe {
                    val uris = Uri.parse(data.pageUrl)
                    val intents = Intent(Intent.ACTION_VIEW, uris)
                    val b = Bundle()
                    b.putBoolean("new_window", true)
                    intents.putExtras(b)
                    context?.startActivity(intents)
                }.apply { disposables?.add(this) }
            }
        }

        override fun setData(data: Any?, idx:Int){
            super.setData(data, idx)
            currentData = data as NewsData
        }

        private fun openContentBox(){
            currentData?.isOpen = true
            contentBox.visibility = View.VISIBLE
            btnOpen.setImageResource(R.drawable.ic_up)
            textTitle.setTextColor(context.resources.getColor(R.color.color_gray))
        }

        private fun closeContentBox(){
            currentData?.isOpen = false
            contentBox.visibility = View.GONE
            btnOpen.setImageResource(R.drawable.ic_down)
            textTitle.setTextColor(context.resources.getColor(R.color.color_white))
        }



    }



}