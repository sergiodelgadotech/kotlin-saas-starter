package org.granchi.saasstarter.web

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus

class NotFoundException(message: String) : RuntimeException(message)
class ForbiddenException(message: String) : RuntimeException(message)

import org.granchi.saasstarter.validation.DomainValidationException

@ControllerAdvice
class GlobalExceptionHandler {

    private val log = LoggerFactory.getLogger(javaClass)

    @ExceptionHandler(DomainValidationException::class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    fun handleDomainValidation(ex: DomainValidationException, model: Model): String {
        model.addAttribute("errors", ex.errors.map { "${it.dataPath}: ${it.message}" })
        return "error/422"
    }

    @ExceptionHandler(NotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleNotFound(ex: NotFoundException, model: Model): String {
        model.addAttribute("message", ex.message)
        return "error/404"
    }

    @ExceptionHandler(ForbiddenException::class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    fun handleForbidden(ex: ForbiddenException, model: Model): String {
        model.addAttribute("message", ex.message)
        return "error/403"
    }

    @ExceptionHandler(Exception::class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    fun handleGeneric(ex: Exception, model: Model): String {
        log.error("Unhandled exception", ex)
        return "error/500"
    }
}
