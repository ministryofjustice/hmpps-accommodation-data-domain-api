package uk.gov.justice.digital.hmpps.accommodationdatadomainapi.infrastructure.persistence.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
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
  @Column
  val eventType: String,
  @Column
  val eventDetailUrl: String?,
  @Column
  val eventOccurredAt: OffsetDateTime,
  @Column
  val createdAt: Instant,
  @Column(nullable = false)
  var processed: Boolean = false
)
