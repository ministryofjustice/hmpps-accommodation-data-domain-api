package uk.gov.justice.digital.hmpps.accommodationdatadomainapi.infrastructure.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.accommodationdatadomainapi.infrastructure.entity.ProposedAccommodationEntity
import java.util.UUID

interface ProposedAccommodationRepository: JpaRepository<ProposedAccommodationEntity, UUID>
