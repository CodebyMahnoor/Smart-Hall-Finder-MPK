package com.smarthallfinder.service;

import com.smarthallfinder.model.Booking;
import com.smarthallfinder.model.Venue;
import com.smarthallfinder.repository.BookingRepository;
import com.smarthallfinder.repository.VenueRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AppService {

    private final VenueRepository venueRepo;
    private final BookingRepository bookingRepo;

    public AppService(VenueRepository venueRepo, BookingRepository bookingRepo) {
        this.venueRepo = venueRepo;
        this.bookingRepo = bookingRepo;
    }

    public Venue getVenueByLoginId(String loginId) {
        return venueRepo.findByLoginId(loginId).orElse(null);
    }

    public List<Venue> getAllVenues() { return venueRepo.findAll(); }
    public List<Venue> getActiveVenues() { return venueRepo.findByActive(true); }
    public Venue getVenue(Long id) { return venueRepo.findById(id).orElseThrow(); }
    public void saveVenue(Venue v) { venueRepo.save(v); }
    
    public void toggleVenue(Long id) {
        Venue v = getVenue(id);
        v.setActive(!v.getActive());
        venueRepo.save(v);
    }

    public void deleteVenue(Long id) {
        List<Booking> bookings = bookingRepo.findByVenueId(id);
        bookingRepo.deleteAll(bookings);
        venueRepo.deleteById(id);
    }

    public List<Venue> getFilteredVenues(String date, String shift, Integer capacity, String type) {
        List<Venue> activeVenues = getActiveVenues();

        // 1. Availability Filter (Date & Shift)
        if (date != null && !date.trim().isEmpty() && shift != null && !shift.trim().isEmpty()) {
            List<Booking> allBookings = bookingRepo.findAll();
            List<Long> bookedVenueIds = allBookings.stream()
                    .filter(b -> b.getBookingDateInput().equalsIgnoreCase(date) && b.getShift().equalsIgnoreCase(shift))
                    .map(b -> b.getVenue().getId())
                    .collect(Collectors.toList());

            activeVenues = activeVenues.stream()
                    .filter(v -> !bookedVenueIds.contains(v.getId()))
                    .collect(Collectors.toList());
        }

        // 2. Capacity Filter
        if (capacity != null && capacity > 0) {
            activeVenues = activeVenues.stream()
                    .filter(v -> v.getCapacityFrom() <= capacity && v.getCapacityTo() >= capacity)
                    .collect(Collectors.toList());
        }

        // 3. Venue Type Filter (Banquet, Marquee, Lawn)
        if (type != null && !type.trim().isEmpty() && !type.equalsIgnoreCase("All")) {
            activeVenues = activeVenues.stream()
                    .filter(v -> v.getType().equalsIgnoreCase(type))
                    .collect(Collectors.toList());
        }

        return activeVenues;
    }

    public List<Booking> getBookings(Long venueId) { return bookingRepo.findByVenueId(venueId); }
    public Booking getBooking(Long bookingId) { return bookingRepo.findById(bookingId).orElseThrow(); }
    public boolean hasBookingForSlot(Long venueId, String bookingDate, String shift, Long excludedBookingId) {
        String normalizedDate = LocalDate.parse(bookingDate).toString();
        return bookingRepo.findByVenueId(venueId).stream()
                .filter(booking -> excludedBookingId == null || !excludedBookingId.equals(booking.getId()))
                .anyMatch(booking -> normalizedDate.equals(booking.getBookingDateInput())
                        && shift.equalsIgnoreCase(booking.getShift()));
    }
    public void saveBooking(Booking b) { bookingRepo.save(b); }
    public void deleteBooking(Long bookingId) { bookingRepo.deleteById(bookingId); }
    public long countShift(Long venueId, String shift) { return bookingRepo.countByVenueIdAndShift(venueId, shift); }
}