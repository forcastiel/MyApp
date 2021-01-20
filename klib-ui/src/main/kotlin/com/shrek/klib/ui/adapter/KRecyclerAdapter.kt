package com.shrek.klib.ui.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.shrek.klib.colligate.MATCH_PARENT
import com.shrek.klib.colligate.ReflectUtils
import com.shrek.klib.colligate.get
import com.shrek.klib.extension.onMyClick
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.UI
import java.util.*

/**
 * @author Shrek
 * @date:  2017-03-14
 */
abstract class  KRecyclerAdapter<HOLER: RecyclerView.ViewHolder,T>(var sourceData:Collection<T>) : RecyclerView.Adapter<HOLER>() {
    //cell的高度
    open var cellHeight:Int = 0

    open var itemClick:((View,Int,T)->Unit)? = null 
    
    var context:Context? = null
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HOLER {
        if(context == null) {
            context = parent!!.context
        }
        val holder = holderStructure(View(context))
        val view = parent.context.UI {
            cellLayout(holder)()
        }.view
        
        ReflectUtils.setFieldValue(holder,"itemView",view)
//        holder::class.java.getField("itemView").set(holder,view)
        return holder
    }
    
    abstract fun cellLayout(holder:HOLER):AnkoContext<Context>.() -> Unit
    
    //hodler的初始化
    abstract fun holderStructure(view: View):HOLER

    //数据绑定
    open fun onBindHolder(holder: HOLER, position: Int, bo:T) {
        
    }

    override final fun onBindViewHolder(holder: HOLER, position: Int) {
        holder?.apply {
            val data = sourceData.get(position)!!
            onBindHolder(this,position,data)
            this.itemView.layoutParams = ViewGroup.LayoutParams(MATCH_PARENT,cellHeight)
            itemClick?.let {
                itemView.onMyClick { it(itemView,position,data) }
            }
        }
    }

    override fun getItemCount(): Int {
        return sourceData.size
    }
    
    //添加元素
    fun addLastData(t:T) {
        (sourceData as? ArrayList<T>)?.let { it.add(t) }
        (sourceData as? HashSet<T>)?.let { it.add(t) }
        notifyItemInserted(sourceData.count() -1)
    }

    //删除元素
    fun removeDataAt(removeIndex:Int) {
        sourceData[removeIndex]?.let { removeData ->
            (sourceData as? ArrayList<T>)?.let { it.remove(removeData) }
            (sourceData as? HashSet<T>)?.let { it.remove(removeData) }
            notifyItemRemoved(removeIndex)
        }
    }
    
    //删除元素
    fun removeData(removeData:T) {
        var findIndex = -1
        var findData:T? = null
        sourceData.forEachIndexed { index, t ->  
            if(removeData?.equals(t)!!) {
                findIndex = index
                findData = t
            }
        }
        
        findData?.let { dataTemp ->
            (sourceData as? ArrayList<T>)?.let { it.remove(dataTemp) }
            (sourceData as? HashSet<T>)?.let { it.remove(dataTemp) }
            notifyItemRemoved(findIndex)
        }
    }
    
}
