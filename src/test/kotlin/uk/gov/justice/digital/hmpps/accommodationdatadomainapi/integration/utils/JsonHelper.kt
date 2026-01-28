package uk.gov.justice.digital.hmpps.accommodationdatadomainapi.integration.utils

import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.KotlinModule

object JsonHelper {

  val jsonMapper: JsonMapper =
    JsonMapper.builder()
      .addModule(KotlinModule.Builder().build())
      .build()
}
