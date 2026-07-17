package uddug.com.naukoteka.ui.fragments.profile.settings

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import uddug.com.domain.entities.profile.BlockUser
import uddug.com.naukoteka.R
import uddug.com.naukoteka.databinding.ItemBlockedUserBinding

class UserBlockListAdapter(
    private val onSelectClick: (BlockUser) -> Unit,
) : ListAdapter<BlockUser, UserBlockListAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemBlockedUserBinding.bind(
            LayoutInflater.from(parent.context).inflate(R.layout.item_blocked_user, parent, false)
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: MutableList<Any>) {
        onBindViewHolder(holder, position)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(val binding: ItemBlockedUserBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: BlockUser) = with(binding) {}
    }

    private class DiffCallback : DiffUtil.ItemCallback<BlockUser>() {
        override fun areItemsTheSame(oldItem: BlockUser, newItem: BlockUser): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: BlockUser, newItem: BlockUser): Boolean =
            oldItem == newItem
    }
}
