ALTER TABLE organizations_referents
    DROP CONSTRAINT organizations_referents_organization_fiscal_code_fkey,
    DROP CONSTRAINT organizations_referents_referent_fiscal_code_fkey;

ALTER TABLE organizations_referents
    ADD CONSTRAINT organizations_referents_organization_fiscal_code_fk 
        FOREIGN KEY (organization_fiscal_code) 
        REFERENCES public.organizations (fiscal_code) 
        ON UPDATE CASCADE 
        ON DELETE CASCADE,
        
    ADD CONSTRAINT organizations_referents_referent_fiscal_code_fk 
        FOREIGN KEY (referent_fiscal_code) 
        REFERENCES public.referents (fiscal_code) 
        ON UPDATE CASCADE 
        ON DELETE CASCADE;