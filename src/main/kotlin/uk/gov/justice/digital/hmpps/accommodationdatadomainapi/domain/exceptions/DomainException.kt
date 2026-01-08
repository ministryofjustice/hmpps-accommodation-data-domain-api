package uk.gov.justice.digital.hmpps.accommodationdatadomainapi.domain.exceptions

sealed class DomainException(message: String) : RuntimeException(message)
