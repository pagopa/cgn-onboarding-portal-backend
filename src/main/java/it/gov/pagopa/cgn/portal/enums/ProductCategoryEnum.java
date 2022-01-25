package it.gov.pagopa.cgn.portal.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ProductCategoryEnum {

    BANKING_SERVICES("Servizi bancari"),
    CULTURE_AND_ENTERTAINMENT("Cultura e tempo libero"),
    HEALTH("Salute e benessere"),
    HOME("Casa"),
    JOB_OFFERS("Lavoro e tirocini"),
    LEARNING("Istruzione e formazione"),
    SPORTS("Sport"),
    SUSTAINABLE_MOBILITY("Mobilità sostenibile"),
    TELEPHONY_AND_INTERNET("Telefonia e internet"),
    TRAVELLING("Viaggi e trasporti");

    private final String description;
}
