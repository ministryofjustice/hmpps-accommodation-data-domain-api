package uk.gov.justice.digital.hmpps.accommodationdatadomainapi.infrastructure.client.corepersonrecord

import java.util.UUID

data class CorePersonRecordAddress(
  val id: UUID,
  val crn: String,
  val address: String
)
