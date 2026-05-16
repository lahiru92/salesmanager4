package com.example.salesmanager4.employees;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import lombok.Data;


// CREATE TABLE IF NOT EXISTS sales_manager.employee
// (
//     id bigint NOT NULL GENERATED ALWAYS AS IDENTITY ( INCREMENT 1 MINVALUE 1 MAXVALUE 9223372036854775807 CACHE 1 ),
//     known_name character varying COLLATE pg_catalog."default",
//     full_name character varying COLLATE pg_catalog."default",
//     address_line_1 character varying COLLATE pg_catalog."default",
//     address_line_2 character varying COLLATE pg_catalog."default",
//     address_line_3 character varying COLLATE pg_catalog."default",
//     address_line_4 character varying COLLATE pg_catalog."default",
//     address_line_5 character varying COLLATE pg_catalog."default",
//     phone_mobile character varying COLLATE pg_catalog."default",
//     phone_home character varying COLLATE pg_catalog."default",
//     phone_office character varying COLLATE pg_catalog."default",
//     email_personal character varying COLLATE pg_catalog."default",
//     email_office character varying COLLATE pg_catalog."default",
//     date_of_birth date,
//     nic_number character varying COLLATE pg_catalog."default",
//     passport_number character varying COLLATE pg_catalog."default",
//     drivers_license_no character varying COLLATE pg_catalog."default",
//     designation character varying COLLATE pg_catalog."default",
//     date_joined date,
//     created_by bigint,
//     created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
//     CONSTRAINT employee_pkey PRIMARY KEY (id)
// )

@Table("employee")
@Data
public class Employee {
    @Id
    private Long id;
    private String knownName;
    private String fullName;
    private String addressLine1;
    private String addressLine2;
    private String addressLine3;
    private String addressLine4;
    private String addressLine5;
    private String phoneMobile;
    private String phoneHome;
    private String phoneOffice;
    private String emailPersonal;
    private String emailOffice;
    private java.time.LocalDate dateOfBirth;
    private String nicNumber;
    private String passportNumber;
    private String driversLicenseNo;
    private String designation;
    private java.time.LocalDate dateJoined;
    // private Long createdBy;
    // private java.time.LocalDateTime createdAt;
}
