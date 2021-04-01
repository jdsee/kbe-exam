package htw.ai.kbe.mailservice.adapter.mail

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus

/**
@author Joscha Seelig <jduesentrieb> 2021
 **/
@RequestMapping("/mail")
interface SimpleMailSenderController {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun sendMail(@RequestBody request: SimpleMailRequest);
}

data class SimpleMailRequest(
    val targetEmail: String,
    val subject: String,
    val text: String
)
