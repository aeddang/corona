package com.ironleft.corona.component

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import com.ironleft.corona.PageID
import com.ironleft.corona.R
import com.jakewharton.rxbinding3.view.clicks
import com.lib.page.PagePresenter
import com.lib.util.animateAlpha
import com.lib.util.animateY
import com.skeleton.rx.RxFrameLayout
import kotlinx.android.synthetic.main.cp_tab.view.*

class Tab : RxFrameLayout {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context,attrs)
    override fun getLayoutResId(): Int { return R.layout.cp_tab }
    private val appTag = javaClass.simpleName
    private val passiveAlpha = 0.4f

    var title:String = ""
        set(value) {
            field = value
            textTitle.text = value
        }

    var currentPage:PageID? = null
        set(value) {
            field = value
            when(value){
                PageID.DATA ->{
                    btnData.alpha = 1.0f
                    btnNews.alpha = passiveAlpha
                }
                PageID.NEWS ->{
                    btnData.alpha = passiveAlpha
                    btnNews.alpha = 1.0f
                }
                else ->{
                    btnData.alpha = passiveAlpha
                    btnNews.alpha = passiveAlpha
                }
            }
        }

    override fun onCreatedView() {
        alpha = 0.0f
        visibility = View.GONE
        btnData.alpha = passiveAlpha
        btnNews.alpha = passiveAlpha
    }

    override fun onDestroyedView() {

    }
    override fun onSubscribe() {
        super.onSubscribe()

        btnData.clicks().subscribe {
            PagePresenter.getInstance<PageID>().pageChange(PageID.DATA)
        }.apply { disposables?.add(this) }

        btnNews.clicks().subscribe {
            PagePresenter.getInstance<PageID>().pageChange(PageID.NEWS)
        }.apply { disposables?.add(this) }

    }

    fun onOpen(){
        animateAlpha(1.0f)
    }

    fun onClose(){
        animateAlpha(0.0f)

    }





}