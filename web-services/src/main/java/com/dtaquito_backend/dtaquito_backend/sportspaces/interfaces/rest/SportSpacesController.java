package com.dtaquito_backend.dtaquito_backend.sportspaces.interfaces.rest;

import com.dtaquito_backend.dtaquito_backend.external_systems.application.internal.commandservices.LocationIQService;
import com.dtaquito_backend.dtaquito_backend.external_systems.domain.model.entities.LocationIQResponse;
import com.dtaquito_backend.dtaquito_backend.iam.infrastructure.authorization.sfs.model.UserDetailsImpl;
import com.dtaquito_backend.dtaquito_backend.reservations.domain.model.aggregates.Reservations;
import com.dtaquito_backend.dtaquito_backend.reservations.infrastructure.persistance.jpa.ReservationsRepository;
import com.dtaquito_backend.dtaquito_backend.sportspaces.domain.model.aggregates.SportSpaces;
import com.dtaquito_backend.dtaquito_backend.sportspaces.domain.model.aggregates.TrendSportData;
import com.dtaquito_backend.dtaquito_backend.sportspaces.domain.model.commands.CreateSportSpacesCommand;
import com.dtaquito_backend.dtaquito_backend.sportspaces.domain.model.queries.GetSportSpacesByUserIdQuery;
import com.dtaquito_backend.dtaquito_backend.sportspaces.domain.services.TrendSportDataCommandService;
import com.dtaquito_backend.dtaquito_backend.sportspaces.domain.services.SportSpacesCommandService;
import com.dtaquito_backend.dtaquito_backend.sportspaces.domain.services.SportSpacesQueryService;
import com.dtaquito_backend.dtaquito_backend.sportspaces.infrastructure.persistance.jpa.SportSpacesRepository;
import com.dtaquito_backend.dtaquito_backend.sportspaces.infrastructure.persistance.jpa.TrendSportDataRepository;
import com.dtaquito_backend.dtaquito_backend.subscriptions.domain.model.aggregates.Subscription;
import com.dtaquito_backend.dtaquito_backend.subscriptions.domain.model.valueObjects.PlanTypes;
import com.dtaquito_backend.dtaquito_backend.subscriptions.domain.services.SubscriptionsQueryService;
import com.dtaquito_backend.dtaquito_backend.users.domain.model.valueObjects.RoleTypes;
import com.dtaquito_backend.dtaquito_backend.users.domain.services.UserQueryService;
import com.dtaquito_backend.dtaquito_backend.sportspaces.interfaces.rest.resources.CreateSportSpacesResource;
import com.dtaquito_backend.dtaquito_backend.sportspaces.interfaces.rest.resources.SportSpacesResource;
import com.dtaquito_backend.dtaquito_backend.sportspaces.interfaces.rest.transform.CreateSportSpacesCommandFromResourceAssembler;
import com.dtaquito_backend.dtaquito_backend.sportspaces.interfaces.rest.transform.SportSpacesResourceFromEntityAssembler;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.parameters.RequestBody;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@RestController
@Slf4j
@RequestMapping(value = "/api/v1/sport-spaces", produces = MediaType.APPLICATION_JSON_VALUE)
public class SportSpacesController {

    private final SportSpacesCommandService sportSpacesCommandService;

    private final SportSpacesQueryService sportSpacesQueryService;
    private final UserQueryService userQueryService;
    private final SubscriptionsQueryService subscriptionsQueryService;
    private final SportSpacesRepository sportSpacesRepository;
    private final ReservationsRepository reservationsRepository;
    private final TrendSportDataCommandService trendSportDataCommandService;
    private final TrendSportDataRepository trendSportDataRepository;
    private final LocationIQService locationIQService;

    public SportSpacesController(SportSpacesCommandService sportSpacesCommandService, SportSpacesQueryService sportSpacesQueryService, UserQueryService userQueryService, SubscriptionsQueryService subscriptionsQueryService, SportSpacesRepository sportSpacesRepository, ReservationsRepository reservationsRepository,
                                 TrendSportDataCommandService trendSportDataCommandService, TrendSportDataRepository trendSportDataRepository,
                                 LocationIQService locationIQService) {
        this.sportSpacesCommandService = sportSpacesCommandService;
        this.sportSpacesQueryService = sportSpacesQueryService;
        this.subscriptionsQueryService = subscriptionsQueryService;
        this.userQueryService = userQueryService;
        this.sportSpacesRepository = sportSpacesRepository;
        this.reservationsRepository = reservationsRepository;
        this.trendSportDataCommandService = trendSportDataCommandService;
        this.trendSportDataRepository = trendSportDataRepository;
        this.locationIQService = locationIQService;
    }

    private static final Logger logger = LoggerFactory.getLogger(SportSpacesController.class);

    @Operation(summary = "Create a new sport space",
            requestBody = @RequestBody(content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)))
    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createSportSpaces(@ModelAttribute CreateSportSpacesResource resource, Authentication authentication) {
        try {
            ZoneId zoneId = ZoneId.of("America/Lima");
            ZonedDateTime now = ZonedDateTime.now(zoneId);

//            // Verificar si es lunes y si la hora está entre las 00:00 y las 06:00
//            if (now.getDayOfWeek() != DayOfWeek.THURSDAY || now.getHour() < 21) { // CAMBIAR A LUNES Y A 6
//                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Creation is only allowed on Mondays from 00:00 to 06:00"));
//            }

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
            LocalTime openTime = LocalTime.parse(resource.openTime(), formatter);
            LocalTime closeTime = LocalTime.parse(resource.closeTime(), formatter);

//            if (openTime.isBefore(LocalTime.of(7, 0)) || closeTime.isAfter(LocalTime.of(23, 0))) {
//                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Open time must be at least 07:00 and close time must be at most 23:00"));
//            }

            UserDetailsImpl principal = (UserDetailsImpl) authentication.getPrincipal();
            Long userId = principal.getId();

            logger.info("Creating sport spaces for user with ID: {}", userId);
            RoleTypes userRole;
            PlanTypes userSubscriptionPlan;
            try {
                userRole = RoleTypes.valueOf(userQueryService.getUserRoleByUserId(userId));
                Subscription subscription = subscriptionsQueryService.getSubscriptionByUserId(userId).orElseThrow(() -> new IllegalArgumentException("Subscription not found for user"));
                userSubscriptionPlan = subscription.getPlan().getPlanType();
            } catch (IllegalArgumentException e) {
                logger.error("Invalid user role or subscription plan", e);
                throw new IllegalArgumentException("Invalid user role or subscription plan", e);
            }
            if (!userRole.equals(RoleTypes.OWNER) || !(userSubscriptionPlan.equals(PlanTypes.GOLD) || userSubscriptionPlan.equals(PlanTypes.SILVER) || userSubscriptionPlan.equals(PlanTypes.BRONZE))) {
                logger.error("User must be OWNER and subscription plan must be GOLD, SILVER, or BRONZE");
                throw new IllegalArgumentException("User must be OWNER and subscription plan must be GOLD, SILVER, or BRONZE");
            }

            List<SportSpaces> userSportSpaces = sportSpacesQueryService.handle(new GetSportSpacesByUserIdQuery(userId));
            int maxSportSpacesAllowed = switch (userSubscriptionPlan) {
                case GOLD -> 3;
                case SILVER -> 2;
                case BRONZE -> 1;
                default -> 0;
            };

            if (userSportSpaces.size() >= maxSportSpacesAllowed) {
                String errorMessage = String.format("You have reached the maximum number of sport spaces allowed for your %s plan.", userSubscriptionPlan.name());
                logger.error(errorMessage);
                throw new IllegalArgumentException(errorMessage);
            }

            double lat  = resource.latitude();
            double lon  = resource.longitude();
            LocationIQResponse loc = locationIQService
                    .reverseGeocode(lat, lon);

            log.info("Coordinates: lat={}, lon={}", lat, lon);
            String displayName = loc.getFormattedAddress();

            CreateSportSpacesCommand command =
                    CreateSportSpacesCommandFromResourceAssembler
                            .toCommandFromResource(resource, displayName);

            Optional<SportSpaces> sportSpaces = sportSpacesCommandService.handle(userId, command);
            sportSpaces.ifPresent(s -> {
                trendSportDataCommandService.createInitialDataForSportSpace(s.getId());
            });

            return sportSpaces.map(s -> ResponseEntity.ok(SportSpacesResourceFromEntityAssembler.toResourceFromEntity(s)))
                    .orElseThrow(() -> new IllegalArgumentException("Failed to create sport space"));
        } catch (Exception e) {
            logger.error("Error creating sport space", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ResponseBody
    public Map<String, String> handleIllegalArgumentException(IllegalArgumentException ex) {
        return Map.of("error", ex.getMessage());
    }

    @GetMapping("/all")
    public ResponseEntity<List<SportSpacesResource>> getAllSportSpaces(Authentication authentication){
        UserDetailsImpl principal = (UserDetailsImpl) authentication.getPrincipal();
        Long userId = principal.getId();
        RoleTypes userRole = RoleTypes.valueOf(userQueryService.getUserRoleByUserId(userId));;
        if (userRole == RoleTypes.OWNER) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body((List<SportSpacesResource>) Map.of("error", "Owners cannot access this endpoint"));
        }
        else {
            var sportSpaces = sportSpacesQueryService.getAllSportSpaces();
            var sportSpacesResources = sportSpaces.stream().map(SportSpacesResourceFromEntityAssembler::toResourceFromEntity).toList();
            return ResponseEntity.ok(sportSpacesResources);
        }
    }

    @GetMapping("/my-space")
    public ResponseEntity<List<SportSpacesResource>> getSportSpacesByUserId(Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        UserDetailsImpl principal = (UserDetailsImpl) authentication.getPrincipal();
        Long userId = principal.getId();
        List<SportSpaces> sportSpaces = sportSpacesQueryService.handle(new GetSportSpacesByUserIdQuery(userId));
        if (sportSpaces.isEmpty()) {
            return ResponseEntity.notFound().build();
        } else {
            List<SportSpacesResource> sportSpacesResources = sportSpaces.stream()
                    .map(SportSpacesResourceFromEntityAssembler::toResourceFromEntity)
                    .collect(toList());
            return ResponseEntity.ok(sportSpacesResources);
        }
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<?> deleteSportSpace(@PathVariable Long id, Authentication authentication) {
        try {
            ZoneId zoneId = ZoneId.of("America/Lima");
            ZonedDateTime now = ZonedDateTime.now(zoneId);

            SportSpaces sportSpace = sportSpacesRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Sport space not found"));

            UserDetailsImpl principal = (UserDetailsImpl) authentication.getPrincipal();
            Long userId = principal.getId();

            if (!sportSpace.getUser().getId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "You are not authorized to delete this sport space"));
            }

            String openTimeString = sportSpace.getOpenTime();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
            LocalTime openTime;
            try {
                openTime = LocalTime.parse(openTimeString, formatter);
            } catch (DateTimeParseException e) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Invalid open time format"));
            }

            LocalTime midnight = LocalTime.MIDNIGHT;
            LocalTime oneHourBeforeOpenTime = openTime.minusHours(1);

//            if (now.getDayOfWeek() != DayOfWeek.MONDAY  ) {
//                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Deletion is only allowed on Mondays"));
//            }

//            if (now.toLocalTime().isBefore(midnight) || now.toLocalTime().isAfter(oneHourBeforeOpenTime)) {
//                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Deletion is only allowed from midnight until 1 hour before the open time"));
//            }
            trendSportDataCommandService.deleteAllDataForSportSpace(id);
            sportSpacesRepository.delete(sportSpace);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Error deleting sport space", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    @Hidden
    @GetMapping("/{id}/image")
    public ResponseEntity<byte[]> getImage(@PathVariable Long id) {
        try {
            byte[] imageBytes = sportSpacesCommandService.getImageById(id);
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_JPEG)
                    .body(imageBytes);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getSportSpaceById(@PathVariable Long id) {
        try {
            SportSpaces sportSpace = sportSpacesRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Sport space not found"));

            SportSpacesResource resource = SportSpacesResourceFromEntityAssembler.toResourceFromEntity(sportSpace);
            return ResponseEntity.ok(resource);
        } catch (IllegalArgumentException e) {
            logger.error("Error fetching sport space by ID", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error fetching sport space by ID", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "An unexpected error occurred"));
        }
    }

    @GetMapping("/can-add-sport-space")
    public ResponseEntity<Map<String, Object>> canAddSportSpace(Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Unauthorized"));
            }

            UserDetailsImpl principal = (UserDetailsImpl) authentication.getPrincipal();
            Long userId = principal.getId();

            RoleTypes userRole = RoleTypes.valueOf(userQueryService.getUserRoleByUserId(userId));
            if (!userRole.equals(RoleTypes.OWNER)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Only owners can add sport spaces"));
            }

            Subscription subscription = subscriptionsQueryService.getSubscriptionByUserId(userId)
                    .orElseThrow(() -> new IllegalArgumentException("Subscription not found for user"));
            PlanTypes userSubscriptionPlan = subscription.getPlan().getPlanType();

            int maxSportSpacesAllowed = switch (userSubscriptionPlan) {
                case GOLD -> 3;
                case SILVER -> 2;
                case BRONZE -> 1;
                default -> 0;
            };

            List<SportSpaces> userSportSpaces = sportSpacesQueryService.handle(new GetSportSpacesByUserIdQuery(userId));
            int currentSportSpaces = userSportSpaces.size();

            boolean canAdd = currentSportSpaces < maxSportSpacesAllowed;

            return ResponseEntity.ok(Map.of(
                    "canAdd", canAdd
            ));
        } catch (Exception e) {
            logger.error("Error checking if user can add sport space", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}/availability")
    public ResponseEntity<?> getSportSpaceAvailabilityForWeek(@PathVariable Long id) {
        try {
            ZoneId zoneId = ZoneId.of("America/Lima");
            LocalDate today = ZonedDateTime.now(zoneId).toLocalDate();
            LocalTime currentTime = ZonedDateTime.now(zoneId).toLocalTime();

            SportSpaces sportSpace = sportSpacesRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Sport space not found"));

            LocalTime openTime = LocalTime.parse(sportSpace.getOpenTime(), DateTimeFormatter.ofPattern("HH:mm"));
            LocalTime closeTime = LocalTime.parse(sportSpace.getCloseTime(), DateTimeFormatter.ofPattern("HH:mm"));

            Map<String, List<String>> weeklyAvailability = new LinkedHashMap<>();
            for (int i = 1; i <= 6; i++) { // De martes (día 1) a domingo (día 6)
                LocalDate currentDay = today.with(DayOfWeek.TUESDAY).plusDays(i - 1);

                // Si la fecha ya pasó, asignar un array vacío
                if (currentDay.isBefore(today)) {
                    weeklyAvailability.put(currentDay.toString(), Collections.emptyList());
                    continue;
                }

                List<String> timeSlots = new ArrayList<>();
                LocalTime slotTime = openTime;
                while (slotTime.isBefore(closeTime)) {
                    timeSlots.add(slotTime.toString());
                    slotTime = slotTime.plusHours(1);
                }

                if (currentDay.isEqual(today)) {
                    timeSlots.removeIf(slot -> LocalTime.parse(slot, DateTimeFormatter.ofPattern("HH:mm")).isBefore(currentTime));
                }

                List<Reservations> reservations = reservationsRepository.findBySportSpacesIdAndGameDay(id, currentDay.toString());

                for (Reservations reservation : reservations) {
                    LocalTime reservationStart = LocalTime.parse(reservation.getStartTime(), DateTimeFormatter.ofPattern("HH:mm"));
                    LocalTime reservationEnd = LocalTime.parse(reservation.getEndTime(), DateTimeFormatter.ofPattern("HH:mm"));

                    timeSlots.removeIf(slot -> {
                        LocalTime slotTimeToCheck = LocalTime.parse(slot, DateTimeFormatter.ofPattern("HH:mm"));
                        return !slotTimeToCheck.isBefore(reservationStart) && slotTimeToCheck.isBefore(reservationEnd);
                    });
                }

                weeklyAvailability.put(currentDay.toString(), timeSlots);
            }

            return ResponseEntity.ok(Map.of("weeklyAvailability", weeklyAvailability));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    @PatchMapping("/create-number/{sportSpaceId}/update-amount-people")
    public ResponseEntity<?> updateAmountPeople(
            @PathVariable Long sportSpaceId,
            @RequestParam int amountPeople) {
        try {
            // Zona horaria de Lima
            ZoneId zoneId = ZoneId.of("America/Lima");
            ZonedDateTime now = ZonedDateTime.now(zoneId);

            String currentMonth = now.getMonth()
                    .getDisplayName(java.time.format.TextStyle.FULL,
                            new java.util.Locale("es"));
            String currentYear = String.valueOf(now.getYear());
            LocalTime currentTime = now.toLocalTime();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

            List<TrendSportData> trendDataList =
                    trendSportDataRepository.findBySportSpaceIdAndCurrentMonthAndCurrentYear(
                            sportSpaceId, currentMonth, currentYear);

            if (trendDataList.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error",
                                "No data found for the given criteria"));
            }

            for (TrendSportData trendData : trendDataList) {
                LocalTime openingHour = LocalTime.parse(trendData.getOpeningHour(), formatter);

                // si estamos entre (openingHour - 1min) y (openingHour + 1h)
                if (currentTime.isAfter(openingHour.minusMinutes(1)) &&
                        currentTime.isBefore(openingHour.plusHours(1))) {

                    // obtener el valor actual, sumarle el nuevo y actualizar
                    int currentAmount = Integer.parseInt(trendData.getAmountPeople());
                    int updatedAmount = currentAmount + amountPeople;
                    trendData.setAmountPeople(String.valueOf(updatedAmount));
                }
            }

            trendSportDataRepository.saveAll(trendDataList);
            return ResponseEntity.ok().build();

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/get-number/{sportSpaceId}/amount-people")
    public ResponseEntity<?> getAmountPeople(
            @PathVariable Long sportSpaceId,
            @RequestParam String currentMonth,
            @RequestParam String currentYear,
            Authentication authentication
    ) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        UserDetailsImpl principal = (UserDetailsImpl) authentication.getPrincipal();
        Long userId = principal.getId();
        RoleTypes userRole = RoleTypes.valueOf(userQueryService.getUserRoleByUserId(userId));

        if (userRole == RoleTypes.PLAYER) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Players cannot access this endpoint"));
        } else {
            List<TrendSportData> datos = trendSportDataRepository
                    .findBySportSpaceIdAndCurrentMonthAndCurrentYear(
                            sportSpaceId, currentMonth, currentYear
                    );

            if (datos.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "No se encontraron registros para los filtros proporcionados"));
            }

            // Agrupar por openingHour y listar su cantidad de personas
            Map<String, Integer> result = datos.stream()
                    .collect(Collectors.toMap(
                            TrendSportData::getOpeningHour,
                            d -> Integer.parseInt(d.getAmountPeople()),
                            Integer::sum // Suma si hay duplicados por misma hora
                    ));

            return ResponseEntity.ok(result);
        }
    }

}