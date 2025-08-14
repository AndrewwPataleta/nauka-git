package uddug.com.naukoteka.global.base

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import java.util.*


abstract class BaseAdapter<T, VH : BaseViewHolder<T>> : RecyclerView.Adapter<VH> {

    private var items: MutableList<T>? = ArrayList()
    internal var onItemClickListener: ((T, position: Int, view: View) -> Unit?)? = null

    protected abstract fun newViewHolder(parent: ViewGroup, viewType: Int): VH


    constructor()

    constructor(items: List<T>) {
        this.items = items.toMutableList()
    }

    fun getItem(position: Int): T? {
        return if (position < 0 || position >= itemCount) null else items?.get(position)
    }

    fun getItems(): List<T>? {
        return items?.toList()
    }

    open fun setItems(items: List<T>) {
        this.items = items.toMutableList()
        notifyDataSetChanged()
    }

    open fun removeItem(position: Int) {
        if (position >= 0) {
            items?.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    fun removeItem(item: T) {
        getItems()?.indexOf(item)?.let { position -> removeItem(position) }
    }

    fun addItems(items: List<T>) {
        if (this.items == null)
            setItems(items)
        else {
            this.items?.addAll(items)
            notifyDataSetChanged()
        }
    }

    fun addItem(item: T) {
        if (this.items == null)
            this.items = ArrayList()
        items?.add(item)
        notifyItemInserted(itemCount - 1)
    }

    fun clear() {
        if (items != null) {
            items?.clear()
            notifyDataSetChanged()
        }
    }

    fun updateItem(position: Int, newItem: T): Boolean {
        if (position < 0 || position >= itemCount) return false
        items?.set(position, newItem)
        notifyItemChanged(position)
        return true
    }

    open fun updateItem(newItem: T): Boolean {
        val position = getItems()?.indexOf(newItem)
        return position?.let {
            if (position < 0 || position >= itemCount) return false
            items?.set(position, newItem)
            notifyItemChanged(position)
            true
        } ?: false
    }

    fun updateItems(newItems: List<T>) {
        newItems.onEach { updateItem(it) }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val holder = newViewHolder(parent, viewType)
        onItemClickListener?.let { listener ->
            holder.itemView.setOnClickListener { view ->
                val position = holder.adapterPosition
                items?.let {
                    if (checkCorrectPosition(position))
                        getItem(position)?.let { listener.invoke(it, position, view) }
                }

            }
        }
        return holder
    }

    fun checkCorrectPosition(position: Int): Boolean =
        (0 <= position && position < items?.size ?: 0)

    override fun onBindViewHolder(holder: VH, position: Int, payloads: MutableList<Any>) {
        super.onBindViewHolder(holder, position, payloads)
        val item = getItems()?.get(position)
        item?.let { holder.updateView(it); holder.updateView(it) }
    }


    override fun getItemCount(): Int {
        items?.run {
            return size
        }
        return 0
    }


    fun setOnItemClickListener(onItemClickListener: (T, position: Int, view: View) -> Unit?) {
        this.onItemClickListener = onItemClickListener
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = getItems()?.get(position)
        item?.let { holder.updateView(it); holder.updateView(it) }
    }


}