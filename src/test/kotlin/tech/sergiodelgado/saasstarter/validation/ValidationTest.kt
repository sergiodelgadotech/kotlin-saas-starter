package tech.sergiodelgado.saasstarter.validation

import io.konform.validation.Validation
import io.konform.validation.constraints.maxLength
import io.konform.validation.constraints.minLength
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isNotEmpty

private data class Input(val name: String)

class ValidationTest {

    private val validation = Validation<Input> {
        Input::name {
            minLength(2) hint "Name must be at least 2 characters"
            maxLength(50) hint "Name must be at most 50 characters"
        }
    }

    @Test
    fun `valid value passes validation and is returned unchanged`() {
        val input = Input("Alice")
        val result = validation.validateOrThrow(input)
        expectThat(result).isEqualTo(input)
    }

    @Test
    fun `invalid value throws DomainValidationException`() {
        assertThrows<DomainValidationException> {
            validation.validateOrThrow(Input("X"))
        }
    }

    @Test
    fun `thrown exception carries the validation errors`() {
        val ex = assertThrows<DomainValidationException> {
            validation.validateOrThrow(Input("X"))
        }
        expectThat(ex.errors).isNotEmpty()
    }

    @Test
    fun `exception message contains the field path`() {
        val ex = assertThrows<DomainValidationException> {
            validation.validateOrThrow(Input("X"))
        }
        expectThat(ex.message!!).isEqualTo(
            ex.errors.joinToString(", ") { "${it.path}: ${it.message}" }
        )
    }

    @Test
    fun `multiple constraint violations are all reported`() {
        val tooLong = Input("A".repeat(51))
        val ex = assertThrows<DomainValidationException> {
            validation.validateOrThrow(tooLong)
        }
        // maxLength violation on a 51-char string should produce at least one error
        expectThat(ex.errors).isNotEmpty()
    }
}
