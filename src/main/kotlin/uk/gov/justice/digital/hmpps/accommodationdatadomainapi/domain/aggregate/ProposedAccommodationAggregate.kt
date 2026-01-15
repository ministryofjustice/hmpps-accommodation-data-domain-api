package uk.gov.justice.digital.hmpps.accommodationdatadomainapi.domain.aggregate

import uk.gov.justice.digital.hmpps.accommodationdatadomainapi.domain.event.AccommodationDataDomainEvent
import uk.gov.justice.digital.hmpps.accommodationdatadomainapi.domain.event.ProposedAccommodationApprovedEvent
import uk.gov.justice.digital.hmpps.accommodationdatadomainapi.domain.event.ProposedAccommodationUnapprovedEvent
import uk.gov.justice.digital.hmpps.accommodationdatadomainapi.domain.exceptions.InvalidProposedAccommodationApprovalState
import java.time.Instant
import java.util.UUID

class ProposedAccommodationAggregate private constructor(
  private val id: UUID,
  private val address: String,
  private var approved: Boolean?,
  private val createdAt: Instant,
  private var lastUpdatedAt: Instant?,
) {
  private val domainEvents = mutableListOf<AccommodationDataDomainEvent>()

  companion object {
    fun hydrate(
      id: UUID,
      address: String,
      approved: Boolean?,
      createdAt: Instant,
      lastUpdatedAt: Instant?,
    ) = ProposedAccommodationAggregate(
      id = id,
      address = address,
      approved = approved,
      createdAt = createdAt,
      lastUpdatedAt = lastUpdatedAt,
    )
  }

  fun updateApproved(approved: Boolean?) {
    if (approved == null) {
      throw InvalidProposedAccommodationApprovalState()
    }
    this.approved = approved
    this.lastUpdatedAt = Instant.now()
    if (approved) {
      domainEvents += ProposedAccommodationApprovedEvent(id)
    } else {
      domainEvents += ProposedAccommodationUnapprovedEvent(id)
    }
  }

  fun pullDomainEvents(): List<AccommodationDataDomainEvent> = domainEvents.toList().also { domainEvents.clear() }

  fun snapshot() = ProposedAccommodationSnapshot(id, address, approved, createdAt, lastUpdatedAt)

  data class ProposedAccommodationSnapshot(
    val id: UUID,
    val address: String,
    val approved: Boolean?,
    val createdAt: Instant,
    val lastUpdatedAt: Instant?,
  )
}
