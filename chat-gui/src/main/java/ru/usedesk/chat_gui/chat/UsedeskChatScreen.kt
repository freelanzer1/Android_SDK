package ru.usedesk.chat_gui.chat

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import ru.usedesk.chat_gui.IUsedeskOnAttachmentClickListener
import ru.usedesk.chat_gui.IUsedeskOnFileClickListener
import ru.usedesk.chat_gui.IUsedeskOnUrlClickListener
import ru.usedesk.chat_gui.R
import ru.usedesk.chat_gui.chat.adapters.MessagePanelAdapter
import ru.usedesk.chat_gui.chat.adapters.MessagesAdapter
import ru.usedesk.chat_gui.chat.adapters.OfflineFormAdapter
import ru.usedesk.chat_sdk.UsedeskChatSdk
import ru.usedesk.chat_sdk.entity.UsedeskFileInfo
import ru.usedesk.common_gui.*

class UsedeskChatScreen : UsedeskFragment() {

    private val viewModel: ChatViewModel by viewModels()

    private lateinit var binding: Binding
    private lateinit var attachment: UsedeskAttachmentDialog

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        doInit {
            val agentName: String? = argsGetString(AGENT_NAME_KEY)

            binding = inflateItem(inflater,
                    container,
                    R.layout.usedesk_screen_chat,
                    R.style.Usedesk_Chat_Screen) { rootView, defaultStyleId ->
                Binding(rootView, defaultStyleId)
            }

            val title = UsedeskResourceManager.getStyleValues(
                    requireContext(),
                    R.style.Usedesk_Chat_Screen
            ).getString(R.attr.usedesk_chat_screen_title_text)

            init(agentName)
            UsedeskToolbarAdapter(requireActivity() as AppCompatActivity, binding.toolbar).apply {
                setTitle(title)
                setBackButton {
                    requireActivity().onBackPressed()
                }
            }
        }

        return binding.rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        attachment = UsedeskAttachmentDialog.create(this)
    }

    private fun init(agentName: String?) {
        UsedeskChatSdk.init(requireContext(), viewModel.actionListenerRx)

        MessagePanelAdapter(binding.messagePanel, viewModel, viewLifecycleOwner) {
            openAttachmentDialog()
        }

        viewModel.chatStateLiveData.observe(viewLifecycleOwner) {
            onChatState(it)
        }

        MessagesAdapter(viewModel,
                binding.rvMessages,
                agentName,
                viewLifecycleOwner,
                { file ->
                    getParentListener<IUsedeskOnFileClickListener>()?.onFileClick(file)
                },
                { url ->
                    getParentListener<IUsedeskOnUrlClickListener>()?.onUrlClick(url)
                            ?: onUrlClick(url)
                })
        viewModel.exceptionLiveData.observe(viewLifecycleOwner) {
            onException(it)
        }
        OfflineFormAdapter(binding.offlineForm,
                viewModel,
                viewLifecycleOwner,
                {
                    UsedeskOfflineFormSuccessDialog.newInstance(binding.rootView).apply {
                        setOnDismissListener {
                            requireActivity().onBackPressed()
                        }
                    }.show()
                },
                {
                    showSnackbarError(binding, R.attr.usedesk_chat_screen_offline_form_failed_to_send_text)
                })

        viewModel.init()
    }

    private fun onChatState(chatState: ChatViewModel.ChatState?) {
        when (chatState) {
            ChatViewModel.ChatState.LOADING -> {
                showChatViews(loading = true)
            }
            ChatViewModel.ChatState.CHAT -> {
                showChatViews(messages = true)
            }
            ChatViewModel.ChatState.OFFLINE_FORM -> {
                showChatViews(offlineForm = true)
                showKeyboard(binding.offlineForm.etMessage)
            }
        }
    }

    private fun showChatViews(loading: Boolean = false,
                              messages: Boolean = false,
                              offlineForm: Boolean = false) {
        binding.tvLoading.visibility = visibleGone(loading)
        binding.rvMessages.visibility = visibleGone(messages)
        binding.messagePanel.rootView.visibility = visibleGone(messages)
        binding.offlineForm.rootView.visibility = visibleGone(offlineForm)
    }

    private fun onUrlClick(url: String) {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        })
    }

    private fun openAttachmentDialog() {
        getParentListener<IUsedeskOnAttachmentClickListener>()?.onAttachmentClick()
                ?: attachment.show()
    }

    private fun onException(exception: Exception) {
        exception.printStackTrace()
    }

    override fun onStart() {
        super.onStart()
        UsedeskChatSdk.stopService(requireContext())
    }

    override fun onStop() {
        super.onStop()
        UsedeskChatSdk.startService(requireContext())
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        attachment.onActivityResult(requestCode, resultCode, data)
    }

    fun setAttachedFiles(attachedFiles: List<UsedeskFileInfo>) {
        viewModel.setAttachedFiles(attachedFiles)
    }

    companion object {
        private const val AGENT_NAME_KEY = "agentNameKey"

        @JvmOverloads
        @JvmStatic
        fun newInstance(agentName: String? = null): UsedeskChatScreen {
            return UsedeskChatScreen().apply {
                arguments = Bundle().apply {
                    if (agentName != null) {
                        putString(AGENT_NAME_KEY, agentName)
                    }
                }
            }
        }
    }

    internal class Binding(rootView: View, defaultStyleId: Int) : UsedeskBinding(rootView, defaultStyleId) {
        val toolbar = UsedeskToolbarAdapter.Binding(rootView.findViewById(R.id.toolbar), defaultStyleId)
        val offlineForm = OfflineFormAdapter.Binding(rootView.findViewById(R.id.l_offline_form), defaultStyleId)
        val messagePanel = MessagePanelAdapter.Binding(rootView.findViewById(R.id.l_message_panel), defaultStyleId)

        val tvLoading: TextView = rootView.findViewById(R.id.tv_loading)
        val rvMessages: RecyclerView = rootView.findViewById(R.id.rv_messages)
    }
}