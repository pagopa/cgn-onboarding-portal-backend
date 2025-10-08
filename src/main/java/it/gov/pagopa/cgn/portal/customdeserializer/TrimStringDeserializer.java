package it.gov.pagopa.cgn.portal.customdeserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

public class TrimStringDeserializer
        extends JsonDeserializer<String> {

    @Override
    public String deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException {
        String s = jp.getValueAsString();
        return (s == null || s.trim().isEmpty()) ? null : s.trim();
    }
}
