package it.gov.pagopa.cgn.portal.controller.backoffice;

import it.gov.pagopa.cgn.portal.controller.AgreementController;
import it.gov.pagopa.cgn.portal.facade.AgreementFacade;
import it.gov.pagopa.cgn.portal.facade.DiscountFacade;
import it.gov.pagopa.cgn.portal.facade.DocumentFacade;
import it.gov.pagopa.cgn.portal.facade.ProfileFacade;
import it.gov.pagopa.cgn.portal.service.ApiTokenService;
import it.gov.pagopa.cgn.portal.service.HelpService;
import it.gov.pagopa.cgn.portal.service.ProfileService;
import it.gov.pagopa.cgnonboardingportal.model.Profile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.bind.WebDataBinder;

import static org.mockito.Mockito.verify;

public class AgreementBindetrTest {


    @Mock
    ProfileFacade profileFacade;
    @Mock
    DiscountFacade discountFacade;
    @Mock
    DocumentFacade documentFacade;
    @Mock
    AgreementFacade agreementFacade;
    @Mock
    ApiTokenService apiTokenService;
    @Mock
    HelpService helpService;
    @Mock
    ProfileService profileservice;

/*

    private AgreementController agreementController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        agreementController = new AgreementController(agreementFacade, documentFacade, profileFacade, discountFacade, apiTokenService, helpService);
    }

    @Test
    void testInitBinder() {
        String input = "   example   ";

        // Chiamata al metodo getProfile
         agreementController.getProfile(input);
        // Verifica che il metodo registerCustomEditor sia stato chiamato con i parametri corretti

        verify(webDataBinder).registerCustomEditor(String.class, new StringTrimmerEditor(true));
    }
*/




   // @Mock
    private WebDataBinder webDataBinder;


    private AgreementController agreementController;

    @BeforeEach
    void setUp() {
        agreementController = new AgreementController(agreementFacade, documentFacade, profileFacade, discountFacade, apiTokenService, helpService);
        profileFacade = Mockito.mock(ProfileFacade.class);
        ReflectionTestUtils.setField(agreementController, "profileFacade", profileFacade);
    }

    @Test
    void testGetProfileTrim() {
        // Input con spazi iniziali e finali
       agreementController.initBinder(new WebDataBinder("  text  "));

        String input = "  text  ";

        // Chiamata al metodo getProfile
        ResponseEntity<Profile> response = agreementController.getProfile(input);

        // Verifica che profileFacade.getProfile sia stato chiamato con l'argomento trimmato
        verify(profileFacade).getProfile("text");
       // profileService.getProfile(agreementId);
    }




}
