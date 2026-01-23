package uk.gov.justice.digital.hmpps.accommodationdatadomainapi.infrastructure.messaging.event

enum class IncomingHmppsDomainEventType(
  val typeName: String,
  val typeDescription: String,
) {
  CPR_PROPOSED_ACCOMMODATION_UPDATE(
    "cpr.proposed.accommodation.update",
    "Proposed accommodation update from Core Person Record service",
  ),
  ;

  companion object {
    fun from(eventType: String): IncomingHmppsDomainEventType? = entries.find { it.typeName == eventType }
  }
}
