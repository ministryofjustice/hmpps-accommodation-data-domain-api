package uk.gov.justice.digital.hmpps.accommodationdatadomainapi.infrastructure.persistence.entity

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "proposed_accommodation")
data class ProposedAccommodationEntity(
  @Id
  val id: UUID,
  val crn: String,
  var address: String,
  var approved: Boolean?,
  var createdAt: Instant,
  var lastUpdatedAt: Instant?,
)
