package com.yipe.finance.exception;

import java.util.function.Supplier;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String resource, Object id) {
        super("%s não encontrado: %s".formatted(resource, id));
    }

    public static Supplier<ResourceNotFoundException> supplier(String resource, Object id) {
        return () -> new ResourceNotFoundException(resource, id);
    }
}
