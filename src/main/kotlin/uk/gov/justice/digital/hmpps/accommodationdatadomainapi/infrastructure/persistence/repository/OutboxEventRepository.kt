package uk.gov.justice.digital.hmpps.accommodationdatadomainapi.infrastructure.persistence.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.accommodationdatadomainapi.infrastructure.persistence.entity.OutboxEventEntity
import java.util.UUID

interface OutboxEventRepository : JpaRepository<OutboxEventEntity, UUID> {
  fun findAllByProcessedFalse(): List<OutboxEventEntity>
}
