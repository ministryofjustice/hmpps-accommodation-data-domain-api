package uk.gov.justice.digital.hmpps.accommodationdatadomainapi.mock

import org.springframework.context.annotation.Profile
import org.springframework.http.ResponseEntity
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.accommodationdatadomainapi.client.ProbationIntegrationService

@Profile(value = ["local", "dev"])
@RestController
class DatabaseIntegrationController(
  private val tableCountProbe: TableCountProbe,
  private val probationIntegrationDeliusService: ProbationIntegrationService,
) {

  @PreAuthorize("hasAnyRole('ROLE_PROBATION', 'ROLE_ACCOMMODATION_API__SINGLE_ACCOMMODATION_SERVICE')")
  @GetMapping("/db/table/count")
  fun getMockData() = ResponseEntity.ok("Current active tables in db: ${tableCountProbe.countTables()}")

  @PreAuthorize("hasAnyRole('ROLE_PROBATION', 'ROLE_PROBATION_API__APPROVED_PREMISES__CASE_DETAIL')")
  @GetMapping("probation-integration/info")
  fun getIntegrationServicesInfo() = ResponseEntity.ok(probationIntegrationDeliusService.getInfoResponse())
}

@Component
class TableCountProbe(
  private val jdbcTemplate: JdbcTemplate,
) {

  fun countTables(): Int = jdbcTemplate.queryForObject(
    """
      SELECT COUNT(*)
      FROM INFORMATION_SCHEMA.TABLES
      WHERE TABLE_TYPE = 'BASE TABLE'
    """.trimIndent(),
    Int::class.java,
  ) ?: 0
}
