package it.gov.pagopa.cgn.portal.service;

import it.gov.pagopa.cgn.portal.model.AgreementUserEntity;
import it.gov.pagopa.cgn.portal.repository.AgreementUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class AgreementUserService {

    private static final String FAKE_ID = "FAKE_ID";

    private final AgreementUserRepository userRepository;

    public Optional<AgreementUserEntity> findCurrentAgreementUser() {
        return userRepository.findById(FAKE_ID);    //TODO get ID from token
    }

    public AgreementUserEntity create() {
        AgreementUserEntity userEntity = new AgreementUserEntity();
        userEntity.setUserId(FAKE_ID);  //TODO get ID from token
        userEntity.setAgreementId(UUID.randomUUID().toString());
        return userRepository.save(userEntity);
    }

    @Autowired
    public AgreementUserService(AgreementUserRepository userRepository) {
        this.userRepository = userRepository;
    }
}