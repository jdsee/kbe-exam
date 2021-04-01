package htw.ai.kbe.songservice.adapter.api

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping

/**
@author Joscha Seelig <jduesentrieb> 2021
 **/
@FeignClient("mail-service")
@RequestMapping("/mail")
interface SimpleMailSenderClient {
    @PostMapping
    fun sendMail(@RequestBody request: SimpleMailRequest);
}

data class SimpleMailRequest(
    val targetEmail: String,
    val subject: String,
    val text: String
)
