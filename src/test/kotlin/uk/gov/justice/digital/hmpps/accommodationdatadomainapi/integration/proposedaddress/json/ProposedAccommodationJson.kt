package uk.gov.justice.digital.hmpps.accommodationdatadomainapi.integration.proposedaddress.json

import java.util.UUID

fun proposedAddressesRequestBody(id: UUID, approved: Boolean): String = """
  {
    "id": "$id",
    "address": "13 Test way, London. W5 9GF",
    "approved": $approved
}
""".trimIndent()

fun expectedProposedAddressesResponseBody(id: UUID, approved: Boolean? = null): String = """
  {
    "id": "$id",
    "address": "13 Test way, London. W5 9GF",
    "approved": $approved
  }
""".trimIndent()
