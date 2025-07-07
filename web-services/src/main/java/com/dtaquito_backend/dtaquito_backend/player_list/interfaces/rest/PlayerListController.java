package com.dtaquito_backend.dtaquito_backend.player_list.interfaces.rest;


import com.dtaquito_backend.dtaquito_backend.iam.infrastructure.authorization.sfs.model.UserDetailsImpl;
import com.dtaquito_backend.dtaquito_backend.player_list.domain.model.aggregates.PlayerList;
import com.dtaquito_backend.dtaquito_backend.player_list.infrastructure.persistance.jpa.PlayerListRepository;
import com.dtaquito_backend.dtaquito_backend.player_list.interfaces.rest.resources.PlayerListDTO;
import com.dtaquito_backend.dtaquito_backend.player_list.interfaces.rest.transform.PlayerListResourceFromEntityAssembler;
import com.dtaquito_backend.dtaquito_backend.rooms.domain.services.RoomsCommandService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@Slf4j
@RequestMapping(value="/api/v1/player-lists", produces = MediaType.APPLICATION_JSON_VALUE)
public class PlayerListController {

    private final PlayerListRepository playerListRepository;
    private final RoomsCommandService roomsCommandService;

    public PlayerListController(PlayerListRepository playerListRepository, RoomsCommandService roomsCommandService) {
        this.playerListRepository = playerListRepository;
        this.roomsCommandService = roomsCommandService;
    }

    @PostMapping("/join/{roomId}")
    public ResponseEntity<Map<String, String>> joinRoom(Authentication authentication, @PathVariable Long roomId) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "User not authenticated"));
            }
            UserDetailsImpl principal = (UserDetailsImpl) authentication.getPrincipal();
            Long userId = principal.getId();
            roomsCommandService.addPlayerToRoomAndChat(roomId, userId);
            return ResponseEntity.ok(Map.of("message", "Player added to room and chat successfully"));
        } catch (IllegalStateException | IllegalArgumentException e) {
            roomsCommandService.refundToUsers(roomId);
            log.info(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        } catch (org.springframework.dao.IncorrectResultSizeDataAccessException e) {
            roomsCommandService.refundToUsers(roomId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Query did not return a unique result"));
        }
    }

    @GetMapping("/room/{roomId}")
    public ResponseEntity<List<Map<String, Object>>> getPlayerListsByRoomId(@PathVariable Long roomId) {
        List<PlayerList> playerLists = playerListRepository.findByRoomId(roomId);
        List<Map<String, Object>> playerListDTOs = playerLists.stream()
                .map(playerList -> {
                    Map<String, Object> playerData = new HashMap<>();
                    playerData.put("id", playerList.getUser().getId());
                    playerData.put("name", playerList.getUser().getName());
                    return playerData;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(playerListDTOs);
    }

    @GetMapping("/{roomId}/user-room-status")
    public ResponseEntity<Map<String, Object>> getMembershipAndCreatorStatus(
            @PathVariable Long roomId, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Usuario no autenticado"));
        }

        UserDetailsImpl principal = (UserDetailsImpl) authentication.getPrincipal();
        Long userId = principal.getId();

        boolean isMember = playerListRepository.existsByRoom_IdAndUser_Id(roomId, userId);
        boolean isCreator = roomsCommandService.isRoomCreator(roomId, userId);

        Map<String, Object> response = new HashMap<>();
        response.put("isMember", isMember);
        response.put("isRoomCreator", isCreator);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/leave/{roomId}")
    public ResponseEntity<Map<String, String>> leaveRoom(Authentication authentication, @PathVariable Long roomId) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "User not authenticated"));
            }
            UserDetailsImpl principal = (UserDetailsImpl) authentication.getPrincipal();
            Long userId = principal.getId();
            roomsCommandService.removePlayerFromRoomAndChat(roomId, userId);
            return ResponseEntity.ok(Map.of("message", "Player removed from room and chat successfully"));
        } catch (IllegalStateException | IllegalArgumentException e) {
            log.info(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        } catch (org.springframework.dao.IncorrectResultSizeDataAccessException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Query did not return a unique result"));
        }
    }
}