package uk.gov.justice.digital.hmpps.accommodationdatadomainapi.domain.aggregate

import uk.gov.justice.digital.hmpps.accommodationdatadomainapi.domain.event.ProposedAccommodationUpdatedEvent
import java.time.LocalDate
import java.util.UUID

class ProposedAccommodationAggregate private constructor(
  private val id: UUID,
  private val address: String,
  private var approved: Boolean?,
  private val createdAt: LocalDate?,
  private var lastUpdatedAt: LocalDate?,
) {

  companion object {
    fun hydrate(
      id: UUID,
      address: String,
      approved: Boolean?,
      createdAt: LocalDate?,
      lastUpdatedAt: LocalDate?,
    ) = ProposedAccommodationAggregate(
      id = id,
      address = address,
      approved = approved,
      createdAt = createdAt,
      lastUpdatedAt = lastUpdatedAt,
    )
  }

  private val domainEvents = mutableListOf<Any>()

  fun updateApproved(approved: Boolean?) {
    require(approved != null) { "Approved must be true or false" }
    this.approved = approved
    this.lastUpdatedAt = LocalDate.now()

    domainEvents += ProposedAccommodationUpdatedEvent(id, approved)
  }

  fun pullDomainEvents(): List<Any> =
    domainEvents.also { domainEvents.clear() }

  fun snapshot() = Snapshot(id, address, approved, createdAt, lastUpdatedAt)

  data class Snapshot(
    val id: UUID,
    val address: String,
    val approved: Boolean?,
    val createdAt: LocalDate?,
    val lastUpdatedAt: LocalDate?,
  )
}