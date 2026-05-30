package tech.sergiodelgado.saasstarter.organization

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import strikt.api.expectThat
import strikt.assertions.isA
import tech.sergiodelgado.saasstarter.validation.DomainValidationException
import tech.sergiodelgado.saasstarter.validation.validateOrThrow

class OrganizationValidationsTest {

    // --- createOrganization ---

    @Test
    fun `createOrganization passes with valid input`() {
        val cmd = CreateOrganizationCommand("Acme Corp", "acme-corp", "user-1")
        expectThat(OrganizationValidations.createOrganization.validateOrThrow(cmd))
            .isA<CreateOrganizationCommand>()
    }

    @Test
    fun `createOrganization rejects single-char name`() {
        assertThrows<DomainValidationException> {
            OrganizationValidations.createOrganization.validateOrThrow(
                CreateOrganizationCommand("A", "acme", "user-1")
            )
        }
    }

    @Test
    fun `createOrganization rejects name exceeding 255 characters`() {
        assertThrows<DomainValidationException> {
            OrganizationValidations.createOrganization.validateOrThrow(
                CreateOrganizationCommand("A".repeat(256), "acme", "user-1")
            )
        }
    }

    @Test
    fun `createOrganization rejects single-char slug`() {
        assertThrows<DomainValidationException> {
            OrganizationValidations.createOrganization.validateOrThrow(
                CreateOrganizationCommand("Acme", "a", "user-1")
            )
        }
    }

    @Test
    fun `createOrganization rejects slug with uppercase letters`() {
        assertThrows<DomainValidationException> {
            OrganizationValidations.createOrganization.validateOrThrow(
                CreateOrganizationCommand("Acme", "Acme-Corp", "user-1")
            )
        }
    }

    @Test
    fun `createOrganization rejects slug with spaces`() {
        assertThrows<DomainValidationException> {
            OrganizationValidations.createOrganization.validateOrThrow(
                CreateOrganizationCommand("Acme", "acme corp", "user-1")
            )
        }
    }

    @Test
    fun `createOrganization rejects blank ownerExternalUserId`() {
        assertThrows<DomainValidationException> {
            OrganizationValidations.createOrganization.validateOrThrow(
                CreateOrganizationCommand("Acme", "acme", "")
            )
        }
    }

    // --- updateOrganization ---

    @Test
    fun `updateOrganization passes with valid name`() {
        expectThat(OrganizationValidations.updateOrganization.validateOrThrow(
            UpdateOrganizationCommand("New Name")
        )).isA<UpdateOrganizationCommand>()
    }

    @Test
    fun `updateOrganization rejects single-char name`() {
        assertThrows<DomainValidationException> {
            OrganizationValidations.updateOrganization.validateOrThrow(
                UpdateOrganizationCommand("N")
            )
        }
    }

    @Test
    fun `updateOrganization rejects name exceeding 255 characters`() {
        assertThrows<DomainValidationException> {
            OrganizationValidations.updateOrganization.validateOrThrow(
                UpdateOrganizationCommand("N".repeat(256))
            )
        }
    }

    // --- inviteMember ---

    @Test
    fun `inviteMember passes with valid userId`() {
        expectThat(OrganizationValidations.inviteMember.validateOrThrow(
            InviteMemberCommand("user-ext-1", "MEMBER")
        )).isA<InviteMemberCommand>()
    }

    @Test
    fun `inviteMember rejects blank userId`() {
        assertThrows<DomainValidationException> {
            OrganizationValidations.inviteMember.validateOrThrow(
                InviteMemberCommand("", "MEMBER")
            )
        }
    }
}
