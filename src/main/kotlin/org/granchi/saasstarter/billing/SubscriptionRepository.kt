package org.granchi.saasstarter.billing

import org.springframework.data.repository.CrudRepository
import java.util.UUID

interface SubscriptionRepository : CrudRepository<Subscription, UUID> {
    fun findByOrganizationId(organizationId: UUID): Subscription?
    fun findByExternalCustomerId(externalCustomerId: String): Subscription?
    fun findByExternalSubscriptionId(externalSubscriptionId: String): Subscription?
}
