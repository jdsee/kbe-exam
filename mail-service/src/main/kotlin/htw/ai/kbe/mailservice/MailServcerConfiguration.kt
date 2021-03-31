package htw.ai.kbe.mailservice

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.JavaMailSenderImpl

/**
@author Joscha Seelig <jduesentrieb> 2021
 **/

@ConstructorBinding
@ConfigurationProperties(prefix = "mail-sender")
data class MailSenderProperties(
    val host: String,
    val port: Int,
    val username: String,
    val password: String,
    val protocol: String,
    val auth: Boolean,
    val starttlsEnable: Boolean,
    val debug: Boolean
)

@Configuration
@EnableConfigurationProperties(MailSenderProperties::class)
class MailSenderConfiguration(
    private val mailSenderProperties: MailSenderProperties
) {
    @Bean
    fun javaMailSender(): JavaMailSender {
        val mailSender = JavaMailSenderImpl()
        mailSender.host = mailSenderProperties.host
        mailSender.port = mailSenderProperties.port
        mailSender.username = mailSenderProperties.username
        mailSender.password = mailSenderProperties.password

        println(mailSenderProperties.password)

        val props = mailSender.javaMailProperties
        props["mail.transport.protocol"] = mailSenderProperties.protocol
        props["mail.smtp.auth"] = mailSenderProperties.auth
        props["mail.smtp.starttls.enable"] = mailSenderProperties.starttlsEnable
        props["mail.debug"] = mailSenderProperties.debug

        return mailSender
    }
}
