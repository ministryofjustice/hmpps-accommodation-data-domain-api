package uk.gov.justice.digital.hmpps.accommodationdatadomainapi.application.service

import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.accommodationdatadomainapi.application.mapper.ProposedAccommodationMapper
import uk.gov.justice.digital.hmpps.accommodationdatadomainapi.infrastructure.repository.ProposedAccommodationRepository
import uk.gov.justice.digital.hmpps.accommodationdatadomainapi.web.dto.ProposedAccommodationDto
import java.util.UUID

@Service
class ProposedAccommodationApplicationService(
  private val repository: ProposedAccommodationRepository,
  private val eventPublisher: ApplicationEventPublisher
) {

  @Transactional(readOnly = true)
  fun getById(id: UUID): ProposedAccommodationDto {
    val entity = repository.findById(id)
      .orElseThrow { IllegalArgumentException("Not found") }

    val aggregate = ProposedAccommodationMapper.toAggregate(entity)

    return ProposedAccommodationMapper.toDto(aggregate)
  }

  @Transactional
  fun update(id: UUID, request: ProposedAccommodationDto): ProposedAccommodationDto {
    val entity = repository.findById(id)
      .orElseThrow { IllegalArgumentException("Not found") }

    val aggregate = ProposedAccommodationMapper.toAggregate(entity)
    aggregate.updateApproved(approved = request.approved)

    repository.save(ProposedAccommodationMapper.toEntity(aggregate))

    aggregate.pullDomainEvents()
      .forEach(eventPublisher::publishEvent)

    return ProposedAccommodationMapper.toDto(aggregate)
  }
}
