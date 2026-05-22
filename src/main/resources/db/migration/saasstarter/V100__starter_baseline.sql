-- V100__starter_baseline.sql — kotlin-saas-starter
-- Multi-tenant baseline: organizations and members.

CREATE TABLE organizations (
    id         UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    name       VARCHAR(255) NOT NULL,
    slug       VARCHAR(100) NOT NULL UNIQUE,
    plan       VARCHAR(50)  NOT NULL DEFAULT 'starter',
    created_at TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE members (
    id               UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id  UUID         NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    external_user_id VARCHAR(255) NOT NULL UNIQUE,
    role             VARCHAR(50)  NOT NULL DEFAULT 'MEMBER',
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

-- Critical: hit on every authenticated request via MemberRepository.findOrganizationIdByUserId
CREATE INDEX idx_members_external_user_id ON members(external_user_id);
CREATE INDEX idx_members_organization_id  ON members(organization_id);
