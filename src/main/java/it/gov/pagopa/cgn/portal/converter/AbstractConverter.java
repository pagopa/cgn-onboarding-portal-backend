package it.gov.pagopa.cgn.portal.converter;

import it.gov.pagopa.cgn.portal.exception.InvalidRequestException;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;


public abstract class AbstractConverter<E, D> {

    protected static InvalidRequestException getInvalidEnumMapping(String inputString) {
        return new InvalidRequestException("Enum mapping not found for " + inputString);
    }

    protected abstract Function<E, D> toDtoFunction();

    protected abstract Function<D, E> toEntityFunction();

    public Optional<D> toDto(Optional<E> entityOpt) {
        return convertToDto(entityOpt, toDtoFunction());
    }

    public D toDto(E entity) {
        return toDto(Optional.ofNullable(entity)).orElse(null);
    }

    public Collection<D> toDtoCollection(Collection<E> entityCollection) {
        return CollectionUtils.isEmpty(entityCollection) ?
               Collections.emptyList():
               entityCollection.stream().map(toDtoFunction()).collect(Collectors.toList());
    }

    public Optional<E> toEntity(Optional<D> dtoOpt) {
        return convertToEntity(dtoOpt, toEntityFunction());
    }

    public E toEntity(D dto) {
        return toEntity(Optional.ofNullable(dto)).orElse(null);
    }

    public Collection<E> toEntityCollection(Collection<D> dtoCollection) {
        return CollectionUtils.isEmpty(dtoCollection) ?
               Collections.emptyList():
               dtoCollection.stream().map(toEntityFunction()).collect(Collectors.toList());
    }

    protected Optional<D> convertToDto(Optional<E> entityOpt, Function<E, D> function) {
        return entityOpt.isEmpty() ? Optional.empty():Optional.of(function.apply(entityOpt.get()));
    }

    protected Optional<E> convertToEntity(Optional<D> entityOpt, Function<D, E> function) {
        return entityOpt.isEmpty() ? Optional.empty():Optional.of(function.apply(entityOpt.get()));
    }

}