package com.shrek.klib.ui.selector

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.shrek.klib.extension.kIntWidth
import com.shrek.klib.ui.CommonUiSetup
import com.shrek.klib.ui.R
import com.shrek.klib.ui.selector.datepick.wheel.WheelView
import java.util.*

/**
 * @author Shrek
 * @date:  2017-03-17
 */
class SelectorDialog<T>(val hostContext:Context,val title:String,val allData:ArrayList<T>,var defaultSel: T? = null,val converter:(T)->String):Dialog(hostContext,R.style.Base_Dialog) {

    lateinit var titleView:TextView
    lateinit var gapView:ImageView
    lateinit var wheelView:WheelView

    lateinit var cancelBtn:Button
    lateinit var submitBtn:Button

    lateinit var adapter:SelectorWheelAdapter<T>
    
    init {
        val view = LayoutInflater.from(hostContext).inflate(R.layout.selector_layout, null)
        titleView = view.findViewById<TextView>(R.id.sl_titleView) as TextView
        gapView = view.findViewById<ImageView>(R.id.sl_gapView) as ImageView
        wheelView = view.findViewById<WheelView>(R.id.sl_wheelView) as WheelView

        submitBtn = view.findViewById<Button>(R.id.sl_submitBtn) as Button
        cancelBtn = view.findViewById<Button>(R.id.sl_cancelBtn) as Button

        titleView.text = title
        gapView.setBackgroundColor(CommonUiSetup.pramaryColor)
//        wheelView.setTextSize(CommonUiSetup.textSize.toInt())
        adapter = SelectorWheelAdapter(allData,converter)
        wheelView.adapter = adapter
        wheelView.isCyclic = true
        if (allData.size > 5) {
            wheelView.visibleItems = 5
        } else {
            wheelView.visibleItems = 3
        }
        setContentView(view)
        
        val lp = window!!.attributes
        lp.width = kIntWidth(0.7f)
        window!!.attributes = lp
    }
    
    //选择当前的mode
    fun setCurrentItem(defaultT:T) {
        allData.forEachIndexed { index, t -> 
            if(t!!.equals(defaultT)) {
                wheelView.currentItem =  index
                return
            }
        }
    }
    
    fun selectorListener(listener:( selectorValue:T )->Unit) {
//        wheelView.addChangingListener { wheel, oldPosition, newPosition ->
//            listener(wheel,allData[oldPosition],allData[newPosition])
//        }
        cancelBtn.setOnClickListener { dismiss() }
        submitBtn.setOnClickListener {
            listener(allData[wheelView.currentItem])
            dismiss() 
        }
    }

    override fun show() {
        super.show()
        defaultSel?.let {
            allData.forEachIndexed { index, t ->
                if(t!!.equals(it)){
                    wheelView.currentItem = index
                } }
        }
    }
    
}