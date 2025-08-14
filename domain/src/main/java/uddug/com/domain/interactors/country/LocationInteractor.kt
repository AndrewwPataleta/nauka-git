package uddug.com.domain.interactors.country

import io.reactivex.Single
import uddug.com.domain.SchedulersProvider
import toothpick.InjectConstructor
import uddug.com.domain.entities.country.Settlement
import uddug.com.domain.repositories.country.LocationRepository

@InjectConstructor
class LocationInteractor(
    private val locationRepository: LocationRepository,
    private val schedulers: SchedulersProvider,
) {

    fun getCountries() =
        locationRepository.getCountries()

    fun findSettlementsByCountry(countryId: String, query: String): Single<List<Settlement>> {
        return locationRepository.findSettlementsByCountry(countryId = countryId, query = query)
    }

    fun findSchool(countryId: String, query: String): Single<List<Settlement>> {
        return locationRepository.findSettlementsByCountry(countryId = countryId, query = query)
    }
}
