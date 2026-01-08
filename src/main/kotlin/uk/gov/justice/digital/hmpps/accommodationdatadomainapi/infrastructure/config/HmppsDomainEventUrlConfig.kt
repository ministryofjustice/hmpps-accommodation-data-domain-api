package uk.gov.justice.digital.hmpps.accommodationdatadomainapi.infrastructure.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import uk.gov.justice.digital.hmpps.accommodationdatadomainapi.infrastructure.messaging.event.HmppsDomainEventType
import java.util.UUID

@Configuration
class HmppsDomainEventUrlConfig(
  @Value($$"${event.details-url.proposed-accommodation-approved}")
  val proposedAccommodationApprovedEventDetailsUrl: String,
  @Value($$"${event.details-url.proposed-accommodation-unapproved}")
  val proposedAccommodationUnapprovedEventDetailsUrl: String,
) {

  fun getUrlForDomainEventId(domainEventType: HmppsDomainEventType, id: UUID): String {
    val template = when (domainEventType) {
      HmppsDomainEventType.ADDA_PROPOSED_ACCOMMODATION_APPROVED -> UrlTemplate(proposedAccommodationApprovedEventDetailsUrl)
      HmppsDomainEventType.ADDA_PROPOSED_ACCOMMODATION_UNAPPROVED -> UrlTemplate(proposedAccommodationUnapprovedEventDetailsUrl)
    }
    return template.resolve("id", id.toString())
  }
}

class UrlTemplate(val template: String) {
  fun resolve(args: Map<String, String>) = args.entries.fold(template) { acc, (key, value) -> acc.replace("#$key", value) }
  fun resolve(paramName: String, paramValue: String) = resolve(mapOf(paramName to paramValue))
}
