package com.ironleft.corona.page

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.ironleft.corona.BuildConfig
import com.ironleft.corona.PageID
import com.ironleft.corona.R
import com.ironleft.corona.page.viewmodel.ViewModelSetting
import com.jakewharton.rxbinding3.view.clicks
import com.jakewharton.rxbinding3.widget.checkedChanges
import com.lib.page.PagePresenter
import com.skeleton.module.ViewModelFactory
import com.skeleton.rx.RxPageFragment
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.page_setup.*
import javax.inject.Inject


class PageSetup  : RxPageFragment() {

    private val appTag = javaClass.simpleName
    override fun getLayoutResId() = R.layout.page_setup

    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    private lateinit var viewModel: ViewModelSetting

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidSupportInjection.inject(this)
        viewModel = ViewModelProvider(this, viewModelFactory).get(ViewModelSetting::class.java)

    }

    override fun onCreatedView() {
        super.onCreatedView()
        textVersion.text = "version ${BuildConfig.VERSION_NAME}"
    }


    override fun onSubscribe() {
        super.onSubscribe()
        switchPush.isChecked =  viewModel.repo.setting.getPushAble()
        switchPush.checkedChanges().subscribe {
            viewModel.repo.setting.putPushAble(it)
        }.apply { disposables?.add(this) }

        btnNotice.clicks().subscribe {
            PagePresenter.getInstance<PageID>().pageChange(PageID.NOTICES)
        }.apply { disposables?.add(this) }

        btnPc.clicks().subscribe {
            val uris = Uri.parse("https://worldviruswatch.com")
            val intents = Intent(Intent.ACTION_VIEW, uris)
            val b = Bundle()
            b.putBoolean("new_window", true)
            intents.putExtras(b)
            context?.startActivity(intents)
        }.apply { disposables?.add(this) }

        btnBack.clicks().subscribe {
            PagePresenter.getInstance<PageID>().goBack()
        }.apply { disposables?.add(this) }
    }



    override fun onDestroyView() {
        super.onDestroyView()
    }



}