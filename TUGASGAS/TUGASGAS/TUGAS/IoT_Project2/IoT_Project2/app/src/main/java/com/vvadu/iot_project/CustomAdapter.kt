package com.vvadu.iot_project

import android.content.Context

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.LinearLayout
import android.widget.TextView


class CustomAdapter(context: Context,arrayListDetails:ArrayList<Model>) : BaseAdapter(){

    private val layoutInflater: LayoutInflater
    private val arrayListDetails:ArrayList<Model>

    init {
        this.layoutInflater = LayoutInflater.from(context)
        this.arrayListDetails=arrayListDetails
    }

    override fun getCount(): Int {
        return arrayListDetails.size
    }

    override fun getItem(position: Int): Any {
        return arrayListDetails.get(position)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
        val view: View?
        val listRowHolder: ListRowHolder
        if (convertView == null) {
            view = this.layoutInflater.inflate(R.layout.adapterl_layout, parent, false)
            listRowHolder = ListRowHolder(view)
            view.tag = listRowHolder
        } else {
            view = convertView
            listRowHolder = view.tag as ListRowHolder
        }

        listRowHolder.tvTemp.text = arrayListDetails.get(position).temp.toString()
        listRowHolder.tvHumid.text = arrayListDetails.get(position).humid.toString()
        listRowHolder.tvGasL.text = arrayListDetails.get(position).gasl.toString()
        listRowHolder.tvtime.text = arrayListDetails.get(position).time
        return view
    }
}

private class ListRowHolder(row: View?) {
    public val tvTemp: TextView
    public val tvHumid: TextView
    public val tvGasL: TextView
    public val tvtime: TextView
    public val linearLayout: LinearLayout

    init {
        this.tvTemp = row?.findViewById<TextView>(R.id.tvTemp) as TextView
        this.tvHumid = row.findViewById<TextView>(R.id.tvHumid) as TextView
        this.tvGasL = row.findViewById<TextView>(R.id.tvGasL) as TextView
        this.tvtime = row.findViewById<TextView>(R.id.tvtime) as TextView
        this.linearLayout = row.findViewById<LinearLayout>(R.id.linearLayout) as LinearLayout
    }
}