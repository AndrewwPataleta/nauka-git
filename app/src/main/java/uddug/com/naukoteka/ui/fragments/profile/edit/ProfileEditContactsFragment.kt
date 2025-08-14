package uddug.com.naukoteka.ui.fragments.profile.edit

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.navigation.fragment.findNavController
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import uddug.com.domain.entities.profile.ContactData
import uddug.com.domain.entities.profile.UserProfileFullInfo
import uddug.com.naukoteka.R
import uddug.com.naukoteka.databinding.FragmentProfileEditContactsBinding
import uddug.com.naukoteka.global.base.BaseFragment
import uddug.com.naukoteka.utils.viewBinding
import uddug.com.naukoteka.databinding.FragmentProfileEditIdBinding
import uddug.com.naukoteka.databinding.FragmentProfileEditPersonalIdsBinding
import uddug.com.naukoteka.presentation.profile.edit.ProfileEditContactsPresenter
import uddug.com.naukoteka.presentation.profile.edit.ProfileEditContactsView
import uddug.com.naukoteka.presentation.profile.edit.ProfileEditIdPresenter
import uddug.com.naukoteka.presentation.profile.edit.ProfileEditIdView
import uddug.com.naukoteka.presentation.profile.edit.ProfileEditPersonalIdsPresenter
import uddug.com.naukoteka.presentation.profile.edit.ProfileEditPersonalIdsView
import uddug.com.naukoteka.presentation.profile.navigation.ContainerNavigationView
import uddug.com.naukoteka.ui.activities.main.ContainerActivity
import uddug.com.naukoteka.ui.custom.AcademicDegreeEditView
import uddug.com.naukoteka.ui.custom.AdditionalEmailEditView
import uddug.com.naukoteka.ui.custom.AdditionalPhoneEditView
import uddug.com.naukoteka.ui.custom.AdditionalSiteEditView

class ProfileEditContactsFragment : BaseFragment(R.layout.fragment_profile_edit_contacts),
    ProfileEditContactsView {

    companion object {

    }

    override val contentView by viewBinding(FragmentProfileEditContactsBinding::bind)

    private var navigationView: ContainerNavigationView? = null

    @InjectPresenter
    lateinit var presenter: ProfileEditContactsPresenter


    @ProvidePresenter
    fun providePresenter(): ProfileEditContactsPresenter {
        return getScope().getInstance(ProfileEditContactsPresenter::class.java)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        navigationView = requireActivity() as ContainerNavigationView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.getParcelable<UserProfileFullInfo>(ContainerActivity.PROFILE_ARGS)
            ?.let { presenter.setProfileFullInfo(it) }

        contentView.back.setOnClickListener {
            findNavController().popBackStack()
        }
        contentView.addNewEmail.setOnClickListener {
            presenter.askToAddNewEmail()
        }
        contentView.addNewPhone.setOnClickListener {
            presenter.askToAddNewPhone()
        }
        contentView.addNewSite.setOnClickListener {
            presenter.askToAddNewSite()
        }
        contentView.done.setOnClickListener {
            presenter.askToSaveContacts()
        }
    }

    override fun onResume() {
        super.onResume()
        navigationView?.showNavigationBottomBar(false)
    }

    override fun setMainInformation(profileInfo: UserProfileFullInfo) {

    }

    override fun setMainEmail(email: String) {
        contentView.mainEmail.setText(email)
    }

    override fun setMainPhone(phone: String) {
        contentView.mainPhoneNumber.setText(phone)
    }

    override fun setMainSite(site: String) {
        contentView.mainWebSite.setText(site)
    }

    override fun setAdditionalSites(sites: List<ContactData>) {
        contentView.additionalSiteContainer.removeAllViews()
        sites.forEach { site ->
            AdditionalSiteEditView(requireContext()).attachAdditionalSite(
                contentView.additionalSiteContainer,
                site
            )
        }
    }

    override fun setAdditionalPhones(phones: List<ContactData>) {
        contentView.additionalPhoneContainer.removeAllViews()
        phones.forEach { phone ->
            AdditionalPhoneEditView(requireContext()).attachAdditionalPhone(
                contentView.additionalPhoneContainer,
                phone
            )
        }
    }

    override fun setAdditionalEmails(email: List<ContactData>) {
        contentView.additionalEmailContainer.removeAllViews()
        email.forEach { email ->
            AdditionalEmailEditView(requireContext()).attachAdditionalEmail(
                contentView.additionalEmailContainer,
                email
            )
        }
    }

    override fun addNewEmail(contactDatum: ContactData) {
        AdditionalEmailEditView(requireContext()).attachAdditionalEmail(
            contentView.additionalEmailContainer,
            contactDatum
        )
    }

    override fun addNewSite(contactDatum: ContactData) {
        AdditionalSiteEditView(requireContext()).attachAdditionalSite(
            contentView.additionalSiteContainer,
            contactDatum
        )
    }

    override fun addNewPhone(contactDatum: ContactData) {
        AdditionalPhoneEditView(requireContext()).attachAdditionalPhone(
            contentView.additionalPhoneContainer,
            contactDatum
        )
    }

    override fun showDataUpdated() {
        Toast.makeText(
            requireActivity(),
            getString(R.string.contact_data_updated_succesfull),
            Toast.LENGTH_LONG
        ).show()
    }

}
