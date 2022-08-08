package ru.usedesk.chat_gui.chat.offlineformselector

import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ru.usedesk.chat_gui.R
import ru.usedesk.chat_gui.chat.offlineform.OfflineFormViewModel
import ru.usedesk.chat_gui.chat.offlineform._entity.OfflineFormList
import ru.usedesk.common_gui.UsedeskCommonFieldCheckBoxAdapter
import ru.usedesk.common_gui.inflateItem

internal class OfflineFormSelectorAdapter(
    private val key: String,
    recyclerView: RecyclerView,
    binding: OfflineFormSelectorPage.Binding,
    private val viewModel: OfflineFormViewModel,
    lifecycleOwner: LifecycleOwner
) : RecyclerView.Adapter<OfflineFormSelectorAdapter.ViewHolder>() {

    private val itemStyle =
        binding.styleValues.getStyle(R.attr.usedesk_chat_screen_offline_form_selector_checkbox)

    private var items = listOf<String>()
    private var selected = ""

    init {
        recyclerView.run {
            layoutManager = LinearLayoutManager(recyclerView.context)
            adapter = this@OfflineFormSelectorAdapter
        }
        viewModel.modelLiveData.initAndObserveWithOld(lifecycleOwner) { old, new ->
            val oldField = old?.customFields?.firstOrNull { it.key == key }
            val newField = new.customFields.first { it.key == key } as OfflineFormList
            if (oldField != newField) {
                val oldItems = items
                val newItems = newField.items

                val oldSelected = selected
                val newSelected = newField.items[newField.selected]

                items = newItems
                selected = newSelected

                DiffUtil.calculateDiff(object : DiffUtil.Callback() {
                    override fun getOldListSize() = oldItems.size

                    override fun getNewListSize() = newItems.size

                    override fun areItemsTheSame(
                        oldItemPosition: Int,
                        newItemPosition: Int
                    ): Boolean {
                        val oldItem = oldItems[oldItemPosition]
                        val newItem = newItems[newItemPosition]
                        return oldItem == newItem
                    }

                    override fun areContentsTheSame(
                        oldItemPosition: Int,
                        newItemPosition: Int
                    ): Boolean {
                        val oldItem = oldItems[oldItemPosition]
                        val newItem = newItems[newItemPosition]
                        return oldSelected == newSelected
                                || (oldItem != oldSelected && newItem != newSelected)
                    }
                }).dispatchUpdatesTo(this)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(inflateItem(
        parent,
        R.layout.usedesk_item_field_checkbox,
        itemStyle
    ) { rootView, defaultStyleId ->
        UsedeskCommonFieldCheckBoxAdapter.Binding(rootView, defaultStyleId)
    })

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

    inner class ViewHolder(
        binding: UsedeskCommonFieldCheckBoxAdapter.Binding
    ) : RecyclerView.ViewHolder(binding.rootView) {

        private val adapter = UsedeskCommonFieldCheckBoxAdapter(binding)

        fun bind(item: String) {
            adapter.run {
                setTitle(item)
                setChecked(item == selected)
                setOnClickListener { viewModel.onListFieldChanged(key, items.indexOf(item)) }
            }
        }
    }
}