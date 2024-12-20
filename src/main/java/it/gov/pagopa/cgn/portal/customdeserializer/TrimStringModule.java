package it.gov.pagopa.cgn.portal.customdeserializer;

import com.fasterxml.jackson.databind.module.SimpleModule;

public class TrimStringModule
        extends SimpleModule {
    public TrimStringModule() {
        addDeserializer(String.class, new TrimStringDeserializer());
    }
}
