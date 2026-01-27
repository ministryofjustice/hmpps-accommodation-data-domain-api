package uk.gov.justice.digital.hmpps.accommodationdatadomainapi.integration.proposedaddress

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import software.amazon.awssdk.services.sns.model.MessageAttributeValue
import software.amazon.awssdk.services.sns.model.PublishRequest
import uk.gov.justice.digital.hmpps.accommodationdatadomainapi.infrastructure.persistence.entity.ProcessedStatus
import uk.gov.justice.digital.hmpps.accommodationdatadomainapi.integration.IntegrationTestBase
import uk.gov.justice.hmpps.sqs.MissingTopicException
import uk.gov.justice.hmpps.test.kotlin.auth.WithMockAuthUser
import java.time.Instant
import java.time.ZoneOffset
import java.util.UUID

class IncomingProposedAddressIntegrationTest : IntegrationTestBase() {
  private val domainTopic by lazy {
    hmppsQueueService.findByTopicId("domainevents") ?: throw MissingTopicException("domainevents topic not found")
  }
  private val proposedAccommodationId: UUID = UUID.fromString("0418d8b8-3599-4224-9a69-49af02f806c5")
  private val crn: String = "X123456"
  private val eventType = "cpr.proposed.accommodation.update"
  private val eventDescription = "Proposed accommodation update from Core Person Record service"
  private val eventDetailUrl = "http://localhost:9993/probation/$crn/proposed-addresses/$proposedAccommodationId"

  @WithMockAuthUser(roles = ["ROLE_PROBATION"])
  @Test
  fun `should process incoming HMPPS CPR_PROPOSED_ACCOMMODATION_UPDATE domain events`() {
    // checks that we process event and create a record in the proposed_accommodation table
    val initialAddress = "18 Old Bath Road, Cheltenham. GL53 7QE"
    corePersonRecordMockServer.stubGetCorePersonRecordAddressesOKResponse(crn, proposedAccommodationId, address = initialAddress)
    publishCprProposedAccommodationUpdateDomainEvent()
    awaitAndAssert(
      proposedAccommodationRecordExistsCall = { proposedAccommodationRepository.existsById(proposedAccommodationId) },
      expectedAddress = initialAddress,
    )

    // checks that we process event and update the same record in the proposed_accommodation table
    val updatedAddress = "22 Old Bath Road, Cheltenham. GL53 7QE"
    corePersonRecordMockServer.stubGetCorePersonRecordAddressesOKResponse(crn, proposedAccommodationId, address = updatedAddress)
    publishCprProposedAccommodationUpdateDomainEvent()
    awaitAndAssert(
      proposedAccommodationRecordExistsCall = { proposedAccommodationRepository.existsByAddress(updatedAddress) },
      expectedAddress = updatedAddress,
    )
    val inboxRecord = inboxEventRepository.findAll().first()
    assertThat(inboxRecord.eventType).isEqualTo(eventType)
    assertThat(inboxRecord.eventDetailUrl).isEqualTo(eventDetailUrl)
    assertThat(inboxRecord.processedStatus).isEqualTo(ProcessedStatus.SUCCESS)
  }

  private fun publishCprProposedAccommodationUpdateDomainEvent() {
    val snsEvent = """ 
      {
        "eventType": "$eventType",
        "version": 1,
        "description": "$eventDescription",
        "detailUrl": "$eventDetailUrl", 
        "occurredAt": "${Instant.now().atOffset(ZoneOffset.UTC)}"
      }
    """.trimIndent()

    domainTopic.snsClient.publish(
      PublishRequest.builder()
        .topicArn(domainTopic.arn)
        .message(snsEvent)
        .messageAttributes(
          mapOf(
            "eventType" to MessageAttributeValue.builder().dataType("String").stringValue("cpr.proposed.accommodation.update").build(),
          ),
        ).build(),
    ).get()
  }

  private fun awaitAndAssert(
    proposedAccommodationRecordExistsCall: () -> Boolean,
    expectedAddress: String,
  ) {
    awaitDbRecordExists {
      assertThat(proposedAccommodationRecordExistsCall()).isTrue()
    }
    val persistedResult = proposedAccommodationRepository.findById(proposedAccommodationId)
    assertThat(persistedResult.get().address).isEqualTo(expectedAddress)
    assertThat(proposedAccommodationRepository.findAll()).hasSize(1)
  }
}
