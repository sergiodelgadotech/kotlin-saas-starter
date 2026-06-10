package tech.sergiodelgado.saasstarter.organization

import io.konform.validation.Validation
import io.konform.validation.constraints.maxLength
import io.konform.validation.constraints.minLength
import io.konform.validation.constraints.pattern

data class CreateOrganizationCommand(
    val name: String,
    val slug: String,
    val ownerExternalUserId: String,
)

data class InviteMemberCommand(
    val email: String,
    val role: String,
)

data class UpdateOrganizationCommand(
    val name: String,
)

object OrganizationValidations {

    val createOrganization = Validation<CreateOrganizationCommand> {
        CreateOrganizationCommand::name {
            minLength(2) hint "Name must be at least 2 characters"
            maxLength(255) hint "Name must be at most 255 characters"
        }
        CreateOrganizationCommand::slug {
            minLength(2) hint "Slug must be at least 2 characters"
            maxLength(100) hint "Slug must be at most 100 characters"
            pattern(Regex("^[a-z0-9-]+$")) hint
                "Slug can only contain lowercase letters, numbers and hyphens"
        }
        CreateOrganizationCommand::ownerExternalUserId {
            minLength(1) hint "Owner is required"
        }
    }

    val updateOrganization = Validation<UpdateOrganizationCommand> {
        UpdateOrganizationCommand::name {
            minLength(2) hint "Name must be at least 2 characters"
            maxLength(255) hint "Name must be at most 255 characters"
        }
    }

    private val allowedRoles = setOf("ADMIN", "MEMBER")

    val inviteMember = Validation<InviteMemberCommand> {
        InviteMemberCommand::email {
            minLength(1) hint "Email is required"
            pattern(Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) hint "Email must be a valid email address"
        }
        InviteMemberCommand::role {
            minLength(1) hint "Role is required"
            constrain("Role must be ADMIN or MEMBER") { it in allowedRoles }
        }
    }
}
