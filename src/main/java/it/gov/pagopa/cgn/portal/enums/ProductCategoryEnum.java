package it.gov.pagopa.cgn.portal.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ProductCategoryEnum {

    ENTERTAINMENTS("Teatro, cinema e spettacolo"),
    TRAVELS("Viaggi"),
    TRANSPORTATION("Carsharing, mobilità"),
    CONNECTIVITY("Telefonia, servizi internet"),
    BOOKS("Libri, audiolibri, e-book"),
    ARTS("Musei, gallerie, parchi"),
    SPORTS("Sport"),
    HEALTH("Salute e benessere");

    private final String descrition;
}
