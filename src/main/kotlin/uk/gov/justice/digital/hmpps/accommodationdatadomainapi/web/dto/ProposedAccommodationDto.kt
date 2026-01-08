package uk.gov.justice.digital.hmpps.accommodationdatadomainapi.web.dto

import java.util.UUID

data class ProposedAccommodationDto(
  val id: UUID,
  val address: String,
  val approved: Boolean?,
)
