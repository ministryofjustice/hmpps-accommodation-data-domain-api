package uk.gov.justice.digital.hmpps.accommodationdatadomainapi.infrastructure.messaging.publisher

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import software.amazon.awssdk.services.sns.model.MessageAttributeValue
import software.amazon.awssdk.services.sns.model.PublishRequest
import software.amazon.awssdk.services.sns.model.PublishResponse
import uk.gov.justice.digital.hmpps.accommodationdatadomainapi.domain.event.AccommodationDataDomainEventType
import uk.gov.justice.digital.hmpps.accommodationdatadomainapi.infrastructure.config.HmppsDomainEventUrlConfig
import uk.gov.justice.digital.hmpps.accommodationdatadomainapi.infrastructure.messaging.event.HmppsDomainEventType
import uk.gov.justice.digital.hmpps.accommodationdatadomainapi.infrastructure.messaging.event.HmppsSnsDomainEvent
import uk.gov.justice.digital.hmpps.accommodationdatadomainapi.infrastructure.persistence.entity.OutboxEventEntity
import uk.gov.justice.digital.hmpps.accommodationdatadomainapi.infrastructure.persistence.repository.OutboxEventRepository
import uk.gov.justice.hmpps.sqs.HmppsQueueService
import uk.gov.justice.hmpps.sqs.MissingTopicException
import java.time.ZoneOffset

@Profile(value = ["local", "dev", "test"])
@Component
class OutboxEventPublisher(
  private val objectMapper: ObjectMapper,
  private val hmppsDomainEventUrlConfig: HmppsDomainEventUrlConfig,
  private val outboxEventRepository: OutboxEventRepository,
  private val hmppsQueueService: HmppsQueueService,
) {
  private val log = LoggerFactory.getLogger(this::class.java)

  private val domainTopic by lazy {
    hmppsQueueService.findByTopicId("hmpps-domain-event-topic") ?: throw MissingTopicException("hmpps-domain-event-topic topic not found")
  }

  @Scheduled(fixedDelay = 5000)
  @Transactional
  fun publish() {
    log.info("Start OutboxEventPublisher...")
    val outboxEventsToPublish = outboxEventRepository.findAllByProcessedFalse()
    if (outboxEventsToPublish.isEmpty()) {
      log.info("No events to publish")
      return
    }
    outboxEventsToPublish.forEach {
      val accommodationDataDomainEventType = AccommodationDataDomainEventType.from(it.domainEventType)!!
      val hmppsDomainEventType = when (accommodationDataDomainEventType) {
        AccommodationDataDomainEventType.PROPOSED_ACCOMMODATION_APPROVED -> HmppsDomainEventType.ADDA_PROPOSED_ACCOMMODATION_APPROVED
        AccommodationDataDomainEventType.PROPOSED_ACCOMMODATION_UNAPPROVED -> HmppsDomainEventType.ADDA_PROPOSED_ACCOMMODATION_UNAPPROVED
      }
      val publishResult = publishHmppsDomainEvent(outboxEventEntity = it, hmppsDomainEventType)
      log.info("Emitted SNS event (Message Id: ${publishResult.messageId()}, Sequence Id: ${publishResult.sequenceNumber()}) for Outbox Event: ${it.id} of type: $hmppsDomainEventType")
      outboxEventRepository.save(
        it.copy(processed = true),
      )
    }
  }

  private fun publishHmppsDomainEvent(
    outboxEventEntity: OutboxEventEntity,
    hmppsDomainEventType: HmppsDomainEventType,
  ): PublishResponse {
    val detailUrl = hmppsDomainEventUrlConfig.getUrlForDomainEventId(hmppsDomainEventType, outboxEventEntity.aggregateId)
    val snsEvent = HmppsSnsDomainEvent(
      eventType = hmppsDomainEventType.typeName,
      version = 1,
      description = hmppsDomainEventType.typeDescription,
      detailUrl = detailUrl,
      occurredAt = outboxEventEntity.createdAt.atOffset(ZoneOffset.UTC),
    )
    return domainTopic.snsClient.publish(
      PublishRequest.builder()
        .topicArn(domainTopic.arn)
        .message(objectMapper.writeValueAsString(snsEvent))
        .messageAttributes(
          mapOf(
            "eventType" to MessageAttributeValue.builder().dataType("String").stringValue(snsEvent.eventType).build(),
          ),
        ).build(),
    ).get()
  }
}
