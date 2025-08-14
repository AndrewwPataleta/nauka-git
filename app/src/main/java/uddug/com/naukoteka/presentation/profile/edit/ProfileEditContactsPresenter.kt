package uddug.com.naukoteka.presentation.profile.edit

import android.annotation.SuppressLint
import io.reactivex.Completable
import io.reactivex.CompletableSource
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import moxy.InjectViewState
import toothpick.InjectConstructor
import uddug.com.domain.entities.profile.ContactData
import uddug.com.domain.entities.profile.UserProfileFullInfo
import uddug.com.domain.interactors.user_profile.UserProfileInteractor
import uddug.com.naukoteka.global.base.BasePresenterImpl
import uddug.com.naukoteka.utils.text.isNotNullOrEmpty
import java.util.concurrent.TimeUnit


@InjectConstructor
@InjectViewState
class ProfileEditContactsPresenter(
    private val userProfileInteractor: UserProfileInteractor
) : BasePresenterImpl<ProfileEditContactsView>() {

    companion object {
        private const val errorTag = "ProfileEditPresenterError"
        private const val cTypeEmail = "31:3"
        private const val cTypePhones = "31:2"
        private const val cTypeSites = "31:7"
    }

    var compositeDisposable: CompositeDisposable = CompositeDisposable()

    var userProfileFullInfo: UserProfileFullInfo? = null
    var sites: MutableList<ContactData> = mutableListOf()
    var phones: MutableList<ContactData> = mutableListOf()
    var emails: MutableList<ContactData> = mutableListOf()

    fun setProfileFullInfo(profileFullInfo: UserProfileFullInfo) {
        this.userProfileFullInfo = profileFullInfo
        sites = profileFullInfo.contactDatum.filter { it.cType == cTypeSites }.toMutableList()
        phones = profileFullInfo.contactDatum.filter { it.cType == cTypePhones }.toMutableList()
        emails = profileFullInfo.contactDatum.filter { it.cType == cTypeEmail }.toMutableList()
        viewState.setMainEmail(emails.first().contact.orEmpty())
        viewState.setMainPhone(phones.first().contact.orEmpty())
        viewState.setMainSite(sites.first().contact.orEmpty())

        try {
            viewState.setAdditionalEmails(emails.take(emails.size - 2))
            viewState.setAdditionalPhones(phones.take(emails.size - 2))
            viewState.setAdditionalSites(sites.take(emails.size - 2))
        } catch (exception: Exception) {

        }

    }

    fun askToAddNewEmail() {
        val newContactData = ContactData(
            cType = cTypeEmail
        )
        emails.add(newContactData)
        viewState.addNewEmail(
            newContactData
        )
    }

    fun askToAddNewPhone() {
        val newContactData = ContactData(
            cType = cTypePhones
        )
        phones.add(newContactData)
        viewState.addNewPhone(newContactData)
    }

    fun askToAddNewSite() {
        val newContactData = ContactData(
            cType = cTypeSites
        )
        sites.add(newContactData)
        viewState.addNewSite(newContactData)
    }

    fun askToSaveContacts() {
        val allContactsToSave: MutableList<ContactData> = mutableListOf()
        allContactsToSave.addAll(sites.filter { it.contact.isNullOrEmpty() && it.id.isNullOrEmpty() })
        allContactsToSave.addAll(emails.filter { it.contact.isNullOrEmpty() && it.id.isNullOrEmpty() })
        allContactsToSave.addAll(phones.filter { it.contact.isNullOrEmpty() && it.id.isNullOrEmpty() })
        val allContactsToUpdate: MutableList<ContactData> = mutableListOf()
        allContactsToUpdate.addAll(sites.filter { it.contact.isNullOrEmpty() && it.id.isNotNullOrEmpty() })
        allContactsToUpdate.addAll(emails.filter { it.contact.isNullOrEmpty() && it.id.isNotNullOrEmpty() })
        allContactsToUpdate.addAll(phones.filter { it.contact.isNullOrEmpty() && it.id.isNotNullOrEmpty() })
        compositeDisposable.addAll(
            userProfileInteractor.saveContacts(
                userProfileFullInfo?.id.orEmpty(),
                allContactsToSave
            ).subscribe({
                viewState.showDataUpdated()
            }, {

            }),
            userProfileInteractor.updateContacts(
                userProfileFullInfo?.id.orEmpty(),
                allContactsToUpdate
            ).subscribe({
                viewState.showDataUpdated()
            }, {

            })
        )

    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.dispose()
    }

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
    }


}
