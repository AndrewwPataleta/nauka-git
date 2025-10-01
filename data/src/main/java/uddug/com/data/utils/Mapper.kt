package uddug.com.data.utils

import uddug.com.data.services.models.response.user_profile.LegalAddressDto
import uddug.com.data.services.models.response.country.CountryDto
import uddug.com.data.services.models.response.country.SettlementDto
import uddug.com.data.services.models.response.user_profile.ActivityAreasDto
import uddug.com.data.services.models.response.user_profile.ActivityAreasMapDto
import uddug.com.data.services.models.response.user_profile.AdditionalPropDto
import uddug.com.data.services.models.response.user_profile.AddressDataDto
import uddug.com.data.services.models.response.user_profile.AddressesDto
import uddug.com.data.services.models.response.user_profile.AuthorsDto
import uddug.com.data.services.models.response.user_profile.ContactDataDto
import uddug.com.data.services.models.response.user_profile.ContactsDto
import uddug.com.data.services.models.response.user_profile.EducationDto
import uddug.com.data.services.models.response.user_profile.FeedStateDto
import uddug.com.data.services.models.response.user_profile.IdentSystemItemDto
import uddug.com.data.services.models.response.user_profile.IdentifiersDto
import uddug.com.data.services.models.response.user_profile.ImageDto
import uddug.com.data.services.models.response.user_profile.KeywordsMapDto
import uddug.com.data.services.models.response.user_profile.LaborActivitiesDto
import uddug.com.data.services.models.response.user_profile.LogoDto
import uddug.com.data.services.models.response.user_profile.MetaDto
import uddug.com.data.services.models.response.user_profile.ROrgItemDto
import uddug.com.data.services.models.response.user_profile.UserAcademicDegreesDto
import uddug.com.data.services.models.response.user_profile.UserProfileFullInfoDto
import uddug.com.data.services.models.response.user_profile.UserTitlesDto
import uddug.com.domain.entities.country.Country
import uddug.com.domain.entities.country.Settlement
import uddug.com.domain.entities.profile.ActivityAreas
import uddug.com.domain.entities.profile.ActivityAreasMap
import uddug.com.domain.entities.profile.AdditionalProp
import uddug.com.domain.entities.profile.AddressData
import uddug.com.domain.entities.profile.Addresses
import uddug.com.domain.entities.profile.Authors
import uddug.com.domain.entities.profile.ContactData
import uddug.com.domain.entities.profile.Contacts
import uddug.com.domain.entities.profile.Education
import uddug.com.domain.entities.profile.FeedState
import uddug.com.domain.entities.profile.IdentSystemItem
import uddug.com.domain.entities.profile.Identifiers
import uddug.com.domain.entities.profile.Image
import uddug.com.domain.entities.profile.KeywordsMap
import uddug.com.domain.entities.profile.LaborActivities
import uddug.com.domain.entities.profile.LegalAddress
import uddug.com.domain.entities.profile.Logo
import uddug.com.domain.entities.feed.Meta
import uddug.com.domain.entities.profile.ROrgItem
import uddug.com.domain.entities.profile.UserAcademicDegrees
import uddug.com.domain.entities.profile.UserProfileFullInfo
import uddug.com.domain.entities.profile.UserTitles

fun ImageDto.toDomain(): Image {
    return Image(
        path = this.path,
    )
}

fun MetaDto.toDomain(): Meta {
    return Meta(
        subscnCount = this.subscnCount,
        subscrCount = this.subscrCount
    )
}

fun AdditionalPropDto.toDomain(): AdditionalProp {
    return AdditionalProp(
        term = this.term,
        code = this.code,
    )
}

fun KeywordsMapDto.toDomain(): KeywordsMap {
    return KeywordsMap(
        additionalProp = this.additionalPropDto?.toDomain(),
        additionalProp2 = this.additionalPropDto2?.toDomain(),
        additionalProp3 = this.additionalPropDto3?.toDomain(),
    )
}

fun UserAcademicDegreesDto.toDomain(): UserAcademicDegrees {
    return UserAcademicDegrees(
        id = this.id,
        sd = this.sd,
        name = this.name,
        titleDate = this.titleDate,
        uref = this.uref
    )
}

fun ContactDataDto.toDomain(): ContactData {
    return ContactData(
        id = this.id,
        sd = this.sd,
        ed = this.ed,
        up = this.up,
        rObject = this.rObject,
        cType = this.cType,
        cForm = this.cForm,
        contact = this.contact,
        cIsIdentified = this.cIsIdentified,
        cLang = this.cLang,
        dsc = this.dsc,
        uref = this.uref
    )
}

fun LogoDto.toDomain(): Logo {
    return Logo(
        path = this.path
    )
}

fun AddressDataDto.toDomain(): AddressData {
    return AddressData(
        id = this.id,
        sd = this.sd,
        ed = this.ed,
        cType = this.toString(),
        postalcode = this.postalcode,
        ctFias = this.ctFias,
        room = this.room,
        notStucturedAddress = this.notStucturedAddress,
        dsc = this.dsc,
        cIsVerified = this.cIsVerified,
        rObject = this.rObject,
        cCountry = this.cCountry,
        cCity = this.cCity,
        city = this.city,
        cityAsString = this.cityAsString,
        uref = this.uref,
    )
}

fun LegalAddressDto.toDomain(): LegalAddress {
    return LegalAddress(
        id = this.id,
        sd = this.sd,
        ed = this.ed,
        cType = this.cType,
        postalcode = this.postalcode,
        ctFias = this.ctFias,
        room = this.room,
        notStucturedAddress = this.notStucturedAddress,
        dsc = this.dsc,
        cIsVerified = this.cIsVerified,
        rObject = this.rObject,
        cCountry = this.cCountry,
        cCity = this.cCity,
        city = this.city,
        cityAsString = this.cityAsString,
        uref = this.uref
    )
}

fun ActivityAreasDto.toDomain(): ActivityAreas {
    return ActivityAreas(
        id = this.id,
        sd = this.sd,
        ed = this.ed,
        status = this.status,
        cls = this.cls,
        parentNum = this.parentNum,
        parentClsNum = this.parentClsNum,
        type = this.type,
        num = this.num,
        lang = this.lang,
        code = this.code,
        term = this.term,
        dsc = this.dsc,
        uref = this.uref
    )
}

fun ROrgItemDto.toDomain(): ROrgItem {
    return ROrgItem(
        id = this.id,
        sd = this.sd,
        up = this.up,
        cType = this.cType,
        cStatus = this.cStatus,
        cForm = this.cForm,
        dsc = this.dsc,
        name = this.name,
        shortName = this.shortName,
        altName = this.altName,
        ogrn = this.ogrn,
        inn = this.inn,
        kpp = this.kpp,
        regDate = this.regDate,
        ctOkpo = this.ctOkpo,
        cIndustrySubmission = this.cIndustrySubmission,
        cIsVerified = this.cIsVerified,
        logo = this.logoDto?.toDomain(),
        addressDatum = this.addressDatumDtos.map {
            it.toDomain()
        },
        legalAddress = this.legalAddressDto?.toDomain(),
        uref = this.uref,
        activityArea = this.activityAreaDtos.map {
            it.toDomain()
        }
    )
}

fun EducationDto.toDomain(): Education {
    return Education(
        id = this.id,
        sd = this.sd,
        ed = this.ed,
        rUser = this.rUser,
        rOrg = this.rOrg,
        
        cType = this.cType,
        cLevel = this.cLevel,
        cLevelName = this.cLevelItem?.term,
        specialty = this.specialty,
        qualification = this.qualification,
        rCertifyingDocuments = this.rCertifyingDocuments,
        dsc = this.dsc,
        name = this.name,
        department = this.department,
        startDate = this.startDate,
        endDate = this.endDate,
        cCity = this.cCity,
        city = this.city,
        cityAsString = this.cityAsString,
        uref = uref,
        orgName = orgName,
        country = this.country?.toDomain(this.id),
    )
}


fun CountryDto.toDomain(addressId: String?): Country =
    Country(
        id = this.id.toString(),
        addressId = addressId,
        term = this.term.orEmpty(),
        isSelected = false,
        sd = this.sd,
        ed = this.ed,
        status = this.status,
        cls = this.cls,
        rObject = this.rObject,
        parentNum = this.parentNum,
        parentClsNum = this.parentClsNum,
        type = this.type,
        lang = this.lang,
        uref = this.uref,
        dsc = this.dsc,
        city = this.city
    )

fun SettlementDto.toDomain() = Settlement(
    city = this.city,
    level = this.level,
    region = this.region,
    socrname = this.socrname,
    territory = this.territory,
    uref = this.uref
)

fun UserTitlesDto.toDomain(): UserTitles {
    return UserTitles(
        id = this.id,
        sd = this.sd,
        ed = this.ed,
        cTitle = this.cTitle,
        rCertifyingDocuments = this.rCertifyingDocuments,
        titleDate = this.titleDate,
        uref = this.uref,
    )
}

fun ActivityAreasMapDto.toDomain(): ActivityAreasMap {
    return ActivityAreasMap(
        additionalProp1 = this.additionalProp1,
        additionalProp2 = this.additionalProp2,
        additionalProp3 = this.additionalProp3
    )
}

fun LaborActivitiesDto.toDomain(): LaborActivities {
    return LaborActivities(
        id = this.id,
        sd = this.sd,
        ed = this.ed,
        rOrg = this.rOrg,
        rOrgItem = this.rOrgItemDto?.toDomain(),
        orgName = this.orgName,
        cPosition = this.cPosition,
        position = this.position,
        startWork = this.startWork,
        endWork = this.endWork,
        dsc = this.dsc,
        cCountry = this.cCountry,
        cCity = this.cCity,
        city = this.city,
        cityAsString = this.cityAsString,
        activityAreasMap = this.activityAreasMapDto,
        uref = this.uref,
        country = this.country?.toDomain(null),
        cActivityAreas = this.cActivityAreas.map {
            it
        }
    )
}

fun ContactsDto.toDomain(): Contacts {
    return Contacts(
        id = this.id,
        sd = this.sd,
        ed = this.ed,
        up = this.up,
        rObject = this.rObject,
        cType = this.cType,
        cForm = this.cForm,
        contact = this.contact,
        cIsIdentified = this.cIsIdentified,
        cLang = this.cLang,
        dsc = this.dsc,
        uref = this.uref
    )
}

fun IdentifiersDto.toDomain(): Identifiers {
    return Identifiers(
        id = this.id,
        sd = this.sd,
        ed = this.ed,
        cIdentSystem = this.cIdentSystem,
        rObject = this.rObject,
        cIdentSystemItem = this.cIdentSystemItem?.toDomain(this.identifier.orEmpty()),
        identifier = this.identifier,
        uref = this.uref
    )
}

fun IdentSystemItemDto.toDomain(identifier: String): IdentSystemItem {
    return IdentSystemItem(
        id = this.id,
        sd = this.sd,
        ed = this.ed,
        cIdentSystem = this.cIdentSystem,
        rObject = this.rObject,
        identifier = identifier,
        uref = this.uref
    )
}

fun AuthorsDto.toDomain(): Authors {
    return Authors(
        id = this.id,
        sd = this.sd,
        ed = this.ed,
        lastName = lastName,
        firstName = firstName,
        middleName = this.middleName,
        fioShort = this.fioShort,
        engFioShort = this.engFioShort,
        dsc = this.dsc,
        engDsc = this.engDsc,
        contacts = this.contacts.map {
            it.toDomain()
        },
        uref = this.uref,
        identifiers = this.identifiers.map {
            it.toDomain()
        }
    )
}

fun AddressesDto.toDomain(rObject: String) = Addresses(
    id = this.id,
    sd = this.sd,
    ed = this.ed,
    cType = this.cType,
    postalcode = this.postalcode,
    ctFias = this.ctFias,
    room = this.room,
    notStucturedAddress = this.notStucturedAddress,
    dsc = this.dsc,
    country = this.country?.toDomain(this.id),
    cIsVerified = this.cIsVerified,
    rObject = rObject,
    cCountry = this.cCountry,
    cCity = this.cCity,
    city = this.city,
    cityAsString = this.cityAsString,
    uref = this.uref,
)

fun FeedStateDto.toDomain(): FeedState {
    return FeedState(
        id = this.id,
        subscribed = this.subscribed
    )
}

fun UserProfileFullInfoDto.toDomain(): UserProfileFullInfo {
    return UserProfileFullInfo(
        id = this.id,
        firstName = this.firstName,
        lastName = this.lastName,
        fullName = this.fullName,
        middleName = this.middleName,
        image = this.imageDto?.toDomain(),
        dsc = this.dsc,
        meta = this.metaDto?.toDomain(),
        firstNameEng = this.firstNameEng,
        lastNameEng = this.lastNameEng,
        nickname = this.nickname,
        email = this.email,
        birthDate = this.birthDate,
        gender = this.gender,
        phone = this.phone,
        phone2 = this.phone2,
        phone3 = this.phone3,
        bannerUrl = this.bannerUrl,
        keywords = this.keywords,
        placeOfResidence = this.placeOfResidence,
        keywordsMap = this.keywordsMapDto?.toDomain(),
        userAcademicDegree = this.userAcademicDegreeDtos.map {
            it.toDomain()
        },
        contactDatum = this.contactDatumDtos.map {
            it.toDomain()
        },
        education = educationDto.map {
            it.toDomain()
        },
        userTitle = this.userTitleDtos.map {
            it.toDomain()
        },
        laborActivity = this.laborActivityDtos.map {
            it.toDomain()
        },
        authors = this.authors.map {
            it.toDomain()
        },
        addresses = this.addresses.map { it.toDomain(this.uref.orEmpty()) },
        feedState = this.feedStateDto?.toDomain(),
        settings = this.settings
    )
}
