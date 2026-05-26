package tech.sergiodelgado.saasstarter.tenant

import java.util.UUID

object TenantContext {
    private val current = ThreadLocal<UUID>()

    fun set(tenantId: UUID) = current.set(tenantId)
    fun get(): UUID = current.get() ?: error("No tenant in context")
    fun clear() = current.remove()
    fun isPresent(): Boolean = current.get() != null
}
