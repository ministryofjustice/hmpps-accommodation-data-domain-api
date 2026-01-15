package uk.gov.justice.digital.hmpps.accommodationdatadomainapi.infrastructure.messaging.listener

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.awspring.cloud.sqs.annotation.SqsListener
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.accommodationdatadomainapi.infrastructure.messaging.event.HmppsDomainEventType
import uk.gov.justice.digital.hmpps.accommodationdatadomainapi.infrastructure.messaging.event.HmppsSnsDomainEvent

@Component
class ProposedAccommodationUpdatedListener(
  private val objectMapper: ObjectMapper,
) {
  private val log = LoggerFactory.getLogger(this::class.java)

  @SqsListener("adda-domain-events-queue", factory = "hmppsQueueContainerFactoryProxy")
  fun processMessage(msg: String) {
    try {
      val (message) = objectMapper.readValue<SQSMessage>(msg)
      val event = objectMapper.readValue<HmppsSnsDomainEvent>(message)
      handleEvent(event)
    } catch (e: Exception) {
      log.error("Exception caught in ProposedAccommodationUpdatedListener", e)
      throw e
    }
  }

  private fun handleEvent(event: HmppsSnsDomainEvent) {
    when (event.eventType) {
      HmppsDomainEventType.ADDA_PROPOSED_ACCOMMODATION_APPROVED.typeName ->
        log.info("Proposed Accommodation Approved -- Event received and occurred-at: ${event.occurredAt}. Event detailUrl: ${event.detailUrl}")

      HmppsDomainEventType.ADDA_PROPOSED_ACCOMMODATION_UNAPPROVED.typeName ->
        log.info("Proposed Accommodation Unapproved -- Event received and occurred-at: ${event.occurredAt}. Event detailUrl: ${event.detailUrl}")
    }
  }

  data class SQSMessage(
    @JsonProperty("Message") val message: String,
  )
}
