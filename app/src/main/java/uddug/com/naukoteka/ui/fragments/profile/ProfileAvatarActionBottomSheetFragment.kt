package uddug.com.naukoteka.ui.fragments.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import uddug.com.naukoteka.R
import uddug.com.naukoteka.databinding.FragmentProfileAdditionalActionBinding
import uddug.com.naukoteka.utils.ui.setDsMaxHeight
import uddug.com.naukoteka.utils.viewBinding

class ProfileAvatarActionBottomSheetFragment : BottomSheetDialogFragment() {

    private val binding: FragmentProfileAdditionalActionBinding by viewBinding(
        FragmentProfileAdditionalActionBinding::bind
    )

    override fun getTheme(): Int = R.style.NauDSBottomSheetDialogTheme

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_profile_avatar_action, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?) =
        BottomSheetDialog(requireContext(), theme)

}
