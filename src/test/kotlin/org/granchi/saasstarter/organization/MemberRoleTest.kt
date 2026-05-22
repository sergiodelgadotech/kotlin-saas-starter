package org.granchi.saasstarter.organization

import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.containsExactly
import strikt.assertions.isA
import strikt.assertions.isEqualTo

class MemberRoleTest {

    @Test
    fun `default member role enum has the three canonical values`() {
        expectThat(DefaultMemberRole.values().toList())
            .containsExactly(DefaultMemberRole.OWNER, DefaultMemberRole.ADMIN, DefaultMemberRole.MEMBER)
    }

    @Test
    fun `default member role implements MemberRole`() {
        expectThat(DefaultMemberRole.OWNER as Any).isA<MemberRole>()
    }

    @Test
    fun `name property returns the enum name for serialization`() {
        expectThat(DefaultMemberRole.OWNER.name).isEqualTo("OWNER")
    }
}
