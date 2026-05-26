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
    val externalUserId: String,
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

    val inviteMember = Validation<InviteMemberCommand> {
        InviteMemberCommand::externalUserId {
            minLength(1) hint "User ID is required"
        }
    }
}
