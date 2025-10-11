package com.SwitchBoard.AuthService.Controller;

import com.SwitchBoard.AuthService.DTO.*;
import com.SwitchBoard.AuthService.Service.OtpService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@CrossOrigin( origins = "*", allowedHeaders = "*")
@Slf4j
@Tag(name = "Authentication", description = "Authentication API endpoints")
public class AuthController {

    private final OtpService otpService;

    @Operation(
        summary = "Send OTP to email",
        description = "Sends a one-time password to the provided email address"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "OTP sent successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Invalid email address",
            content = @Content
        )
    })
    @PostMapping("/send-otp")
    public ResponseEntity<ApiResponse> sendOtp(@RequestBody AuthRequest authRequest) {
        log.info("AuthController : sendOtp : Request received for email - {}", authRequest.getEmail());
        ApiResponse apiResponse = otpService.generateOtp(authRequest.getEmail());
        log.info("AuthController : sendOtp : OTP sent successfully for email - {}", authRequest.getEmail());
        return ResponseEntity.ok().body(apiResponse);
    }

    @Operation(
        summary = "Verify OTP",
        description = "Validates the OTP sent to the email address and returns authentication token"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "OTP verified successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Invalid OTP or email",
            content = @Content
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "OTP expired or invalid",
            content = @Content
        )
    })
    @PostMapping("/verify-otp")
    public ResponseEntity<AuthResponse> verifyOtp(@RequestBody AuthValidateRequest authValidateRequest) throws Exception {
        log.info("AuthController : verifyOtp : Request received for email - {}", authValidateRequest.getEmail());
        AuthResponse authResponse = otpService.validateOtp(authValidateRequest.getEmail(), authValidateRequest.getOtp());
        log.info("AuthController : verifyOtp : OTP verified successfully for email - {}", authValidateRequest.getEmail());
        return ResponseEntity.ok(authResponse);
    }
}
