package com.dagger


import com.dagger.module.view.ActivityModule
import com.dagger.module.view.MainActivityModule
import com.dagger.module.view.PageModule
import com.ironleft.corona.MainActivity
import com.ironleft.corona.page.*
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
internal abstract class AndroidBindingModule {


    /**
     * Main Activity
     */

    @ActivityScope
    @ContributesAndroidInjector(modules = [MainActivityModule::class, ActivityModule::class])
    internal abstract fun bindMainActivity(): MainActivity

    @PageScope
    @ContributesAndroidInjector(modules = [PageModule::class])
    internal abstract fun bindPageIntro(): PageIntro

    @PageScope
    @ContributesAndroidInjector(modules = [PageModule::class])
    internal abstract fun bindPageData(): PageData

    @PageScope
    @ContributesAndroidInjector(modules = [PageModule::class])
    internal abstract fun bindPageNews(): PageNews

    @PageScope
    @ContributesAndroidInjector(modules = [PageModule::class])
    internal abstract fun bindPageMap(): PageMap

    @PageScope
    @ContributesAndroidInjector(modules = [PageModule::class])
    internal abstract fun bindPageGraph(): PageGraph

    @PageScope
    @ContributesAndroidInjector(modules = [PageModule::class])
    internal abstract fun bindPageSetup(): PageSetup

}
