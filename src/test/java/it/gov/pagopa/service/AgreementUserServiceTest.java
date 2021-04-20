package it.gov.pagopa.service;

import it.gov.pagopa.BaseTest;
import it.gov.pagopa.model.AgreementUserEntity;
import it.gov.pagopa.repository.AgreementUserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("dev")
class AgreementUserServiceTest extends BaseTest {

    @Autowired
    private AgreementUserService userService;

    @Autowired
    private AgreementUserRepository userRepository;

    @BeforeEach
    void beforeEach() {
        userRepository.deleteAll();
    }


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