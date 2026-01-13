@file:Suppress("ktlint:standard:filename")

package uk.gov.justice.digital.hmpps.accommodationdatadomainapi.web.health

import org.springframework.boot.health.contributor.Health
import org.springframework.boot.health.contributor.HealthIndicator
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpStatusCodeException
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientException
import org.springframework.web.client.toEntity

// HMPPS Auth health ping is required if your service calls HMPPS Auth to get a token to call other services
@Component("hmppsAuth")
class HmppsAuthHealthPing(hmppsAuthHealthRestClient: RestClient) : HealthPingCheck(hmppsAuthHealthRestClient)

abstract class HealthPingCheck(private val restClient: RestClient) : HealthIndicator {
  override fun health(): Health = restClient.ping()
}

private fun RestClient.ping(): Health = try {
  val response = get()
    .uri("/health/ping")
    .retrieve()
    .toEntity<String>()

  Health.up()
    .withDetail("HttpStatus", response.statusCode)
    .build()
} catch (ex: HttpStatusCodeException) {
  Health.down(ex)
    .withDetail("HttpStatus", ex.statusCode)
    .withDetail("body", ex.responseBodyAsString)
    .build()
} catch (ex: RestClientException) {
  Health.down(ex).build()
}
