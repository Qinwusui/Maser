package com.wusui.server.adpters

import android.util.SparseArray
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.wusui.server.ui.FmServer
import com.wusui.server.ui.FmSetting
import com.wusui.server.ui.FmUser
import com.wusui.server.ui.FmWebView

/**
 * @author wusui
 */
class MyFragmentAdapter(fragmentActivity: FragmentActivity) :
    FragmentStateAdapter(fragmentActivity) {
    private val fragments: SparseArray<Fragment> = SparseArray()

    init {
        fragments[0] = FmServer()
        fragments[1] = FmWebView()
        fragments[2] = FmUser()
        fragments[3] = FmSetting()
        // fragments[4]=Fmyuanqi()
        // fragments[PAGE_MINECRAFT]=FmMinecraft()
    }

    override fun getItemCount(): Int = fragments.size()

    override fun createFragment(position: Int): Fragment = fragments[position]

}