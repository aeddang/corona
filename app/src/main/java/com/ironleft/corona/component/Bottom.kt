package com.ironleft.corona.component

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.Button
import android.widget.ImageButton
import com.ironleft.corona.PageFactory
import com.ironleft.corona.PageID
import com.ironleft.corona.R
import com.jakewharton.rxbinding3.view.clicks
import com.lib.page.PagePresenter
import com.lib.util.Log
import com.lib.util.animateY
import com.skeleton.rx.RxFrameLayout
import com.skeleton.view.tab.Tab
import kotlinx.android.synthetic.main.cp_bottom.view.*

class Bottom : RxFrameLayout {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    override fun getLayoutResId(): Int {
        return R.layout.cp_bottom
    }

    override fun onCreatedView() {

    }

    override fun onDestroyedView() {
    }


    override fun onSubscribe() {
        super.onSubscribe()
        btns.forEach {btn->
            btn.clicks().subscribe {
                onSelectedPage(btn)
            }.apply { disposables?.add(this) }
        }

    }

    private val appTag = javaClass.simpleName
    private val btns = arrayOf(btnData, btnNews)
    var currentPage:PageID? = null
        set(value) {
            field = value
            value ?: return
            val groupID = PageFactory.getCategoryIdx(value)
            btns.forEachIndexed { index, btn ->
                btn.alpha = if(groupID == (index+1)) 1.0f else 0.5f
            }
        }

    private fun onSelectedPage(btn:ImageButton){
        val pageID = when(btn){
            btnData -> PageID.DATA
            btnNews -> PageID.NEWS
            else -> null
        }
        pageID ?: return
        PagePresenter.getInstance<PageID>().pageChange(pageID)
    }

    private var isView = false
    fun onOpen(){
        if (isView) return
        isView = true
        this.animateY(0, true).apply {
            interpolator = AccelerateInterpolator()
            startAnimation(this)
        }
    }

    fun onClose(){
        if (! isView) return
        isView = false

        this.animateY(- height, true).apply {
            interpolator = DecelerateInterpolator()
            startAnimation(this)
        }

    }

}