package com.lib.view.animate

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.View
import androidx.annotation.CallSuper
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

abstract class AnimatedDrawView @kotlin.jvm.JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : View(context, attrs, defStyleAttr) {

    private val appTag = "AnimatedDrawView"

    private var disposable: Disposable? = null
    protected var fps: Long = 1000/60
    protected var frm:Int = 0 ; private set
    protected var isDrawing = true
    protected var duration:Long = 0
    protected val currentTime:Long
        get() { return frm * fps}

    fun startAnimation( d:Long, delay:Long = 0 ) {
        frm = 0
        duration = d
        disposable?.dispose()
        disposable = Observable.interval(fps, TimeUnit.MILLISECONDS)
            .timeInterval().delay(delay, TimeUnit.MILLISECONDS)
            .observeOn(Schedulers.computation())
            .subscribe {
                frm ++
                if(frm == 1) {
                    onStart()
                }
                onCompute(frm)
                if(duration > 0) {
                    if (duration <= currentTime) {
                        stopAnimation()
                        onCompleted(frm)
                    }
                }
                postInvalidate()
            }
    }

    fun stopAnimation() {
        disposable?.dispose()
        disposable = null
    }

    abstract fun onStart()
    abstract fun onCompute( f:Int )
    abstract fun onCompleted( f:Int )
    abstract fun onDrawAnimation(canvas: Canvas?)
    @CallSuper
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopAnimation()
    }
    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (!isDrawing) return
        onDrawAnimation(canvas)
    }
}