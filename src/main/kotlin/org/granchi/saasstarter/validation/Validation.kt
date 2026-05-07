package org.granchi.saasstarter.validation

import io.konform.validation.Invalid
import io.konform.validation.Validation
import io.konform.validation.ValidationError
import io.konform.validation.jsonschema.maxLength
import io.konform.validation.jsonschema.minLength
import io.konform.validation.jsonschema.pattern

// Reusable extension — throws if validation fails, returns value if valid
fun <T> Validation<T>.validate(value: T): T {
    val result = this(value)
    if (result is Invalid) {
        val messages = result.errors.joinToString(", ") { "${it.dataPath}: ${it.message}" }
        throw DomainValidationException(messages, result.errors)
    }
    return value
}

class DomainValidationException(
    message: String,
    val errors: List<ValidationError>
) : RuntimeException(message)
