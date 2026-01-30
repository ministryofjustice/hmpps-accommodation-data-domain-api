package uk.gov.justice.digital.hmpps.accommodationdatadomainapi.integration

import org.awaitility.kotlin.await
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.restclient.test.autoconfigure.AutoConfigureRestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpHeaders
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.client.RestTestClient
import uk.gov.justice.digital.hmpps.accommodationdatadomainapi.integration.wiremock.CorePersonRecordMockServer
import uk.gov.justice.digital.hmpps.accommodationdatadomainapi.integration.wiremock.HmppsAuthMockServer
import uk.gov.justice.hmpps.test.kotlin.auth.JwtAuthorisationHelper
import java.time.Duration

@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@AutoConfigureRestClient
abstract class IntegrationTestBase {

  @LocalServerPort
  protected lateinit var port: Integer

  @Autowired
  protected lateinit var jwtAuthHelper: JwtAuthorisationHelper

  protected lateinit var client: RestTestClient

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
    client = RestTestClient.bindToServer().baseUrl("http://localhost:$port").build()
    hmppsAuth.resetAll()
    corePersonRecordMockServer.resetAll()
  }

  @AfterAll
  fun after() {
    hmppsAuth.stop()
    corePersonRecordMockServer.stop()
  }

  fun awaitDbRecordExists(block: () -> Unit) {
    await.atMost(Duration.ofSeconds(10))
      .pollInterval(Duration.ofMillis(200))
      .untilAsserted(block)
  }

  fun RestTestClient.RequestHeadersSpec<*>.withJwt(
    roles: List<String> = listOf("ROLE_PROBATION"),
  ): RestTestClient.RequestHeadersSpec<*> = this.headers {
    it.setBearerAuth(
      jwtAuthHelper.createJwtAccessToken(roles = roles),
    )
  }
}
