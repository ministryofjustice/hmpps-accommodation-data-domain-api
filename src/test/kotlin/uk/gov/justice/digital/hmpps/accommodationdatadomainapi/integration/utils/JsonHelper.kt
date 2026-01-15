package uk.gov.justice.digital.hmpps.accommodationdatadomainapi.integration.utils

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule

object JsonHelper {
  @JvmStatic
  val objectMapper: ObjectMapper =
    jacksonObjectMapper()
      .registerModule(JavaTimeModule())
      .registerKotlinModule()
      .setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL)
}
