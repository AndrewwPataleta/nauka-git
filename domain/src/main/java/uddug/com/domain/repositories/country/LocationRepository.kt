package uddug.com.domain.repositories.country


import io.reactivex.Single
import uddug.com.domain.entities.country.Country
import uddug.com.domain.entities.country.Settlement

interface LocationRepository {

    fun getCountries(): Single<List<Country>>

    fun findSettlementsByCountry(countryId: String, query: String): Single<List<Settlement>>

}
