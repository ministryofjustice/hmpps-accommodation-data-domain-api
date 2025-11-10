package uk.gov.justice.digital.hmpps.accommodationdatadomainapi

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class AccommodationDataDomainApi

fun main(args: Array<String>) {
  runApplication<AccommodationDataDomainApi>(*args)
}
