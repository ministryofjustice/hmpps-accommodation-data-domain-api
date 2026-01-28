package uk.gov.justice.digital.hmpps.accommodationdatadomainapi.infrastructure.messaging.listener

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.awspring.cloud.sqs.annotation.SqsListener
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.accommodationdatadomainapi.infrastructure.messaging.event.HmppsSnsDomainEvent
import uk.gov.justice.digital.hmpps.accommodationdatadomainapi.infrastructure.persistence.entity.InboxEventEntity
import uk.gov.justice.digital.hmpps.accommodationdatadomainapi.infrastructure.persistence.entity.ProcessedStatus
import uk.gov.justice.digital.hmpps.accommodationdatadomainapi.infrastructure.persistence.repository.InboxEventRepository
import java.time.Instant
import java.util.UUID

@Profile(value = ["local", "development", "test"])
@Component
class HmppsDomainEventListener(
  private val objectMapper: ObjectMapper,
  private val inboxEventRepository: InboxEventRepository,
) {
  private val log = LoggerFactory.getLogger(this::class.java)

  @SqsListener("adda-domain-events-queue", factory = "hmppsQueueContainerFactoryProxy")
  fun processMessage(msg: String) {
    try {
      val (message) = objectMapper.readValue<SQSMessage>(msg)
      val event = objectMapper.readValue<HmppsSnsDomainEvent>(message)
      inboxEventRepository.save(
        InboxEventEntity(
          id = UUID.randomUUID(),
          eventType = event.eventType,
          eventDetailUrl = event.detailUrl,
          eventOccurredAt = event.occurredAt,
          createdAt = Instant.now(),
          processedStatus = ProcessedStatus.PENDING,
          processedAt = null,
        ),
      )
    } catch (e: Exception) {
      log.error("Exception caught in ProposedAccommodationUpdatedListener", e)
      throw e
    }
  }

  data class SQSMessage(
    @JsonProperty("Message") val message: String,
  )
}
