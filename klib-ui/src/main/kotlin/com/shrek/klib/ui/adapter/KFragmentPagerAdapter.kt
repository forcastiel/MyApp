package com.shrek.klib.ui.adapter


import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.shrek.klib.extension.uiThread
import com.shrek.klib.view.KActivity
import com.shrek.klib.view.KFragment
import java.lang.ref.WeakReference

/**
 * showProcess参数 第一个 原来选择的fragment 第二个 新选择的fragment
 * @author Shrek
 * @date:  2017-01-10
 */
class KFragmentPagerAdapter<F: KFragment>(val hostAct : KActivity, val hostContainer:WeakReference<ViewPager>, val dateSource:Array<F>
                                          , val showProcess:(Int,F,F)->Unit = {positon,oldFragment,newFragment ->}) : FragmentPagerAdapter(hostAct.supportFragmentManager){

    private var hideProcess:((F)->Unit)? = null
    
    var onPageScrolled:((Int,Float,Int)->Unit)? = null
    
    private var orginShowPositon = 0
    private var lastShowPositon = -1
    
    var positon = 0
        get() = orginShowPositon
    //得到当前的 fragment
    var currContent:F = dateSource[0]
        get() = dateSource[orginShowPositon]
    
    
    init {
        hostContainer.get()?.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {  }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) { onPageScrolled?.invoke(position,positionOffset,positionOffsetPixels) }

            override fun onPageSelected(position: Int) {
                onHide()
                uiThread(100) { onShow(position) }
            }
        })
    }
    
    override fun getItem(position: Int): F {
        return dateSource[position]
    }
    
    fun hideProcess(hideProcess:(F)->Unit): KFragmentPagerAdapter<F> {
        this.hideProcess = hideProcess
        return this
    }

    fun onShow(position: Int){
        lastShowPositon = orginShowPositon
        orginShowPositon = position
        val orginF = dateSource[lastShowPositon]
        val f = dateSource[position]
        showProcess(orginShowPositon,orginF,f)
    }

    fun onHide(){
        if(lastShowPositon < 0 || lastShowPositon > dateSource.size) {
            return
        }
        val orginF = dateSource[lastShowPositon]
        hideProcess?.invoke(orginF)
    }
    
    override fun getCount(): Int {
        return dateSource.size
    }
    
    //遍历所有的fragment
    fun forEach(process:(F)->Unit) {
        dateSource.forEach { process(it) }
    }
}