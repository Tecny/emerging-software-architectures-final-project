package com.dtaquito_backend.dtaquito_backend.bank_transfer.interfaces.rest;

import com.dtaquito_backend.dtaquito_backend.bank_transfer.domain.model.aggregates.BankTransfer;
import com.dtaquito_backend.dtaquito_backend.bank_transfer.domain.services.BankTransferCommandServices;
import com.dtaquito_backend.dtaquito_backend.bank_transfer.infrastructure.persistance.jpa.BankTransferRepository;
import com.dtaquito_backend.dtaquito_backend.bank_transfer.interfaces.rest.resources.CreateBankTransferResource;
import com.dtaquito_backend.dtaquito_backend.bank_transfer.interfaces.rest.transform.BankTransferResourceFromEntityAssembler;
import com.dtaquito_backend.dtaquito_backend.bank_transfer.interfaces.rest.transform.CreateBankTransferCommandFromResourceAssembler;
import com.dtaquito_backend.dtaquito_backend.iam.infrastructure.authorization.sfs.model.UserDetailsImpl;
import com.dtaquito_backend.dtaquito_backend.reservations.domain.model.valueobjects.Status;
import com.dtaquito_backend.dtaquito_backend.users.domain.model.aggregates.User;
import com.dtaquito_backend.dtaquito_backend.users.domain.model.valueObjects.RoleTypes;
import com.dtaquito_backend.dtaquito_backend.users.domain.services.UserQueryService;
import com.dtaquito_backend.dtaquito_backend.users.infrastructure.persistance.jpa.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Optional;

@RestController
@Slf4j
@RequestMapping(value = "/api/v1/bank-transfer")
public class BankTransferController {

    private final UserRepository userRepository;
    private final BankTransferCommandServices bankTransferCommandServices;
    private final UserQueryService userQueryService;
    private final BankTransferRepository bankTransferRepository;

    public BankTransferController(UserRepository userRepository,
                                  BankTransferCommandServices bankTransferCommandServices,
                                  UserQueryService userQueryService, BankTransferRepository bankTransferRepository) {
        this.userRepository = userRepository;
        this.bankTransferCommandServices = bankTransferCommandServices;
        this.userQueryService = userQueryService;
        this.bankTransferRepository = bankTransferRepository;
    }

    @PostMapping("/create")
    public ResponseEntity<?> createBankTransfer(@RequestBody CreateBankTransferResource resource, Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
        }

        UserDetailsImpl principal = (UserDetailsImpl) authentication.getPrincipal();
        Long userId = principal.getId();

        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));
        ZoneId zoneId = ZoneId.of("America/Lima");
        ZonedDateTime now = ZonedDateTime.now(zoneId);
        RoleTypes userRole = RoleTypes.valueOf(userQueryService.getUserRoleByUserId(userId));;
        try {

            if (userRole == RoleTypes.OWNER) {

                if (now.getDayOfWeek() != DayOfWeek.MONDAY || now.getHour() > 6) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Creation is only allowed on Mondays from 00:00 to 06:00"));
                }

                String transferType = resource.transferType().toUpperCase();

                if (!(transferType.equals("CC") || transferType.equals("CCI"))) {
                    return ResponseEntity.badRequest().body("Only CC or CCI are allowed");
                }

                Optional<BankTransfer> existingTransfer = bankTransferRepository
                        .findByUserIdAndStatus(userId, Status.PENDING);

                if (existingTransfer.isPresent()) {
                    return ResponseEntity.badRequest().body("Your transfer request is still under review.");
                }

                Optional<BankTransfer> bankTransfer = bankTransferCommandServices.handle(userId, CreateBankTransferCommandFromResourceAssembler.toCommandFromResource(resource));
                BankTransfer bankTransferEntity = bankTransfer.orElseThrow(() -> new IllegalArgumentException("Bank transfer not created"));

                bankTransferEntity.setAmount(user.getCredits().longValue());
                bankTransferEntity.setTransferType(transferType);
                bankTransferEntity = bankTransferRepository.save(bankTransferEntity);
                return ResponseEntity.ok(BankTransferResourceFromEntityAssembler.toResourceFromEntity(bankTransferEntity));

            } else {
                return ResponseEntity.badRequest().body("Players cannot create bank transfers");
            }

        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllBankTransfers(Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
        }

        UserDetailsImpl principal = (UserDetailsImpl) authentication.getPrincipal();
        Long userId = principal.getId();

        RoleTypes userRole = RoleTypes.valueOf(userQueryService.getUserRoleByUserId(userId));
        log.info("User role: {}", userRole);

        if (userRole == RoleTypes.ADMIN) {
            try {
                return ResponseEntity.ok(bankTransferRepository.findAll());
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while fetching all bank transfers.");
            }
        } else  {
            log.info("User role: {}", userRole);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only ADMINS can view all bank transfers.");
        }
    }

    @GetMapping("/owner")
    public ResponseEntity<?> getBankTransfersByUserId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
        }

        UserDetailsImpl principal = (UserDetailsImpl) authentication.getPrincipal();
        Long authUserId = principal.getId();
        RoleTypes userRole = RoleTypes.valueOf(userQueryService.getUserRoleByUserId(authUserId));

        if (userRole == RoleTypes.OWNER) {
            try {
                return ResponseEntity.ok(bankTransferRepository.findByUserId(authUserId));
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while fetching bank transfers.");
            }
        } else if (userRole == RoleTypes.PLAYER) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Players cannot view bank transfers.");
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not authorized to view this user's bank transfers.");
        }
    }

    @PatchMapping("/confirm/{id}")
    public ResponseEntity<?> updateTransferStatus(@PathVariable Long id) {

        User userAdmin = userRepository.findById(1L).orElseThrow(() -> new IllegalArgumentException("User not found"));
        RoleTypes userRole = RoleTypes.valueOf(userQueryService.getUserRoleByUserId(1L));
        Optional<BankTransfer> bankTransfer = bankTransferRepository.findById(id);

        if (userRole == RoleTypes.ADMIN) {
            try {
                if (bankTransfer.isPresent() && (bankTransfer.get().getStatus() == Status.PENDING || bankTransfer.get().getStatus() == Status.DEFERRED)) {
                    BankTransfer bankTransferEntity = bankTransfer.get();
                    bankTransferEntity.setStatus(Status.CONFIRMED);
                    userAdmin.setCredits(BigDecimal.valueOf(userAdmin.getCredits().longValue()).add(BigDecimal.valueOf(bankTransferEntity.getAmount())));
                    User bankTransferUser = bankTransferEntity.getUser();
                    bankTransferUser.setCredits(BigDecimal.valueOf(bankTransferUser.getCredits().longValue()).subtract(BigDecimal.valueOf(bankTransferEntity.getAmount())));

                    userRepository.save(bankTransferUser);
                    userRepository.save(userAdmin);
                    bankTransferRepository.save(bankTransferEntity);
                    return ResponseEntity.ok(Map.of("message", "Bank transfer confirmed successfully."));
                } else if (bankTransfer.isEmpty()) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Bank transfer not found."));
                } else {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Bank transfer was already confirmed."));
                }
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "An error occurred while updating bank transfer status."));
            }
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Only ADMINS can update bank transfer status."));
        }
    }

    @PatchMapping("/defer/{id}")
    public ResponseEntity<?> updateTransferDeferredStatus(@PathVariable Long id) {

        RoleTypes userRole = RoleTypes.valueOf(userQueryService.getUserRoleByUserId(1L));
        Optional<BankTransfer> bankTransfer = bankTransferRepository.findById(id);

        if (userRole == RoleTypes.ADMIN) {
            try {
                if (bankTransfer.isPresent() && bankTransfer.get().getStatus() == Status.PENDING) {
                    BankTransfer bankTransferEntity = bankTransfer.get();
                    bankTransferEntity.setStatus(Status.DEFERRED);
                    bankTransferRepository.save(bankTransferEntity);
                    return ResponseEntity.ok(Map.of("message", "Bank transfer deferred successfully."));
                } else if (bankTransfer.isEmpty()) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Bank transfer not found."));
                } else {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Bank transfer was already confirmed."));
                }
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "An error occurred while updating bank transfer status."));
            }
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Only ADMINS can update bank transfer status."));
        }
    }
}
