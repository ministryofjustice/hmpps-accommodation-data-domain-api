package uk.gov.justice.digital.hmpps.accommodationdatadomainapi.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.JdkClientHttpRequestFactory
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import org.springframework.web.client.RestClient
import org.springframework.web.client.support.RestClientAdapter
import org.springframework.web.service.invoker.HttpServiceProxyFactory
import uk.gov.justice.digital.hmpps.accommodationdatadomainapi.infrastructure.client.corepersonrecord.CorePersonRecordClient
import uk.gov.justice.digital.hmpps.accommodationdatadomainapi.infrastructure.client.delius.ProbationIntegrationDeliusClient
import java.net.http.HttpClient
import java.time.Duration
import kotlin.reflect.KClass

@Configuration
class RestClientConfig(
  private val restClientBuilder: RestClient.Builder,
  private val clientManager: OAuth2AuthorizedClientManager,
  @param:Value("\${hmpps-auth.url}") val hmppsAuthBaseUri: String,
  @param:Value("\${api.health-timeout:2s}") val healthTimeout: Duration,
) {

  // HMPPS Auth health ping is required if your service calls HMPPS Auth to get a token to call other services
  @Bean
  fun hmppsAuthHealthRestClient(builder: RestClient.Builder): RestClient = builder.healthRestClient(hmppsAuthBaseUri, healthTimeout)

  @Bean
  fun probationIntegrationDeliusClient(@Value($$"${service.probation-integration-delius.base-url}") baseUrl: String) = createClient(
    baseUrl,
    ProbationIntegrationDeliusClient::class,
  )

  @Bean
  fun corePersonRecordClient(@Value($$"${service.core-person-record.base-url}") baseUrl: String) = createClient(
    baseUrl,
    CorePersonRecordClient::class,
  )

  private fun <T : Any> createClient(baseUrl: String, type: KClass<T>): T {
    val client = restClientBuilder
      .requestFactory(withTimeouts(Duration.ofSeconds(1), Duration.ofSeconds(5)))
      .requestInterceptor(HmppsAuthInterceptor(clientManager, "default"))
      .baseUrl(baseUrl)
      .build()

    val proxyFactory = HttpServiceProxyFactory
      .builderFor(RestClientAdapter.create(client))
      .build()

    return proxyFactory.createClient(type.java)
  }

  private fun withTimeouts(connection: Duration, read: Duration) = JdkClientHttpRequestFactory(
    HttpClient.newBuilder()
      .connectTimeout(connection)
      .build(),
  )
    .also { it.setReadTimeout(read) }

  fun RestClient.Builder.healthRestClient(
    url: String,
    healthTimeout: Duration = Duration.ofSeconds(2),
  ): RestClient {
    val httpClient = HttpClient.newBuilder()
      .connectTimeout(healthTimeout)
      .build()

    val requestFactory = JdkClientHttpRequestFactory(httpClient).apply {
      setReadTimeout(healthTimeout)
    }

    return this
      .baseUrl(url)
      .requestFactory(requestFactory)
      .build()
  }
}
