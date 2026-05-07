package org.granchi.saasstarter.tenant

import java.util.UUID

/**
 * Resolves the tenant ID for an authenticated user.
 *
 * The starter library doesn't know how your app stores user-to-tenant
 * mappings — that's domain-specific. Implement this interface in your
 * app to let the [TenantInterceptor] do its work.
 *
 * Typical implementation:
 * ```kotlin
 * @Component
 * class MemberTenantResolver(
 *     private val memberRepository: MemberRepository
 * ) : TenantResolver {
 *     override fun resolveTenantId(userId: String): UUID? =
 *         memberRepository.findOrganizationIdByUserId(userId)
 * }
 * ```
 */
interface TenantResolver {
    fun resolveTenantId(userId: String): UUID?
}
