package com.ironleft.corona.page

import android.os.Bundle
import com.ironleft.corona.PageID
import com.ironleft.corona.R
import com.lib.page.PagePresenter
import com.skeleton.rx.RxPageFragment
import dagger.android.support.AndroidSupportInjection
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import java.util.concurrent.TimeUnit


class PageIntro  : RxPageFragment() {

    private val appTag = javaClass.simpleName
    override fun getLayoutResId() = R.layout.page_intro



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidSupportInjection.inject(this)
    }
    override fun onPause() {
        super.onPause()
        disposables.clear()
    }

    override fun onResume() {
        super.onResume()
        Observable.interval(1500, TimeUnit.MILLISECONDS)
            .take(1)
            .observeOn(AndroidSchedulers.mainThread()).subscribe {
                PagePresenter.getInstance<PageID>().pageInit()
            }.apply { disposables.add(this) }
    }



    override fun onDestroyView() {
        super.onDestroyView()
    }



}