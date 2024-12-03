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
        String value = jp.getValueAsString();
        if (value!=null) {
            value = value.trim();
        }
        return value;
    }

}
