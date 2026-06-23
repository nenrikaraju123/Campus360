package com.campus360.admissions.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public class AdmissionApplicationRequest {

    public Long leadId;

    @NotBlank
    @Size(max = 80)
    public String firstName;

    @NotBlank
    @Size(max = 80)
    public String lastName;

    @NotBlank
    @Email
    @Size(max = 180)
    public String email;

    @Size(max = 30)
    public String phone;

    public LocalDate dateOfBirth;
    public String gender;
    public String category;
    public String quota;
    public Long programId;
    public Long departmentId;
    public Long preferredSectionId;
    public String academicYear;
    public String guardianName;
    @Email
    public String guardianEmail;
    public String guardianPhone;
}
