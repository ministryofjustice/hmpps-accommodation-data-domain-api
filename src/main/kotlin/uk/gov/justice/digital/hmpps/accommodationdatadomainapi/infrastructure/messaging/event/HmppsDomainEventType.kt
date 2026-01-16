package uk.gov.justice.digital.hmpps.accommodationdatadomainapi.infrastructure.messaging.event

enum class HmppsDomainEventType(
  val typeName: String,
  val typeDescription: String,
) {
  ADDA_PROPOSED_ACCOMMODATION_APPROVED(
    "adda.proposed.accommodation.approved",
    "The proposed accommodation has been approved",
  ),
  ADDA_PROPOSED_ACCOMMODATION_UNAPPROVED(
  "adda.proposed.accommodation.unapproved",
    "The proposed accommodation have been unapproved",
  ),
}
