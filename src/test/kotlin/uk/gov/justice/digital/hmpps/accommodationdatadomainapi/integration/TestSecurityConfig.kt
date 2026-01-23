package uk.gov.justice.digital.hmpps.accommodationdatadomainapi.integration

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.filter.OncePerRequestFilter

@TestConfiguration
class TestSecurityConfig {

  @Bean
  fun testSecurityFilterChain(http: HttpSecurity): SecurityFilterChain = http
    .authorizeHttpRequests { it.anyRequest().authenticated() }
    .addFilterBefore(
      TestAuthenticationFilter(),
      UsernamePasswordAuthenticationFilter::class.java,
    )
    .csrf { it.disable() }
    .build()
}

class TestAuthenticationFilter : OncePerRequestFilter() {

  override fun doFilterInternal(
    request: HttpServletRequest,
    response: HttpServletResponse,
    filterChain: FilterChain,
  ) {
    val authentication = UsernamePasswordAuthenticationToken(
      "test-user",
      null,
      listOf(SimpleGrantedAuthority("ROLE_PROBATION")),
    )

    SecurityContextHolder.getContext().authentication = authentication
    filterChain.doFilter(request, response)
  }
}
