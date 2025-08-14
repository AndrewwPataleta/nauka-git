package uddug.com.naukoteka.ui.fragments.county.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import uddug.com.domain.entities.PasswordRequirementsEntity
import uddug.com.domain.entities.country.Country
import uddug.com.domain.entities.profile.Education
import uddug.com.naukoteka.R
import uddug.com.naukoteka.databinding.ItemCountrySelectBinding
import uddug.com.naukoteka.databinding.ItemEducationMiddleBinding
import uddug.com.naukoteka.databinding.ItemPasswordRequirementsBinding
import uddug.com.naukoteka.global.base.BaseAdapter
import uddug.com.naukoteka.global.base.BaseViewHolder
import uddug.com.naukoteka.utils.text.isNotNullOrEmpty

class CountrySelectAdapter(
    private val onSelectClick: (Country) -> Unit,
) :
    ListAdapter<Country, CountrySelectAdapter.ViewHolder>(
        object : DiffUtil.ItemCallback<Country>() {
            override fun areItemsTheSame(
                oldItem: Country,
                newItem: Country
            ): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(
                oldItem: Country,
                newItem: Country
            ): Boolean {
                return oldItem == newItem
            }
        }
    ) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemCountrySelectBinding.bind(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_country_select, parent, false)
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: MutableList<Any>) {
        onBindViewHolder(holder, position)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(val binding: ItemCountrySelectBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Country) = with(binding) {
            title.text = item.term
            checked.isChecked = item.isSelected
            root.setOnClickListener {
                onSelectClick(item)
            }
        }

    }
}