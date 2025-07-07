package com.dtaquito_backend.dtaquito_backend.rooms.application.internal.commandservices;

import com.dtaquito_backend.dtaquito_backend.chat.domain.model.aggregates.ChatRoom;
import com.dtaquito_backend.dtaquito_backend.chat.infrastructure.persistance.jpa.ChatRoomRepository;
import com.dtaquito_backend.dtaquito_backend.player_list.domain.model.aggregates.PlayerList;
import com.dtaquito_backend.dtaquito_backend.player_list.infrastructure.persistance.jpa.PlayerListRepository;
import com.dtaquito_backend.dtaquito_backend.reservations.domain.model.aggregates.Reservations;
import com.dtaquito_backend.dtaquito_backend.reservations.domain.model.valueobjects.Status;
import com.dtaquito_backend.dtaquito_backend.reservations.domain.services.ReservationsQueryService;
import com.dtaquito_backend.dtaquito_backend.reservations.infrastructure.persistance.jpa.ReservationsRepository;
import com.dtaquito_backend.dtaquito_backend.rooms.domain.model.aggregates.Rooms;
import com.dtaquito_backend.dtaquito_backend.rooms.domain.services.RoomsCommandService;
import com.dtaquito_backend.dtaquito_backend.rooms.infrastructure.persistance.jpa.RoomsRepository;
import com.dtaquito_backend.dtaquito_backend.users.domain.model.aggregates.User;
import com.dtaquito_backend.dtaquito_backend.users.infrastructure.persistance.jpa.UserRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Slf4j
@Service
public class RoomsCommandServiceImpl implements RoomsCommandService {

    private final Logger logger = LoggerFactory.getLogger(RoomsCommandServiceImpl.class);
    private final RoomsRepository roomsRepository;
    private final UserRepository userRepository;
    private final PlayerListRepository playerListRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ReservationsRepository reservationsRepository;

    public RoomsCommandServiceImpl(RoomsRepository roomsRepository, UserRepository userRepository, PlayerListRepository playerListRepository, ChatRoomRepository chatRoomRepository, ReservationsRepository reservationsRepository) {
        this.roomsRepository = roomsRepository;
        this.userRepository = userRepository;
        this.playerListRepository = playerListRepository;
        this.chatRoomRepository = chatRoomRepository;
        this.reservationsRepository = reservationsRepository;
    }

    @Transactional
    @Scheduled(fixedRate = 60000)
    public void deleteRoomByEndTimeConcluded() {
        List<Rooms> roomsList = roomsRepository.findAll();

        for (Rooms room : roomsList) {
            Reservations reservation = room.getReservations();

            if (reservation != null) {
                DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                LocalDate roomDate = LocalDate.parse(reservation.getGameDay(), dateFormatter);

                DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
                LocalTime endTime = LocalTime.parse(reservation.getEndTime(), timeFormatter);

                ZoneId limaZone = ZoneId.of("America/Lima");
                ZonedDateTime endDateTime = ZonedDateTime.of(LocalDateTime.of(roomDate, endTime), limaZone);

                if (room.getPlayerLists().size() >= room.getReservations().getSportSpaces().getGame().getGameMode().getMaxPlayers()) {
                    transferToCreator(room);
                    room.setAccumulatedAmount(BigDecimal.valueOf(0));
                    log.info("Room with ID {} is full. Transferring accumulated amount to creator.", room.getId());
                    room.getReservations().setStatus(Status.valueOf("CONFIRMED"));
                    if (endDateTime.isBefore(ZonedDateTime.now(ZoneId.of("America/Lima")))) {
                        room.getReservations().setStatus(Status.valueOf("CONCLUDED"));
                        Optional<ChatRoom> chatRoomOptional = chatRoomRepository.findByRoomId(room.getId());
                        chatRoomOptional.ifPresent(chatRoom -> {
                            chatRoom.getMessages().clear();
                            chatRoomRepository.save(chatRoom);
                        });

                        playerListRepository.deleteByRoomId(room.getId());
                        chatRoomRepository.deleteByRoomId(room.getId());

                        roomsRepository.delete(room);
                    }
               }
            }
        }
    }

    @Override
    public void transferToCreator(Rooms room) {
        BigDecimal accumulatedAmount = room.getAccumulatedAmount();
        User creator = room.getReservations().getSportSpaces().getUser();
        creator.setCredits(creator.getCredits().add(accumulatedAmount));
        userRepository.save(creator);
    }

    @Override
    public void refundToUsers(Long roomId) {
        List<PlayerList> playerLists = playerListRepository.findByRoomId(roomId);
        if (playerLists.isEmpty()) {
            logger.warn("No players found for room ID: {}", roomId);
            return;
        }
        Rooms room = playerLists.get(0).getRoom();
        BigDecimal accumulatedAmount = room.getAccumulatedAmount();

        List<User> users = playerLists.stream().map(PlayerList::getUser).toList();

        if (users.isEmpty()) {
            logger.warn("No users to refund in room: {}", room.getId());
            return;
        }

        BigDecimal amountPerUser = accumulatedAmount.divide(BigDecimal.valueOf(users.size()), RoundingMode.HALF_UP);

        for (User user : users) {
            user.setCredits(user.getCredits().add(amountPerUser));
            userRepository.save(user);
        }
    }

    @Override
    public void addPlayerToRoomAndChat(Long roomId, Long userId) {
        Rooms room = roomsRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        String startTime = room.getReservations().getStartTime();
        String endTime = room.getReservations().getEndTime();
        LocalTime reservationStartTime = LocalTime.parse(startTime, DateTimeFormatter.ofPattern("HH:mm"));
        LocalTime reservationEndTime = LocalTime.parse(endTime, DateTimeFormatter.ofPattern("HH:mm"));
        long hours = ChronoUnit.HOURS.between(reservationStartTime, reservationEndTime);
        BigDecimal sportSpaceAmount = BigDecimal.valueOf(room.getReservations().getSportSpaces().getAmount());
        BigDecimal totalPriceCommunity = sportSpaceAmount.multiply(BigDecimal.valueOf(hours));

        int maxPlayers = room.getReservations().getSportSpaces().getGame().getGameMode().getMaxPlayers();
        if (room.getPlayerLists().size() >= maxPlayers) {
            throw new IllegalStateException("Room is already full");
        }

        if (playerListRepository.existsByRoomAndUser(room, user)) {
            throw new IllegalStateException("User already in the room");
        }

        if (user.getCredits().compareTo(totalPriceCommunity) < 0) {
            throw new IllegalStateException("Insufficient credits. Please recharge.");
        }

        user.setCredits(user.getCredits().subtract(totalPriceCommunity));
        userRepository.save(user);

        room.setAccumulatedAmount(room.getAccumulatedAmount().add(totalPriceCommunity));
        roomsRepository.save(room);

        PlayerList playerList = new PlayerList();
        playerList.setRoom(room);
        playerList.setUser(user);

        List<PlayerList> playerLists = playerListRepository.findByRoomId(roomId);
        if (playerLists.isEmpty()) {
            throw new IllegalStateException("No chat rooms associated with roomId: " + roomId);
        }
        ChatRoom chatRoom = playerLists.get(0).getChatRoom();
        playerList.setChatRoom(chatRoom);

        playerListRepository.save(playerList);
    }

    @Override
    public boolean isRoomCreator(Long roomId, Long userId) {
        Rooms room = roomsRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found"));
        return room.getReservations().getUser().getId().equals(userId);
    }

    @Override
    @Transactional
    public void removePlayerFromRoomAndChat(Long roomId, Long userId) {
        Rooms room = roomsRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Room not found: " + roomId));
        int currentPlayers = room.getPlayerLists().size();
        int maxPlayers = room.getReservations()
                .getSportSpaces()
                .getGame()
                .getGameMode()
                .getMaxPlayers();
        if (currentPlayers >= maxPlayers) {
            log.error("Room is full ({} / {}) and cannot be left", currentPlayers, maxPlayers);
            throw new IllegalStateException("Cannot leave room once it is full");
        }

        Long reservationId = room.getReservations().getId();
        Reservations reservation = reservationsRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Reservation not found for ID: " + reservationId));

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate roomDate = LocalDate.parse(reservation.getGameDay(), dateFormatter);

        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        LocalTime startTime = LocalTime.parse(reservation.getStartTime(), timeFormatter);

        ZoneId limaZone = ZoneId.of("America/Lima");
        ZonedDateTime startDateTime = ZonedDateTime.of(
                LocalDateTime.of(roomDate, startTime),
                limaZone
        );

        ZonedDateTime now = ZonedDateTime.now(limaZone);
        if (!now.isBefore(startDateTime.minusHours(24))) {
            throw new IllegalStateException(
                    "Cannot leave room less than 24 hours before start time"
            );
        }


        PlayerList entry = playerListRepository.findByRoomAndUser(roomId, userId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Player not in room: roomId=" + roomId + ", userId=" + userId));

        List<PlayerList> playerLists = playerListRepository.findByRoomId(roomId);
        if (playerLists.isEmpty()) {
            throw new IllegalStateException("No chat rooms associated with roomId: " + roomId);
        }
        ChatRoom chatRoom = playerLists.get(0).getChatRoom();
        chatRoom.getMessages().removeIf(msg -> msg.getUser().getId().equals(userId));
        chatRoom.getPlayerLists().remove(entry);
        chatRoomRepository.save(chatRoom);

        room.getPlayerLists().remove(entry);
        roomsRepository.save(room);
        playerListRepository.delete(entry);

        refundLeavingUser(roomId, userId);
    }

    private void refundLeavingUser(Long roomId, Long userId) {
        Rooms room = roomsRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found: " + roomId));

        BigDecimal accumulated = room.getAccumulatedAmount();

        int remainingPlayers = room.getPlayerLists().size();
        int originalPlayers  = remainingPlayers + 1;

        if (originalPlayers <= 0) {
            throw new IllegalStateException("No players to refund");
        }

        BigDecimal refundAmount = accumulated
                .divide(BigDecimal.valueOf(originalPlayers), RoundingMode.HALF_UP);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        user.setCredits(user.getCredits().add(refundAmount));
        userRepository.save(user);

        room.setAccumulatedAmount(accumulated.subtract(refundAmount));
        roomsRepository.save(room);
    }
}