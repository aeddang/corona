package com.ironleft.corona
import android.content.pm.ActivityInfo
import com.ironleft.corona.page.*

import com.lib.page.PageFragment
import com.lib.page.PagePosition

class PageFactory {

    companion object {
        private var currentInstance: PageFactory? = null
        fun getInstance(): PageFactory {
            if (currentInstance == null) currentInstance = PageFactory()
            return currentInstance !!
        }

        fun getCategoryIdx(pageID: PageID):Int{
            return pageID.position.toString().first().toString().toInt()
        }
    }

    init {
        currentInstance = this
    }

    /**
     * 홈페이지 등록
     * 등록시 뒤로실행시 옙종료
     */
    val homePages: Array<PageID> = arrayOf(PageID.DATA)

    /**
     * 히스토리 사용안함
     * 등록시 뒤로실행시 패스
     */
    val disableHistoryPages: Array<PageID> = arrayOf(PageID.INTRO)

    /**
     * 재사용가능 페이지등록
     * 등록시 viewModel 및 fragment가 재사용 -> 페이지 재구성시 효율적
     */
    val backStackPages: Array<PageID> = arrayOf()


    private val fullScreenPage: Array<PageID> = arrayOf()
    fun isFullScreenPage(id: PageID): Boolean {
        return fullScreenPage.indexOf(id) != - 1
    }

    private val useHeaderPage: Array<PageID> = arrayOf(PageID.NEWS, PageID.DATA)
    fun needHeaderPage(id: PageID): Boolean {
        return useTabPage.indexOf(id) != - 1
    }

    private val useTabPage: Array<PageID> = arrayOf(PageID.NEWS, PageID.DATA)
    fun needTabPage(id: PageID): Boolean {
        return useTabPage.indexOf(id) != - 1
    }

    private val useBottomPage: Array<PageID> = arrayOf(PageID.NEWS, PageID.DATA)
    fun needBottomPage(id: PageID): Boolean {
        return useBottomPage.indexOf(id) != - 1
    }


    fun getPageOrientation(id: PageID): Int {
        return when (id) {
            PageID.DATA, PageID.GRAPH -> ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            else -> ActivityInfo.SCREEN_ORIENTATION_SENSOR
        }
    }



    fun getPageByID(id: PageID): PageFragment {
        return when (id) {
            PageID.INTRO -> PageIntro()
            PageID.DATA -> PageData()
            PageID.NEWS -> PageNews()
            PageID.NOTICES -> PageNotices()
            PageID.GRAPH -> PageGraph()
            PageID.MAP -> PageMap()
            PageID.SETUP -> PageSetup()
            PageID.POPUP_WEBVIEW -> PopupWebView()
        }
    }
}

/**
 * PageID
 * position 값에따라 시작 에니메이션 변경
 * 기존페이지보다 클때 : 오른쪽 -> 왼족
 * 기존페이지보다 작을때 : 왼쪽 -> 오른쪽
 * history back 반대
 */
enum class PageID(val resId: Int, override var position: Int = 9999) : PagePosition {
    //group1
    INTRO(R.string.page_intro,0),
    DATA(R.string.page_data,101),
    NEWS(R.string.page_news,201),
    GRAPH(R.string.page_graph,103),
    MAP(R.string.page_map,102),
    SETUP(R.string.page_setup,301),
    NOTICES(R.string.page_notices,401),
    POPUP_WEBVIEW(R.string.popup_webview)
}
