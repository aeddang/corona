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
import kotlinx.android.synthetic.main.cp_header.view.*

class Header : RxFrameLayout {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context,attrs)
    override fun getLayoutResId(): Int { return R.layout.cp_header }
    private val appTag = javaClass.simpleName


    override fun onCreatedView() {
        alpha = 0.0f
        visibility = View.GONE
    }

    override fun onDestroyedView() {

    }



    override fun onSubscribe() {
        super.onSubscribe()
        btnHome.clicks().subscribe {
            PagePresenter.getInstance<PageID>().pageChange(PageID.DATA)
        }.apply { disposables?.add(this) }

    }

    fun onOpen(){
        animateAlpha(1.0f)
    }

    fun onClose(){
        animateAlpha(0.0f)

    }





}