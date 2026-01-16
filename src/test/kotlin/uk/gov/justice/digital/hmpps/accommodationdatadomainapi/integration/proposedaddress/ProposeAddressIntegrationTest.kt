package uk.gov.justice.digital.hmpps.accommodationdatadomainapi.integration.proposedaddress

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.accommodationdatadomainapi.infrastructure.messaging.event.HmppsDomainEventType
import uk.gov.justice.digital.hmpps.accommodationdatadomainapi.infrastructure.persistence.entity.ProposedAccommodationEntity
import uk.gov.justice.digital.hmpps.accommodationdatadomainapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.accommodationdatadomainapi.integration.assertions.assertThatJson
import uk.gov.justice.digital.hmpps.accommodationdatadomainapi.integration.messaging.TestSqsDomainEventListener
import uk.gov.justice.digital.hmpps.accommodationdatadomainapi.integration.proposedaddress.json.expectedProposedAddressesResponseBody
import uk.gov.justice.digital.hmpps.accommodationdatadomainapi.integration.proposedaddress.json.proposedAddressesRequestBody
import uk.gov.justice.digital.hmpps.accommodationdatadomainapi.integration.wiremock.HmppsAuthApiExtension.Companion.hmppsAuth
import uk.gov.justice.hmpps.test.kotlin.auth.WithMockAuthUser
import java.time.Instant
import java.util.UUID

class ProposeAddressIntegrationTest : IntegrationTestBase() {
  @Autowired
  lateinit var testSqsDomainEventListener: TestSqsDomainEventListener

  private val proposedAccommodationId: UUID = UUID.randomUUID()

  @BeforeEach
  fun setup() {
    proposedAccommodationRepository.deleteAll()
    outboxEventRepository.deleteAll()

    hmppsAuth.stubGrantToken()
    proposedAccommodationRepository.save(
      ProposedAccommodationEntity(
        proposedAccommodationId,
        address = "13 Test way, London. W5 9GF",
        approved = null,
        createdAt = Instant.now(),
        lastUpdatedAt = null,
      ),
    )
  }

  @WithMockAuthUser(roles = ["ROLE_PROBATION"])
  @Test
  fun `should get proposed-accommodation by id`() {
    val result = mockMvc.perform(get("/proposed-accommodation/$proposedAccommodationId"))
      .andExpect(status().isOk)
      .andReturn()
      .response
      .contentAsString

    assertThatJson(result).matchesExpectedJson(expectedProposedAddressesResponseBody(proposedAccommodationId))
  }

  @WithMockAuthUser(roles = ["ROLE_PROBATION"])
  @Test
  fun `should update proposed-accommodation approved to be true`() {
    val result = mockMvc.perform(
      put("/proposed-accommodation/{id}", proposedAccommodationId)
        .contentType(MediaType.APPLICATION_JSON)
        .content(proposedAddressesRequestBody(proposedAccommodationId, approved = true)),
    )
      .andExpect(status().isOk)
      .andReturn()
      .response
      .contentAsString

    assertThatJson(result).matchesExpectedJson(expectedProposedAddressesResponseBody(proposedAccommodationId, approved = true))

    assertPublishedSNSEvent(
      proposedAccommodationId,
      eventType = HmppsDomainEventType.ADDA_PROPOSED_ACCOMMODATION_APPROVED,
      eventDescription = HmppsDomainEventType.ADDA_PROPOSED_ACCOMMODATION_APPROVED.typeDescription,
    )
  }

  private fun assertPublishedSNSEvent(
    proposedAccommodationId: UUID,
    eventType: HmppsDomainEventType,
    eventDescription: String,
    detailUrl: String = "http://api-host/proposed-accommodation",
  ) {
    val emittedMessage = testSqsDomainEventListener.blockForMessage(eventType)
    assertThat(emittedMessage.description).isEqualTo(eventDescription)
    assertThat(emittedMessage.detailUrl).isEqualTo("$detailUrl/$proposedAccommodationId")
  }
}
