package it.gov.pagopa.cgn.portal.util;

import org.springframework.test.util.ReflectionTestUtils;

import java.util.function.Supplier;

public class ReflectiveFieldOverrideRunner {

    private Object target;
    private String fieldName;
    private Object tempValue;

    public ReflectiveFieldOverrideRunner on(Object target) {
        this.target = target;
        return this;
    }

    public ReflectiveFieldOverrideRunner override(String fieldName, Object tempValue) {
        this.fieldName = fieldName;
        this.tempValue = tempValue;
        return this;
    }

    public void run(Runnable action) {
        Object original = ReflectionTestUtils.getField(target, fieldName);
        try {
            ReflectionTestUtils.setField(target, fieldName, tempValue);
            action.run();
        } finally {
            ReflectionTestUtils.setField(target, fieldName, original);
        }
    }

    public <R> R runAndGet(Supplier<R> action) {
        Object original = ReflectionTestUtils.getField(target, fieldName);
        try {
            ReflectionTestUtils.setField(target, fieldName, tempValue);
            return action.get();
        } finally {
            ReflectionTestUtils.setField(target, fieldName, original);
        }
    }
}
