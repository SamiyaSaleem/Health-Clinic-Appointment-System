-- Sample Data for AppointCare Solutions

-- Admin User (password: Admin@123)
MERGE INTO users (full_name, email, phone, address, date_of_birth, password_hash, role, enabled, failed_login_attempts, account_locked, created_at)
KEY (email)
VALUES ('Admin User', 'admin@appointcare.com', '03001234567', 'Clinic, Karachi', '1980-01-01', '$2a$10$slYQmyNdGzin7olVZiYUOOjJ4F4xVw3fwlBH3fOFGDELB5L8byxzy', 'ADMIN', true, 0, false, NOW());

-- Sample Patients (password: Patient@123)
MERGE INTO users (full_name, email, phone, address, date_of_birth, password_hash, role, enabled, failed_login_attempts, account_locked, created_at)
KEY (email)
VALUES ('Ahmed Khan', 'ahmed@patient.com', '03101234567', 'Karachi', '1990-05-15', '$2a$10$xZ4kT5mV6l9nX2yQ4wL8l.5B7pJ2kG8mN3oP1qR5sT7uV9wZ1aB', 'PATIENT', true, 0, false, NOW());

MERGE INTO users (full_name, email, phone, address, date_of_birth, password_hash, role, enabled, failed_login_attempts, account_locked, created_at)
KEY (email)
VALUES ('Fatima Ali', 'fatima@patient.com', '03111234567', 'Islamabad', '1988-03-22', '$2a$10$xZ4kT5mV6l9nX2yQ4wL8l.5B7pJ2kG8mN3oP1qR5sT7uV9wZ1aB', 'PATIENT', true, 0, false, NOW());

-- Sample Doctors (password: Doctor@123) - Approved
MERGE INTO users (full_name, email, phone, address, date_of_birth, password_hash, role, enabled, failed_login_attempts, account_locked, created_at)
KEY (email)
VALUES ('Dr. Sarah Khan', 'sarah.khan@doctor.com', '03201234567', 'Clinic, Karachi', '1985-07-10', '$2a$10$Z3bM7xJ5kL9pQ2wR4yT8m.6C9dE1fG3hI5jK7lM9nO1pQ3rS5tU', 'DOCTOR', true, 0, false, NOW());

MERGE INTO users (full_name, email, phone, address, date_of_birth, password_hash, role, enabled, failed_login_attempts, account_locked, created_at)
KEY (email)
VALUES ('Dr. Ahmed Raza', 'ahmed.raza@doctor.com', '03211234567', 'Clinic, Lahore', '1982-11-20', '$2a$10$Z3bM7xJ5kL9pQ2wR4yT8m.6C9dE1fG3hI5jK7lM9nO1pQ3rS5tU', 'DOCTOR', true, 0, false, NOW());

MERGE INTO users (full_name, email, phone, address, date_of_birth, password_hash, role, enabled, failed_login_attempts, account_locked, created_at)
KEY (email)
VALUES ('Dr. Fatima Malik', 'fatima.malik@doctor.com', '03221234567', 'Clinic, Islamabad', '1987-09-15', '$2a$10$Z3bM7xJ5kL9pQ2wR4yT8m.6C9dE1fG3hI5jK7lM9nO1pQ3rS5tU', 'DOCTOR', true, 0, false, NOW());

-- Additional Doctors (password: Doctor@123) - Approved
MERGE INTO users (full_name, email, phone, address, date_of_birth, password_hash, role, enabled, failed_login_attempts, account_locked, created_at)
KEY (email)
VALUES ('Dr. Ayesha Noor', 'ayesha.noor@doctor.com', '03231234567', 'Clinic, Karachi', '1986-02-12', '$2a$10$Z3bM7xJ5kL9pQ2wR4yT8m.6C9dE1fG3hI5jK7lM9nO1pQ3rS5tU', 'DOCTOR', true, 0, false, NOW());

MERGE INTO users (full_name, email, phone, address, date_of_birth, password_hash, role, enabled, failed_login_attempts, account_locked, created_at)
KEY (email)
VALUES ('Dr. Bilal Hussain', 'bilal.hussain@doctor.com', '03241234567', 'Clinic, Lahore', '1983-06-05', '$2a$10$Z3bM7xJ5kL9pQ2wR4yT8m.6C9dE1fG3hI5jK7lM9nO1pQ3rS5tU', 'DOCTOR', true, 0, false, NOW());

MERGE INTO users (full_name, email, phone, address, date_of_birth, password_hash, role, enabled, failed_login_attempts, account_locked, created_at)
KEY (email)
VALUES ('Dr. Sana Iqbal', 'sana.iqbal@doctor.com', '03251234567', 'Clinic, Islamabad', '1984-10-18', '$2a$10$Z3bM7xJ5kL9pQ2wR4yT8m.6C9dE1fG3hI5jK7lM9nO1pQ3rS5tU', 'DOCTOR', true, 0, false, NOW());

MERGE INTO users (full_name, email, phone, address, date_of_birth, password_hash, role, enabled, failed_login_attempts, account_locked, created_at)
KEY (email)
VALUES ('Dr. Usman Tariq', 'usman.tariq@doctor.com', '03261234567', 'Clinic, Karachi', '1981-01-30', '$2a$10$Z3bM7xJ5kL9pQ2wR4yT8m.6C9dE1fG3hI5jK7lM9nO1pQ3rS5tU', 'DOCTOR', true, 0, false, NOW());

MERGE INTO users (full_name, email, phone, address, date_of_birth, password_hash, role, enabled, failed_login_attempts, account_locked, created_at)
KEY (email)
VALUES ('Dr. Hira Siddiqui', 'hira.siddiqui@doctor.com', '03271234567', 'Clinic, Lahore', '1989-08-09', '$2a$10$Z3bM7xJ5kL9pQ2wR4yT8m.6C9dE1fG3hI5jK7lM9nO1pQ3rS5tU', 'DOCTOR', true, 0, false, NOW());

-- Associate Doctors with their profiles
MERGE INTO doctors (user_id, specialization, qualifications, experience_years, available_days, available_time_start, available_time_end, appointment_duration_minutes, is_active, is_approved, created_at)
KEY (user_id)
SELECT id, 'General Physician', 'MBBS, FCPS', 8, 'MON,TUE,WED,THU,FRI', '09:00:00', '17:00:00', 30, true, true, NOW()
FROM users
WHERE email = 'sarah.khan@doctor.com';

MERGE INTO doctors (user_id, specialization, qualifications, experience_years, available_days, available_time_start, available_time_end, appointment_duration_minutes, is_active, is_approved, created_at)
KEY (user_id)
SELECT id, 'Cardiologist', 'MBBS, MD Cardiology', 12, 'MON,WED,FRI', '10:00:00', '16:00:00', 30, true, true, NOW()
FROM users
WHERE email = 'ahmed.raza@doctor.com';

MERGE INTO doctors (user_id, specialization, qualifications, experience_years, available_days, available_time_start, available_time_end, appointment_duration_minutes, is_active, is_approved, created_at)
KEY (user_id)
SELECT id, 'Dermatologist', 'MBBS, DDV', 5, 'TUE,THU,SAT', '11:00:00', '18:00:00', 30, true, true, NOW()
FROM users
WHERE email = 'fatima.malik@doctor.com';

MERGE INTO doctors (user_id, specialization, qualifications, experience_years, available_days, available_time_start, available_time_end, appointment_duration_minutes, is_active, is_approved, created_at)
KEY (user_id)
SELECT id, 'Pediatrician', 'MBBS, FCPS (Peds)', 7, 'MON,TUE,THU,FRI', '09:30:00', '15:30:00', 30, true, true, NOW()
FROM users
WHERE email = 'ayesha.noor@doctor.com';

MERGE INTO doctors (user_id, specialization, qualifications, experience_years, available_days, available_time_start, available_time_end, appointment_duration_minutes, is_active, is_approved, created_at)
KEY (user_id)
SELECT id, 'Orthopedist', 'MBBS, MS Ortho', 10, 'MON,WED,FRI', '10:00:00', '16:30:00', 30, true, true, NOW()
FROM users
WHERE email = 'bilal.hussain@doctor.com';

MERGE INTO doctors (user_id, specialization, qualifications, experience_years, available_days, available_time_start, available_time_end, appointment_duration_minutes, is_active, is_approved, created_at)
KEY (user_id)
SELECT id, 'Neurologist', 'MBBS, MD Neurology', 11, 'TUE,THU,SAT', '11:00:00', '17:00:00', 30, true, true, NOW()
FROM users
WHERE email = 'sana.iqbal@doctor.com';

MERGE INTO doctors (user_id, specialization, qualifications, experience_years, available_days, available_time_start, available_time_end, appointment_duration_minutes, is_active, is_approved, created_at)
KEY (user_id)
SELECT id, 'ENT Specialist', 'MBBS, MS ENT', 9, 'MON,TUE,WED,THU', '12:00:00', '18:00:00', 30, true, true, NOW()
FROM users
WHERE email = 'usman.tariq@doctor.com';

MERGE INTO doctors (user_id, specialization, qualifications, experience_years, available_days, available_time_start, available_time_end, appointment_duration_minutes, is_active, is_approved, created_at)
KEY (user_id)
SELECT id, 'Gynecologist', 'MBBS, FCPS (Gyn)', 8, 'MON,WED,THU,SAT', '09:00:00', '14:00:00', 30, true, true, NOW()
FROM users
WHERE email = 'hira.siddiqui@doctor.com';
