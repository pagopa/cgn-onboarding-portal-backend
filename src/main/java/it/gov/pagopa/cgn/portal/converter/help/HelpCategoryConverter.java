package it.gov.pagopa.cgn.portal.converter.help;


import it.gov.pagopa.cgn.portal.exception.InvalidRequestException;
import org.springframework.stereotype.Component;

@Component
public class HelpCategoryConverter {

    private static final String ACCESS = "Accesso";
    private static final String DATAFILLING = "Compilazione Dati";
    private static final String DISCOUNTS = "Agevolazioni";
    private static final String DOCUMENTS = "Documenti";
    private static final String TECHNICALPROBLEM = "Problema Tecnico";
    private static final String CGNOWNERREPORTING = "Segnalazione Titolare di CGN";
    private static final String SUGGESTIONS = "Suggerimenti";
    private static final String OTHER = "Altro";

    public String helpCategoryFromEnum(it.gov.pagopa.cgnonboardingportal.model.HelpRequest.CategoryEnum category) {
        switch (category) {
            case ACCESS:
                return ACCESS;
            case DATA_FILLING:
                return DATAFILLING;
            case DISCOUNTS:
                return DISCOUNTS;
            case DOCUMENTS:
                return DOCUMENTS;
            case TECHNICAL_PROBLEM:
                return TECHNICALPROBLEM;
            case CGN_OWNER_REPORTING:
                return CGNOWNERREPORTING;
            case SUGGESTIONS:
                return SUGGESTIONS;
            case OTHER:
                return OTHER;
            default:
                throw new InvalidRequestException("Invalid help category: " + category.getValue());
        }
    }

    public String helpCategoryFromEnum(it.gov.pagopa.cgnonboardingportal.publicapi.model.HelpRequest.CategoryEnum category) {
        switch (category) {
            case ACCESS:
                return ACCESS;
            case DATA_FILLING:
                return DATAFILLING;
            case DISCOUNTS:
                return DISCOUNTS;
            case DOCUMENTS:
                return DOCUMENTS;
            case TECHNICAL_PROBLEM:
                return TECHNICALPROBLEM;
            case CGN_OWNER_REPORTING:
                return CGNOWNERREPORTING;
            case SUGGESTIONS:
                return SUGGESTIONS;
            case OTHER:
                return OTHER;
            default:
                throw new InvalidRequestException("Invalid help category: " + category.getValue());
        }
    }
}
