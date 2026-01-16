package uk.gov.justice.digital.hmpps.accommodationdatadomainapi.integration.proposedaddress

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.accommodationdatadomainapi.domain.event.AccommodationDataDomainEventType
import uk.gov.justice.digital.hmpps.accommodationdatadomainapi.infrastructure.messaging.event.OutgoingHmppsDomainEventType
import uk.gov.justice.digital.hmpps.accommodationdatadomainapi.infrastructure.persistence.entity.ProcessedStatus
import uk.gov.justice.digital.hmpps.accommodationdatadomainapi.infrastructure.persistence.entity.ProposedAccommodationEntity
import uk.gov.justice.digital.hmpps.accommodationdatadomainapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.accommodationdatadomainapi.integration.assertions.assertThatJson
import uk.gov.justice.digital.hmpps.accommodationdatadomainapi.integration.messaging.TestSqsDomainEventListener
import uk.gov.justice.digital.hmpps.accommodationdatadomainapi.integration.proposedaddress.json.expectedProposedAccommodationApprovedDomainEventJson
import uk.gov.justice.digital.hmpps.accommodationdatadomainapi.integration.proposedaddress.json.expectedProposedAccommodationUnapprovedDomainEventJson
import uk.gov.justice.digital.hmpps.accommodationdatadomainapi.integration.proposedaddress.json.expectedProposedAddressesResponseBody
import uk.gov.justice.digital.hmpps.accommodationdatadomainapi.integration.proposedaddress.json.proposedAddressesRequestBody
import uk.gov.justice.hmpps.test.kotlin.auth.WithMockAuthUser
import java.time.Instant
import java.util.UUID

class ProposeAddressApiIntegrationTest : IntegrationTestBase() {
  @Autowired
  lateinit var testSqsDomainEventListener: TestSqsDomainEventListener

  private val proposedAccommodationId: UUID = UUID.randomUUID()
  private val crn: String = "X123456"

  @BeforeEach
  fun setup() {
    hmppsAuth.stubGrantToken()
    proposedAccommodationRepository.save(
      ProposedAccommodationEntity(
        id = proposedAccommodationId,
        crn = crn,
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
  @ParameterizedTest
  @ValueSource(booleans = [true, false])
  fun `should update proposed-accommodation approval value`(approved: Boolean) {
    val result = mockMvc.perform(
      put("/proposed-accommodation/{id}", proposedAccommodationId)
        .contentType(MediaType.APPLICATION_JSON)
        .content(proposedAddressesRequestBody(proposedAccommodationId, approved)),
    )
      .andExpect(status().isOk)
      .andReturn()
      .response
      .contentAsString

    assertThatJson(result).matchesExpectedJson(expectedProposedAddressesResponseBody(proposedAccommodationId, approved))

    val expectedOutgoingHmppsDomainEventType = when (approved) {
      true -> OutgoingHmppsDomainEventType.ADDA_PROPOSED_ACCOMMODATION_APPROVED
      false -> OutgoingHmppsDomainEventType.ADDA_PROPOSED_ACCOMMODATION_UNAPPROVED
    }
    assertPublishedSNSEvent(
      proposedAccommodationId,
      eventType = expectedOutgoingHmppsDomainEventType,
      eventDescription = expectedOutgoingHmppsDomainEventType.typeDescription,
    )
    assertThatOutboxIsAsExpected(approved)
  }

  private fun assertPublishedSNSEvent(
    proposedAccommodationId: UUID,
    eventType: OutgoingHmppsDomainEventType,
    eventDescription: String,
    detailUrl: String = "http://api-host/proposed-accommodation",
  ) {
    val emittedMessage = testSqsDomainEventListener.blockForMessage(eventType)
    assertThat(emittedMessage.description).isEqualTo(eventDescription)
    assertThat(emittedMessage.detailUrl).isEqualTo("$detailUrl/$proposedAccommodationId")
  }

  private fun assertThatOutboxIsAsExpected(approved: Boolean) {
    val (expectedInternalDomainEventType, expectedPayload) = when (approved) {
      true ->
        AccommodationDataDomainEventType.PROPOSED_ACCOMMODATION_APPROVED to expectedProposedAccommodationApprovedDomainEventJson(proposedAccommodationId)
      false ->
        AccommodationDataDomainEventType.PROPOSED_ACCOMMODATION_UNAPPROVED to expectedProposedAccommodationUnapprovedDomainEventJson(proposedAccommodationId)
    }
    val outboxRecord = outboxEventRepository.findAll().first()
    assertThat(outboxRecord.aggregateId).isEqualTo(proposedAccommodationId)
    assertThat(outboxRecord.aggregateType).isEqualTo("ProposedAccommodation")
    assertThat(outboxRecord.domainEventType).isEqualTo(expectedInternalDomainEventType.name)
    assertThatJson(outboxRecord.payload).matchesExpectedJson(expectedPayload)
    assertThat(outboxRecord.processedStatus).isEqualTo(ProcessedStatus.SUCCESS)
  }
}
