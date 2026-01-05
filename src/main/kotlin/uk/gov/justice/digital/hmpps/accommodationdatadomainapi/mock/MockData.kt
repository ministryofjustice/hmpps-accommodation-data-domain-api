package uk.gov.justice.digital.hmpps.accommodationdatadomainapi.mock

import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty
import org.springframework.context.annotation.Condition
import org.springframework.context.annotation.ConditionContext
import org.springframework.context.annotation.Profile
import org.springframework.core.type.AnnotatedTypeMetadata
import org.springframework.http.ResponseEntity
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@Profile(value = ["local", "dev"])
@RestController
class MockData(private val tableCountProbe: TableCountProbe) {

  @PreAuthorize("permitAll()")
  @GetMapping("/db")
  fun getMockData() = ResponseEntity.ok("Current active tables in db: ${tableCountProbe.countTables()}")
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
