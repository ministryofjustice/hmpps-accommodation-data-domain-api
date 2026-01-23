package uk.gov.justice.digital.hmpps.accommodationdatadomainapi.application.service

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.accommodationdatadomainapi.application.exceptions.NotFoundException
import uk.gov.justice.digital.hmpps.accommodationdatadomainapi.application.mapper.ProposedAccommodationMapper
import uk.gov.justice.digital.hmpps.accommodationdatadomainapi.domain.aggregate.ProposedAccommodationAggregate
import uk.gov.justice.digital.hmpps.accommodationdatadomainapi.infrastructure.client.corepersonrecord.CorePersonRecordAddress
import uk.gov.justice.digital.hmpps.accommodationdatadomainapi.infrastructure.persistence.entity.OutboxEventEntity
import uk.gov.justice.digital.hmpps.accommodationdatadomainapi.infrastructure.persistence.entity.ProcessedStatus
import uk.gov.justice.digital.hmpps.accommodationdatadomainapi.infrastructure.persistence.repository.OutboxEventRepository
import uk.gov.justice.digital.hmpps.accommodationdatadomainapi.infrastructure.persistence.repository.ProposedAccommodationRepository
import uk.gov.justice.digital.hmpps.accommodationdatadomainapi.web.dto.ProposedAccommodationDto
import java.time.Instant
import java.util.UUID

@Service
class ProposedAccommodationApplicationService(
  private val objectMapper: ObjectMapper,
  private val proposedAccommodationRepository: ProposedAccommodationRepository,
  private val outboxEventRepository: OutboxEventRepository,
) {

  @Transactional(readOnly = true)
  fun getById(id: UUID): ProposedAccommodationDto {
    val entity = proposedAccommodationRepository.findById(id)
      .orElseThrow { NotFoundException("Proposed Accommodation not found for id: $id") }

    val aggregate = ProposedAccommodationMapper.toAggregate(entity)

    return ProposedAccommodationMapper.toDto(aggregate)
  }

  @Transactional
  fun update(id: UUID, request: ProposedAccommodationDto): ProposedAccommodationDto {
    val entity = proposedAccommodationRepository.findById(id)
      .orElseThrow { NotFoundException("Proposed Accommodation not found for id: $id") }

    val aggregate = ProposedAccommodationMapper.toAggregate(entity)
    aggregate.updateApproved(approved = request.approved)

    proposedAccommodationRepository.save(ProposedAccommodationMapper.toEntity(aggregate.snapshot()))

    aggregate.pullDomainEvents().forEach { event ->
      outboxEventRepository.save(
        OutboxEventEntity(
          id = UUID.randomUUID(),
          aggregateId = event.aggregateId,
          aggregateType = "ProposedAccommodation",
          domainEventType = event.type.name,
          payload = objectMapper.writeValueAsString(event),
          createdAt = Instant.now(),
          processedStatus = ProcessedStatus.PENDING,
          processedAt = null,
        ),
      )
    }

    return ProposedAccommodationMapper.toDto(aggregate)
  }

  @Transactional
  fun upsertAddress(
    corePersonRecordAddress: CorePersonRecordAddress,
  ) {
    val proposedAccommodation = proposedAccommodationRepository.findByCrn(corePersonRecordAddress.crn)
    val aggregate = proposedAccommodation?.let {
      ProposedAccommodationMapper.toAggregate(it)
    } ?: ProposedAccommodationAggregate.createNew(
      id = corePersonRecordAddress.id,
      crn = corePersonRecordAddress.crn,
    )
    aggregate.upsertAddress(
      newAddress = corePersonRecordAddress.address,
    )
    proposedAccommodationRepository.save(
      ProposedAccommodationMapper.toEntity(aggregate.snapshot()),
    )
  }
}
