package com.SwitchBoard.AuthService.Controller;

import com.SwitchBoard.AuthService.DTO.Authentication.ApiResponse;
import com.SwitchBoard.AuthService.DTO.Authentication.AuthRequest;
import com.SwitchBoard.AuthService.DTO.Authentication.AuthResponse;
import com.SwitchBoard.AuthService.DTO.Authentication.AuthValidateRequest;
import com.SwitchBoard.AuthService.DTO.Authentication.RefreshTokenRequest;
import com.SwitchBoard.AuthService.Exception.ResourceNotFoundException;
import com.SwitchBoard.AuthService.Exception.UnauthorizedException;
import com.SwitchBoard.AuthService.Model.Account;
import com.SwitchBoard.AuthService.Model.RefreshToken;
import com.SwitchBoard.AuthService.Service.OtpService;
import com.SwitchBoard.AuthService.Service.RefreshTokenService;
import com.SwitchBoard.AuthService.Util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "Authentication API endpoints")
public class AuthController {

    private final OtpService otpService;
    private final RefreshTokenService refreshTokenService;
    private final JwtUtil jwtUtil;

    @Value("${jwt.expiration}")
    private Long jwtExpiration;

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
    
    @Operation(
        summary = "Refresh access token",
        description = "Uses refresh token to generate a new access token"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Token refreshed successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "Invalid or expired refresh token",
            content = @Content
        )
    })
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(@RequestBody RefreshTokenRequest request) throws Exception {
        log.info("AuthController : refreshToken : Request received to refresh token");
        
        RefreshToken refreshToken = refreshTokenService.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));
        
        if (!refreshTokenService.isTokenValid(refreshToken)) {
            log.warn("AuthController : refreshToken : Invalid or expired refresh token");
            throw new UnauthorizedException("Refresh token expired or invalid");
        }
        
        Account account = refreshToken.getAccount();
        
        // Generate new access token
        String newAccessToken = jwtUtil.generateToken(
            account.getEmail(), 
            account.getName(), 
            account.getId(), 
            account.getUserRole()
        );
        
        // Create new refresh token
        RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(account);
        
        AuthResponse response = AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken.getToken())
                .expiresIn(jwtExpiration)
                .build();
        
        log.info("AuthController : refreshToken : Token refreshed successfully for account - {}", account.getEmail());
        return ResponseEntity.ok(response);
    }
}
