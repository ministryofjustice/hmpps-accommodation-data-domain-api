package uk.gov.justice.digital.hmpps.accommodationdatadomainapi.domain.aggregate

import uk.gov.justice.digital.hmpps.accommodationdatadomainapi.domain.event.AccommodationDataDomainEvent
import uk.gov.justice.digital.hmpps.accommodationdatadomainapi.domain.event.ProposedAccommodationApprovedEvent
import uk.gov.justice.digital.hmpps.accommodationdatadomainapi.domain.event.ProposedAccommodationUnapprovedEvent
import uk.gov.justice.digital.hmpps.accommodationdatadomainapi.domain.exceptions.InvalidProposedAccommodationApprovalState
import uk.gov.justice.digital.hmpps.accommodationdatadomainapi.utils.RootAggregateHydrateFunction
import java.time.Instant
import java.util.UUID

class ProposedAccommodationAggregate private constructor(
  private val id: UUID,
  private val crn: String,
  private val createdAt: Instant = Instant.now(),
  private var approved: Boolean? = null,
  private var address: String? = null,
  private var lastUpdatedAt: Instant? = null,
) {
  private val domainEvents = mutableListOf<AccommodationDataDomainEvent>()

  companion object {
    @RootAggregateHydrateFunction
    fun hydrate(
      id: UUID,
      crn: String,
      address: String,
      approved: Boolean?,
      createdAt: Instant,
      lastUpdatedAt: Instant?,
    ) = ProposedAccommodationAggregate(
      id = id,
      crn = crn,
      address = address,
      approved = approved,
      createdAt = createdAt,
      lastUpdatedAt = lastUpdatedAt,
    )

    fun createNew(id: UUID, crn: String) = ProposedAccommodationAggregate(
      id = id,
      crn = crn,
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

  fun upsertAddress(
    newAddress: String,
  ) {
    address = newAddress
  }

  fun pullDomainEvents(): List<AccommodationDataDomainEvent> = domainEvents.toList().also { domainEvents.clear() }

  fun snapshot() = ProposedAccommodationSnapshot(id, crn, address, approved, createdAt, lastUpdatedAt)

  data class ProposedAccommodationSnapshot(
    val id: UUID,
    val crn: String,
    val address: String?,
    val approved: Boolean?,
    val createdAt: Instant,
    val lastUpdatedAt: Instant?,
  )
}
