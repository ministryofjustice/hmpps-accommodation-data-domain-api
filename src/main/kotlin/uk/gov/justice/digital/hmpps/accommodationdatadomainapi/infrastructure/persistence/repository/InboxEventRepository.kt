package uk.gov.justice.digital.hmpps.accommodationdatadomainapi.infrastructure.persistence.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.accommodationdatadomainapi.infrastructure.persistence.entity.InboxEventEntity
import java.util.UUID

interface InboxEventRepository : JpaRepository<InboxEventEntity, UUID> {

  fun findAllByProcessedFalse(): List<InboxEventEntity>
}

