package ru.usedesk.chat_gui.chat.offlineformselector

import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ru.usedesk.chat_gui.R
import ru.usedesk.common_gui.IUsedeskAdapter
import ru.usedesk.common_gui.UsedeskCommonFieldCheckBoxAdapter
import ru.usedesk.common_gui.inflateItem

internal class OfflineFormSelectorAdapter(
        recyclerView: RecyclerView,
        private val viewModel: OfflineFormSelectorViewModel,
        private val items: List<String>,
        var selectedIndex: Int
) : RecyclerView.Adapter<OfflineFormSelectorAdapter.ViewHolder>(), IUsedeskAdapter<OfflineFormSelectorViewModel> {

    init {
        onSelected(selectedIndex)
        recyclerView.run {
            layoutManager = LinearLayoutManager(recyclerView.context)
            adapter = this@OfflineFormSelectorAdapter
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(inflateItem(parent, R.layout.usedesk_item_field_checkbox,
                R.style.Usedesk_Common_Field_CheckBox) { rootView, defaultStyleId ->
            UsedeskCommonFieldCheckBoxAdapter.Binding(rootView, defaultStyleId)
        })
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount() = items.size

    private fun onSelected(index: Int) {
        if (index != selectedIndex) {
            val oldSelectedIndex = selectedIndex
            selectedIndex = index
            notifyItemChanged(oldSelectedIndex)
            notifyItemChanged(selectedIndex)
        }
    }

    override fun onLiveData(viewModel: OfflineFormSelectorViewModel,
                            lifecycleOwner: LifecycleOwner) {
        viewModel.selectedIndexLiveData.observe(lifecycleOwner) {
            onSelected(it)
        }
    }

    inner class ViewHolder(
            binding: UsedeskCommonFieldCheckBoxAdapter.Binding
    ) : RecyclerView.ViewHolder(binding.rootView) {

        private val adapter = UsedeskCommonFieldCheckBoxAdapter(binding)

        fun bind(index: Int) {
            adapter.setTitle(items[index])
            adapter.setChecked(index == selectedIndex)
            adapter.setOnClickListener {
                viewModel.onSelected(if (selectedIndex != index) {
                    index
                } else {
                    -1
                })
            }
        }
    }
}