package it.gov.pagopa.cgn.portal.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ProductCategoryEnum {

    ENTERTAINMENT("Tempo libero"),
    TRAVELLING("Viaggi Trasporti e Mobilità"),
    FOOD_DRINK("Ristoranti e cucina"),
    SERVICES("Servizi"),
    LEARNING("Istruzione e formazione"),
    HOTELS("Hotel"),
    SPORTS("Sport"),
    HEALTH("Salute e benessere"),
    SHOPPING("Shopping");

    private final String descrition;
}
