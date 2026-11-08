package com.smarthallfinder.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;

@Entity
@Table(name = "bookings")
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String clientName;
    private String bookingDate;
    private String shift;
    private Integer guestCount;
    private String status;
    private LocalDateTime createdAt = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "venue_id")
    private Venue venue;

    public Booking() {}
    public Booking(String clientName, String bookingDate, String shift, Integer guestCount, String status, Venue venue) {
        this.clientName = clientName; this.bookingDate = bookingDate; this.shift = shift;
        this.guestCount = guestCount; this.status = status; this.venue = venue;
    }

    public Long getId() { return id; }
    public String getClientName() { return clientName; }
    public void setClientName(String clientName) { this.clientName = clientName; }
    public String getBookingDate() { return bookingDate; }
    public void setBookingDate(String bookingDate) { this.bookingDate = bookingDate; }
    public String getBookingDateInput() {
        if (bookingDate == null || bookingDate.isBlank()) {
            return "";
        }

        try {
            return LocalDate.parse(bookingDate).toString();
        } catch (DateTimeParseException ignored) {
            try {
                return LocalDate.parse(bookingDate, DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.ENGLISH)).toString();
            } catch (DateTimeParseException ignoredLegacyFormat) {
                return "";
            }
        }
    }
    public String getBookingDateDisplay() {
        String inputDate = getBookingDateInput();
        if (inputDate.isEmpty()) {
            return bookingDate == null ? "" : bookingDate;
        }
        return LocalDate.parse(inputDate)
                .format(DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH))
                .toLowerCase(Locale.ENGLISH);
    }
    public String getShift() { return shift; }
    public void setShift(String shift) { this.shift = shift; }
    public Integer getGuestCount() { return guestCount; }
    public void setGuestCount(Integer guestCount) { this.guestCount = guestCount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public Venue getVenue() { return venue; }
    public void setVenue(Venue venue) { this.venue = venue; }
}