package uk.gov.justice.digital.hmpps.accommodationdatadomainapi.infrastructure.persistence.entity

import jakarta.persistence.Column
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
  @Column
  var address: String,
  @Column
  var approved: Boolean?,
  @Column(name = "created_at")
  var createdAt: Instant,
  @Column(name = "last_updated_at")
  var lastUpdatedAt: Instant?,
)
