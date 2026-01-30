CREATE TABLE organizations (
    fiscal_code VARCHAR(16) NOT NULL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    pec VARCHAR(100) NOT NULL,
    inserted_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE referents (
    fiscal_code VARCHAR(16) NOT NULL PRIMARY KEY
);

CREATE TABLE organizations_referents (
    organization_fiscal_code VARCHAR(16) NOT NULL,
    referent_fiscal_code VARCHAR(16) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    PRIMARY KEY (organization_fiscal_code, referent_fiscal_code),
    FOREIGN KEY (organization_fiscal_code) REFERENCES organizations(fiscal_code),
    FOREIGN KEY (referent_fiscal_code) REFERENCES referents(fiscal_code)
);
