package com.smarthallfinder.controller;

import com.smarthallfinder.model.Booking;
import com.smarthallfinder.model.Venue;
import com.smarthallfinder.service.AppService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

@Controller
public class AppController {

    private final AppService service;

    public AppController(AppService service) {
        this.service = service;
    }

    // 1. Directory Filter View
    @GetMapping("/")
    public String directory(
            @RequestParam(required = false) String date,
            @RequestParam(required = false) String shift,
            @RequestParam(required = false) Integer capacity,
            @RequestParam(required = false) String type,
            Model model) {
        
        List<Venue> venues = service.getFilteredVenues(date, shift, capacity, type);

        model.addAttribute("venues", venues);
        model.addAttribute("selectedDate", date);
        model.addAttribute("selectedShift", shift);
        model.addAttribute("selectedCapacity", capacity);
        model.addAttribute("selectedType", type);
        return "directory";
    }

    // 2. Super Admin Panel View
    @GetMapping("/admin")
    public String admin(Model model) {
        model.addAttribute("venues", service.getAllVenues());
        return "admin";
    }

    // 3. Add Venue with Auto Unique Login ID & Image Upload
    @PostMapping("/admin/venue/add")
    public String addVenue(
            @ModelAttribute Venue venue,
            @RequestParam("imageFile") MultipartFile imageFile) throws IOException {

        // Auto Generate Unique Login ID (e.g., SHF-48291)
        String uniqueLoginId = "SHF-" + (int)(Math.random() * 90000 + 10000);
        venue.setLoginId(uniqueLoginId);

        if (imageFile != null && !imageFile.isEmpty()) {
            String uploadDir = "uploads/";
            Path uploadPath = Paths.get(uploadDir);
            
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String originalFileName = imageFile.getOriginalFilename() != null ? 
                    imageFile.getOriginalFilename().replaceAll("\\s+", "_") : "image.jpg";
            String fileName = System.currentTimeMillis() + "_" + originalFileName;

            Path filePath = uploadPath.resolve(fileName);
            Files.copy(imageFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            venue.setImageUrl("/uploads/" + fileName);
        } else if (venue.getImageUrl() == null || venue.getImageUrl().isEmpty()) {
            venue.setImageUrl("https://images.unsplash.com/photo-1519167758481-83f550bb49b3");
        }

        venue.setActive(false);
        service.saveVenue(venue);
        return "redirect:/admin";
    }

    // 4. Update Existing Venue Details
    @PostMapping("/admin/venue/update/{id}")
    public String updateVenue(
            @PathVariable Long id,
            @ModelAttribute Venue updatedVenue,
            @RequestParam("imageFile") MultipartFile imageFile) throws IOException {

        Venue existingVenue = service.getVenue(id);
        existingVenue.setName(updatedVenue.getName());
        existingVenue.setType(updatedVenue.getType());
        existingVenue.setCapacityFrom(updatedVenue.getCapacityFrom());
        existingVenue.setCapacityTo(updatedVenue.getCapacityTo());
        existingVenue.setOwnerName(updatedVenue.getOwnerName());
        existingVenue.setOwnerPhone(updatedVenue.getOwnerPhone());
        existingVenue.setLocation(updatedVenue.getLocation());

        if (imageFile != null && !imageFile.isEmpty()) {
            String uploadDir = "uploads/";
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String originalFileName = imageFile.getOriginalFilename() != null ? 
                    imageFile.getOriginalFilename().replaceAll("\\s+", "_") : "image.jpg";
            String fileName = System.currentTimeMillis() + "_" + originalFileName;

            Path filePath = uploadPath.resolve(fileName);
            Files.copy(imageFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            existingVenue.setImageUrl("/uploads/" + fileName);
        }

        service.saveVenue(existingVenue);
        return "redirect:/admin";
    }

    // 5. Delete Venue
    @PostMapping("/admin/venue/delete/{id}")
    public String deleteVenue(@PathVariable Long id) {
        service.deleteVenue(id);
        return "redirect:/admin";
    }

    // 6. Toggle Active / Inactive Status
    @PostMapping("/admin/venue/toggle/{id}")
    public String toggleVenue(@PathVariable Long id) {
        service.toggleVenue(id);
        return "redirect:/admin";
    }
    @GetMapping("/admin/login")
    public String adminLoginPage() {
        return "adminlogin";
    }

    @GetMapping("/halllogin")
    public String hallLoginPage() {
        return "halllogin";
    }

    @GetMapping("/login")
    public String legacyAdminLoginRedirect() {
        return "redirect:/admin/login";
    }

    @PostMapping("/hall/login")
    public String hallLogin(@RequestParam("loginId") String loginId, HttpSession session) {
        Venue venue = service.getVenueByLoginId(loginId != null ? loginId.trim() : "");
        if (venue != null) {
            session.setAttribute("hallVenueId", venue.getId());
            return "redirect:/dashboard/" + venue.getId();
        } else {
            session.removeAttribute("hallVenueId");
            return "redirect:/halllogin?error=true";
        }
    }

    // 7. Hall Manager Dashboard
    @GetMapping("/dashboard/{id}")
    public String dashboard(@PathVariable Long id, HttpSession session, Authentication authentication, Model model) {
        if (!canAccessVenue(id, session, authentication)) {
            return "redirect:/halllogin";
        }

        model.addAttribute("venue", service.getVenue(id));
        model.addAttribute("bookings", service.getBookings(id));
        model.addAttribute("totalBookings", service.getBookings(id).size());
        model.addAttribute("morningBookings", service.countShift(id, "Morning"));
        model.addAttribute("eveningBookings", service.countShift(id, "Evening"));
        model.addAttribute("today", LocalDate.now().toString());
        return "dashboard";
    }

    // 8. Add Booking from Dashboard
    @PostMapping("/dashboard/{id}/booking/add")
    public String addBooking(@PathVariable Long id, @ModelAttribute Booking booking, HttpSession session, Authentication authentication) {
        if (!canAccessVenue(id, session, authentication)) {
            return "redirect:/halllogin";
        }

        if (!isValidBookingDate(booking.getBookingDate())) {
            return "redirect:/dashboard/" + id + "?dateError=true";
        }

        if (service.hasBookingForSlot(id, booking.getBookingDate(), booking.getShift(), null)) {
            return "redirect:/dashboard/" + id + "?bookingError=exists";
        }

        booking.setVenue(service.getVenue(id));
        service.saveBooking(booking);
        return "redirect:/dashboard/" + id;
    }

    @PostMapping("/dashboard/{id}/booking/{bookingId}/update")
    public String updateBooking(
            @PathVariable Long id,
            @PathVariable Long bookingId,
            @ModelAttribute Booking updatedBooking,
            HttpSession session,
            Authentication authentication) {
        if (!canAccessVenue(id, session, authentication)) {
            return "redirect:/halllogin";
        }

        Booking booking = service.getBooking(bookingId);
        if (!id.equals(booking.getVenue().getId())) {
            return "redirect:/dashboard/" + id;
        }

        if (!isValidBookingDate(updatedBooking.getBookingDate())
                && !booking.getBookingDateInput().equals(updatedBooking.getBookingDate())) {
            return "redirect:/dashboard/" + id + "?dateError=true";
        }

        if (service.hasBookingForSlot(id, updatedBooking.getBookingDate(), updatedBooking.getShift(), bookingId)) {
            return "redirect:/dashboard/" + id + "?bookingError=exists";
        }

        booking.setClientName(updatedBooking.getClientName());
        booking.setBookingDate(updatedBooking.getBookingDate());
        booking.setShift(updatedBooking.getShift());
        booking.setGuestCount(updatedBooking.getGuestCount());
        booking.setStatus(updatedBooking.getStatus());
        service.saveBooking(booking);
        return "redirect:/dashboard/" + id;
    }

    private boolean isValidBookingDate(String bookingDate) {
        try {
            return !LocalDate.parse(bookingDate).isBefore(LocalDate.now());
        } catch (DateTimeParseException | NullPointerException exception) {
            return false;
        }
    }

    @PostMapping("/dashboard/{id}/booking/{bookingId}/delete")
    public String deleteBooking(
            @PathVariable Long id,
            @PathVariable Long bookingId,
            HttpSession session,
            Authentication authentication) {
        if (!canAccessVenue(id, session, authentication)) {
            return "redirect:/halllogin";
        }

        Booking booking = service.getBooking(bookingId);
        if (id.equals(booking.getVenue().getId())) {
            service.deleteBooking(bookingId);
        }
        return "redirect:/dashboard/" + id;
    }

    private boolean canAccessVenue(Long venueId, HttpSession session, Authentication authentication) {
        boolean hallManagerAccess = venueId.equals(session.getAttribute("hallVenueId"));
        boolean adminAccess = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
        return hallManagerAccess || adminAccess;
    }
}