package com.dtaquito_backend.dtaquito_backend.reservations.interfaces.rest;

import com.dtaquito_backend.dtaquito_backend.chat.domain.model.aggregates.ChatRoom;
import com.dtaquito_backend.dtaquito_backend.chat.domain.services.ChatRoomCommandService;
import com.dtaquito_backend.dtaquito_backend.chat.infrastructure.persistance.jpa.ChatRoomRepository;
import com.dtaquito_backend.dtaquito_backend.iam.infrastructure.authorization.sfs.model.UserDetailsImpl;
import com.dtaquito_backend.dtaquito_backend.external_systems.application.internal.commandservices.BlockchainService;
import com.dtaquito_backend.dtaquito_backend.player_list.domain.model.aggregates.PlayerList;
import com.dtaquito_backend.dtaquito_backend.player_list.infrastructure.persistance.jpa.PlayerListRepository;
import com.dtaquito_backend.dtaquito_backend.reservations.domain.model.aggregates.Reservations;
import com.dtaquito_backend.dtaquito_backend.reservations.domain.model.entities.QRCodeGenerator;
import com.dtaquito_backend.dtaquito_backend.reservations.domain.model.valueobjects.Status;
import com.dtaquito_backend.dtaquito_backend.reservations.domain.model.valueobjects.Type;
import com.dtaquito_backend.dtaquito_backend.reservations.domain.services.ReservationsCommandService;
import com.dtaquito_backend.dtaquito_backend.reservations.infrastructure.persistance.jpa.ReservationsRepository;
import com.dtaquito_backend.dtaquito_backend.reservations.interfaces.rest.resources.CreateReservationsResource;
import com.dtaquito_backend.dtaquito_backend.reservations.interfaces.rest.transform.CreateReservationsCommandFromResourceAssembler;
import com.dtaquito_backend.dtaquito_backend.reservations.interfaces.rest.transform.ReservationsResourceFromEntityAssembler;
import com.dtaquito_backend.dtaquito_backend.rooms.domain.model.aggregates.Rooms;
import com.dtaquito_backend.dtaquito_backend.rooms.domain.services.RoomsCommandService;
import com.dtaquito_backend.dtaquito_backend.rooms.infrastructure.persistance.jpa.RoomsRepository;
import com.dtaquito_backend.dtaquito_backend.sportspaces.domain.model.aggregates.SportSpaces;
import com.dtaquito_backend.dtaquito_backend.sportspaces.domain.model.queries.GetSportSpacesByIdQuery;
import com.dtaquito_backend.dtaquito_backend.sportspaces.domain.services.SportSpacesQueryService;
import com.dtaquito_backend.dtaquito_backend.users.domain.model.aggregates.User;
import com.dtaquito_backend.dtaquito_backend.users.domain.model.valueObjects.RoleTypes;
import com.dtaquito_backend.dtaquito_backend.users.infrastructure.persistance.jpa.UserRepository;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.time.temporal.ChronoUnit;
import java.util.*;

import java.math.RoundingMode;
import java.math.BigDecimal;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@RestController
@Slf4j
@RequestMapping(value = "/api/v1/reservations", produces = MediaType.APPLICATION_JSON_VALUE)
public class ReservationsController {

    private final ReservationsCommandService reservationsCommandService;
    private final UserRepository userRepository;
    private final SportSpacesQueryService sportSpacesQueryService;
    private final ReservationsRepository reservationsRepository;
    private final RoomsCommandService roomsCommandService;
    private final RoomsRepository roomsRepository;
    private final ChatRoomCommandService chatRoomCommandService;
    private final PlayerListRepository playerListRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final QRCodeGenerator qrCodeGenerator = new QRCodeGenerator();
    public ReservationsController(ReservationsCommandService reservationsCommandService,
                                  UserRepository userRepository,
                                  SportSpacesQueryService sportSpacesQueryService, ReservationsRepository reservationsRepository,
                                  RoomsCommandService roomsCommandService, RoomsRepository roomsRepository, ChatRoomCommandService chatRoomCommandServiceImpl, PlayerListRepository playerListRepository, ChatRoomRepository chatRoomRepository) {
        this.reservationsCommandService = reservationsCommandService;
        this.userRepository = userRepository;
        this.sportSpacesQueryService = sportSpacesQueryService;
        this.reservationsRepository = reservationsRepository;
        this.roomsCommandService = roomsCommandService;
        this.roomsRepository = roomsRepository;
        this.chatRoomCommandService = chatRoomCommandServiceImpl;
        this.playerListRepository = playerListRepository;
        this.chatRoomRepository = chatRoomRepository;
    }

    @PostMapping("/create")
    public ResponseEntity<?> createReservation(@RequestBody CreateReservationsResource resource, Authentication authentication) {
        try {
            if (authentication == null || authentication.getPrincipal() == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Collections.singletonMap("message", "Unauthorized"));
            }
            UserDetailsImpl principal = (UserDetailsImpl) authentication.getPrincipal();
            Long userId = principal.getId();

            Optional<User> userOptional = userRepository.findById(Long.parseLong(String.valueOf(userId)));
            if (userOptional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Collections.singletonMap("message", "User not found: " + userId));
            }

            User user = userOptional.get();
            RoleTypes userRole = user.getRole().getRoleType();
            BigDecimal userCredits = user.getCredits();

            if (userCredits.compareTo(BigDecimal.ZERO) <= 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Collections.singletonMap("message", "User with ID: " + userId + " does not have enough credits to create a reservation"));
            }

            if (!userRole.equals(RoleTypes.PLAYER)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Collections.singletonMap("message", "User with ID: " + userId + " does not have the required role to create a reservation"));
            }

            Long sportSpaceId = resource.sportSpacesId();
            GetSportSpacesByIdQuery query = new GetSportSpacesByIdQuery(sportSpaceId);
            SportSpaces sportSpace = sportSpacesQueryService.handle(query)
                    .orElseThrow(() -> new IllegalArgumentException("Sport space not found"));
            String openTime = sportSpace.getOpenTime();
            String closeTime = sportSpace.getCloseTime();
            String startTime = resource.startTime();
            String endTime = resource.endTime();
            String gameDay = resource.gameDay();

            LocalDate today = LocalDate.now(ZoneId.of("America/Lima"));
            LocalTime currentTime = LocalTime.now(ZoneId.of("America/Lima"));
            LocalDate reservationDate = LocalDate.parse(gameDay);
            DayOfWeek todayDayOfWeek = today.getDayOfWeek();
            DayOfWeek reservationDayOfWeek = reservationDate.getDayOfWeek();
            LocalTime reservationStartTime = LocalTime.parse(startTime, DateTimeFormatter.ofPattern("HH:mm"));
            LocalTime reservationEndTime = LocalTime.parse(endTime, DateTimeFormatter.ofPattern("HH:mm"));
            long hours = ChronoUnit.HOURS.between(reservationStartTime, reservationEndTime);

            if (reservationDate.isEqual(today) && Type.COMMUNITY.equals(Type.valueOf(resource.type()))) {
                if (reservationStartTime.isBefore(currentTime.plusHours(2))) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(Collections.singletonMap("message", "Reservations for the same day must be made at least 2 hours in advance."));
                }
            }
            if (reservationDate.isBefore(today)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Collections.singletonMap("message", "Reservations cannot be made for past dates"));
            }

            if (reservationDate.isEqual(today) && reservationStartTime.isBefore(currentTime)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Collections.singletonMap("message", "Reservations cannot be made for past hours on the same day"));
            }

            if (reservationDate.isAfter(today.plusDays(6))) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Collections.singletonMap("message", "Reservations can only be made from Tuesday to Sunday of the current week"));
            }
            if (todayDayOfWeek == DayOfWeek.MONDAY) {
                LocalTime openLocalTime = LocalTime.parse(openTime, DateTimeFormatter.ofPattern("HH:mm"));
                if (currentTime.isBefore(openLocalTime.minusHours(1))) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(Collections.singletonMap("message", "Reservations can only be made from one hour before the open time on Mondays"));
                }
                if (reservationDayOfWeek == DayOfWeek.MONDAY) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(Collections.singletonMap("message", "Reservations cannot be made for Mondays"));
                }
            }

            if (!isTimeWithinRange(startTime, endTime, openTime, closeTime)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Collections.singletonMap("message", "Reservation time is not within the open hours of the sport space"));
            }

            boolean isSportSpaceAvailable = sportSpacesQueryService.isSportSpaceAvailable(sportSpaceId, gameDay, startTime, endTime);
            if (!isSportSpaceAvailable) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Collections.singletonMap("message", "Sport space is not available at the requested time"));
            }

            BigDecimal sportSpacesPrice = BigDecimal.valueOf(sportSpace.getPrice());
            BigDecimal totalPrice = sportSpacesPrice.multiply(BigDecimal.valueOf(hours));
            BigDecimal sportSpaceAmount = BigDecimal.valueOf(sportSpace.getAmount());
            BigDecimal totalPriceCommunity = sportSpaceAmount.multiply(BigDecimal.valueOf(hours));
            BigDecimal credits = totalPrice.divide(BigDecimal.valueOf(2), RoundingMode.HALF_UP);

            // Llamar al contrato inteligente para crear la reserva
            BigInteger spaceId = BigInteger.valueOf(sportSpaceId);
            BigInteger userIdBigInt = BigInteger.valueOf(userId);

            if (resource.type().equals("PERSONAL")) {
                if (userCredits.compareTo(credits) < 0) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(Collections.singletonMap("message", "User with ID: " + userId + " does not have enough credits to create a personal reservation"));
                } else {
                    user.setCredits(userCredits.subtract(credits));
                    userRepository.save(user);
                    Optional<Reservations> reservation = reservationsCommandService.handle(userId, CreateReservationsCommandFromResourceAssembler.toCommandFromResource(resource));
                    if (reservation.isPresent()) {
                        Reservations createdReservation = reservation.get();
                        createdReservation.setStatus(Status.CONFIRMED);
                        reservationsRepository.save(createdReservation);
                        createdReservation.getSportSpaces().getUser().setCredits(createdReservation.getSportSpaces().getUser().getCredits().add(credits));
                        userRepository.save(createdReservation.getSportSpaces().getUser());
                        return reservation.map(s -> ResponseEntity.ok(ReservationsResourceFromEntityAssembler.toResourceFromEntity(s)))
                                .orElseThrow(() -> new IllegalArgumentException("Failed to create reservation"));
                    } else {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(Collections.singletonMap("message", "Error creating reservation"));
                    }
                }
            } else if (resource.type().equals("COMMUNITY")) {
                Optional<Reservations> reservation = reservationsCommandService.handle(userId, CreateReservationsCommandFromResourceAssembler.toCommandFromResource(resource));

                if (reservation.isPresent()) {
                    Reservations createdReservation = reservation.get();
                    createdReservation.setStatus(Status.PENDING);
                    reservationsRepository.save(createdReservation);
                    blockchainService.createReservation(spaceId, gameDay, startTime, userIdBigInt);
                    log.info("Reservation created successfully on the blockchain: {}", createdReservation);

                    if (createdReservation.getType() == Type.COMMUNITY) {
                        if (createdReservation.getUser().getCredits().compareTo(totalPriceCommunity) < 0) {
                            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                    .body(Collections.singletonMap("message", "User with ID: " + userId + " does not have enough credits to create a community reservation"));
                        } else {
                            BigDecimal currentCredits = createdReservation.getUser().getCredits();
                            BigDecimal newCredits = currentCredits.subtract(totalPriceCommunity);
                            createdReservation.getUser().setCredits(newCredits);

                            Rooms room = new Rooms();
                            room.setAccumulatedAmount(totalPriceCommunity);
                            room.setReservations(createdReservation);
                            roomsRepository.save(room);
                            userRepository.save(createdReservation.getUser());

                            ChatRoom chatRoom = chatRoomCommandService.createChatRoom(room);

                            if (!playerListRepository.existsByRoomAndUser(room, user)) {
                                PlayerList playerList = new PlayerList();
                                playerList.setRoom(room);
                                playerList.setUser(user);
                                playerList.setChatRoom(chatRoom);
                                playerListRepository.save(playerList);
                            }
                        }
                    }
                } else {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(Collections.singletonMap("message", "Error creating reservation"));
                }
            }
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Collections.singletonMap("message", "Reservation created successfully for user ID: " + userId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Collections.singletonMap("message", "Error creating reservation for user: " + authentication.getName()));
        }
    }

    private boolean isTimeWithinRange(String startTime, String endTime, String openTime, String closeTime) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
            LocalTime start = LocalTime.parse(startTime, formatter);
            LocalTime end = LocalTime.parse(endTime, formatter);
            LocalTime open = LocalTime.parse(openTime, formatter);
            LocalTime close = LocalTime.parse(closeTime, formatter);

            log.info("Parsed times - Start: {}, End: {}, Open: {}, Close: {}", start, end, open, close);
            return !start.isBefore(open) && !end.isAfter(close);
        } catch (DateTimeParseException e) {
            log.error("Error parsing time: {}", e.getMessage());
            return false;
        }
    }

    @Transactional
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteReservation(@PathVariable Long id, Authentication authentication) {

        Rooms rooms = roomsRepository.findByReservationsId(id).get(0);

        if (rooms.getPlayerLists().size() >= rooms.getReservations().getSportSpaces().getGame().getGameMode().getMaxPlayers()) {
            log.error("Room is full and cannot be eliminated");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Room is full and cannot be eliminated");
        }

        UserDetailsImpl principal = (UserDetailsImpl) authentication.getPrincipal();
        Long userId = principal.getId();

        try {
            Optional<Reservations> reservation = reservationsRepository.findById(id);
            if (reservation.isEmpty()) {
                log.error("Reservation not found: {}", id);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Reservation not found: " + id);
            }
            if (!reservation.get().getUser().getId().equals(userId)) {
                log.error("User {} is not the owner of reservation {}", userId, id);
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("You are not authorized to delete this reservation");
            }
            if (reservation.get().getType() == Type.COMMUNITY) {

                DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                LocalDate roomDate = LocalDate.parse(reservation.get().getGameDay(), dateFormatter);

                DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
                LocalTime startTime = LocalTime.parse(reservation.get().getStartTime(), timeFormatter);

                ZoneId limaZone = ZoneId.of("America/Lima");
                ZonedDateTime startDateTime = ZonedDateTime.of(LocalDateTime.of(roomDate, startTime), limaZone);

                if (startDateTime.isBefore(ZonedDateTime.now(limaZone).plusHours(24))) {
                    log.info("Community reservations can only be eliminated 24 hours before the game starts");
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Community reservations can only be eliminated 24 hours before the game starts");
                } else {
                    Optional<ChatRoom> chatRoomOptional = chatRoomRepository.findByRoomId(rooms.getId());
                    chatRoomOptional.ifPresent(chatRoom -> {
                        chatRoom.getMessages().clear();
                        chatRoomRepository.save(chatRoom);
                    });

                    if (rooms.getPlayerLists().size() < rooms.getReservations().getSportSpaces().getGame().getGameMode().getMaxPlayers()) {
                        roomsCommandService.refundToUsers(rooms.getId());
                        userRepository.save(rooms.getReservations().getUser());
                    }
                    playerListRepository.deleteByRoomId(rooms.getId());
                    chatRoomRepository.deleteByRoomId(rooms.getId());

                    rooms.getReservations().setStatus(Status.CANCELLED);
                    roomsRepository.delete(rooms);

                    reservationsRepository.delete(rooms.getReservations());

                    log.info("Reservation with ID: {} has been deleted", reservation.get().getId());
                    return ResponseEntity.ok(Collections.singletonMap("message", "Reservation with ID: " + id + " has been deleted"));
                }
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Reservations cannot be deleted if they are personal");
            }
        } catch (Exception e) {
            log.error("Error deleting reservation with ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error deleting reservation with ID: " + id);
        }
    }

    @GetMapping("/my-reservations")
    public ResponseEntity<List<Map<String, Object>>> getMyReservations(Authentication authentication) {
        try {
            if (authentication == null || authentication.getPrincipal() == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Collections.emptyList());
            }
            UserDetailsImpl principal = (UserDetailsImpl) authentication.getPrincipal();
            Long userId = principal.getId();
            log.info("Fetching reservations for user with ID: {}", userId);

            List<Reservations> reservations = reservationsRepository.findByUserId(userId);

            List<Map<String, Object>> response = new ArrayList<>();
            for (Reservations reservation : reservations) {
                Map<String, Object> reservationData = new HashMap<>();
                reservationData.put("id", reservation.getId());
                reservationData.put("name", reservation.getReservationName());
                reservationData.put("status", reservation.getStatus());
                reservationData.put("gameDay", reservation.getGameDay());
                reservationData.put("startTime", reservation.getStartTime());
                reservationData.put("endTime", reservation.getEndTime());
                reservationData.put("type", reservation.getType());

                Map<String, Object> sportSpacesData = new HashMap<>();
                SportSpaces sportSpace = reservation.getSportSpaces();
                sportSpacesData.put("name", sportSpace.getName());
                sportSpacesData.put("image", sportSpace.getImage());
                sportSpacesData.put("price", sportSpace.getPrice());
                sportSpacesData.put("amount", sportSpace.getAmount());
                sportSpacesData.put("sport", sportSpace.getSport().getSportType());
                sportSpacesData.put("gamemode", sportSpace.getGame().getGameMode());

                reservationData.put("sportSpaces", sportSpacesData);
                response.add(reservationData);
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching reservations: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
        }
    }

    @Hidden
    @GetMapping("/generate-qr-session")
    public ResponseEntity<Map<String, Object>> generateQrForSession(@RequestParam Long reservationId, Authentication authentication) {
        try {
            UserDetailsImpl principal = (UserDetailsImpl) authentication.getPrincipal();
            Long authenticatedUserId = principal.getId();

            Reservations reservation = reservationsRepository.findById(reservationId)
                    .orElseThrow(() -> new RuntimeException("Reserva no encontrada"));

            if (!reservation.getUser().getId().equals(authenticatedUserId)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body( Collections.singletonMap("message", "⛔ Esta reserva no te pertenece."));
            }

            String qrToken = qrCodeGenerator.generateQrToken(reservationId, authenticatedUserId, reservation.getStartTime(), reservation.getEndTime(), reservation.getGameDay());

            Map<String, Object> response = new HashMap<>();
            response.put("qrToken", qrToken);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.info("Error generating QR code: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Collections.singletonMap("message", "⛔ Error al generar el QR."));
        }
    }

    @GetMapping("/verify-qr-image")
    public ResponseEntity<byte[]> verifyQrImage(@RequestParam String token, Authentication authentication) {
        try {
            Claims claims = qrCodeGenerator.validateQrToken(token);
            Long reservationId = claims.get("reservationId", Long.class);

            UserDetailsImpl principal = (UserDetailsImpl) authentication.getPrincipal();
            Long authenticatedUserId = principal.getId();

            Reservations reservation = reservationsRepository.findById(reservationId)
                    .orElseThrow(() -> new RuntimeException("Reserva no encontrada"));

            if (!reservation.getUser().getId().equals(authenticatedUserId)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("⛔ Este QR no te pertenece.".getBytes());
            }

            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            Map<EncodeHintType, Object> hintMap = new HashMap<>();
            hintMap.put(EncodeHintType.MARGIN, 1);

            BitMatrix bitMatrix = qrCodeWriter.encode(token, BarcodeFormat.QR_CODE, 250, 250, hintMap);

            BufferedImage bufferedImage = new BufferedImage(250, 250, BufferedImage.TYPE_INT_RGB);
            for (int x = 0; x < 250; x++) {
                for (int y = 0; y < 250; y++) {
                    bufferedImage.setRGB(x, y, bitMatrix.get(x, y) ? 0x000000 : 0xFFFFFF);
                }
            }

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "PNG", byteArrayOutputStream);
            byte[] imageBytes = byteArrayOutputStream.toByteArray();

            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_PNG)
                    .body(imageBytes);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("⛔ QR inválido o expirado".getBytes());
        }
    }

    @PostMapping("/use-qr-token")
    public ResponseEntity<Map<String, Object>> useQrToken(@RequestParam String token) {
        try {
            boolean tokenUsed = qrCodeGenerator.checkIfTokenUsed(token);

            if (tokenUsed) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Collections.singletonMap("message", "El QR ya ha sido usado."));
            }

            qrCodeGenerator.markTokenAsUsed(token);

            Claims claims = qrCodeGenerator.validateQrToken(token);

            Long reservationId = claims.get("reservationId", Long.class);
            Long userId = claims.get("userId", Long.class);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "✅ El QR es válido para usar.");
            response.put("reservationId", reservationId);
            response.put("userId", userId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Collections.singletonMap("message", "⛔ QR inválido o expirado"));
        }
    }
}

