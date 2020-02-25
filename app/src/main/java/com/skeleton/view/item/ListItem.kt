package com.skeleton.view.item

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.annotation.CallSuper
import com.lib.page.Page
import io.reactivex.disposables.CompositeDisposable


abstract class ListItem: FrameLayout,  Page {
    constructor(context: Context): super(context) { init(context) }
    constructor(context: Context, attrs: AttributeSet): super(context, attrs) { init(context) }
    protected var disposables: CompositeDisposable? = null
    @CallSuper
    open protected fun init(context: Context) {
        LayoutInflater.from(context).inflate(getLayoutResId(), this, true)
    }

    @CallSuper
    override fun onAttachedToWindow() {
        onAttached()
        super.onAttachedToWindow()
        disposables = CompositeDisposable()
        onCreatedView()
    }

    @CallSuper
    override fun onDetachedFromWindow() {
        onDetached()
        super.onDetachedFromWindow()
        disposables?.clear()
        disposables = null
        onDestroyedView()
    }

    override fun onCreatedView() {}
    override fun onDestroyedView() {}
    open fun setData(data:Any?, idx:Int = -1){
        disposables?.clear()
        disposables = CompositeDisposable()
    }

}
