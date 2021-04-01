package htw.ai.kbe.songservice.domain.ports

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.http.HttpHeaders
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping

/**
@author Joscha Seelig <jduesentrieb> 2021
 **/
@FeignClient("mail-service")
@RequestMapping("/rest/mail")
interface SimpleMailSenderClient {
    @PostMapping
    fun sendMail(
        @RequestHeader(HttpHeaders.AUTHORIZATION) jwtToken: String,
        @RequestBody request: SimpleMailRequest
    )
}

data class SimpleMailRequest(
    val targetEmail: String,
    val subject: String,
    val text: String
)
