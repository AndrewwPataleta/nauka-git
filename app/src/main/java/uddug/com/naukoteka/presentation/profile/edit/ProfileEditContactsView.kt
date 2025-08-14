package uddug.com.naukoteka.presentation.profile.edit

import uddug.com.naukoteka.global.views.InformativeView
import uddug.com.naukoteka.global.views.LoadingView
import moxy.MvpView
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.StateStrategyType
import uddug.com.domain.entities.country.Country
import uddug.com.domain.entities.country.Settlement
import uddug.com.domain.entities.profile.ContactData
import uddug.com.domain.entities.profile.UserProfileFullInfo

@StateStrategyType(AddToEndSingleStrategy::class)
interface ProfileEditContactsView : MvpView, LoadingView, InformativeView {
    fun setMainInformation(profileInfo: UserProfileFullInfo)

    fun setMainEmail(email: String)
    fun setMainPhone(email: String)
    fun setMainSite(email: String)

    fun setAdditionalSites(sites: List<ContactData>)
    fun setAdditionalPhones(phones: List<ContactData>)
    fun setAdditionalEmails(email: List<ContactData>)

    fun addNewEmail(contactDatum: ContactData)
    fun addNewSite(contactDatum: ContactData)
    fun addNewPhone(contactDatum: ContactData)
    fun showDataUpdated()
}
