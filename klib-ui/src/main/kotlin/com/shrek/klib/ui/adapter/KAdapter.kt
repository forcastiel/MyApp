package com.shrek.klib.ui.adapter

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.shrek.klib.colligate.MATCH_PARENT
import com.shrek.klib.colligate.WRAP_CONTENT
import com.shrek.klib.extension.kApplication
import com.shrek.klib.extension.onMyClick
import org.jetbrains.anko.UI
import org.jetbrains.anko._LinearLayout
import org.jetbrains.anko.verticalLayout

/**
 * @author Shrek
 * @date:  2017-03-15
 */
class KAdapter<SOURCE_BO, HOLDER : HolderBo>(setHasStableIds: Boolean = true, var sourceData: Collection<SOURCE_BO>, doing: KAdapter<SOURCE_BO, HOLDER>.() -> Unit)
    : RecyclerView.Adapter<KHolder<HOLDER>>() {
    private var bindData: ((HOLDER, SOURCE_BO, Int) -> Unit)? = null

    internal lateinit var itemConstructor: () -> HOLDER

    private var itemClickDoing: ((SOURCE_BO, Int) -> Unit)? = { s, i -> }
    private var itemLongClickProcess: ((SOURCE_BO, Int) -> Unit)? = null
    init {
        doing.invoke(this)
        setHasStableIds(setHasStableIds)
    }

    //绑定数据
    fun bindData(bindDataDoing: (HOLDER, SOURCE_BO, Int) -> Unit) {
        this.bindData = bindDataDoing
    }

    fun itemConstructor(itemBoConstructor: () -> HOLDER) {
        itemConstructor = itemBoConstructor
    }

    fun itemClickDoing(doing: (SOURCE_BO, Int) -> Unit) {
        itemClickDoing = doing
    }

    fun itemLongClickDoing(doing: (SOURCE_BO, Int) -> Unit) {
        itemLongClickProcess = doing
    }

    override fun getItemCount(): Int {
        return sourceData.size
    }

    fun getItem(position: Int): SOURCE_BO {
        return sourceData.elementAt(position)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    fun append(datas:Collection<SOURCE_BO>){
        if ( sourceData is ArrayList){
            (sourceData as? ArrayList)?.addAll(datas)
        } else if ( sourceData is HashSet ){
            (sourceData as? HashSet)?.addAll(datas)
        } else {
            sourceData = sourceData.plus(datas)
        }  
    }

    override fun getItemViewType(position: Int): Int {
        return super.getItemViewType(position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): KHolder<HOLDER> {
        val bo = itemConstructor()
        return KHolder<HOLDER>(bo)
    }

    override final fun onBindViewHolder(holder: KHolder<HOLDER>, position: Int) {
        val sourceBo = getItem(position)
        bindData?.invoke(holder.bo, sourceBo, position)
        if(isVerticalOrientation){
            if (holder.cellHeight() > 0) {
                holder.itemView.layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, holder.cellHeight())
            } else {
                holder.itemView.layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
            }
        }
        holder.bo.holderContentLayout.onMyClick {
            itemClickDoing?.invoke(sourceBo, position)
        }
        if (itemLongClickProcess != null){
            holder.bo.holderContentLayout.setOnLongClickListener{
                itemLongClickProcess?.invoke(sourceBo,position)
                 true
            }
        }
    }

    var isVerticalOrientation = true

    public fun setIsVerticalOrientation(orientation: Boolean){
        isVerticalOrientation = orientation
    }

}

class KHolder<BO : HolderBo>(var bo: BO) : RecyclerView.ViewHolder(bo.rootView) {
    fun cellHeight(): Int {
        return bo.cellHeight
    }
}

/**
 * 传值通过params  传参数
 * 属性传值传不进去
 */

abstract class HolderBo(var cellHeight: Int = WRAP_CONTENT, val params: Map<String, Any>? = null) {
    lateinit var rootView: View
    lateinit var holderContentLayout: View
    var footView: ViewGroup? = null
    init {
        rootView = kApplication.UI {
            verticalLayout {
                holderContentLayout = rootViewInit().invoke(this)
                footView = footViewInit().invoke(this)
            }
        }.view
    }

    abstract fun rootViewInit(): _LinearLayout.() -> View

    open fun footViewInit() : _LinearLayout.() -> ViewGroup? {
        return { null }
    }

    open fun setupFootView(isFoot: Boolean) {
        footView?.visibility = if (isFoot) View.VISIBLE else View.GONE
    }
}