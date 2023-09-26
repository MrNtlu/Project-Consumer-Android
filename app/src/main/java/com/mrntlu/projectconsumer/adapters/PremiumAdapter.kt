package com.mrntlu.projectconsumer.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mrntlu.projectconsumer.databinding.CellEmptyBinding
import com.mrntlu.projectconsumer.databinding.CellPremiumBinding
import com.mrntlu.projectconsumer.utils.RecyclerViewEnum
import com.mrntlu.projectconsumer.utils.setSafeOnClickListener
import com.revenuecat.purchases.Package

class PremiumAdapter(
    private val packageList: List<Package>,
    private val onClick: (Package) -> Unit,
): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            RecyclerViewEnum.Empty.value -> EmptyHolder(CellEmptyBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            else -> ItemHolder(CellPremiumBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        }
    }

    override fun getItemCount() = if (packageList.isEmpty())
        1
    else packageList.size

    override fun getItemViewType(position: Int): Int {
        return if (packageList.isEmpty())
            RecyclerViewEnum.Empty.value
        else RecyclerViewEnum.View.value
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (getItemViewType(position) == RecyclerViewEnum.View.value) {
            val item = packageList[position]
            val product = item.product
            val itemHolder = holder as ItemHolder

            itemHolder.binding.apply {
                planTV.text = product.title.split(" Membership")[0]
                priceTV.text = product.price.formatted
                frequencyTV.text = if (product.period?.unit?.name == "YEAR") "/year" else "/mo"

                premiumInfoTV.text = if (product.title.split(" Membership")[0] == "Premium")
                    "AI Suggestions, 3 from each content type"
                else
                    "AI Suggestions, 5 from each content type"

                root.setSafeOnClickListener {
                    onClick(item)
                }
            }
        }
    }

    inner class ItemHolder(
        val binding: CellPremiumBinding,
    ): RecyclerView.ViewHolder(binding.root)

    inner class EmptyHolder(
        val binding: CellEmptyBinding,
    ): RecyclerView.ViewHolder(binding.root)
}