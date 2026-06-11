-- V102__starter_member_profile.sql — kotlin-saas-starter
-- Add display profile fields to members, populated at invite time and refreshed from OIDC claims on login.

ALTER TABLE members ADD COLUMN email      VARCHAR(255) NOT NULL DEFAULT '';
ALTER TABLE members ADD COLUMN first_name VARCHAR(255);
ALTER TABLE members ADD COLUMN last_name  VARCHAR(255);
