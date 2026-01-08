package uk.gov.justice.digital.hmpps.accommodationdatadomainapi.application.service

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.accommodationdatadomainapi.application.exceptions.NotFoundException
import uk.gov.justice.digital.hmpps.accommodationdatadomainapi.application.mapper.ProposedAccommodationMapper
import uk.gov.justice.digital.hmpps.accommodationdatadomainapi.infrastructure.persistence.entity.OutboxEventEntity
import uk.gov.justice.digital.hmpps.accommodationdatadomainapi.infrastructure.persistence.repository.OutboxEventRepository
import uk.gov.justice.digital.hmpps.accommodationdatadomainapi.infrastructure.persistence.repository.ProposedAccommodationRepository
import uk.gov.justice.digital.hmpps.accommodationdatadomainapi.web.dto.ProposedAccommodationDto
import java.time.Instant
import java.util.UUID

@Service
class ProposedAccommodationApplicationService(
  private val objectMapper: ObjectMapper,
  private val repository: ProposedAccommodationRepository,
  private val outboxEventRepository: OutboxEventRepository,
) {

  @Transactional(readOnly = true)
  fun getById(id: UUID): ProposedAccommodationDto {
    val entity = repository.findById(id)
      .orElseThrow { NotFoundException("Proposed Accommodation not found for id: $id") }

    val aggregate = ProposedAccommodationMapper.toAggregate(entity)

    return ProposedAccommodationMapper.toDto(aggregate)
  }

  @Transactional
  fun update(id: UUID, request: ProposedAccommodationDto): ProposedAccommodationDto {
    val entity = repository.findById(id)
      .orElseThrow { NotFoundException("Proposed Accommodation not found for id: $id") }

    val aggregate = ProposedAccommodationMapper.toAggregate(entity)
    aggregate.updateApproved(approved = request.approved)

    repository.save(ProposedAccommodationMapper.toEntity(aggregate))

    aggregate.pullDomainEvents().forEach { event ->
      outboxEventRepository.save(
        OutboxEventEntity(
          id = UUID.randomUUID(),
          aggregateId = event.aggregateId,
          aggregateType = "ProposedAccommodation",
          domainEventType = event.type.name,
          payload = objectMapper.writeValueAsString(event),
          createdAt = Instant.now(),
        ),
      )
    }

    return ProposedAccommodationMapper.toDto(aggregate)
  }
}
