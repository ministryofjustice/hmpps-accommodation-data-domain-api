package uk.gov.justice.digital.hmpps.accommodationdatadomainapi.infrastructure.client.corepersonrecord

import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import uk.gov.justice.digital.hmpps.accommodationdatadomainapi.application.exceptions.NotFoundException
import java.util.UUID

interface CorePersonRecordClient {
  fun fetchAddress(resourceUrl: String): CorePersonRecordAddress
}

@Service
open class CorePersonRecordCachingService : CorePersonRecordClient {
  private val webClient = WebClient.create()

  override fun fetchAddress(resourceUrl: String): CorePersonRecordAddress = webClient.get()
    .uri(resourceUrl)
    .retrieve()
    .bodyToMono(CorePersonRecordAddress::class.java)
    .block()
    ?: throw NotFoundException("Cannot retrieve address from CorePersonRecord for url: $resourceUrl")
}

data class CorePersonRecordAddress(
  val id: UUID,
  val crn: String,
  val address: String,
)
