package uk.gov.justice.digital.hmpps.accommodationdatadomainapi.infrastructure.client.corepersonrecord

import org.springframework.web.service.annotation.GetExchange
import org.springframework.web.service.annotation.HttpExchange
import java.net.URI
import java.util.UUID

@HttpExchange
interface CorePersonRecordClient {

  @GetExchange
  fun fetchAddress(uri: URI): CorePersonRecordAddress
}

data class CorePersonRecordAddress(
  val id: UUID,
  val crn: String,
  val address: String,
)
