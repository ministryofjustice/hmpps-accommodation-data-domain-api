package uk.gov.justice.digital.hmpps.accommodationdatadomainapi.integration.wiremock

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import org.springframework.http.HttpStatus
import java.util.UUID

class CorePersonRecordMockServer : WireMockServer(9993) {

  fun stubGetCorePersonRecordAddressesOKResponse(
    crn: String,
    proposedAccommodationId: UUID,
    address: String,
  ) {
    val proposedAddressResponse = """
      {
        "id": "$proposedAccommodationId",
        "crn": "$crn",
        "address": "$address"
      }
    """.trimIndent()
    stubFor(
      WireMock
        .get(WireMock.urlPathEqualTo("/probation/$crn/proposed-addresses/$proposedAccommodationId"))
        .willReturn(
          WireMock
            .aResponse()
            .withHeader("Content-Type", "application/json")
            .withStatus(HttpStatus.OK.value())
            .withBody(proposedAddressResponse),
        ),
    )
  }
}
