package ru.usedesk.chat_gui.chat.offlineform

import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ru.usedesk.chat_gui.R
import ru.usedesk.chat_gui.chat.offlineform._entity.OfflineFormItem
import ru.usedesk.chat_gui.chat.offlineform._entity.OfflineFormList
import ru.usedesk.chat_gui.chat.offlineform._entity.OfflineFormText
import ru.usedesk.common_gui.UsedeskCommonFieldListAdapter
import ru.usedesk.common_gui.UsedeskCommonFieldTextAdapter
import ru.usedesk.common_gui.inflateItem

internal class OfflineFormFieldsAdapter(
    recyclerView: RecyclerView,
    binding: OfflineFormPage.Binding,
    private val viewModel: OfflineFormViewModel,
    lifecycleOwner: LifecycleOwner,
    private val onSubjectClick: (items: List<String>, selectedIndex: Int) -> Unit
) : RecyclerView.Adapter<OfflineFormFieldsAdapter.BaseViewHolder>() {

    private val textFieldStyle =
        binding.styleValues.getStyle(R.attr.usedesk_chat_screen_offline_form_text_field)
    private val listFieldStyle =
        binding.styleValues.getStyle(R.attr.usedesk_chat_screen_offline_form_list_field)

    private var items: List<OfflineFormItem> = listOf()

    init {
        recyclerView.run {
            adapter = this@OfflineFormFieldsAdapter
            layoutManager = LinearLayoutManager(recyclerView.context)
            itemAnimator = null
        }
        viewModel.fieldsLiveData.observe(lifecycleOwner) {
            it?.let {
                val oldItems = items
                items = it
                if (oldItems.isEmpty()) {
                    notifyDataSetChanged()
                } else {
                    oldItems.forEachIndexed { index, offlineFormItem ->
                        if (offlineFormItem is OfflineFormList &&
                            offlineFormItem != items[index]
                        ) {
                            notifyItemChanged(index)
                        }
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return when (viewType) {
            TYPE_TEXT -> TextViewHolder(inflateItem(parent,
                    R.layout.usedesk_item_field_text,
                    textFieldStyle) { rootView, defaultStyleId ->
                UsedeskCommonFieldTextAdapter.Binding(rootView, defaultStyleId)
            })
            TYPE_LIST -> ListViewHolder(inflateItem(parent,
                    R.layout.usedesk_item_field_list,
                    listFieldStyle) { rootView, defaultStyleId ->
                UsedeskCommonFieldListAdapter.Binding(rootView, defaultStyleId)
            })
            else -> throw RuntimeException("Unknown list type")
        }
    }

    override fun onBindViewHolder(holderText: BaseViewHolder, position: Int) {
        holderText.bind(position)
    }

    override fun getItemCount() = items.size

    override fun getItemViewType(position: Int): Int {
        return if (position == 2) {
            TYPE_LIST
        } else {
            TYPE_TEXT
        }
    }

    inner class TextViewHolder(
            binding: UsedeskCommonFieldTextAdapter.Binding
    ) : BaseViewHolder(binding.rootView) {
        private val adapter = UsedeskCommonFieldTextAdapter(binding)

        override fun bind(index: Int) {
            (items[index] as OfflineFormText).let { item ->
                adapter.setTitle(item.title, item.required)
                adapter.setText(item.text)
                adapter.setTextChangeListener {
                    viewModel.onTextFieldChanged(index, it)
                }
            }
        }
    }

    inner class ListViewHolder(
            binding: UsedeskCommonFieldListAdapter.Binding
    ) : BaseViewHolder(binding.rootView) {
        private val adapter = UsedeskCommonFieldListAdapter(binding)

        override fun bind(index: Int) {
            (items[index] as OfflineFormList).let {
                adapter.setTitle(it.title, it.required)
                adapter.setText(it.items.getOrNull(it.selected))
                adapter.setOnClickListener {
                    onSubjectClick(it.items, it.selected)
                }
            }
        }
    }

    abstract inner class BaseViewHolder(rootView: View) : RecyclerView.ViewHolder(rootView) {

        abstract fun bind(index: Int)
    }

    companion object {
        const val TYPE_TEXT = 1
        const val TYPE_LIST = 2
    }
}