package com.example;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@SpringBootApplication
@Controller
public class App {
    private static ConfigurableApplicationContext context;
    private static final Logger logger = LoggerFactory.getLogger(App.class);
    private static List<Patient> patients = new ArrayList<>();
    private static List<Doctor> doctors = new ArrayList<>();
    private static List<Appointment> appointments = new ArrayList<>();
    private static List<Bed> beds = new ArrayList<>();

    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "admin";
    private static final String AUTHENTICATED_SESSION_KEY = "authenticated";
    private static final String MESSAGE_ATTRIBUTE = "message";
    private static final String ERROR_ATTRIBUTE = "error";

    private boolean isAuthenticated(HttpSession session) {
        return session.getAttribute(AUTHENTICATED_SESSION_KEY) != null;
    }

    public static void main(String[] args) {
        // Initialize 15 beds
        for (int i = 1; i <= 15; i++) {
            beds.add(new Bed(i));
        }

        // Start the Spring application
        context = SpringApplication.run(App.class, args);

        // Open the default browser
        openBrowser("http://localhost:8080");
    }

    private static void openBrowser(String url) {
        String os = System.getProperty("os.name").toLowerCase();
        Runtime rt = Runtime.getRuntime();
        try {
            if (os.contains("win")) {
                // Windows
                rt.exec("rundll32 url.dll,FileProtocolHandler " + url);
            } else if (os.contains("mac")) {
                // macOS
                rt.exec("open " + url);
            } else if (os.contains("nix") || os.contains("nux")) {
                // Linux
                String[] browsers = { "google-chrome", "firefox", "mozilla", "epiphany", "konqueror",
                        "netscape", "opera", "links", "lynx" };

                StringBuilder cmd = new StringBuilder();
                for (int i = 0; i < browsers.length; i++)
                    cmd.append(i == 0 ? "" : " || ").append(browsers[i]).append(" \"").append(url).append("\" ");
                rt.exec(new String[] { "sh", "-c", cmd.toString() });
            } else {
                logger.info("Please open {} manually.", url);
            }
        } catch (IOException e) {
            logger.error("Error opening browser: {}", e.getMessage());
        }
    }




    @GetMapping("/billing")
    public String billing(Model model) {
        model.addAttribute("patients", patients);
        return "billing";
    }

    @PostMapping("/generateBill")
    public String generateBill(@RequestParam int patientIndex, Model model) {
        Patient patient = patients.get(patientIndex);
        int appointmentCount = (int) appointments.stream()
            .filter(a -> a.getPatient().equals(patient))
            .count();

        int totalBill = (appointmentCount * 100) + (patient.getDaysOfStay() * 1000);
        patient.setTotalBill(totalBill);

        model.addAttribute("patient", patient);
        model.addAttribute("appointmentCount", appointmentCount);
        model.addAttribute("totalBill", totalBill);
        return "billDetails";
    }
@PostMapping("/clearBill")
public String clearBill(@RequestParam int patientIndex, Model model) {
    if (patientIndex >= 0 && patientIndex < patients.size()) {
        Patient patient = patients.get(patientIndex);
        patient.clearBill();
        model.addAttribute(MESSAGE_ATTRIBUTE, "Bill cleared successfully for " + patient.getName());
    } else {
        model.addAttribute(ERROR_ATTRIBUTE, "Invalid patient selected");
    }
    return "redirect:/billing";
}


    @GetMapping("/login")
    public String loginPage(HttpSession session, Model model) {
        if (isAuthenticated(session)) {
            return "redirect:/";
        }
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username, @RequestParam String password, 
                        HttpSession session, RedirectAttributes redirectAttributes) {
        if (ADMIN_USERNAME.equals(username) && ADMIN_PASSWORD.equals(password)) {
            session.setAttribute(AUTHENTICATED_SESSION_KEY, true);
            redirectAttributes.addFlashAttribute(MESSAGE_ATTRIBUTE, "Login successful");
            return "redirect:/";
        } else {
            redirectAttributes.addFlashAttribute(ERROR_ATTRIBUTE, "Invalid credentials");
            return "redirect:/login";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session, RedirectAttributes redirectAttributes) {
        session.removeAttribute(AUTHENTICATED_SESSION_KEY);
        redirectAttributes.addFlashAttribute(MESSAGE_ATTRIBUTE, "You have been logged out");
        return "redirect:/login";
    }

    @GetMapping("/")
    public String index(HttpSession session, Model model) {
        if (isAuthenticated(session)) {
            model.addAttribute(MESSAGE_ATTRIBUTE, "Welcome to the Hospital Management System");
            return "index";
        } else {
            return "redirect:/login";
        }
    }



    @GetMapping("/patients")
    public String patients(Model model, HttpSession session) {
        if (!isAuthenticated(session)) {
            return "redirect:/login";
        }
        model.addAttribute("patients", patients);
        return "patients";
    }

    // Update all other @GetMapping and @PostMapping methods similarly

    @PostMapping("/addPatient")
    public String addPatient(@RequestParam String name, @RequestParam int age, @RequestParam String gender) {
        patients.add(new Patient(name, age, gender));
        return "redirect:/patients";
    }

    @GetMapping("/doctors")
    public String doctors(Model model) {
        model.addAttribute("doctors", doctors);
        return "doctors";
    }

    @PostMapping("/addDoctor")
    public String addDoctor(@RequestParam String name, @RequestParam String specialization) {
        doctors.add(new Doctor(name, specialization));
        return "redirect:/doctors";
    }

    @GetMapping("/appointments")
    public String appointments(Model model) {
        model.addAttribute("appointments", appointments);
        model.addAttribute("patients", patients);
        model.addAttribute("doctors", doctors);
        return "appointments";
    }

    @PostMapping("/addAppointment")
    public String addAppointment(@RequestParam int patientIndex, @RequestParam int doctorIndex, 
                                 @RequestParam String date, @RequestParam String timeSlot, Model model) {
        // Check if the appointment slot is already booked
        boolean isSlotAvailable = appointments.stream()
            .noneMatch(a -> a.getDoctor().equals(doctors.get(doctorIndex)) 
                         && a.getDate().equals(date) 
                         && a.getTimeSlot().equals(timeSlot));

        if (isSlotAvailable) {
            appointments.add(new Appointment(patients.get(patientIndex), doctors.get(doctorIndex), date, timeSlot));
            patients.get(patientIndex).addToBill(100); // Add appointment charge to the bill
            return "redirect:/appointments";
        } else {
            model.addAttribute(ERROR_ATTRIBUTE, "This appointment slot is already booked. Please choose another.");
            model.addAttribute("appointments", appointments);
            model.addAttribute("patients", patients);
            model.addAttribute("doctors", doctors);
            return "appointments";
        }
    }

    @GetMapping("/beds")
    public String beds(@RequestParam(required = false) String date, Model model) {
        model.addAttribute("beds", beds);
        model.addAttribute("patients", patients);

        if (date != null && !date.isEmpty()) {
            model.addAttribute("selectedDate", date);
            // Update bed status based on the selected date
            for (Bed bed : beds) {
                if (bed.isOccupied() && !bed.getOccupiedDate().equals(date)) {
                    bed.release();
                }
            }
        }

        return "beds";
    }



    @PostMapping("/bookBed")
    public String bookBed(@RequestParam int patientIndex, @RequestParam int bedNumber, 
                          @RequestParam String date, Model model) {
        Patient patient = patients.get(patientIndex);
        Bed bed = beds.get(bedNumber - 1);

        if (!bed.isOccupied()) {
            bed.occupy(patient, date);
            patient.setDaysOfStay(patient.getDaysOfStay() + 1);
            patient.addToBill(1000); // Add bed charge to the bill
            return "redirect:/beds";
        } else {
            model.addAttribute(ERROR_ATTRIBUTE, "This bed is already occupied. Please choose another.");
            model.addAttribute("beds", beds);
            model.addAttribute("patients", patients);
            return "beds";
        }
    }


    @GetMapping("/availableBeds")
    public String availableBeds(@RequestParam String date, Model model) {
        List<Bed> availableBeds = beds.stream()
            .filter(bed -> !bed.isOccupied() || !bed.getOccupiedDate().equals(date))
            .collect(Collectors.toList());

        model.addAttribute("availableBeds", availableBeds);
        model.addAttribute("date", date);
        return "availableBeds";
    }

    @GetMapping("/shutdown")
    public String shutdown() {
        new Thread(() -> {
            try {
                Thread.sleep(1000);
                context.close();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
        return "redirect:/";
    }
}

