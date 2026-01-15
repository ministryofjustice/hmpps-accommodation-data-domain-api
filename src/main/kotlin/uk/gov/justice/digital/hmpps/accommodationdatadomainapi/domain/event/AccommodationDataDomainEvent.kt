package uk.gov.justice.digital.hmpps.accommodationdatadomainapi.domain.event

import java.util.UUID

interface AccommodationDataDomainEvent {
  val aggregateId: UUID
  val type: AccommodationDataDomainEventType
}

enum class AccommodationDataDomainEventType {
  PROPOSED_ACCOMMODATION_APPROVED,
  PROPOSED_ACCOMMODATION_UNAPPROVED,
  ;

  companion object {
    fun from(eventType: String): AccommodationDataDomainEventType? = entries.find { it.name == eventType }
  }
}
