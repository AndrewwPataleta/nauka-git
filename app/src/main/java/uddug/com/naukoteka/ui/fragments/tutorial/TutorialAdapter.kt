package uddug.com.naukoteka.ui.fragments.tutorial

import android.view.ViewGroup
import androidx.annotation.LayoutRes
import uddug.com.domain.entities.TutorialEntity
import uddug.com.naukoteka.R
import uddug.com.naukoteka.databinding.ListItemPageBinding
import uddug.com.naukoteka.global.base.BaseAdapter
import uddug.com.naukoteka.global.base.BaseViewHolder

class TutorialAdapter : BaseAdapter<TutorialEntity, TutorialAdapter.ViewHolder>() {

    override fun newViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(R.layout.list_item_page, parent)

    inner class ViewHolder(@LayoutRes layoutRes: Int, parent: ViewGroup) :
        BaseViewHolder<TutorialEntity>(layoutRes, parent) {

        private val rootView: ListItemPageBinding get() = ListItemPageBinding.bind(itemView)

        override fun updateView(item: TutorialEntity) {
            rootView.run {
                item.run {
                    tvTitle.text = getContext().getString(title)
                    ivImage.setImageResource(image)
                }
            }
        }
    }
}

