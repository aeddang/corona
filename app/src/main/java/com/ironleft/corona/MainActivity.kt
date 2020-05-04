package com.ironleft.corona

import android.content.Intent
import android.view.View
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.iid.FirebaseInstanceId
import com.ironleft.corona.page.PageData
import com.ironleft.corona.store.Repository
import com.lib.page.PageActivity
import com.lib.page.PageFragment
import com.lib.page.PagePresenter
import com.lib.util.CommonUtil
import com.lib.util.Log
import com.skeleton.module.ViewModelFactory
import com.skeleton.rx.Rx
import dagger.android.AndroidInjection
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject

class MainActivity : PageActivity<PageID>(), Rx {

    private val appTag = javaClass.simpleName
    override fun getLayoutResId(): Int = R.layout.activity_main
    override fun getPageExitMsg(): Int = R.string.notice_app_exit
    override fun getPageAreaId(): Int = R.id.area

    @Inject
    lateinit var repository: Repository

    override fun onCreatedView() {
        AndroidInjection.inject(this)
        CommonUtil.getApplicationSignature(this)
        val d = repository.selectedCountryObservable.subscribe { tab.title = it.title }
        if(repository.setting.getPushAble()) repository.setting.putPushAble(true)
        PagePresenter.getInstance<PageID>().pageStart(PageID.INTRO)
    }

    override fun onPageInit() {
        super.onPageInit()
        if (intent != null && intent.extras != null) PagePresenter.getInstance<PageID>().pageStart(PageID.NOTICES)
        else PagePresenter.getInstance<PageID>().pageStart(PageID.DATA)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if(!isPageInit) return
        PagePresenter.getInstance<PageID>().pageStart(PageID.NOTICES)
    }

    override fun onDestroyedView() {

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

    }

    override fun onSubscribe() {
        super.onSubscribe()
    }


    override fun onWillChangePageFragment(id: PageID, param: Map<String, Any?>?, isPopup: Boolean) {
        loaded()
        tab.currentPage = id
        bottom.currentPage = id
        val willChangeOrientation = PageFactory.getInstance().getPageOrientation(id)
        if (willChangeOrientation != -1 && requestedOrientation != willChangeOrientation)
            requestedOrientation = willChangeOrientation


        if(PageFactory.getInstance().needHeaderPage(id)) header.onOpen() else header.onClose()
        if(PageFactory.getInstance().needTabPage(id)) tab.onOpen() else tab.onClose()
        if(PageFactory.getInstance().needBottomPage(id)) bottom.onOpen() else bottom.onClose()

        val isFullScreen = PageFactory.getInstance().isFullScreenPage(id)
        if (isFullScreen) CommonUtil.enterFullScreenMode(this)
        else CommonUtil.enterDefaultMode(this)
    }


    override fun onResume() {
        super.onResume()
        currentTopPage?.let {
            val isFullScreen = PageFactory.getInstance().isFullScreenPage(it)
            if (isFullScreen) CommonUtil.enterFullScreenMode(this)
            else CommonUtil.enterDefaultMode(this)
        }

    }

    override fun getPageByID(id: PageID): PageFragment {
        return PageFactory.getInstance().getPageByID(id)
    }

    override fun getPopupByID(id: PageID): PageFragment {
        return PageFactory.getInstance().getPageByID(id)
    }

    override fun getHomes(): Array<PageID> {
        return PageFactory.getInstance().homePages
    }

    override fun getDisableHistorys(): Array<PageID>? {
        return PageFactory.getInstance().disableHistoryPages
    }

    override fun getBackStacks(): Array<PageID> {
        return PageFactory.getInstance().backStackPages
    }

    override fun loading(isRock: Boolean) {
        dimed.visibility = View.VISIBLE
    }

    override fun loaded() {
        dimed.visibility = View.GONE
    }



    override fun getPageIn(isBack: Boolean): Int = if (isBack) R.anim.slide_in_left else R.anim.slide_in_right
    override fun getPageOut(isBack: Boolean): Int = if (isBack) R.anim.slide_out_right else R.anim.slide_out_left
    override fun getPopupIn(): Int = R.anim.slide_in_down
    override fun getPopupOut(): Int = R.anim.slide_out_down

}
