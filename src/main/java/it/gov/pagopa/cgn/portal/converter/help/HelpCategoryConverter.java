package it.gov.pagopa.cgn.portal.converter.help;


import it.gov.pagopa.cgn.portal.exception.InvalidRequestException;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class HelpCategoryConverter {

    private static final Map<String, String> CATEGORY_LOCALIZATION_MAP = Map.of("ACCESS",
                                                                                "Accesso",
                                                                                "DATA_FILLING",
                                                                                "Compilazione Dati",
                                                                                "DISCOUNTS",
                                                                                "Agevolazioni",
                                                                                "DOCUMENTS",
                                                                                "Documenti",
                                                                                "TECHNICAL_PROBLEM",
                                                                                "Problema Tecnico",
                                                                                "CGN_OWNER_REPORTING",
                                                                                "Segnalazione Titolare di CGN",
                                                                                "SUGGESTIONS",
                                                                                "Suggerimenti",
                                                                                "OTHER",
                                                                                "Altro");
    
    public String helpCategoryFromEnum(Enum<?> categoryEnum) {
        String key = categoryEnum.name();
        String localized = CATEGORY_LOCALIZATION_MAP.get(key);
        if (localized==null) {
            throw new InvalidRequestException("Invalid help category: " + key);
        }
        return localized;
    }
}
