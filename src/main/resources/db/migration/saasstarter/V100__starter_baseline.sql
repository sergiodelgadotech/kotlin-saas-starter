-- V100__starter_baseline.sql — kotlin-saas-starter
-- Multi-tenant baseline: organizations, members, and subscriptions.

CREATE TABLE organizations (
    id         UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    name       VARCHAR(255) NOT NULL,
    slug       VARCHAR(100) NOT NULL UNIQUE,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE members (
    id               UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id  UUID         NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    external_user_id VARCHAR(255) NOT NULL UNIQUE,
    role             VARCHAR(50)  NOT NULL DEFAULT 'MEMBER',
    email            VARCHAR(255) NOT NULL,
    first_name       VARCHAR(255),
    last_name        VARCHAR(255),
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

-- Critical: hit on every authenticated request via MemberRepository.findOrganizationIdByUserId
-- Note: no explicit index needed for external_user_id; the UNIQUE constraint above already creates one.
CREATE INDEX idx_members_organization_id ON members(organization_id);

CREATE TABLE subscriptions (
    id                       UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id          UUID         NOT NULL UNIQUE REFERENCES organizations(id) ON DELETE CASCADE,
    external_customer_id     VARCHAR(255) NOT NULL UNIQUE,
    external_subscription_id VARCHAR(255),
    plan                     VARCHAR(50)  NOT NULL DEFAULT 'STARTER',
    status                   VARCHAR(50)  NOT NULL DEFAULT 'TRIALING',
    current_period_end       TIMESTAMPTZ,
    cancel_at_period_end     BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at               TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_subscriptions_external_customer_id     ON subscriptions(external_customer_id);
CREATE INDEX idx_subscriptions_external_subscription_id ON subscriptions(external_subscription_id);
