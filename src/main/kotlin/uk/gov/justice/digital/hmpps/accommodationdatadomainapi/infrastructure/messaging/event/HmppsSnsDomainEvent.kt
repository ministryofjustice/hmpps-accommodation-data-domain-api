package uk.gov.justice.digital.hmpps.accommodationdatadomainapi.infrastructure.messaging.event

import java.time.OffsetDateTime

data class HmppsSnsDomainEvent(
  val eventType: String,
  val version: Int,
  val description: String? = null,
  val detailUrl: String? = null,
  val occurredAt: OffsetDateTime,
)
