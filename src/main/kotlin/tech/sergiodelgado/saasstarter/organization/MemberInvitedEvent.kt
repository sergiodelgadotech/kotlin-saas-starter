package tech.sergiodelgado.saasstarter.organization

import java.util.UUID

data class MemberInvitedEvent(
    val organizationId: UUID,
    val memberId: UUID,
    val invitedExternalUserId: String,
    val actorExternalUserId: String,
)
