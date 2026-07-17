package uddug.com.naukoteka.presentation.profile.edit

import android.annotation.SuppressLint
import io.reactivex.disposables.CompositeDisposable
import moxy.InjectViewState
import toothpick.InjectConstructor
import uddug.com.domain.entities.profile.ContactData
import uddug.com.domain.entities.profile.UserProfileFullInfo
import uddug.com.domain.interactors.user_profile.UserProfileInteractor
import uddug.com.naukoteka.global.base.BasePresenterImpl
import uddug.com.naukoteka.utils.text.isNotNullOrEmpty

@InjectConstructor
@InjectViewState
class ProfileEditContactsPresenter(
    private val userProfileInteractor: UserProfileInteractor
) : BasePresenterImpl<ProfileEditContactsView>() {

    companion object {
        private const val cTypeEmail = "31:3"
        private const val cTypePhone = "31:2"
        private const val cTypeSite = "31:7"
    }

    private val compositeDisposable = CompositeDisposable()

    var userProfileFullInfo: UserProfileFullInfo? = null
    var sites: MutableList<ContactData> = mutableListOf()
    var phones: MutableList<ContactData> = mutableListOf()
    var emails: MutableList<ContactData> = mutableListOf()

    fun setProfileFullInfo(profileFullInfo: UserProfileFullInfo) {
        this.userProfileFullInfo = profileFullInfo
        sites = profileFullInfo.contactDatum.filter { it.cType == cTypeSite }.toMutableList()
        phones = profileFullInfo.contactDatum.filter { it.cType == cTypePhone }.toMutableList()
        emails = profileFullInfo.contactDatum.filter { it.cType == cTypeEmail }.toMutableList()
        viewState.setMainEmail(emails.first().contact.orEmpty())
        viewState.setMainPhone(phones.first().contact.orEmpty())
        viewState.setMainSite(sites.first().contact.orEmpty())
        try {
            viewState.setAdditionalEmails(emails.take(emails.size - 2))
            viewState.setAdditionalPhones(phones.take(emails.size - 2))
            viewState.setAdditionalSites(sites.take(emails.size - 2))
        } catch (exception: Exception) {}
    }

    fun askToAddNewEmail() {
        val newContact = ContactData(cType = cTypeEmail)
        emails.add(newContact)
        viewState.addNewEmail(newContact)
    }

    fun askToAddNewPhone() {
        val newContact = ContactData(cType = cTypePhone)
        phones.add(newContact)
        viewState.addNewPhone(newContact)
    }

    fun askToAddNewSite() {
        val newContact = ContactData(cType = cTypeSite)
        sites.add(newContact)
        viewState.addNewSite(newContact)
    }

    fun askToSaveContacts() {
        val userId = userProfileFullInfo?.id.orEmpty()
        val toSave = mutableListOf<ContactData>().apply {
            addAll(sites.filter { it.contact.isNullOrEmpty() && it.id.isNullOrEmpty() })
            addAll(emails.filter { it.contact.isNullOrEmpty() && it.id.isNullOrEmpty() })
            addAll(phones.filter { it.contact.isNullOrEmpty() && it.id.isNullOrEmpty() })
        }
        val toUpdate = mutableListOf<ContactData>().apply {
            addAll(sites.filter { it.contact.isNullOrEmpty() && it.id.isNotNullOrEmpty() })
            addAll(emails.filter { it.contact.isNullOrEmpty() && it.id.isNotNullOrEmpty() })
            addAll(phones.filter { it.contact.isNullOrEmpty() && it.id.isNotNullOrEmpty() })
        }
        compositeDisposable.addAll(
            userProfileInteractor.saveContacts(userId, toSave).subscribe({ viewState.showDataUpdated() }, {}),
            userProfileInteractor.updateContacts(userId, toUpdate).subscribe({ viewState.showDataUpdated() }, {})
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.dispose()
    }
}
