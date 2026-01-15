package uk.gov.justice.digital.hmpps.accommodationdatadomainapi.web.controller

import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.accommodationdatadomainapi.application.service.ProposedAccommodationApplicationService
import uk.gov.justice.digital.hmpps.accommodationdatadomainapi.web.dto.ProposedAccommodationDto
import java.util.UUID

@RestController
@RequestMapping("/proposed-accommodation")
class ProposedAccommodationController(
  private val proposedAccommodationApplicationService: ProposedAccommodationApplicationService,
) {
  @PreAuthorize("hasAnyRole('ROLE_PROBATION')")
  @GetMapping("/{id}")
  fun get(@PathVariable id: UUID): ResponseEntity<ProposedAccommodationDto> {
    val response = proposedAccommodationApplicationService.getById(id)
    return ResponseEntity.ok(response)
  }

  @PreAuthorize("hasAnyRole('ROLE_PROBATION')")
  @PutMapping("/{id}")
  fun update(
    @PathVariable id: UUID,
    @RequestBody request: ProposedAccommodationDto,
  ): ResponseEntity<ProposedAccommodationDto> {
    val response = proposedAccommodationApplicationService.update(id, request)
    return ResponseEntity.ok(response)
  }
}
