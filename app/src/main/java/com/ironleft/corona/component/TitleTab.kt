package com.ironleft.corona.component

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.ironleft.corona.PageID
import com.ironleft.corona.R
import com.lib.util.animateAlpha
import com.skeleton.rx.RxFrameLayout
import kotlinx.android.synthetic.main.cp_tab.view.*

class TitleTab : RxFrameLayout {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context,attrs)
    override fun getLayoutResId(): Int { return R.layout.cp_tab }
    private val appTag = javaClass.simpleName
    private val passiveAlpha = 0.4f

    private var areaTitle = context.getString(R.string.cp_tab_title)
    var title:String = ""
        set(value) {
            field = value
            areaTitle = field
            textTitle.text = value
        }

    var currentPage:PageID? = null
        set(value) {
            field = value
            when(value){
                PageID.DATA ->{
                    textTitle.text = areaTitle
                }
                PageID.NEWS ->{
                    textTitle.text = context.getString(value.resId)

                }
                else ->{
                    /*
                    value?.let {
                        textTitle.text = context.getString(it.resId)
                    }
                    */
                }
            }
        }

    override fun onCreatedView() {
        alpha = 0.0f
        visibility = View.GONE
    }


    override fun onDestroyedView() {

    }
    override fun onSubscribe() {
        super.onSubscribe()



    }

    fun onOpen(){
        animateAlpha(1.0f)
    }

    fun onClose(){
        animateAlpha(0.0f)

    }





}