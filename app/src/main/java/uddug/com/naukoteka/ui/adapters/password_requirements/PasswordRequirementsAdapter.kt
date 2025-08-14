package uddug.com.naukoteka.ui.adapters.password_requirements

import android.view.ViewGroup
import androidx.annotation.LayoutRes
import uddug.com.domain.entities.PasswordRequirementsEntity
import uddug.com.naukoteka.R
import uddug.com.naukoteka.databinding.ItemPasswordRequirementsBinding
import uddug.com.naukoteka.global.base.BaseAdapter
import uddug.com.naukoteka.global.base.BaseViewHolder

class PasswordRequirementsAdapter :
    BaseAdapter<PasswordRequirementsEntity, PasswordRequirementsAdapter.ViewHolder>() {

    override fun newViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(R.layout.item_password_requirements, parent)

    inner class ViewHolder(@LayoutRes layoutRes: Int, parent: ViewGroup) :
        BaseViewHolder<PasswordRequirementsEntity>(layoutRes, parent) {

        private val rootView: ItemPasswordRequirementsBinding
            get() = ItemPasswordRequirementsBinding.bind(
                itemView
            )

        override fun updateView(item: PasswordRequirementsEntity) {
            rootView.run {
                item.run {
                    tvPasswordRequirements.text = getContext().getString(passwordRequirement)
                }
            }
        }
    }
}