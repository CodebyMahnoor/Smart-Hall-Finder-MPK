package com.smarthallfinder.repository;

import com.smarthallfinder.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByVenueId(Long venueId);
    long countByVenueIdAndShift(Long venueId, String shift);
}