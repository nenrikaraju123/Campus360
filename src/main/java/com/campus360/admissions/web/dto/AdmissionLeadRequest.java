package com.campus360.admissions.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AdmissionLeadRequest {

    @NotBlank
    @Size(max = 80)
    public String firstName;

    @NotBlank
    @Size(max = 80)
    public String lastName;

    @Email
    @Size(max = 180)
    public String email;

    @Size(max = 30)
    public String phone;

    public String source;
    public String programInterest;
    public String notes;
    public Long assignedTo;
}
