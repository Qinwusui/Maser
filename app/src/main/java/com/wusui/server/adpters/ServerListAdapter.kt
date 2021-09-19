package com.wusui.server.adpters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.ColorUtils
import com.wusui.server.R
import kotlin.random.Random

/**
 * @author wusui
 */
class ServerListAdapter(private val list: List<String>) :
    RecyclerView.Adapter<ServerListAdapter.MyHolder>() {
    var lisener: MainItemClickListener? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        @SuppressLint("InflateParams") val v =
            LayoutInflater.from(parent.context).inflate(R.layout.server_item, null)

        return MyHolder(v)
    }

    override fun onBindViewHolder(holder: MyHolder, position: Int) {
        holder.tx.text = list[position]
        holder.tx.setTextColor(ColorUtils.getColor(R.color.hei))
        holder.view.setOnClickListener { lisener!!.itemClick(position) }
        val list = listOf(
            R.drawable.ic_balloon,
            R.drawable.ic_love_chemistry,
            R.drawable.ic_send,
            R.drawable.ic_sign
        )
        val random = Random(System.currentTimeMillis())
        holder.img.setImageResource(list[random.nextInt(list.size)])
    }

    fun setItemClickListener(listener: MainItemClickListener?) {
        lisener = listener
    }

    override fun getItemCount(): Int {
        return list.size
    }

    interface MainItemClickListener {
        fun itemClick(position: Int)
    }

    class MyHolder(val view: View) : RecyclerView.ViewHolder(
        view
    ) {
        val tx: TextView = view.findViewById(R.id.item_server_text)
        val img: ImageView = view.findViewById(R.id.item_server_img)

    }
}