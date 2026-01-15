package uk.gov.justice.digital.hmpps.accommodationdatadomainapi.domain.event

import java.util.UUID

data class ProposedAccommodationApprovedEvent(
  override val aggregateId: UUID,
  override val type: AccommodationDataDomainEventType = AccommodationDataDomainEventType.PROPOSED_ACCOMMODATION_APPROVED,
) : AccommodationDataDomainEvent

data class ProposedAccommodationUnapprovedEvent(
  override val aggregateId: UUID,
  override val type: AccommodationDataDomainEventType = AccommodationDataDomainEventType.PROPOSED_ACCOMMODATION_UNAPPROVED,
) : AccommodationDataDomainEvent
