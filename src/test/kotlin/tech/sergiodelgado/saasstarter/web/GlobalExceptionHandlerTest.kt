package tech.sergiodelgado.saasstarter.web

import io.konform.validation.Validation
import io.konform.validation.constraints.minLength
import org.junit.jupiter.api.Test
import org.springframework.ui.ExtendedModelMap
import strikt.api.expectThat
import strikt.assertions.contains
import strikt.assertions.isEqualTo
import strikt.assertions.isNotEmpty
import strikt.assertions.isNotNull
import tech.sergiodelgado.saasstarter.validation.DomainValidationException
import tech.sergiodelgado.saasstarter.validation.validateOrThrow

private data class TestInput(val value: String)

class GlobalExceptionHandlerTest {

    private val handler = GlobalExceptionHandler()
    private val model = ExtendedModelMap()

    private fun domainValidationException(): DomainValidationException {
        val validation = Validation<TestInput> {
            TestInput::value { minLength(10) hint "Value too short" }
        }
        return try {
            validation.validateOrThrow(TestInput("x"))
            error("Expected DomainValidationException")
        } catch (e: DomainValidationException) { e }
    }

    @Test
    fun `DomainValidationException handler returns 422 view name`() {
        val ex = domainValidationException()
        val view = handler.handleDomainValidation(ex, model)
        expectThat(view).isEqualTo("error/422")
    }

    @Test
    fun `DomainValidationException handler adds errors list to model`() {
        val ex = domainValidationException()
        handler.handleDomainValidation(ex, model)
        val errors = model.getAttribute("errors") as? List<*>
        expectThat(errors).isNotNull().isNotEmpty()
    }

    @Test
    fun `NotFoundException handler returns 404 view name`() {
        val ex = NotFoundException("Resource not found")
        val view = handler.handleNotFound(ex, model)
        expectThat(view).isEqualTo("error/404")
    }

    @Test
    fun `NotFoundException handler adds message to model`() {
        val ex = NotFoundException("Resource not found")
        handler.handleNotFound(ex, model)
        expectThat(model.getAttribute("message") as? String).isEqualTo("Resource not found")
    }

    @Test
    fun `ForbiddenException handler returns 403 view name`() {
        val ex = ForbiddenException("Access denied")
        val view = handler.handleForbidden(ex, model)
        expectThat(view).isEqualTo("error/403")
    }

    @Test
    fun `ForbiddenException handler adds message to model`() {
        val ex = ForbiddenException("Access denied")
        handler.handleForbidden(ex, model)
        expectThat(model.getAttribute("message") as? String).isEqualTo("Access denied")
    }

    @Test
    fun `generic Exception handler returns 500 view name`() {
        val ex = RuntimeException("Unexpected server error")
        val view = handler.handleGeneric(ex, model)
        expectThat(view).isEqualTo("error/500")
    }

    @Test
    fun `DomainValidationException error messages include field path`() {
        val ex = domainValidationException()
        handler.handleDomainValidation(ex, model)
        @Suppress("UNCHECKED_CAST")
        val errors = model.getAttribute("errors") as List<String>
        expectThat(errors.first()).contains("value")
    }
}
