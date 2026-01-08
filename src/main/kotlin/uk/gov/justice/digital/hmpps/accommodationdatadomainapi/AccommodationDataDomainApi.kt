package uk.gov.justice.digital.hmpps.accommodationdatadomainapi

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class AccommodationDataDomainApi

fun main(args: Array<String>) {
  runApplication<AccommodationDataDomainApi>(*args)
}
