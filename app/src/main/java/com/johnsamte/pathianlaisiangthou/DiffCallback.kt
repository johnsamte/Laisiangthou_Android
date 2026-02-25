package com.johnsamte.pathianlaisiangthou

import androidx.recyclerview.widget.DiffUtil

class DiffCallback : DiffUtil.ItemCallback<LaisiangthouBu>() {

    override fun areItemsTheSame(
        oldItem: LaisiangthouBu,
        newItem: LaisiangthouBu
    ): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(
        oldItem: LaisiangthouBu,
        newItem: LaisiangthouBu
    ): Boolean {
        return oldItem == newItem
    }
}
