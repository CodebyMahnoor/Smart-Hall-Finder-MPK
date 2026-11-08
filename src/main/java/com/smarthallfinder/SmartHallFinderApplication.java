package com.smarthallfinder;

import com.smarthallfinder.model.Booking;
import com.smarthallfinder.model.Venue;
import com.smarthallfinder.repository.BookingRepository;
import com.smarthallfinder.repository.VenueRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.List;

@SpringBootApplication
public class SmartHallFinderApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartHallFinderApplication.class, args);
    }

    @Bean
    CommandLineRunner init(VenueRepository venueRepo, BookingRepository bookingRepo) {
        return args -> {
            if (venueRepo.count() == 0) {
                Venue v1 = new Venue("City Banquet", "Banquet", 200, 1500, "Near Mirchi Hotel Umerkot Road Mirpurkhas, Sindh", "Kaka Panhwar", "03033333333", "https://lh3.googleusercontent.com/aida-public/AB6AXuBjHWd2M533y70llMbXbx5R_nguybOgQ6JzaLVyYWjaV6nn_H0Zgmwt3ee3OThjszI3N-sq7HCZmTcp8f4nsAIhMZ2HUbRDE3w4x-oRirCY6I-DMqjB-eapHALmtOmSboKn_VY1fLQvNH-L0ya836M33otCTeL33uizBqQdCusMK6XVPUEDcCr_CV7XXvWUSqJQlns2LZemF34bRUEJPRQjh6_3vpJonYhhgC7sLg", true);
                Venue v2 = new Venue("JNJ BANQUET", "Banquet", 250, 1400, "Near Shah Pump Hotel, Mirpurkhas", "JAMEEL KHAN", "03003312345", "https://lh3.googleusercontent.com/aida-public/AB6AXuDPf0nQdxfMlhPRTTxmCLhIv-Q5UPpJI6zHqDHorBmUYZS5dJt1JKXh_CsaspEBty3PjtOTQ6lQ6uNdE2MDwpTGC0MicBfXg0UX-ZG8xehIxrn9LmibuFu0zGdE4K2jaPLD3ig-X_xBWV-e9S0pQo6493zXD8WepFXoXr3p8PBt4xGP5VTbXOG_Yf8BGOLy5IkBmb5e2A7gYkdFeMVt9gp7a1lb26BB0l-1Lk6mug", true);
                Venue v3 = new Venue("Ammara Banquet", "Banquet", 150, 1000, "Chona factory Pump Mirpurkhas, Sindh", "Malik Rashid", "03330000000", "https://lh3.googleusercontent.com/aida-public/AB6AXuA1rQeTGuQvkhOCX6RcWcbbYwAxVXXSChj5ZDC4XkpP7ZzXT-Qsvd2Yor8kUsEHs6nTbAHpz7ymUQjNv__y29UBYDuFR0VxxKs5hXxKcGWR8kUiqgy-s-Age1e0XCjzentkyIRa-_Agvh99WMevwozf8S7vAqQI-3wJk4n7DRFi4X1yQv48NczHuW9GAdJlCUzjzRz05VrV54mnnENODTZEAqDpI7jBFKN3GXnzgw", false);

                venueRepo.saveAll(List.of(v1, v2, v3));

                bookingRepo.save(new Booking("Khan", "October 28, 2026", "Morning", 450, "Confirmed", v2));
                bookingRepo.save(new Booking("Kk", "October 28, 2026", "Evening", 650, "Confirmed", v2));
                bookingRepo.save(new Booking("Jawaid Arshad", "December 13, 2026", "Morning", 650, "Confirmed", v2));
            }
        };
    }
}