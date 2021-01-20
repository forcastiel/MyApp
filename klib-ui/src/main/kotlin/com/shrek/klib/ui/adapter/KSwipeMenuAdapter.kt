package com.shrek.klib.ui.adapter

import android.view.ViewGroup
import com.shrek.klib.ui.swipe.SwipeMenuAdapter

/**
 * @author Shrek
 * @date:  2017-03-29
 */
class KSwipeMenuAdapter<SOURCE_BO, HOLDER : HolderBo>(var sourceData: Collection<SOURCE_BO>, doing: KAdapter<SOURCE_BO, HOLDER>.() -> Unit)
    : SwipeMenuAdapter<HOLDER>() {
    
    lateinit var kTranforAdapter: KAdapter<SOURCE_BO, HOLDER>

    init {
        kTranforAdapter = KAdapter(sourceData = sourceData) {
            doing.invoke(this)
        }
    }

    override fun onCreateContentView(p0: ViewGroup?, p1: Int): HOLDER {
        return kTranforAdapter.itemConstructor.invoke()
    }

    override fun getItemCount(): Int { return kTranforAdapter.getItemCount() }

    override fun onBindViewHolder(holder: KHolder<HOLDER>, position: Int) {
        kTranforAdapter.onBindViewHolder(holder,position)
    }

}