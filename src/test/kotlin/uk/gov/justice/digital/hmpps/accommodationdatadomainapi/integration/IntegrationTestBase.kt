package uk.gov.justice.digital.hmpps.accommodationdatadomainapi.integration

import org.awaitility.kotlin.await
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.http.HttpHeaders
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.servlet.MockMvc
import uk.gov.justice.digital.hmpps.accommodationdatadomainapi.infrastructure.persistence.repository.InboxEventRepository
import uk.gov.justice.digital.hmpps.accommodationdatadomainapi.infrastructure.persistence.repository.OutboxEventRepository
import uk.gov.justice.digital.hmpps.accommodationdatadomainapi.infrastructure.persistence.repository.ProposedAccommodationRepository
import uk.gov.justice.digital.hmpps.accommodationdatadomainapi.integration.wiremock.CorePersonRecordMockServer
import uk.gov.justice.digital.hmpps.accommodationdatadomainapi.integration.wiremock.HmppsAuthMockServer
import uk.gov.justice.hmpps.sqs.HmppsQueueService
import uk.gov.justice.hmpps.test.kotlin.auth.JwtAuthorisationHelper
import java.time.Duration

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
abstract class IntegrationTestBase {

  @Autowired
  lateinit var mockMvc: MockMvc

  @Autowired
  protected lateinit var webTestClient: WebTestClient

  @Autowired
  protected lateinit var jwtAuthHelper: JwtAuthorisationHelper

  @Autowired
  lateinit var proposedAccommodationRepository: ProposedAccommodationRepository

  @Autowired
  lateinit var outboxEventRepository: OutboxEventRepository

  @Autowired
  lateinit var inboxEventRepository: InboxEventRepository

  @Autowired
  lateinit var hmppsQueueService: HmppsQueueService

  val hmppsAuth = HmppsAuthMockServer()
  val corePersonRecordMockServer = CorePersonRecordMockServer()

  internal fun setAuthorisation(
    username: String? = "AUTH_ADM",
    roles: List<String> = listOf(),
    scopes: List<String> = listOf("read"),
  ): (HttpHeaders) -> Unit = jwtAuthHelper.setAuthorisationHeader(username = username, scope = scopes, roles = roles)

  protected fun stubPingWithResponse(status: Int) {
    hmppsAuth.stubHealthPing(status)
  }

  @BeforeAll
  fun startMocks() {
    hmppsAuth.start()
    corePersonRecordMockServer.start()
  }

  @BeforeEach
  fun resetStubsAndTeardownDb() {
    hmppsAuth.resetAll()
    corePersonRecordMockServer.resetAll()

    inboxEventRepository.deleteAll()
    outboxEventRepository.deleteAll()
    proposedAccommodationRepository.deleteAll()
  }

  @AfterAll
  fun after() {
    inboxEventRepository.deleteAll()
    outboxEventRepository.deleteAll()
    proposedAccommodationRepository.deleteAll()

    hmppsAuth.stop()
    corePersonRecordMockServer.stop()
  }

  fun awaitDbRecordExists(block: () -> Unit) {
    await.atMost(Duration.ofSeconds(10))
      .pollInterval(Duration.ofMillis(200))
      .untilAsserted(block)
  }
}
