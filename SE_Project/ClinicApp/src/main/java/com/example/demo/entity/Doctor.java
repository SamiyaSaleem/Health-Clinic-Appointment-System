package com.example.demo.entity;

import jakarta.persistence.*;
import java.time.LocalTime;
import java.time.LocalDateTime;

@Entity
@Table(name = "doctors")
public class Doctor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    private User user;

    private String specialization;
    private String qualifications;
    private Integer experienceYears;

    // e.g., "MON,TUE,WED,THU,FRI"
    private String availableDays;

    private LocalTime availableTimeStart;
    private LocalTime availableTimeEnd;

    @Column(nullable = false)
    private Integer appointmentDurationMinutes = 30;

    @Column(nullable = false)
    private boolean isActive = true;

    @Column(nullable = false)
    private boolean isApproved = false;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // Constructors
    public Doctor() {}

    public Doctor(User user, String specialization, String qualifications, Integer experienceYears) {
        this.user = user;
        this.specialization = specialization;
        this.qualifications = qualifications;
        this.experienceYears = experienceYears;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }

    public String getQualifications() { return qualifications; }
    public void setQualifications(String qualifications) { this.qualifications = qualifications; }

    public Integer getExperienceYears() { return experienceYears; }
    public void setExperienceYears(Integer experienceYears) { this.experienceYears = experienceYears; }

    public String getAvailableDays() { return availableDays; }
    public void setAvailableDays(String availableDays) { this.availableDays = availableDays; }

    public LocalTime getAvailableTimeStart() { return availableTimeStart; }
    public void setAvailableTimeStart(LocalTime availableTimeStart) { this.availableTimeStart = availableTimeStart; }

    public LocalTime getAvailableTimeEnd() { return availableTimeEnd; }
    public void setAvailableTimeEnd(LocalTime availableTimeEnd) { this.availableTimeEnd = availableTimeEnd; }

    public Integer getAppointmentDurationMinutes() { return appointmentDurationMinutes; }
    public void setAppointmentDurationMinutes(Integer appointmentDurationMinutes) { this.appointmentDurationMinutes = appointmentDurationMinutes; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public boolean isApproved() { return isApproved; }
    public void setApproved(boolean approved) { isApproved = approved; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
