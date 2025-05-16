package com.example.frontzephiro.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.frontzephiro.R
import com.example.frontzephiro.models.ContactResponse
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

class ContactsAdapter(
    private var items: List<ContactResponse>,
    private val onItemClick: (ContactResponse) -> Unit,
    private val onEditClick: (ContactResponse) -> Unit = {},
    private val onDeleteClick: (ContactResponse) -> Unit = {}
) : RecyclerView.Adapter<ContactsAdapter.ViewHolder>() {

    fun updateItems(newItems: List<ContactResponse>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        LayoutInflater.from(parent.context)
            .inflate(R.layout.item_card_emergency_contact, parent, false),
        onItemClick, onEditClick, onDeleteClick
    )

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    class ViewHolder(
        private val view: View,
        private val onItemClick: (ContactResponse) -> Unit,
        private val onEditClick: (ContactResponse) -> Unit,
        private val onDeleteClick: (ContactResponse) -> Unit
    ) : RecyclerView.ViewHolder(view) {

        private val card      = view.findViewById<CardView>(R.id.cardContent)
        private val tvTitle   = view.findViewById<TextView>(R.id.tvTitle)
        private val tvAuthor  = view.findViewById<TextView>(R.id.tvAuthor)
        private val chipGroup = view.findViewById<ChipGroup>(R.id.chipGroupItem)
        private val ivEdit    = view.findViewById<ImageView>(R.id.ivAction1)
        private val ivDelete  = view.findViewById<ImageView>(R.id.ivAction2)

        fun bind(contact: ContactResponse) {
            // 1) Texto
            tvTitle.text  = contact.fullName
            tvAuthor.text = contact.email

            // 3) Clicks
            card.setOnClickListener   { onItemClick(contact) }
            ivEdit.setOnClickListener { onEditClick(contact) }
            ivDelete.setOnClickListener { onDeleteClick(contact) }
        }
    }
    fun removeItem(contactId: String) {
        val index = items.indexOfFirst { it.id == contactId }
        if (index != -1) {
            val mutable = items.toMutableList()
            mutable.removeAt(index)
            items = mutable
            notifyItemRemoved(index)
        }
    }
}
