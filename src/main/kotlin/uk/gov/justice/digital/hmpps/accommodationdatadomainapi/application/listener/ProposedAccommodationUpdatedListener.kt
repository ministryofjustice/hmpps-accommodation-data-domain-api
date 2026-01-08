package uk.gov.justice.digital.hmpps.accommodationdatadomainapi.application.listener

import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener
import uk.gov.justice.digital.hmpps.accommodationdatadomainapi.domain.event.ProposedAccommodationUpdatedEvent

@Component
class ProposedAccommodationUpdatedListener {

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  fun handle(event: ProposedAccommodationUpdatedEvent) {
    println("Received ProposedAccommodationUpdatedEvent: $event")
  }
}
