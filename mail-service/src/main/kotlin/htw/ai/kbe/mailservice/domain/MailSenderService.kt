package htw.ai.kbe.mailservice.domain

import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Service

/**
@author Joscha Seelig <jduesentrieb> 2021
 **/
@Service
class MailSenderService(
    private val emailSender: JavaMailSender
) {
    fun sendEmail(
        targetEmail: String,
        subject: String,
        text: String,
    ) {
        val message = SimpleMailMessage()
        message.setSubject(subject)
        message.setText(text)
        message.setTo(targetEmail)

        emailSender.send(message)
    }
}
