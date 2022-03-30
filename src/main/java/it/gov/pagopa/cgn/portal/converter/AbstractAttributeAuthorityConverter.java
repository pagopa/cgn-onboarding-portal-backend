package it.gov.pagopa.cgn.portal.converter;

import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;


public abstract class AbstractAttributeAuthorityConverter<A, B> {

    protected abstract Function<A, B> fromAttributeAuthorityModelFunction();

    protected abstract Function<B, A> toAttributeAuthorityModelFunction();

    public Optional<B> fromAttributeAuthorityModel(Optional<A> maybeAttributeAuthorityModel) {
        return convertFromAttributeAuthorityModel(maybeAttributeAuthorityModel, fromAttributeAuthorityModelFunction());
    }

    public B fromAttributeAuthorityModel(A attributeAuthorityModel) {
        return fromAttributeAuthorityModel(Optional.ofNullable(attributeAuthorityModel)).orElse(null);
    }

    public Collection<B> fromAttributeAuthorityModelCollection(Collection<A> attributeAuthorityModelCollection) {
        return CollectionUtils.isEmpty(attributeAuthorityModelCollection) ?
                Collections.emptyList() :
                attributeAuthorityModelCollection.stream().map(fromAttributeAuthorityModelFunction()).collect(Collectors.toList());
    }

    public Optional<A> toAttributeAuthorityModel(Optional<B> maybeBackofficeModel) {
        return convertToAttributeAuthorityModel(maybeBackofficeModel, toAttributeAuthorityModelFunction());
    }

    public A toAttributeAuthorityModel(B backofficeModel) {
        return toAttributeAuthorityModel(Optional.ofNullable(backofficeModel)).orElse(null);
    }

    public Collection<A> toAttributeAuthorityModelCollection(Collection<B> backofficeModelCollection) {
        return CollectionUtils.isEmpty(backofficeModelCollection) ?
                Collections.emptyList() :
                backofficeModelCollection.stream().map(toAttributeAuthorityModelFunction()).collect(Collectors.toList());
    }

    protected Optional<B> convertFromAttributeAuthorityModel(Optional<A> maybeAttributeAuthorityModel, Function<A, B> function) {
        return maybeAttributeAuthorityModel.isEmpty() ? Optional.empty() : Optional.of(function.apply(maybeAttributeAuthorityModel.get()));
    }

    protected Optional<A> convertToAttributeAuthorityModel(Optional<B> maybeAttributeAuthorityModel, Function<B, A> function) {
        return maybeAttributeAuthorityModel.isEmpty() ? Optional.empty() : Optional.of(function.apply(maybeAttributeAuthorityModel.get()));
    }

}