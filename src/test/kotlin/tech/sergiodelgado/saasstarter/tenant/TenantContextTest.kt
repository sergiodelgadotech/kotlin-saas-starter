package tech.sergiodelgado.saasstarter.tenant

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isFalse
import strikt.assertions.isTrue
import java.util.UUID
import java.util.concurrent.CountDownLatch

class TenantContextTest {

    @AfterEach
    fun cleanup() {
        TenantContext.clear()
    }

    @Test
    fun `isPresent returns false when no tenant is set`() {
        expectThat(TenantContext.isPresent()).isFalse()
    }

    @Test
    fun `isPresent returns true after set`() {
        TenantContext.set(UUID.randomUUID())
        expectThat(TenantContext.isPresent()).isTrue()
    }

    @Test
    fun `get returns the value that was set`() {
        val id = UUID.randomUUID()
        TenantContext.set(id)
        expectThat(TenantContext.get()).isEqualTo(id)
    }

    @Test
    fun `clear removes the tenant from context`() {
        TenantContext.set(UUID.randomUUID())
        TenantContext.clear()
        expectThat(TenantContext.isPresent()).isFalse()
    }

    @Test
    fun `get throws when no tenant is set`() {
        assertThrows<IllegalStateException> {
            TenantContext.get()
        }
    }

    @Test
    fun `child thread does not inherit tenant set in parent thread`() {
        TenantContext.set(UUID.randomUUID())

        var childSawTenant = true
        val latch = CountDownLatch(1)
        Thread {
            childSawTenant = TenantContext.isPresent()
            latch.countDown()
        }.start()
        latch.await()

        expectThat(childSawTenant).isFalse()
    }

    @Test
    fun `set overwrites previously set tenant`() {
        val first = UUID.randomUUID()
        val second = UUID.randomUUID()
        TenantContext.set(first)
        TenantContext.set(second)
        expectThat(TenantContext.get()).isEqualTo(second)
    }
}
