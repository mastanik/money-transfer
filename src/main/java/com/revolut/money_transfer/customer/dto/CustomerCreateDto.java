package com.revolut.money_transfer.customer.dto;

import com.revolut.money_transfer.api.validation.LocalDateFormat;

import javax.validation.constraints.NotNull;

public class CustomerCreateDto {
    @NotNull
    private final String firstName;
    @NotNull
    private final String lastName;
    @NotNull
    @LocalDateFormat
    private final String dateOfBirth;

    public CustomerCreateDto(String firstName, String lastName, String dateOfBirth) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.dateOfBirth = dateOfBirth;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    @Override
    public String toString() {
        return "CustomerCreateDto{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", dateOfBirth=" + dateOfBirth +
                '}';
    }
}
