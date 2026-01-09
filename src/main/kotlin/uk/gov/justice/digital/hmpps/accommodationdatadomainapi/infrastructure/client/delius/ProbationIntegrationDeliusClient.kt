package uk.gov.justice.digital.hmpps.accommodationdatadomainapi.infrastructure.client.delius

import org.springframework.stereotype.Service
import org.springframework.web.service.annotation.GetExchange
import org.springframework.web.service.annotation.HttpExchange

@HttpExchange
interface ProbationIntegrationDeliusClient {
  @GetExchange(value = "/info")
  fun getInfo(): String
}

@Service
class ProbationIntegrationService(private val probationIntegrationDeliusClient: ProbationIntegrationDeliusClient) {
  fun getInfoResponse(): String = probationIntegrationDeliusClient.getInfo()
}
