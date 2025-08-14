package uddug.com.naukoteka.presentation.profile.edit.adapter

import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.core.view.isVisible
import uddug.com.domain.entities.profile.Education
import uddug.com.naukoteka.R
import uddug.com.naukoteka.databinding.ItemEducationMiddleBinding
import uddug.com.naukoteka.global.base.BaseAdapter
import uddug.com.naukoteka.global.base.BaseViewHolder
import uddug.com.naukoteka.utils.text.isNotNullOrEmpty
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class EducationAdapter(
    private val onDeleteClick: (Education) -> Unit,
    private val onDetailClick: (Education) -> Unit
) :
    BaseAdapter<Education, EducationAdapter.ViewHolder>() {

    companion object {

        val dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    }

    override fun newViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(R.layout.item_education_middle, parent)

    inner class ViewHolder(@LayoutRes layoutRes: Int, parent: ViewGroup) :
        BaseViewHolder<Education>(layoutRes, parent) {

        private val rootView: ItemEducationMiddleBinding
            get() = ItemEducationMiddleBinding.bind(
                itemView
            )

        override fun updateView(item: Education) {
            rootView.root.setOnClickListener {
                onDetailClick(item)
            }
            rootView.removeEducation.setOnClickListener {
                onDeleteClick(item)
            }
            rootView.title.text = item.name ?: item.specialty
            rootView.subTitle.text = item.cityAsString.toString().plus(
                if (item.startDate != null) {
                    " • ${LocalDate.parse(item.startDate, dateFormat).year}"
                } else ""
            ).plus(
                if (item.endDate != null) {
                    " – ${
                        LocalDate.parse(item.endDate, dateFormat).year
                    } "
                } else ""
            )
            if (item.cLevelName.isNotNullOrEmpty()) {
                rootView.subTitleDirection.text = item.cLevelName
                rootView.subTitleDirection.isVisible = true
            }

        }
    }
}