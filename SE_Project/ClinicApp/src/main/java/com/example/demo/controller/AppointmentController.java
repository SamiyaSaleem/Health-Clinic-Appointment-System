package com.example.demo.controller;
import com.example.demo.entity.Appointment;
import com.example.demo.repository.AppointmentRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/appointments")
public class AppointmentController {

    private final AppointmentRepository repo;

    public AppointmentController(AppointmentRepository repo) {
        this.repo = repo;
    }

    @GetMapping("/new")
    public String showForm() {
        return "appointment";
    }

    @PostMapping("/add")
    public String addAppointment(
            @RequestParam String patientName,
            @RequestParam String doctorName,
            @RequestParam String date,
            @RequestParam String time
    ) {
        Appointment a = new Appointment(patientName, doctorName, date, time);
        repo.save(a);
        return "redirect:/appointments/new?success=true";
    }

    @GetMapping("/list")
    public String viewAppointments(Model model) {
        model.addAttribute("appointments", repo.findAll());
        return "appointments-list";
    }

    @GetMapping("/delete/{id}")
    public String deleteAppointment(@PathVariable Long id) {
        repo.deleteById(id);
        return "redirect:/appointments/list?deleted=true";
    }

    @GetMapping("/edit/{id}")
    public String editAppointmentPage(@PathVariable Long id, Model model) {
        Appointment appointment = repo.findById(id).orElseThrow();
        model.addAttribute("appointment", appointment);
        return "edit-appointment";
    }

    @PostMapping("/update")
    public String updateAppointment(
            @RequestParam Long id,
            @RequestParam String patientName,
            @RequestParam String doctorName,
            @RequestParam String date,
            @RequestParam String time
    ) {
        Appointment appointment = repo.findById(id).orElseThrow();

        appointment.setPatientName(patientName);
        appointment.setDoctorName(doctorName);
        appointment.setDate(date);
        appointment.setTime(time);

        repo.save(appointment);
        return "redirect:/appointments/list?updated=true";
    }
}