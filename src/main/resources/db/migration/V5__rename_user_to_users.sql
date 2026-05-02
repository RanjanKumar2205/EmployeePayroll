-- Rename 'user' to 'users' so the table name no longer conflicts with
-- the SQL reserved keyword USER (also an issue in H2 for integration tests).
RENAME TABLE user TO users;