package com.dtaquito_backend.dtaquito_backend.reservations.application.internal.commandservices;

import com.dtaquito_backend.dtaquito_backend.chat.domain.model.aggregates.ChatRoom;
import com.dtaquito_backend.dtaquito_backend.chat.infrastructure.persistance.jpa.ChatRoomRepository;
import com.dtaquito_backend.dtaquito_backend.player_list.infrastructure.persistance.jpa.PlayerListRepository;
import com.dtaquito_backend.dtaquito_backend.reservations.domain.model.aggregates.Reservations;
import com.dtaquito_backend.dtaquito_backend.reservations.domain.model.commands.CreateReservationsCommand;
import com.dtaquito_backend.dtaquito_backend.reservations.domain.model.events.ReservationCreatedEvent;
import com.dtaquito_backend.dtaquito_backend.reservations.domain.model.valueobjects.Status;
import com.dtaquito_backend.dtaquito_backend.reservations.domain.model.valueobjects.Type;
import com.dtaquito_backend.dtaquito_backend.reservations.domain.services.ReservationsCommandService;
import com.dtaquito_backend.dtaquito_backend.reservations.infrastructure.persistance.jpa.ReservationsRepository;
import com.dtaquito_backend.dtaquito_backend.rooms.domain.model.aggregates.Rooms;
import com.dtaquito_backend.dtaquito_backend.rooms.domain.services.RoomsCommandService;
import com.dtaquito_backend.dtaquito_backend.rooms.infrastructure.persistance.jpa.RoomsRepository;
import com.dtaquito_backend.dtaquito_backend.sportspaces.infrastructure.persistance.jpa.SportSpacesRepository;
import com.dtaquito_backend.dtaquito_backend.users.infrastructure.persistance.jpa.UserRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class ReservationsCommandServiceImpl implements ReservationsCommandService {

    private final UserRepository userRepository;
    private final SportSpacesRepository sportSpacesRepository;
    private final ReservationsRepository reservationsRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final RoomsRepository roomsRepository;
    private final PlayerListRepository playerListRepository;
    private final RoomsCommandService roomCommandService;

    public ReservationsCommandServiceImpl(UserRepository userRepository,
                                          SportSpacesRepository sportSpacesRepository, ReservationsRepository reservationsRepository, ChatRoomRepository chatRoomRepository, RoomsRepository roomsRepository, PlayerListRepository playerListRepository,
                                          RoomsCommandService roomCommandService) {

        this.userRepository = userRepository;
        this.sportSpacesRepository = sportSpacesRepository;
        this.reservationsRepository = reservationsRepository;
        this.chatRoomRepository = chatRoomRepository;
        this.roomsRepository = roomsRepository;
        this.playerListRepository = playerListRepository;
        this.roomCommandService = roomCommandService;
    }

    @Override
    public Optional<Reservations> handle(Long id, CreateReservationsCommand command){

        var user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        var sportSpaces = sportSpacesRepository.findById(command.sportSpacesId())
                .orElseThrow(() -> new IllegalArgumentException("SportSpaces not found"));
        var reservation = new Reservations(command, user, sportSpaces);
        return Optional.of(reservation);
    }

    @Override
    public void handleReservationsCreatedEvent(ReservationCreatedEvent event){
        System.out.println("ReservationCreatedEvent received for reservation ID: " + event.getReservationId());
    }

    @Transactional
    @Scheduled(fixedRate = 60000)
    public void deleteCommunityReservationsByOneHourBefore() {
        List<Reservations> reservations = reservationsRepository.findAll();
        for (Reservations reservation : reservations) {
            if (reservation.getType() == Type.COMMUNITY) {
                try {
                    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                    LocalDate roomDate = LocalDate.parse(reservation.getGameDay(), dateFormatter);

                    DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
                    LocalTime startTime = LocalTime.parse(reservation.getStartTime(), timeFormatter);

                    ZoneId limaZone = ZoneId.of("America/Lima");
                    ZonedDateTime startDateTime = ZonedDateTime.of(LocalDateTime.of(roomDate, startTime), limaZone);

                    if (startDateTime.isBefore(ZonedDateTime.now(limaZone).plusHours(2))) {
                        log.info("Reservations are eliminated two hours before the game starts");

                        List<Rooms> roomsList = roomsRepository.findByReservationsId(reservation.getId());

                        boolean roomIsFull = false;

                        for (Rooms room : roomsList) {
                            if (room.getPlayerLists().size() >= room.getReservations().getSportSpaces().getGame().getGameMode().getMaxPlayers()) {
                                log.info("Room with ID: {} is full. Skipping deletion for this room.", room.getId());
                                roomIsFull = true;
                                break;
                            }
                        }

                        if (!roomIsFull) {
                            for (Rooms room : roomsList) {
                                try {
                                    log.info("Deleting room with ID: {}", room.getId());
                                    log.info(room.getPlayerLists().toString());
                                    Optional<ChatRoom> chatRoomOptional = chatRoomRepository.findByRoomId(room.getId());
                                    chatRoomOptional.ifPresent(chatRoom -> {
                                        chatRoom.getMessages().clear();
                                        chatRoomRepository.save(chatRoom);
                                    });

                                    roomCommandService.refundToUsers(room.getId());
                                    playerListRepository.deleteByRoomId(room.getId());
                                    chatRoomRepository.deleteByRoomId(room.getId());

                                    room.getReservations().setStatus(Status.CANCELLED);
                                    roomsRepository.delete(room);

                                } catch (Exception e) {
                                    log.error("Error processing room with ID: {}", room.getId(), e);
                                }
                            }

                            reservation.setStatus(Status.CANCELLED);
                            userRepository.save(reservation.getUser());

                            reservationsRepository.delete(reservation);

                            log.info("Reservation with ID: {} has been deleted", reservation.getId());
                        } else {
                            log.info("Reservation with ID: {} was not deleted because the room is full", reservation.getId());
                        }
                    }
                } catch (Exception e) {
                    log.error("Error processing reservation with ID: {}", reservation.getId(), e);
                }
            }
        }
    }

    @Transactional
    @Scheduled(fixedRate = 60000)
    public void deletePersonalReservationByEndTimeConcluded() {
        List<Reservations> reservationsList = reservationsRepository.findAll();

        for (Reservations reservation : reservationsList) {
            if (reservation.getType() == Type.PERSONAL) {
                try {
                    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                    LocalDate roomDate = LocalDate.parse(reservation.getGameDay(), dateFormatter);

                    DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
                    LocalTime endTime = LocalTime.parse(reservation.getEndTime(), timeFormatter);

                    ZoneId limaZone = ZoneId.of("America/Lima");
                    ZonedDateTime endDateTime = ZonedDateTime.of(LocalDateTime.of(roomDate, endTime), limaZone);

                    if (endDateTime.isBefore(ZonedDateTime.now(limaZone))) {
                        reservation.setStatus(Status.CONCLUDED);
                        reservationsRepository.delete(reservation);
                        log.info("Reservation with ID: {} has been deleted", reservation.getId());
                    }
                } catch (Exception e) {
                    log.error("Error processing reservation with ID: {}", reservation.getId(), e);
                }
            }
        }
    }
}
