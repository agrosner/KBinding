package com.andrewgrosner.kbinding.sample.widget

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.support.annotation.LayoutRes
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.andrewgrosner.kbinding.sample.R
import java.util.*

typealias OnItemClickListener<T> = (Int, T, BaseViewHolder<*>) -> Unit
typealias OnHFItemClickListener = (Int, BaseViewHolder<*>) -> Unit

/**
 * Description: The base adapter that consolidates logic here.
 */
@Suppress("unused")
abstract class BaseRecyclerViewAdapter<TModel, VH : BaseViewHolder<TModel>>
    : RecyclerView.Adapter<BaseViewHolder<*>>() {

    val headerHolders = ArrayList<BaseViewHolder<*>>()
    private val headerLayoutIds = ArrayList<Int>()
    val footerHolders = ArrayList<BaseViewHolder<*>>()
    private val footerLayoutIds = ArrayList<Int>()

    private var mainHandler: Handler? = null

    private var itemsList: MutableList<TModel>? = null

    var onItemClickListener: OnItemClickListener<TModel>? = null
        set(onItemClickListener) {
            field = onItemClickListener
            notifyDataSetChanged()
        }
    private var onHeaderClickListener: OnHFItemClickListener? = null
    private var onFooterClickListener: OnHFItemClickListener? = null

    fun setItemsList(itemsList: List<TModel>?) {
        setItemsListDirect(itemsList?.toMutableList() ?: mutableListOf<TModel>())
    }

    /**
     * Doesn't copy the incoming list.
     */
    fun setItemsListDirect(itemsList: MutableList<TModel>?) {
        synchronized(this) {
            this.itemsList = itemsList ?: ArrayList<TModel>()
            if (Looper.myLooper() != Looper.getMainLooper()) {
                mainHandler?.post { notifyItemsListChanged() }
            } else {
                notifyItemsListChanged()
            }
        }
    }

    fun addItemsList(itemsList: List<TModel>?) = addItemsList(itemsListCount, itemsList)

    fun addItemsList(location: Int, itemsList: List<TModel>?) {
        if (itemsList != null) {
            synchronized(this) {
                val adjustedLocation = location + headersCount

                val count = itemsList.size
                if (this.itemsList == null) {
                    this.itemsList = ArrayList<TModel>()
                }
                this.itemsList?.addAll(location, itemsList)

                if (Looper.myLooper() != Looper.getMainLooper()) {
                    mainHandler?.post { notifyItemsListAdded(adjustedLocation, count) }
                } else {
                    notifyItemsListAdded(adjustedLocation, count)
                }
            }
        }
    }

    fun removeItemsList(location: Int, count: Int) {
        val itemsList = getItemsList()
        if (itemsList != null) {
            synchronized(this) {
                val removeList = (0..count - 1).map { itemsList[location + it] }
                itemsList.removeAll(removeList)
                if (Looper.myLooper() != Looper.getMainLooper()) {
                    mainHandler?.post { notifyItemsListRemoved(location, count) }
                } else {
                    notifyItemsListRemoved(location, count)
                }
            }
        }
    }

    fun setOnHeaderClickListener(onHeaderClickListener: OnHFItemClickListener) {
        this.onHeaderClickListener = onHeaderClickListener
    }

    fun setOnFooterClickListener(onFooterClickListener: OnHFItemClickListener) {
        this.onFooterClickListener = onFooterClickListener
    }

    /**
     * Clears existing headers out.
     */
    fun clearHeaders() {
        val count = headerHolders.size
        headerHolders.clear()
        notifyItemRangeRemoved(0, count)
    }

    fun addHeaderView(layoutResId: Int, parent: ViewGroup) {
        addHeaderHolder(layoutResId, NoBindingViewHolder<Any>(inflateView(parent, layoutResId)))
    }

    fun addHeaderHolder(layoutResId: Int, baseViewHolder: BaseViewHolder<*>) {
        preventReuseOfIdsFrom(footerLayoutIds, layoutResId, "footer")
        headerHolders.add(baseViewHolder)
        headerLayoutIds.add(layoutResId)
        notifyItemInserted(headersCount - 1)
    }

    fun addFooterHolder(footerResId: Int, baseViewHolder: BaseViewHolder<*>) {
        preventReuseOfIdsFrom(headerLayoutIds, footerResId, "header")
        footerHolders.add(baseViewHolder)
        footerLayoutIds.add(footerResId)
        notifyItemInserted(footerStartIndex + footersCount)
    }

    fun addFooterView(footerResId: Int, parent: ViewGroup) {
        addFooterHolder(footerResId, NoBindingViewHolder<Any>(inflateView(parent, footerResId)))
    }

    protected fun notifyItemsListAdded(location: Int, itemsCount: Int) = notifyItemRangeInserted(location, itemsCount)

    protected fun notifyItemsListRemoved(location: Int, itemsCount: Int) = notifyItemRangeRemoved(location, itemsCount)

    protected fun notifyItemsListChanged() = notifyDataSetChanged()

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        mainHandler = Handler(Looper.getMainLooper())
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        mainHandler = null
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<*> {
        var viewHolder: BaseViewHolder<*>? = headerViewHolderForViewType(viewType)
        if (viewHolder != null) {
            if (onHeaderClickListener != null) {
                val finalViewHolder = viewHolder
                viewHolder.itemView.setOnClickListener {
                    val position = getViewHolderPosition(finalViewHolder)
                    onHeaderClickListener?.invoke(position, finalViewHolder)
                }
            }
        } else {
            viewHolder = footerHolderForViewType(viewType)
            if (viewHolder != null) {
                if (onFooterClickListener != null) {
                    val finalViewHolder1 = viewHolder
                    viewHolder.itemView.setOnClickListener {
                        val position = getViewHolderPosition(finalViewHolder1) - footerStartIndex - 1
                        onFooterClickListener?.invoke(position, finalViewHolder1)
                    }
                }
            } else {
                viewHolder = onCreateItemViewHolder(parent, viewType)

                if (this.onItemClickListener != null) {
                    val finalViewHolder2 = viewHolder
                    setItemClickListener(viewHolder, View.OnClickListener {
                        val position = getAdjustedItemPosition(getViewHolderPosition(finalViewHolder2))
                        onItemPositionClicked(finalViewHolder2, position)
                    })
                }
            }
        }
        if (viewHolder.itemView.parent === parent) {
            // The viewHolder wasn't fully recycled - RecyclerView.Recycler might choke if we're not careful
            parent.removeView(viewHolder.itemView)
        }
        return viewHolder
    }

    override fun onBindViewHolder(holder: BaseViewHolder<*>, position: Int) {
        if (position < headersCount) {
            onBindItemHeaderViewHolder(holder, position)
        } else if (position > footerStartIndex) {
            onBindItemFooterViewHolder(holder, position - footerStartIndex - 1)
        } else {
            val adjusted = getAdjustedItemPosition(position)
            onBindViewHolder(holder as VH, getItem(adjusted), adjusted)
        }
        // workaround for adapters not bound by the containing RecyclerView.
        holder.itemView.setTag(R.id.tag_position, position)
    }

    fun getItemOrNull(position: Int): TModel? {
        if (position < 0 || position >= itemsListCount) {
            return null
        } else {
            return if (itemsList != null) itemsList!![position] else null
        }
    }

    fun getItem(position: Int) = getItemOrNull(position) ?: throw ArrayIndexOutOfBoundsException("Could not locate item at $position")

    override fun getItemCount(): Int {
        return headersCount + fullItemsCount + footersCount
    }

    private val fullItemsCount: Int
        get() = itemsListCount + extraItemsCount

    val itemsListCount: Int
        get() = if (itemsList == null) 0 else itemsList!!.size

    protected val extraItemsCount: Int
        get() = 0

    override fun getItemViewType(position: Int): Int {
        val viewType: Int
        if (position < headersCount) {
            viewType = headerLayoutIds[position]
        } else if (position > footerStartIndex && footersCount > 0) {
            viewType = footerLayoutIds[position - footerStartIndex - 1]
        } else {
            viewType = getViewType(position - headersCount)
        }
        return viewType
    }

    fun itemIndexOf(model: TModel) = getItemsList()?.indexOf(model) ?: -1

    val headersCount: Int
        get() = headerHolders.size

    val footersCount: Int
        get() = footerHolders.size

    fun isHeaderPosition(rawPosition: Int) = rawPosition < headersCount

    fun isFooterPosition(rawPosition: Int) = rawPosition > footerStartIndex

    fun getItemsList() = itemsList

    /**
     * Variant of [.getItemViewType] with the `position`
     * parameter corrected to account for header items.
     * Subclasses are strongly recommended to return the layout resource Id of the view instead
     * to ensure types never clash.
     *

     * @param position index into the item list of the item whose ViewHolder type is
     * *                 desired
     * *
     * @return that type
     */
    protected fun getViewType(position: Int) = 0

    protected fun setItemClickListener(viewHolder: BaseViewHolder<*>, onClickListener: View.OnClickListener) {
        viewHolder.itemView.setOnClickListener(onClickListener)
    }

    protected fun onBindItemFooterViewHolder(holder: BaseViewHolder<*>, footerPosition: Int) {}

    protected fun onBindItemHeaderViewHolder(holder: BaseViewHolder<*>, headerPosition: Int) {}

    /**
     * Subclasses should implement this to create ViewHolders for the main set of items.
     * This does not cover any headers or footers that may be present in the layout.

     * @param parent   the RecyclerView itself
     * *
     * @param viewType what type of view to create
     * *
     * @return a new 'item' ViewHolder
     */
    protected abstract fun onCreateItemViewHolder(parent: ViewGroup, viewType: Int): VH

    protected abstract fun onBindViewHolder(holder: VH, item: TModel, position: Int)

    /**
     * @return The starting index of footer views (if any exist)
     */
    val footerStartIndex: Int
        get() = headersCount + fullItemsCount - 1

    protected fun isPositionItem(rawPosition: Int) = rawPosition in headersCount..footerStartIndex

    protected fun getAdjustedItemPosition(rawPosition: Int) = rawPosition - headersCount

    protected fun getRawPosition(itemPosition: Int) = itemPosition + headersCount

    protected fun onItemPositionClicked(viewHolder: BaseViewHolder<*>, position: Int) {
        val item = getItemOrNull(position)
        if (this.onItemClickListener != null && item != null) {
            this.onItemClickListener?.invoke(position, item, viewHolder)
        }
    }

    private fun getViewHolderPosition(viewHolder: BaseViewHolder<*>): Int {
        var position = viewHolder.adapterPosition
        // no contained recyclerview, but this might be part of a LinearLayout or BaseAdapter
        if (position == RecyclerView.NO_POSITION) {
            position = viewHolder.layoutPosition
            if (position == RecyclerView.NO_POSITION) {
                val tagPosition = viewHolder.itemView.getTag(R.id.tag_position) as Int
                if (tagPosition != null) {
                    position = tagPosition
                }
            }
        }
        return position
    }

    private fun headerViewHolderForViewType(viewType: Int): BaseViewHolder<*>? {
        for (i in headerLayoutIds.indices) {
            val resId = headerLayoutIds[i]
            if (viewType == resId) {
                return headerHolders[i]
            }
        }
        return null
    }

    private fun footerHolderForViewType(viewType: Int): BaseViewHolder<*>? {
        for (i in footerLayoutIds.indices) {
            val resId = footerLayoutIds[i]
            if (viewType == resId) {
                return footerHolders[i]
            }
        }
        return null
    }

    fun clear() {
        itemsList?.clear()
        notifyItemsListChanged()
    }

    companion object {

        fun inflateView(parent: ViewGroup, @LayoutRes layoutResId: Int): View {
            return LayoutInflater.from(parent.context).inflate(layoutResId, parent, false)
        }

        fun inflateView(context: Context, @LayoutRes layoutResId: Int): View {
            return LayoutInflater.from(context).inflate(layoutResId, null)
        }

        /**
         * To preempt that, this method throws an IllegalArgumentException as soon
         * as a ViewHolder with such a [.getViewType] is proffered to
         * [.addFooterHolder] or
         * [.addHeaderHolder], which should make
         * early detection and diagnosis of this problem much more feasible.
         *

         * @param forbiddenIds  a list of ids already in use. Typically [.headerLayoutIds]
         * *                      or [.footerLayoutIds]
         * *
         * @param id            the suggested id
         * *
         * @param forbiddenType a name for the type of ids - this will appear in the exception
         * *                      message
         */
        protected fun preventReuseOfIdsFrom(
                forbiddenIds: List<Int>, id: Int, forbiddenType: String) {
            if (forbiddenIds.contains(id)) {
                throw IllegalArgumentException(
                        "The layout id $id is already registered to a $forbiddenType."
                )
            }
        }
    }
}
