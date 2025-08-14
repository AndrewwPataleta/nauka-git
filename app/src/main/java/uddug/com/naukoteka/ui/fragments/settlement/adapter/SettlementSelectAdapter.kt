package uddug.com.naukoteka.ui.fragments.settlement.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import uddug.com.domain.entities.country.Settlement
import uddug.com.naukoteka.R
import uddug.com.naukoteka.databinding.ItemSettlementSelectBinding

class SettlementSelectAdapter(
    private val onSelectClick: (Settlement) -> Unit,
) :
    ListAdapter<Settlement, SettlementSelectAdapter.ViewHolder>(
        object : DiffUtil.ItemCallback<Settlement>() {
            override fun areItemsTheSame(
                oldItem: Settlement,
                newItem: Settlement
            ): Boolean {
                return oldItem.uref == newItem.uref
            }

            override fun areContentsTheSame(
                oldItem: Settlement,
                newItem: Settlement
            ): Boolean {
                return oldItem == newItem
            }
        }
    ) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemSettlementSelectBinding.bind(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_settlement_select, parent, false)
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: MutableList<Any>) {
        onBindViewHolder(holder, position)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(val binding: ItemSettlementSelectBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Settlement) = with(binding) {
            title.text = item.city
            checked.isChecked = item.isSelected
            root.setOnClickListener {
                onSelectClick(item)
            }
        }

    }
}