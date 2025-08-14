package uddug.com.naukoteka.ui.fragments.profile.settings

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import uddug.com.domain.entities.profile.BlockUser
import uddug.com.domain.entities.profile.Education
import uddug.com.naukoteka.R
import uddug.com.naukoteka.databinding.ItemBlockedUserBinding
import uddug.com.naukoteka.utils.text.isNotNullOrEmpty

class UserBlockListAdapter(
    private val onSelectClick: (BlockUser) -> Unit,
) :
    ListAdapter<BlockUser, UserBlockListAdapter.ViewHolder>(
        object : DiffUtil.ItemCallback<BlockUser>() {
            override fun areItemsTheSame(
                oldItem: BlockUser,
                newItem: BlockUser
            ): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(
                oldItem: BlockUser,
                newItem: BlockUser
            ): Boolean {
                return oldItem == newItem
            }
        }
    ) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemBlockedUserBinding.bind(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_blocked_user, parent, false)
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: MutableList<Any>) {
        onBindViewHolder(holder, position)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(val binding: ItemBlockedUserBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: BlockUser) = with(binding) {
        }

    }
}