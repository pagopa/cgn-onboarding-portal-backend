package it.gov.pagopa.cgn.portal.repository;

import it.gov.pagopa.cgn.portal.model.AddressEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AddressRepository
        extends JpaRepository<AddressEntity, Long> {

    List<AddressEntity> findByProfileId(Long profileId);
}