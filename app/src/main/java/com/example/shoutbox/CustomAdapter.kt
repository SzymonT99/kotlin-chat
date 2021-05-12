package com.example.shoutbox

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.recycler_item.view.*

class CustomAdapter(private val postsList: MutableList<Post>, var itemClickListener: OnItemClickListener) : RecyclerView.Adapter<CustomAdapter.CustomViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.recycler_item,
            parent, false)
        return CustomViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        val currentItem = postsList[position]
        holder.bind(currentItem, itemClickListener)
    }

    override fun getItemCount() = postsList.size

    interface OnItemClickListener{
        fun onItemClick(message: Post)
    }

    fun removeAt(position: Int) {
        postsList.removeAt(position)
        notifyItemRemoved(position)
    }

    class CustomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var textLogin: TextView = itemView.textView_login
        var textData: TextView = itemView.textView_date
        var textContent: TextView = itemView.textView_content

        fun bind(message: Post, clickListener: OnItemClickListener){
            textLogin.text = message.getLogin()
            textData.text = (message.getDate()!!.replace("T", " ")).substring(0, 19)
            if(message.getContent()!!.length > 32){
                textContent.text = ((message.getContent())!!.substring(0, 32)).plus("...")
            }
            else{
                textContent.text = message.getContent()
            }

            itemView.setOnClickListener {
                clickListener.onItemClick(message)
            }
        }
    }
}