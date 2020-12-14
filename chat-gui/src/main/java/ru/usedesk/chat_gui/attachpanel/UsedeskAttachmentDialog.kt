package ru.usedesk.chat_gui.attachpanel

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.bottomsheet.BottomSheetDialog
import ru.usedesk.chat_gui.R
import ru.usedesk.chat_gui.chat.UsedeskChatScreen
import ru.usedesk.chat_gui.databinding.UsedeskDialogAttachmentBinding
import ru.usedesk.chat_sdk.entity.UsedeskFileInfo
import ru.usedesk.common_gui.UsedeskPermissionUtil
import ru.usedesk.common_gui.UsedeskStyleManager
import ru.usedesk.common_gui.inflateItem
import ru.usedesk.common_sdk.FileUtil
import java.io.File


class UsedeskAttachmentDialog(
        private val screen: UsedeskChatScreen
) : BottomSheetDialog(screen.requireContext(), UsedeskStyleManager.getStyle(R.style.Usedesk_Chat_Screen_Chat_Attachment_Dialog)) {

    init {
        val container = screen.view as ViewGroup

        inflateItem<UsedeskDialogAttachmentBinding>(layoutInflater,
                container,
                R.layout.usedesk_dialog_attachment,
                UsedeskStyleManager.getStyle(R.style.Usedesk_Chat_Screen_Chat_Attachment_Dialog)).apply {

            setContentView(root)

            pickPhotoButton.setOnClickListener {
                dismiss()
                UsedeskPermissionUtil.needReadExternalPermission(container,
                        R.string.need_permission,
                        R.string.settings
                ) {
                    pickImage(screen)
                }
            }

            takePhotoButton.setOnClickListener {
                dismiss()
                UsedeskPermissionUtil.needCameraPermission(container,
                        R.string.need_permission,
                        R.string.settings
                ) {
                    takePhoto(screen)
                }
            }

            pickDocumentButton.setOnClickListener {
                dismiss()
                UsedeskPermissionUtil.needReadExternalPermission(container,
                        R.string.need_permission,
                        R.string.settings
                ) {
                    pickDocument(screen)
                }
            }
        }
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            val attachedFiles = onResult(screen.requireContext(), requestCode, data)
            if (attachedFiles != null) {
                screen.setAttachedFiles(attachedFiles)
            }
        }
    }

    private fun pickFile(fragment: Fragment, mimeType: String) {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            type = mimeType
        }
        fragment.startActivityForResult(intent, REQUEST_CODE_PICK_FILE)
    }

    private fun pickImage(fragment: Fragment) {
        pickFile(fragment, MIME_TYPE_ALL_IMAGES)
    }

    private fun pickDocument(fragment: Fragment) {
        pickFile(fragment, MIME_TYPE_ALL_DOCS)
    }

    private fun takePhoto(fragment: Fragment) {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_OUTPUT, getTakePhotoUri(fragment.requireContext()))
        }
        fragment.startActivityForResult(cameraIntent, REQUEST_CODE_TAKE_PHOTO)
    }

    private fun onResult(context: Context, requestCode: Int, data: Intent?): List<UsedeskFileInfo>? {
        when (requestCode) {
            REQUEST_CODE_PICK_FILE -> {
                if (data != null) {
                    return getUsedeskFileInfoList(context, data)
                }
            }
            REQUEST_CODE_TAKE_PHOTO -> {
                return getUsedeskFileInfo(context, getTakePhotoUri(context))
            }
        }
        return null
    }

    private fun getTakePhotoUri(context: Context): Uri {
        return Uri.fromFile(File(context.externalCacheDir, "camera.jpg"))
    }

    private fun getUriList(data: Intent): List<Uri> {
        val uri = data.data //single file
        val clipData = data.clipData //list of files
        if (clipData != null) {
            return (0 until clipData.itemCount).mapNotNull { i ->
                clipData.getItemAt(i).uri
            }
        } else if (uri != null) {
            return listOf(uri)
        }
        return listOf()
    }

    private fun getUsedeskFileInfoList(context: Context, data: Intent): List<UsedeskFileInfo> {
        return getUriList(data).map {
            createUsedeskFileInfo(context, it)
        }
    }

    private fun getUsedeskFileInfo(context: Context, uri: Uri): List<UsedeskFileInfo> {
        return listOf(createUsedeskFileInfo(context, uri))
    }

    private fun createUsedeskFileInfo(context: Context, uri: Uri): UsedeskFileInfo {
        val mimeType = FileUtil.getMimeType(context, uri)
        val name = FileUtil.getFileName(context, uri)
        return UsedeskFileInfo(uri, mimeType ?: "", name)
    }

    companion object {
        private const val REQUEST_CODE_PICK_FILE = 38141
        private const val REQUEST_CODE_TAKE_PHOTO = 38142
        private const val MIME_TYPE_ALL_IMAGES = "image/*"
        private const val MIME_TYPE_ALL_DOCS = "*/*"
    }
}