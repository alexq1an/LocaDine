package com.example.locadine.adapters

import com.example.locadine.R
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.locadine.pojos.Message
import com.example.locadine.pojos.SenderType

class MessageAdapter(private val messageModalArrayList: ArrayList<Message>, private val context: Context) :
    RecyclerView.Adapter<ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val senderType = SenderType.fromNumber(viewType);
        return if (senderType == SenderType.USER) {
            UserViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.user_message, parent, false))
        } else {
            BotViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.bot_message, parent, false))
        }
    }

    override fun getItemCount(): Int {
        return messageModalArrayList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val modal = messageModalArrayList[position]
        if (modal.sender == SenderType.USER) {
            (holder as UserViewHolder).userTextView.text = modal.message
        } else {
            (holder as BotViewHolder).botTextView.text = modal.message
        }
    }

    override fun getItemViewType(position: Int): Int {
        return messageModalArrayList[position].sender.number
    }

    class UserViewHolder(itemView: View) : ViewHolder(itemView) {
        var userTextView: TextView
        init {
            userTextView = itemView.findViewById(R.id.user_message)
        }
    }

    class BotViewHolder(itemView: View) : ViewHolder(itemView) {
        var botTextView: TextView

        init {
            botTextView = itemView.findViewById(R.id.bot_message)
        }
    }
}