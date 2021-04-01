package htw.ai.kbe.songservice.adapter.api

import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException
import com.fasterxml.jackson.module.kotlin.MissingKotlinParameterException
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.HttpMediaTypeNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.web.servlet.NoHandlerFoundException
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import javax.persistence.EntityNotFoundException
import javax.validation.ConstraintViolationException

@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice
/**
@author Joscha Seelig <jduesentrieb> 2021
 **/
class ApiResponseExceptionHandler : ResponseEntityExceptionHandler() {
    /**
     * Handle MissingServletRequestParameterException. Triggered when a 'required' request parameter is missing.
     *
     * @param ex      MissingServletRequestParameterException
     * @param headers HttpHeaders
     * @param status  HttpStatus
     * @param request WebRequest
     * @return error response
     */
    override fun handleMissingServletRequestParameter(
            ex: MissingServletRequestParameterException, headers: HttpHeaders,
            status: HttpStatus, request: WebRequest): ResponseEntity<Any> {
        val error = ex.parameterName + " parameter is missing"
        return ResponseEntity(error, HttpStatus.BAD_REQUEST)
    }

    /**
     * Handle HttpMediaTypeNotSupportedException. This one triggers when JSON is invalid as well.
     *
     * @param ex      HttpMediaTypeNotSupportedException
     * @param headers HttpHeaders
     * @param status  HttpStatus
     * @param request WebRequest
     * @return error response
     */
    override fun handleHttpMediaTypeNotSupported(
            ex: HttpMediaTypeNotSupportedException,
            headers: HttpHeaders,
            status: HttpStatus,
            request: WebRequest): ResponseEntity<Any> {
        val message = "${ex.contentType} media type is not supported. Supported media types are ${
            ex.supportedMediaTypes.joinToString(separator = ",")
        }"
        return ResponseEntity(message, HttpStatus.UNSUPPORTED_MEDIA_TYPE)
    }

    /**
     * Handle MethodArgumentNotValidException. Triggered when an object fails @Valid validation.
     *
     * @param ex      the MethodArgumentNotValidException that is thrown when @Valid validation fails
     * @param headers HttpHeaders
     * @param status  HttpStatus
     * @param request WebRequest
     * @return error response
     */
    override fun handleMethodArgumentNotValid(
            ex: MethodArgumentNotValidException,
            headers: HttpHeaders,
            status: HttpStatus,
            request: WebRequest): ResponseEntity<Any> {
        val message = """
            |Validation error: 
            |Field errors: ${ex.bindingResult.fieldErrors}
            |Global errors: ${ex.bindingResult.globalErrors}
        """.trimMargin()
        return ResponseEntity(message, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(MissingKotlinParameterException::class)
    protected fun handleMissingKotlinParameterException(ex: MissingKotlinParameterException) = handleMissingProperty(
            "Following parameter was expected but could not be found in request: ${ex.parameter.name}")

    @ExceptionHandler(UnrecognizedPropertyException::class)
    protected fun handleUnrecognizedPropertyException(ex: UnrecognizedPropertyException) =
            handleMissingProperty(ex.message ?: "One of the required properties could not be found in request.")

    private fun handleMissingProperty(message: String) = ResponseEntity.badRequest().body(message)

    /**
     * Handles javax.validation.ConstraintViolationException. Thrown when @Validated fails.
     *
     * @param ex the ConstraintViolationException
     * @return error response
     */
    @ExceptionHandler(ConstraintViolationException::class)
    protected fun handleConstraintViolation(
            ex: ConstraintViolationException): ResponseEntity<Any> {
        val message = """
            |Validation error: 
            |Field errors: ${ex.constraintViolations}
        """.trimMargin()
        return ResponseEntity(message, HttpStatus.BAD_REQUEST)
    }

    /**
     * Handle NoHandlerFoundException.
     *
     * @param ex
     * @param headers
     * @param status
     * @param request
     * @return
     */
    override fun handleNoHandlerFoundException(
            ex: NoHandlerFoundException, headers: HttpHeaders, status: HttpStatus, request: WebRequest): ResponseEntity<Any> {
        val message = "Could not find the ${ex.requestURL} method for URL ${ex.httpMethod}"
        return ResponseEntity(message, HttpStatus.BAD_REQUEST)
    }

    /**
     * Handle javax.persistence.EntityNotFoundException
     */
    @ExceptionHandler(EntityNotFoundException::class)
    protected fun handleEntityNotFound(ex: EntityNotFoundException): ResponseEntity<String?> {
        return ResponseEntity(ex.message, HttpStatus.NOT_FOUND)
    }

    /**
     * Handle DataIntegrityViolationException, inspects the cause for different DB causes.
     *
     * @param ex the DataIntegrityViolationException
     * @return error response
     */
    @ExceptionHandler(DataIntegrityViolationException::class)
    fun handleDataIntegrityViolation(ex: DataIntegrityViolationException): ResponseEntity<Any> {
        return if (ex.cause is org.hibernate.exception.ConstraintViolationException) {
            ResponseEntity("Database error ${ex.cause}", HttpStatus.CONFLICT)
        } else ResponseEntity(ex.toString(), HttpStatus.INTERNAL_SERVER_ERROR)
    }

    /**
     * Handle Exception, handle generic Exception.class
     *
     * @param ex the Exception
     * @return error response
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    protected fun handleMethodArgumentTypeMismatch(ex: MethodArgumentTypeMismatchException,
                                                   request: WebRequest?): ResponseEntity<Any> {
        val message = "The parameter '${ex.name}' of value '${ex.value}' " +
                "could not be converted to type '${ex.requiredType!!.simpleName}'"
        return ResponseEntity(message, HttpStatus.BAD_REQUEST)
    }
}
