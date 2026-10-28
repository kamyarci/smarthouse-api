package com.petamparo.smarthouse.exception;

import java.util.List;

public class InvalidSettingsException extends RuntimeException {

    private final List<String> errors;

    public InvalidSettingsException(List<String> errors) {
        super("Configurações inválidas");
        this.errors = List.copyOf(errors);
    }

    public List<String> getErrors() {
        return errors;
    }
}
