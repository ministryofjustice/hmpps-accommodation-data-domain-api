package uk.gov.justice.digital.hmpps.accommodationdatadomainapi.infrastructure.persistence.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.accommodationdatadomainapi.infrastructure.persistence.entity.ProposedAccommodationEntity
import java.util.UUID

interface ProposedAccommodationRepository : JpaRepository<ProposedAccommodationEntity, UUID>
