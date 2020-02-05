package com.revolut.money_transfer.customer.dto;

import java.time.LocalDate;

public class CustomerDto {
    private final Long id;
    private final String firstName;
    private final String lastName;
    private final LocalDate dateOfBirth;

    private CustomerDto(Long id, String firstName, String lastName, LocalDate dateOfBirth) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.dateOfBirth = dateOfBirth;
    }

    public static class CustomerDtoBuilder {
        private Long id;
        private String firstName;
        private String lastName;
        private LocalDate dateOfBirth;

        public CustomerDtoBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public CustomerDtoBuilder firstName(String firstName) {
            this.firstName = firstName;
            return this;
        }

        public CustomerDtoBuilder lastName(String lastName) {
            this.lastName = lastName;
            return this;
        }

        public CustomerDtoBuilder dateOfBirth(LocalDate dateOfBirth) {
            this.dateOfBirth = dateOfBirth;
            return this;
        }

        public CustomerDto build() {
            return new CustomerDto(id, firstName, lastName, dateOfBirth);
        }
    }

    public Long getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }
}
