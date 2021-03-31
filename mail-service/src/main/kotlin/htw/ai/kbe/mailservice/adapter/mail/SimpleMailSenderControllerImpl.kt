package htw.ai.kbe.mailservice.adapter.mail

import htw.ai.kbe.mailservice.domain.MailSenderService
import org.springframework.web.bind.annotation.RestController

@RestController
class SimpleMailSenderControllerImpl(
    private val mailSenderService: MailSenderService
) : SimpleMailSenderController {
    override fun sendMail(request: SimpleMailRequest) =
        mailSenderService.sendEmail(request.targetEmail, request.subject, request.text)
}
