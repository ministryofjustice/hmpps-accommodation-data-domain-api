package uk.gov.justice.digital.hmpps.accommodationdatadomainapi.application.processor

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.accommodationdatadomainapi.application.service.ProposedAccommodationApplicationService
import uk.gov.justice.digital.hmpps.accommodationdatadomainapi.infrastructure.client.corepersonrecord.CorePersonRecordClient
import uk.gov.justice.digital.hmpps.accommodationdatadomainapi.infrastructure.messaging.event.IncomingHmppsDomainEventType
import uk.gov.justice.digital.hmpps.accommodationdatadomainapi.infrastructure.persistence.entity.ProcessedStatus
import uk.gov.justice.digital.hmpps.accommodationdatadomainapi.infrastructure.persistence.repository.InboxEventRepository
import java.time.Instant

@Profile(value = ["local", "development", "test"])
@Component
class InboxEventProcessor(
  private val inboxEventRepository: InboxEventRepository,
  private val corePersonRecordClient: CorePersonRecordClient,
  private val proposedAccommodationApplicationService: ProposedAccommodationApplicationService,
) {

  private val log = LoggerFactory.getLogger(this::class.java)

  @Scheduled(fixedDelay = 5000)
  @SchedulerLock(
    name = "InboxEventProcessor",
    lockAtMostFor = "PT2M",
    lockAtLeastFor = "PT1S",
  )
  @Transactional
  fun process() {
    log.info("Start InboxEventProcessor...")
    val inboxEvents = inboxEventRepository.findAllByProcessedStatus(ProcessedStatus.PENDING)
    if (inboxEvents.isEmpty()) {
      log.info("No inbox events to process")
      return
    }
    inboxEvents.forEach { inboxEvent ->
      val outgoingHmppsDomainEventType = IncomingHmppsDomainEventType.from(inboxEvent.eventType)
      when (outgoingHmppsDomainEventType) {
        IncomingHmppsDomainEventType.CPR_PROPOSED_ACCOMMODATION_UPDATE -> {
          log.info("Making callback to CPR using detailUrl ${inboxEvent.eventDetailUrl}")
          try {
            val newCprAddress = corePersonRecordClient.fetchAddress(resourceUrl = inboxEvent.eventDetailUrl!!)
            proposedAccommodationApplicationService.upsertAddress(corePersonRecordAddress = newCprAddress)
            inboxEvent.processedStatus = ProcessedStatus.SUCCESS
            inboxEvent.processedAt = Instant.now()
            inboxEventRepository.save(inboxEvent)
          } catch (e: Exception) {
            log.error("Filed to process inbox event with id ${inboxEvent.id} exception ${e.message}")
            inboxEvent.processedStatus = ProcessedStatus.FAILED
          }
        }
        else -> log.error("Unexpected event in inbox with inbox event id ${inboxEvent.id} and event type ${inboxEvent.eventType}")
      }
    }
  }
}
