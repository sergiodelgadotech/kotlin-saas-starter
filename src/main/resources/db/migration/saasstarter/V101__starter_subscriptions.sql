-- V101__starter_subscriptions.sql — kotlin-saas-starter
-- Subscription table for billing module.

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
