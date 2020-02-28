package com.ironleft.corona.page

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.ironleft.corona.PageID
import com.ironleft.corona.PageParam
import com.ironleft.corona.R
import com.ironleft.corona.model.NewsData
import com.ironleft.corona.page.viewmodel.ViewModelNews
import com.jakewharton.rxbinding3.view.clicks
import com.lib.page.PagePresenter
import com.lib.view.adapter.SingleAdapter
import com.skeleton.module.ViewModelFactory
import com.skeleton.rx.RxPageFragment
import com.skeleton.view.item.ListItem
import com.skeleton.view.item.VerticalLinearLayoutManager
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.item_news.view.*
import kotlinx.android.synthetic.main.page_news.*
import javax.inject.Inject




class PageNews  : RxPageFragment() {

    private val appTag = javaClass.simpleName
    override fun getLayoutResId() = R.layout.page_news

    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    private lateinit var viewModel: ViewModelNews
    private val pageSize = 30
    private val adapter = ListAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidSupportInjection.inject(this)
        viewModel = ViewModelProvider(this, viewModelFactory).get(ViewModelNews::class.java)
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
        viewModel.repo.clearNews()
        viewModel.repo.getNews(0, pageSize)
    }

    
    override fun onDestroyView() {
        super.onDestroyView()
    }

    override fun onSubscribe() {
        super.onSubscribe()
        viewModel.repo.newsDatasObservable.subscribe { datas ->
            loadingBar.visibility = View.GONE

            adapter.setDataArray(datas.toTypedArray())
        }.apply { disposables.add(this) }
    }


    inner class ListAdapter : SingleAdapter<NewsData>(true, pageSize){
        override fun getListCell(parent: ViewGroup): ListItem {
            return Item(context!!)
        }

        override fun onViewEnd(page: Int, size: Int) {
            super.onViewEnd(page, size)
            loadingBar.visibility = View.VISIBLE
            viewModel.repo.getNews(page, size)
        }
    }

    inner class Item(context: Context) : ListItem(context) {
        override fun getLayoutResId(): Int  = R.layout.item_news

        private var currentData:NewsData? = null

            set(value) {
                field = value

                value?.let { data->
                    textDesc.text = data.desc
                    textDate.text = data.date
                }
            }


        override fun onAttachedToWindow() {
            super.onAttachedToWindow()
            currentData?.let {data->
                clicks().subscribe {
                    val param = HashMap<String, Any?>()
                    param[PageParam.PAGE_URL] = data.pageUrl
                    param[PageParam.PAGE_CONTENT] = data.pageContent
                    PagePresenter.getInstance<PageID>().openPopup(PageID.POPUP_WEBVIEW, param)

                }.apply { disposables?.add(this) }
            }
        }

        override fun setData(data: Any?, idx:Int){
            super.setData(data, idx)
            currentData = data as NewsData




        }





    }



}