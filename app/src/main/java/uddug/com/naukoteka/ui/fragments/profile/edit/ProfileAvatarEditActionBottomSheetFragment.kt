package uddug.com.naukoteka.ui.fragments.profile.edit

import android.Manifest
import android.Manifest.permission.READ_MEDIA_IMAGES
import android.Manifest.permission.READ_MEDIA_VIDEO
import android.Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.appcompat.app.AppCompatActivity.RESULT_OK
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.FileProvider
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResult
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.permissionx.guolindev.PermissionX
import com.yalantis.ucrop.UCrop
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import uddug.com.domain.entities.profile.UserProfileFullInfo
import uddug.com.naukoteka.R
import uddug.com.naukoteka.databinding.FragmentProfileAvatarEditBinding
import uddug.com.naukoteka.global.base.BaseDialogFragment
import uddug.com.naukoteka.presentation.profile.navigation.ContainerView
import uddug.com.naukoteka.presentation.profile.ProfileAvatarActionPresenter
import uddug.com.naukoteka.presentation.profile.ProfilePhotoActionView
import uddug.com.naukoteka.utils.URIPathHelper
import uddug.com.naukoteka.utils.viewBinding
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date

class ProfileAvatarEditActionBottomSheetFragment : BaseDialogFragment(), ProfilePhotoActionView {

    override fun getTheme(): Int = R.style.NauDSBottomSheetDialogTheme
    override val contentView: FragmentProfileAvatarEditBinding by viewBinding(
        FragmentProfileAvatarEditBinding::bind
    )
    private var uploadChooseDialog: AlertDialog? = null

    companion object {
        const val GALLERY_CODE = 102
        private const val CAMERA_CODE = 555
        const val PROFILE_FULL_INFO_ARGS = "PROFILE_FULL_INFO_ARGS"
        const val DELETE_AVATAR_RESULT = "DELETE_AVATAR_RESULT"
        const val UPLOAD_AVATAR_RESULT = "UPLOAD_AVATAR_RESULT"
        private const val RESULT_OF_DELETE = "RESULT_OF_DELETE"
        private const val RESULT_OF_UPLOAD = "RESULT_OF_UPLOAD"
        private const val IMAGE_TYPE_CHANGE = "IMAGE_TYPE_CHANGE"

        fun newInstance(
            profileFullInfo: UserProfileFullInfo,
            imageTypeChange: ProfileAvatarActionPresenter.IMAGE_TYPE_AVATAR
        ): ProfileAvatarEditActionBottomSheetFragment {
            return ProfileAvatarEditActionBottomSheetFragment().apply {
                arguments = bundleOf(
                    PROFILE_FULL_INFO_ARGS to profileFullInfo,
                    IMAGE_TYPE_CHANGE to imageTypeChange.type
                )
            }
        }
    }

    @InjectPresenter
    lateinit var presenter: ProfileAvatarActionPresenter

    @ProvidePresenter
    fun providePresenter(): ProfileAvatarActionPresenter =
        getScope().getInstance(ProfileAvatarActionPresenter::class.java)

    private var containerNavigation: ContainerView? = null
    private var currentPhotoFile: File? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        containerNavigation = requireActivity() as ContainerView
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_profile_avatar_edit, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.getParcelable<UserProfileFullInfo>(PROFILE_FULL_INFO_ARGS)
            ?.let { presenter.setProfileFullInfo(it) }
        arguments?.getString(IMAGE_TYPE_CHANGE)
            ?.let { presenter.setCurrentChangeType(it) }
        contentView.openPhotoContainer.setOnClickListener {
            dismiss()
            presenter.selectShowProfileImage()
        }
        contentView.editPhotoContainer.setOnClickListener { showUploadPhotoDialog() }
        contentView.deletePhotoContainer.setOnClickListener { presenter.chooseDeletePhoto() }
    }

    private fun showUploadPhotoDialog() {
        val mediaPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            arrayOf(READ_MEDIA_IMAGES, READ_MEDIA_VIDEO, READ_MEDIA_VISUAL_USER_SELECTED)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO)
        } else {
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA
            )
        }
        val cameraPermissions = arrayOf(Manifest.permission.CAMERA)
        val dialogView = layoutInflater.inflate(
            R.layout.view_custom_alert_dialog_photo,
            ConstraintLayout(requireContext())
        )
        val builder = AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog)
            .setCancelable(true)
            .setView(dialogView)
        dialogView.findViewById<View>(R.id.openPhotoContainer).setOnClickListener {
            PermissionX.init(requireActivity()).permissions(*mediaPermissions).request { allGranted, _, _ ->
                if (allGranted) {
                    uploadChooseDialog?.dismiss()
                    ImagePicker.with(this)
                        .galleryOnly()
                        .galleryMimeTypes(arrayOf("image/png", "image/jpeg", "image/jpg"))
                        .start(GALLERY_CODE)
                }
            }
        }
        dialogView.findViewById<View>(R.id.takePhotoContainer).setOnClickListener {
            PermissionX.init(requireActivity()).permissions(*cameraPermissions).request { allGranted, _, _ ->
                if (allGranted) {
                    val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    currentPhotoFile = try {
                        createImageFileInDownloads()
                    } catch (ex: IOException) {
                        null
                    }
                    currentPhotoFile?.let { file ->
                        val photoUri = FileProvider.getUriForFile(
                            requireContext(),
                            "com.nauchat.fileprovider",
                            file
                        )
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                        takePictureIntent.addFlags(
                            Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                        )
                        val chooserIntent = Intent.createChooser(takePictureIntent, getString(R.string.take_photo_profile))
                        uploadChooseDialog?.dismiss()
                        startActivityForResult(chooserIntent, CAMERA_CODE)
                    }
                }
            }
        }
        uploadChooseDialog = builder.show()
    }

    @Throws(IOException::class)
    private fun createImageFileInDownloads(): File {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        return File(downloadsDir, "FILE_$timestamp")
    }

    override fun onCreateDialog(savedInstanceState: Bundle?) =
        BottomSheetDialog(requireContext(), theme)

    override fun openProfilePhotoImageView(userProfileFullInfo: UserProfileFullInfo) {
        containerNavigation?.openPhotoView(userProfileFullInfo)
    }

    override fun openProfileBannerImageView(userProfileFullInfo: UserProfileFullInfo) {
        containerNavigation?.openBannerView(userProfileFullInfo)
    }

    override fun showDeletePhotoDialog() {
        Dialog(requireActivity(), R.style.Theme_Dialog).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            setCancelable(true)
            setCanceledOnTouchOutside(true)
            setContentView(R.layout.dialog_remove_photo)
            findViewById<View>(R.id.cancelDeleteBtn)?.setOnClickListener { dismiss() }
            findViewById<View>(R.id.deleteConfirmBtn)?.setOnClickListener {
                dismiss()
                presenter.confirmDeletePhoto()
            }
        }.show()
    }

    override fun successfulDeleteAvatar() {
        setFragmentResult(DELETE_AVATAR_RESULT, bundleOf(RESULT_OF_DELETE to true))
        uploadChooseDialog?.dismiss()
        dismiss()
    }

    override fun successfulUpload() {
        setFragmentResult(UPLOAD_AVATAR_RESULT, bundleOf(RESULT_OF_UPLOAD to true))
        uploadChooseDialog?.dismiss()
        dismiss()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                UCrop.REQUEST_CROP -> handleCropResult(data)
                CAMERA_CODE -> handleCameraResult()
                GALLERY_CODE -> handleGalleryResult(data)
            }
        }
    }

    private fun handleCropResult(data: Intent?) {
        data?.let {
            if (currentPhotoFile != null) {
                presenter.uploadUserImage(currentPhotoFile!!)
                currentPhotoFile = null
            } else {
                val outputUri = UCrop.getOutput(it)
                outputUri?.let { uri ->
                    val docId = DocumentsContract.getDocumentId(uri)
                    if (docId.startsWith("msf:")) {
                        val file = try { createImageFileInDownloads() } catch (ex: IOException) { null }
                        file?.let { destFile ->
                            try {
                                context?.contentResolver?.openInputStream(uri).use { inputStream ->
                                    FileOutputStream(destFile).use { output ->
                                        val buffer = ByteArray(4 * 1024)
                                        var read: Int
                                        while ((inputStream?.read(buffer).also { read = it!! }) != -1) {
                                            output.write(buffer, 0, read)
                                        }
                                        output.flush()
                                        presenter.uploadUserImage(destFile)
                                    }
                                }
                            } catch (ex: IOException) {
                                ex.printStackTrace()
                            }
                        }
                    } else {
                        val path = URIPathHelper().getPath(requireActivity(), uri)
                        presenter.uploadUserImage(File(path))
                    }
                }
            }
        }
    }

    private fun handleCameraResult() {
        currentPhotoFile?.let {
            UCrop.of(Uri.fromFile(it), Uri.fromFile(it))
                .withAspectRatio(16f, 9f)
                .start(requireContext(), this)
        }
    }

    private fun handleGalleryResult(data: Intent?) {
        data?.data?.let { uri ->
            UCrop.of(uri, uri)
                .withAspectRatio(16f, 9f)
                .start(requireContext(), this)
        }
    }

    override fun setOpenPhotoAvailable(available: Boolean) {
        contentView.openPhotoContainer.isVisible = available
        contentView.deletePhotoContainer.isVisible = available
    }
}
