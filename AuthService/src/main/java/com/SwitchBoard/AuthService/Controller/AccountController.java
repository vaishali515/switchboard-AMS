package com.SwitchBoard.AuthService.Controller;


import com.SwitchBoard.AuthService.DTO.Account.AccountRequestDto;
import com.SwitchBoard.AuthService.DTO.Account.AccountResponseDto;
import com.SwitchBoard.AuthService.DTO.Authentication.ApiResponse;
import com.SwitchBoard.AuthService.Model.Account;
import com.SwitchBoard.AuthService.Service.Account.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth/account")
@Slf4j
@RequiredArgsConstructor
@Tag(name = "Account Management", description = "API endpoints for managing user accounts")
public class AccountController {
    private final AccountService accountService;

    @Operation(summary = "Create a new user account")
    @PostMapping("/create")
    public ResponseEntity<ApiResponse> createUser(@RequestBody AccountRequestDto accountRequestDto) {
            log.info("AccountController : createUser : Received request to create account - {}", accountRequestDto.getEmail());

            ApiResponse apiResponse = accountService.createProfile(accountRequestDto);

            log.info("AccountController : createUser : Request Completed to create account - {}", accountRequestDto.getEmail());
            return new ResponseEntity<>(apiResponse, HttpStatus.CREATED);
    }

    @Operation(summary = "Get user by ID")
    @GetMapping("/get/{id}")
    public ResponseEntity<AccountResponseDto> getUser(@PathVariable UUID id) {
        log.info("AccountController : getUser : Fetching user with id - {}", id);
        try {
            AccountResponseDto user = accountService.getUser(id);
            log.info("AccountController : getUser : Successfully retrieved user - {}", id);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            log.error("AccountController : getUser : Exception while retrieving user - {}", e.getMessage(), e);
            throw e;
        }
    }

    @Operation(summary = "Get all users")
    @GetMapping("/getAll")
    public ResponseEntity<List<AccountResponseDto>> getAllUsers() {
        log.info("AccountController : getAllUsers : Fetching all users");
        try {
            List<AccountResponseDto> userDTOS = accountService.getAllUsers();
            log.info("AccountController : getAllUsers : Successfully retrieved {} users", userDTOS.size());
            return ResponseEntity.ok(userDTOS);
        } catch (Exception e) {
            log.error("AccountController : getAllUsers : Exception while retrieving all users - {}", e.getMessage(), e);
            throw e;
        }
    }

    @Operation(summary = "Update user")
    @PatchMapping("/update/{id}")
    public ResponseEntity<ApiResponse> updateUser(@PathVariable UUID id, @RequestBody AccountRequestDto updates) {
        log.info("AccountController : updateUser : Received request to update account - {}", id);
        log.debug("AccountController : updateUser : Update details - {}", updates);
        try {
            ApiResponse apiResponse = accountService.updateProfile(id, updates);
            if (!apiResponse.isSuccess()) {
                log.warn("AccountController : updateUser : Failed to update account - {}", id);
                return new ResponseEntity<>(apiResponse, HttpStatus.BAD_REQUEST);
            }
            log.info("AccountController : updateUser : Successfully updated account - {}", id);
            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            log.error("AccountController : updateUser : Exception while updating account - {}", e.getMessage(), e);
            throw e;
        }
    }
}

