package com.revolut.money_transfer.api;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.revolut.money_transfer.api.exception.BadRequestException;
import com.revolut.money_transfer.api.exception.ErrorCodes;
import spark.Request;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class BaseEndpoint {

    private static final String ID_PARAMETER = "id";

    private final Gson gson;
    private final Validator validator;

    public BaseEndpoint(Gson gson) {
        this.gson = gson;
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    protected Long getIdFromRequest(Request request) {
        try {
            return Long.valueOf(request.params(ID_PARAMETER));
        } catch (NumberFormatException nfe) {
            throw new BadRequestException("Bad id parameter type", ErrorCodes.BAD_RESOURCE_ID_TYPE);
        }
    }

    protected <T> T getValidObject(Request request, Class<T> clazz) {
        T object;
        try {
            object = gson.fromJson(request.body(), clazz);
        } catch (JsonSyntaxException jse) {
            throw new BadRequestException("Json Validation failed", ErrorCodes.VALIDATION_ERROR_JSON_SYNTAX);
        }
        Set<ConstraintViolation<T>> violations = validator.validate(object);
        if (!violations.isEmpty()) {
            Map<String, String> validationErrors = violations.stream()
                    .collect(Collectors.toMap(violation -> violation.getPropertyPath().toString(), ConstraintViolation::getMessage));
            throw new BadRequestException("Validation failed", ErrorCodes.VALIDATION_ERROR, validationErrors);
        }
        return object;
    }

}
