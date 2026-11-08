# SmartHallFinder Project Overview

## 1. Purpose

SmartHallFinder is a Spring Boot web application for discovering and managing wedding halls in Mirpurkhas, Sindh. It supports a public venue directory with date/shift/capacity/type filters, a super-admin management panel for venue records, and a hall-manager dashboard for booking entries.

The application is designed to let users:
- browse available halls for a requested date and shift,
- filter halls by guest capacity and venue type,
- add, update, delete, and activate/deactivate halls from an admin screen,
- log in through a unique hall access key and manage bookings for a specific venue.

## 2. Technology Stack

- Java 17
- Spring Boot 3.2.0
- Spring MVC + Thymeleaf for server-rendered web UI
- Spring Data JPA for persistence
- H2 embedded database stored in the local `data/` folder
- Spring Security for access control
- Maven for build and dependency management

## 3. Project Structure

```text
src/
  main/
    java/
      com/smarthallfinder/
        SmartHallFinderApplication.java
        SecurityConfig.java
        WebConfig.java
        controller/
          AppController.java
        model/
          Venue.java
          Booking.java
        repository/
          VenueRepository.java
          BookingRepository.java
        service/
          AppService.java
    resources/
      application.properties
      templates/
        directory.html
        admin.html
        dashboard.html
        halllogin.html
  
uploads/
  (uploaded hall photos are saved here)

data/
  smarthalldb.mv.db
  smarthalldb.trace.db
```

## 4. Core Components

### SmartHallFinderApplication
This is the Spring Boot entry point. It boots the app and also seeds initial data if the database is empty.

Seed data includes a few sample venue records and a few booking entries for demonstration. This lets the app immediately show some hall listings and booking data when launched for the first time.

### AppController
`AppController` is the main web controller. It handles page rendering and form submissions for the app's major workflows:

- `GET /` — public hall directory with filters
- `GET /admin` — admin panel listing all venues
- `POST /admin/venue/add` — create a new venue and upload an image
- `POST /admin/venue/update/{id}` — update an existing venue
- `POST /admin/venue/delete/{id}` — delete a venue and its associated bookings
- `POST /admin/venue/toggle/{id}` — activate or deactivate a venue
- `GET /login` and `POST /hall/login` — hall manager login with unique hall key
- `GET /dashboard/{id}` — venue-specific dashboard
- `POST /dashboard/{id}/booking/add` — add a booking for a venue

### AppService
`AppService` contains the business logic. It sits between the controller and repositories and handles:

- retrieving venues by login ID,
- fetching all or active venues,
- toggling venue activation,
- deleting a venue and all its bookings,
- filtering available venues by date, shift, capacity, and type,
- retrieving bookings for a venue,
- counting bookings by Morning/Evening shift.

### Repositories
Two repositories are used:

- `VenueRepository`: persists venue data and supports lookup by active flag and login ID.
- `BookingRepository`: persists booking data and fetches bookings for a given venue.

## 5. Data Model

### Venue
`Venue` is a JPA entity mapped to the `venues` table.

Key fields:
- `id`
- `active` (boolean)
- `name`
- `type` (Banquet, Marquee, Lawn)
- `capacityFrom` and `capacityTo`
- `location`
- `ownerName`
- `ownerPhone`
- `loginId` (unique hall access key)
- `imageUrl`

The `loginId` is auto-generated on addition as `SHF-xxxxx`, giving each venue a unique access code for hall-manager login.

### Booking
`Booking` is a JPA entity mapped to the `bookings` table.

Key fields:
- `id`
- `clientName`
- `bookingDate`
- `shift` (Morning or Evening)
- `guestCount`
- `status`
- `createdAt`
- `venue` (many-to-one relationship to `Venue`)

The booking logic is used to block a venue on a given date/shift when checking availability.

## 6. Business Logic and User Flows

### Public Directory
The homepage (`/`) displays a filtered directory of active halls.

It accepts the following filters:
- event date
- shift
- guest count
- hall type

The service removes venues that already have a booking on the same date and shift. This gives the public directory a real-time “available halls” view.

### Venue Management (Super Admin)
The admin screen lists all venues with actions to:
- add a new venue,
- toggle active/inactive status,
- delete a venue,
- update venue details.

New venues can upload an image, which is stored in the `uploads/` directory and exposed via `WebConfig` at `/uploads/**`.

### Hall Manager Dashboard
Each venue has a dashboard at `/dashboard/{id}`. A hall manager first submits the venue's unique key at `/halllogin`; a successful check stores that venue ID in the session. The dashboard and its booking form then verify that the session belongs to the requested venue. Admin users can also access venue dashboards after admin authentication. From the dashboard, a hall manager can:
- view bookings for that venue,
- see totals for morning and evening bookings,
- add a booking using client name, date (calendar input that disallows past dates), guest count, shift, and status,
- edit booking details or delete a booking.

Each venue can have at most one booking for a given date and shift; Morning and Evening remain separate slots. Booking dates are displayed as `11 dec 2026`. Booking edit and delete actions verify that the booking belongs to the venue in the current dashboard.

## 7. Security Model

`SecurityConfig` configures Spring Security with these behaviors:

- public access allowed for `/`, the separate login pages and processing endpoints, `/dashboard/**`, `/uploads/**`, `/css/**`, and `/js/**`
- protected access for `/admin/**` and `/h2-console/**`
- form login enabled with default success redirect to `/admin`
- logout redirects to `/`

An in-memory admin user is configured:
- username: `admin`
- password: `admin123`

The admin login uses Spring Security username/password authentication. Hall-manager login is separate: the app checks the submitted unique `loginId`, stores the matching venue ID in the HTTP session, and checks that ID on dashboard and booking requests. This prevents a hall manager from switching to another venue by changing the dashboard URL.

## 8. Static Files and Upload Handling

The app includes a custom MVC configuration in `WebConfig` that exposes the local `uploads` folder as static web resources:

- resource path: `/uploads/**`
- filesystem location: local `uploads/` directory

This allows uploaded images to be displayed directly in the UI.

## 9. Data and Persistence

The application uses a local H2 database file configured in `application.properties`:

- JDBC URL: `jdbc:h2:file:./data/smarthalldb`
- Hibernate auto-update mode: `update`
- H2 console enabled for DB inspection

The database persists in the `data/` folder, so venue and booking records remain available across app restarts.

## 10. UI Templates

The project uses Thymeleaf templates for page generation:

- `directory.html` — public search page for halls
- `admin.html` — super admin management interface
- `dashboard.html` — registered hall/dashboard view
- `halllogin.html` — hall manager login page

The interface is implemented with Tailwind CSS via CDN.

## 11. Notable Observations / Current Gaps

A few implementation details are worth noting:

- The project is a server-rendered MVC app rather than a modern REST API.
- Hall login routing appears slightly inconsistent: the directory template links to `/halllogin`, while the controller exposes `/login` and a POST endpoint `/hall/login`. The template form also submits to `/hall/halllogin`, which does not match the controller mapping. This likely needs cleanup.
- The app seeds demo data during startup, which is helpful for testing but may need removal or replacement in production.
- The availability filter compares bookings by `date` and `shift`, but the filter is client-facing and based on booking data only; it does not currently enforce stricter business rules beyond the same-date/same-shift conflict check.

## 12. Summary

SmartHallFinder is a focused venue-booking and management application for wedding halls. It combines public hall discovery, admin operations, and hall-specific booking management in a single Spring Boot MVC project. The codebase is compact, readable, and organized around a simple layered architecture: controllers, services, repositories, JPA entities, and Thymeleaf templates.

If this project were expanded further, the next likely improvements would be:
- standardizing login and dashboard routes,
- strengthening validation and error handling,
- adding proper security roles for hall managers,
- introducing edit/delete support for bookings,
- and moving toward a cleaner multi-role admin system.
