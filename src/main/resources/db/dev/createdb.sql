CREATE SCHEMA remindly;
CREATE SCHEMA liquibase;

CREATE ROLE remindly_adm_role;
GRANT remindly_adm_role TO remindly;

GRANT ALL PRIVILEGES ON SCHEMA remindly TO remindly_adm_role;
GRANT ALL PRIVILEGES ON SCHEMA liquibase TO remindly_adm_role;