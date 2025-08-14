package uddug.com.naukoteka.presentation.profile.edit.adapter

import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.core.view.isVisible
import uddug.com.domain.entities.profile.LaborActivities
import uddug.com.naukoteka.R
import uddug.com.naukoteka.databinding.ItemCarrerBinding
import uddug.com.naukoteka.global.base.BaseAdapter
import uddug.com.naukoteka.global.base.BaseViewHolder
import uddug.com.naukoteka.utils.text.isNotNullOrEmpty
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class CarrierAdapter(
    private val onDeleteClick: (LaborActivities) -> Unit,
    private val onDetailClick: (LaborActivities) -> Unit
) :
    BaseAdapter<LaborActivities, CarrierAdapter.ViewHolder>() {

    companion object {
        val dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    }

    override fun newViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(R.layout.item_carrer, parent)

    inner class ViewHolder(@LayoutRes layoutRes: Int, parent: ViewGroup) :
        BaseViewHolder<LaborActivities>(layoutRes, parent) {

        private val rootView: ItemCarrerBinding
            get() = ItemCarrerBinding.bind(
                itemView
            )

        override fun updateView(item: LaborActivities) {
            rootView.root.setOnClickListener {
                onDetailClick(item)
            }
            rootView.remove.setOnClickListener {
                onDeleteClick(item)
            }
            rootView.title.text = item.position
            rootView.titleDesc.text = item.orgName
            rootView.subTitle.text = item.activityAreasMap.values.joinToString(
                separator = rootView.root.context.getString(
                    R.string.comma
                )
            )
            rootView.subTitleDirection.text = item.cityAsString.toString().plus(
                if (item.startWork != null) {
                    " • ${LocalDate.parse(item.startWork, dateFormat).year}"
                } else ""
            ).plus(
                if (item.endWork != null) {
                    " – ${
                        LocalDate.parse(item.endWork, dateFormat).year
                    } "
                } else ""
            )

        }
    }
}