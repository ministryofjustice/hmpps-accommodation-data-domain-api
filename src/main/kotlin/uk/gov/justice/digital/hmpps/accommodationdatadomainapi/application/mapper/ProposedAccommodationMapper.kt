package uk.gov.justice.digital.hmpps.accommodationdatadomainapi.application.mapper

import uk.gov.justice.digital.hmpps.accommodationdatadomainapi.domain.aggregate.ProposedAccommodationAggregate
import uk.gov.justice.digital.hmpps.accommodationdatadomainapi.infrastructure.persistence.entity.ProposedAccommodationEntity
import uk.gov.justice.digital.hmpps.accommodationdatadomainapi.web.dto.ProposedAccommodationDto

object ProposedAccommodationMapper {

  fun toAggregate(entity: ProposedAccommodationEntity): ProposedAccommodationAggregate = ProposedAccommodationAggregate.hydrate(
    id = entity.id,
    address = entity.address,
    approved = entity.approved,
    createdAt = entity.createdAt,
    lastUpdatedAt = entity.lastUpdatedAt,
  )

  fun toEntity(aggregate: ProposedAccommodationAggregate): ProposedAccommodationEntity {
    val snapshot = aggregate.snapshot()

    return ProposedAccommodationEntity(
      id = snapshot.id,
      address = snapshot.address,
      approved = snapshot.approved,
      createdAt = snapshot.createdAt,
      lastUpdatedAt = snapshot.lastUpdatedAt,
    )
  }

  fun toDto(aggregate: ProposedAccommodationAggregate): ProposedAccommodationDto {
    val snapshot = aggregate.snapshot()

    return ProposedAccommodationDto(
      id = snapshot.id,
      address = snapshot.address,
      approved = snapshot.approved,
    )
  }
}
