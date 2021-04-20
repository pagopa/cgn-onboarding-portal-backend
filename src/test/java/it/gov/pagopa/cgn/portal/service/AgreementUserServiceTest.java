package it.gov.pagopa.cgn.portal.service;

import it.gov.pagopa.cgn.portal.IntegrationAbstractTest;
import it.gov.pagopa.cgn.portal.model.AgreementUserEntity;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("dev")
class AgreementUserServiceTest extends IntegrationAbstractTest {

    @Autowired
    private AgreementUserService userService;

    @Test
    void Create_CreateSubscriptionUserWithSubscriptionIdAndUserId_Ok() {
        AgreementUserEntity userCreated = userService.create();
        Assertions.assertNotNull(userCreated.getSubscriptionId());
        Assertions.assertEquals(36, userCreated.getSubscriptionId().length());
        Assertions.assertNotNull(userCreated.getUserId());
    }

    @Test
    void Update_UpdateSubscriptionUser_ThrowsException() {
        AgreementUserEntity userCreated = userService.create();
        userCreated.setSubscriptionId("NEW_SUBSCRIPTION");
        Assertions.assertThrows(Exception.class, () -> {
            userRepository.save(userCreated);
        });

    }
}