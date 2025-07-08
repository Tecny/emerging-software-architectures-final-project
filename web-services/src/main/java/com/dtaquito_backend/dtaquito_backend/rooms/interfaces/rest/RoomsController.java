package com.dtaquito_backend.dtaquito_backend.rooms.interfaces.rest;

import com.dtaquito_backend.dtaquito_backend.iam.infrastructure.authorization.sfs.model.UserDetailsImpl;
import com.dtaquito_backend.dtaquito_backend.rooms.domain.model.aggregates.Views;
import com.dtaquito_backend.dtaquito_backend.rooms.domain.model.aggregates.Rooms;
import com.dtaquito_backend.dtaquito_backend.rooms.domain.model.queries.GetRoomByUserIdQuery;
import com.dtaquito_backend.dtaquito_backend.rooms.domain.services.RoomsQueryService;
import com.dtaquito_backend.dtaquito_backend.rooms.infrastructure.persistance.jpa.RoomsRepository;
import com.dtaquito_backend.dtaquito_backend.rooms.interfaces.rest.resources.RoomResource;
import com.dtaquito_backend.dtaquito_backend.rooms.interfaces.rest.transform.RoomsResourceFromEntityAssembler;
import com.dtaquito_backend.dtaquito_backend.users.domain.model.aggregates.User;
import com.dtaquito_backend.dtaquito_backend.users.domain.model.valueObjects.RoleTypes;
import com.dtaquito_backend.dtaquito_backend.users.domain.services.UserQueryService;

import com.dtaquito_backend.dtaquito_backend.users.infrastructure.persistance.jpa.UserRepository;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@Slf4j
@RequestMapping(value="/api/v1/rooms", produces = MediaType.APPLICATION_JSON_VALUE)
public class RoomsController {

    private final RoomsRepository roomsRepository;
    private final RoomsQueryService roomsQueryService;
    private final UserRepository userRepository;


    public RoomsController(RoomsRepository roomsRepository, RoomsQueryService roomsQueryService, UserRepository userRepository) {
        this.roomsRepository = roomsRepository;
        this.roomsQueryService = roomsQueryService;
        this.userRepository = userRepository;
    }

    @GetMapping("{id}")
    public ResponseEntity<RoomResource> getRoomById(@PathVariable Long id, Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            return ResponseEntity.status(401).build();
        }

        UserDetailsImpl principal = (UserDetailsImpl) authentication.getPrincipal();
        Long userId = principal.getId();

        Rooms room = roomsRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Room not found"));

        boolean userBelongsToRoom = room.getReservations().getUser().getId().equals(userId) ||
                room.getPlayerLists().stream().anyMatch(player -> player.getUser().getId().equals(userId));

        if (!userBelongsToRoom) {
            return ResponseEntity.status(403).build();
        }

        RoomResource resource = RoomsResourceFromEntityAssembler.toResourceFromEntity(room);

        return ResponseEntity.ok(resource);
    }

    @GetMapping("/my-rooms")
    public ResponseEntity<List<RoomResource>> getRoomByUserId(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            return ResponseEntity.badRequest().body(Collections.emptyList());
        }

        UserDetailsImpl principal = (UserDetailsImpl) authentication.getPrincipal();
        Long userId = principal.getId();

        List<Rooms> rooms = roomsQueryService.handle(new GetRoomByUserIdQuery(userId));

        List<RoomResource> resources = rooms.stream()
                .map(RoomsResourceFromEntityAssembler::toResourceFromEntity)
                .toList();

        return ResponseEntity.ok(resources);
    }

    @GetMapping("/my-join-rooms")
    public ResponseEntity<List<RoomResource>> getRoomsUserJoined(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            return ResponseEntity.badRequest().body(Collections.emptyList());
        }

        UserDetailsImpl principal = (UserDetailsImpl) authentication.getPrincipal();
        Long userId = principal.getId();

        List<Rooms> rooms = roomsQueryService.handleFindRoomsUserJoined(userId);

        List<RoomResource> filteredRooms = rooms.stream()
                .filter(room -> !room.getReservations().getUser().getId().equals(userId))
                .map(RoomsResourceFromEntityAssembler::toResourceFromEntity)
                .toList();

        return ResponseEntity.ok(filteredRooms);
    }

    @GetMapping("/my-rooms-by-spaces")
    public ResponseEntity<List<RoomResource>> getRoomsBySpacesOwner(Authentication authentication) {
        try {
            validateUserRole(authentication, RoleTypes.OWNER);
            List<Rooms> rooms = roomsQueryService.handleFindRoomsBySportSpacesOwner(
                    ((UserDetailsImpl) authentication.getPrincipal()).getId()
            );
            List<RoomResource> resources = rooms.stream()
                    .map(RoomsResourceFromEntityAssembler::toResourceFromEntity)
                    .toList();
            return ResponseEntity.ok(resources);
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(Collections.emptyList());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Collections.emptyList());
        }
    }

    @GetMapping("/all")
    public ResponseEntity<List<RoomResource>> getAllRooms(Authentication authentication) {
        try {
            validateUserRole(authentication, RoleTypes.PLAYER);
            var rooms = roomsQueryService.getAllRooms();
            List<RoomResource> resources = rooms.stream()
                    .map(RoomsResourceFromEntityAssembler::toResourceFromEntity)
                    .toList();
            return ResponseEntity.ok(resources);
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(null);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Collections.emptyList());
        }
    }

    private void validateUserRole(Authentication authentication, RoleTypes requiredRole) {
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new IllegalArgumentException("Usuario no autenticado");
        }

        UserDetailsImpl principal = (UserDetailsImpl) authentication.getPrincipal();
        Long userId = principal.getId();

        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty() || userOptional.get().getRole().getRoleType() != requiredRole) {
            throw new SecurityException("Acceso denegado para el rol requerido: " + requiredRole);
        }
    }
}