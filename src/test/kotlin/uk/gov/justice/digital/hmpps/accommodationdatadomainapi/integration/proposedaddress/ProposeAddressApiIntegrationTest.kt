package uk.gov.justice.digital.hmpps.accommodationdatadomainapi.integration.proposedaddress

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.http.MediaType.APPLICATION_JSON
import uk.gov.justice.digital.hmpps.accommodationdatadomainapi.domain.event.AccommodationDataDomainEventType
import uk.gov.justice.digital.hmpps.accommodationdatadomainapi.infrastructure.messaging.event.OutgoingHmppsDomainEventType
import uk.gov.justice.digital.hmpps.accommodationdatadomainapi.infrastructure.persistence.entity.ProcessedStatus
import uk.gov.justice.digital.hmpps.accommodationdatadomainapi.infrastructure.persistence.entity.ProposedAccommodationEntity
import uk.gov.justice.digital.hmpps.accommodationdatadomainapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.accommodationdatadomainapi.integration.assertions.assertThatJson
import uk.gov.justice.digital.hmpps.accommodationdatadomainapi.integration.proposedaddress.json.expectedProposedAccommodationApprovedDomainEventJson
import uk.gov.justice.digital.hmpps.accommodationdatadomainapi.integration.proposedaddress.json.expectedProposedAccommodationUnapprovedDomainEventJson
import uk.gov.justice.digital.hmpps.accommodationdatadomainapi.integration.proposedaddress.json.expectedProposedAddressesResponseBody
import uk.gov.justice.digital.hmpps.accommodationdatadomainapi.integration.proposedaddress.json.proposedAddressesRequestBody
import java.time.Instant
import java.util.UUID

class ProposeAddressApiIntegrationTest : IntegrationTestBase() {

  private val proposedAccommodationId: UUID = UUID.randomUUID()
  private val crn: String = "X123456"

  @BeforeEach
  fun setup() {
    hmppsAuth.stubGrantToken()
    outboxEventRepository.deleteAll()
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

  @AfterAll
  fun tearDown() {
    outboxEventRepository.deleteAll()
  }

  @Test
  fun `should get proposed-accommodation by id`() {
    client.get().uri("/proposed-accommodation/$proposedAccommodationId")
      .withJwt()
      .exchange()
      .expectStatus().isOk
      .expectBody(String::class.java)
      .value {
        assertThatJson(it!!).matchesExpectedJson(expectedProposedAddressesResponseBody(proposedAccommodationId))
      }
  }

  @ParameterizedTest
  @ValueSource(booleans = [true, false])
  fun `should update proposed-accommodation approval value`(approved: Boolean) {
    client.put().uri("/proposed-accommodation/{id}", proposedAccommodationId)
      .contentType(APPLICATION_JSON)
      .body(proposedAddressesRequestBody(proposedAccommodationId, approved))
      .withJwt()
      .exchange()
      .expectStatus().isOk
      .expectBody(String::class.java)
      .value {
        assertThatJson(it!!).matchesExpectedJson(
          expectedProposedAddressesResponseBody(
            proposedAccommodationId,
            approved,
          ),
        )
      }

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
