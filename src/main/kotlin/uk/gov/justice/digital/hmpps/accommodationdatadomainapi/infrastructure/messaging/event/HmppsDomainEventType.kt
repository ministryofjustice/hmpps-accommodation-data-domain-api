package uk.gov.justice.digital.hmpps.accommodationdatadomainapi.infrastructure.messaging.event

enum class HmppsDomainEventType(
  val typeName: String,
  val typeDescription: String,
) {
  ADDA_PROPOSED_ACCOMMODATION_APPROVED(
    HmppsAddaDomainEventType.PROPOSED_ACCOMMODATION_APPROVED.value,
    "The proposed address has been approved",
  ),
  ADDA_PROPOSED_ACCOMMODATION_UNAPPROVED(
    HmppsAddaDomainEventType.PROPOSED_ACCOMMODATION_UNAPPROVED.value,
    "The proposed address have been unapproved",
  ),
}

enum class HmppsAddaDomainEventType(val value: String) {
  PROPOSED_ACCOMMODATION_APPROVED("adda.proposed.accommodation.approved"),
  PROPOSED_ACCOMMODATION_UNAPPROVED("adda.proposed.accommodation.unapproved"),
}
