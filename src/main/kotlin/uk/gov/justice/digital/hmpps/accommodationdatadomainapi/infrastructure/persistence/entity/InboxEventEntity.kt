package uk.gov.justice.digital.hmpps.accommodationdatadomainapi.infrastructure.persistence.entity

import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "inbox_event")
class InboxEventEntity(
  @Id
  val id: UUID,
  val eventType: String,
  val eventDetailUrl: String?,
  val eventOccurredAt: OffsetDateTime,
  val createdAt: Instant,
  @Enumerated(EnumType.STRING)
  var processedStatus: ProcessedStatus,
  var processedAt: Instant?,
)
