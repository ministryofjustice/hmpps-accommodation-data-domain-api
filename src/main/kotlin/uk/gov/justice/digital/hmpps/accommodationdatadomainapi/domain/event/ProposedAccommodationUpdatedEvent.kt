package uk.gov.justice.digital.hmpps.accommodationdatadomainapi.domain.event

import java.util.UUID

data class ProposedAccommodationUpdatedEvent(
  val accommodationId: UUID,
  var approved: Boolean,
)