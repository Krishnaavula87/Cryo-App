package com.cryo.auth.service;

import com.cryo.auth.config.util.UtilityHelper;
import com.cryo.auth.dto.AuthResponse;
import com.cryo.auth.dto.LoginRequest;
import com.cryo.auth.dto.SignupRequest;
import com.cryo.auth.dto.UpdateProfileRequest;
import com.cryo.auth.entity.Otp;
import com.cryo.auth.entity.User;
import com.cryo.auth.repository.UserRepository;
import com.cryo.common.exception.BadRequestException;
import com.cryo.common.exception.InvalidCredentialsException;
import com.cryo.common.exception.ResourceNotFoundException;
import com.cryo.common.util.JwtTokenUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Arrays;
import java.util.stream.Collectors;

@Service
public class   AuthService {
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final OtpService otpService;
    private final EmailService emailService;
    private final JwtTokenUtil jwtTokenUtil;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       OtpService otpService,
                       EmailService emailService,
                       @Value("${jwt.secret}") String jwtSecret,
                       @Value("${jwt.accessExpiration:900}") Long accessExpiration,
                       @Value("${jwt.refreshExpiration:604800}") Long refreshExpiration) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.otpService = otpService;
        this.emailService = emailService;
        this.jwtTokenUtil = new JwtTokenUtil(jwtSecret, accessExpiration, refreshExpiration);
    }

    @Transactional
    public void signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already registered");
        }
        User user = new User();

        // ✅ FIX: Force "C" prefix for Customers
        // This ensures it never accidentally grabs an "A" ID from an admin
        user.setOwnerUserId(generateUserId("C"));

        System.out.println();
        System.out.println("owner userid " + user.getOwnerUserId());

        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setMobileNumber(request.getMobileNumber());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setStatus(User.UserStatus.PENDING_VERIFICATION);

        // Ensure role is set (Default to CUSTOMER for signup)
        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            user.setRoles("CUSTOMER");
        }

        User savedUser = userRepository.save(user);
        logger.info("User registered: {}", savedUser.getEmail());

        Otp otp = otpService.createOtp(savedUser.getId());
        emailService.sendOtpEmail(savedUser.getEmail(), otp.getOtpCode());
    }

    // ✅ NEW SMART GENERATOR: Handles both "C" and "A" correctly
    private String generateUserId(String prefix) {
        String lastId = null;

        // 1. Decide which DB query to run based on the prefix we want
        if (prefix.equals("C")) {
            // Find the last CUSTOMER id (ignores Admins)
            lastId = userRepository.findLastCustomerUserId().orElse(null);
        } else if (prefix.equals("A")) {
            // Find the last ADMIN id (ignores Customers)
            lastId = userRepository.findLastAdminUserId().orElse(null);
        }

        // 2. If this is the very first user of this type
        if (lastId == null) {
            return prefix + "00001";
        }

        // 3. Strip the letter (A or C) to get just the numbers
        String numericPart = lastId.substring(1);

        // 4. Use your UtilityHelper to do the math
        int[] arr = Arrays.stream(numericPart.split(""))
                .mapToInt(Integer::valueOf)
                .toArray();

        arr = UtilityHelper.incrementByOne(arr);

        String newNumericPart = Arrays.stream(arr)
                .mapToObj(String::valueOf)
                .collect(Collectors.joining());

        // 5. Attach the correct prefix back
        return prefix + newNumericPart;
    }

    // ✅ OLD METHOD: Kept for compatibility (if anything else uses it), but not used for signup anymore
    private String generateUserIdLegacy() {
        String userId = userRepository.findByUserId().orElse("C00001");
        String u = userId.substring(1);
        int[] arr = Arrays.stream(u.split("")).mapToInt(Integer::valueOf).toArray();
        arr = UtilityHelper.incrementByOne(arr);
        String user = Arrays.stream(arr).mapToObj(String::valueOf).collect(Collectors.joining());
        userId = userId.charAt(0) + user;
        return userId;
    }

    @Transactional
    public void verifyOtp(String email, String otpCode) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", email));

        otpService.validateOtp(user.getId(), otpCode);

        user.setStatus(User.UserStatus.ACTIVE);
        userRepository.save(user);
        logger.info("User verified and activated: {}", email);
    }

    @Transactional
    public void resendOtp(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", email));

        Otp otp = otpService.createOtp(user.getId());
        emailService.sendOtpEmail(user.getEmail(), otp.getOtpCode());
        logger.info("OTP resent to: {}", email);
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        if (user.getStatus() != User.UserStatus.ACTIVE) {
            throw new BadRequestException("User account is not active. Please verify your email first.");
        }

        String accessToken = jwtTokenUtil.generateAccessToken(
                user.getOwnerUserId(),
                user.getEmail(),
                user.getRoles(),
                user.getMobileNumber()
        );

        String refreshToken = jwtTokenUtil.generateRefreshToken(
                user.getOwnerUserId(),
                user.getEmail(),
                user.getRoles(),
                user.getMobileNumber()
        );

        logger.info("User logged in: {}", user.getEmail());

        return new AuthResponse(
                accessToken,
                refreshToken,
                user.getOwnerUserId(),
                user.getEmail(),
                user.getRoles()
        );
    }

    public AuthResponse refreshAccessToken(String refreshToken) {
        if (!jwtTokenUtil.validateRefreshToken(refreshToken)) {
            throw new InvalidCredentialsException("Invalid or expired refresh token");
        }

        String email = jwtTokenUtil.extractEmail(refreshToken);
        String ownerUserId = jwtTokenUtil.extractOwnerUserId(refreshToken);
        String role = jwtTokenUtil.extractRole(refreshToken);
        String mobileNumber = jwtTokenUtil.extractMobileNumber(refreshToken);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidCredentialsException("User not found for this token"));

        if (user.getStatus() != User.UserStatus.ACTIVE) {
            throw new BadRequestException("User account is not active.");
        }

        String newAccessToken = jwtTokenUtil.generateAccessToken(
                ownerUserId,
                email,
                role,
                mobileNumber
        );

        String newRefreshToken = jwtTokenUtil.generateRefreshToken(
                ownerUserId,
                email,
                role,
                mobileNumber
        );

        return new AuthResponse(
                newAccessToken,
                newRefreshToken,
                user.getOwnerUserId(),
                user.getEmail(),
                user.getRoles()
        );
    }
    // ✅ FIXED METHOD: Search by OwnerUserId (e.g., C00009)
    public User getProfile(String ownerUserId) {
        return userRepository.findByOwnerUserId(ownerUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", ownerUserId));
    }
    // Inside AuthService.java

    @Transactional
    public User updateProfile(String ownerUserId, UpdateProfileRequest request) {
        User user = userRepository.findByOwnerUserId(ownerUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", ownerUserId));

        // Update Mobile Number if provided
        if (request.getMobileNumber() != null && !request.getMobileNumber().isBlank()) {
            user.setMobileNumber(request.getMobileNumber());
        }

        // Update Alternative Mobile (can be null/empty if they want to clear it)
        user.setAlternativeMobileNumber(request.getAlternativeMobileNumber());
        // 3. ✅ Update Notification Preferences
        if(request.getNotifyWhatsapp() != null) user.setNotifyWhatsapp(request.getNotifyWhatsapp());
        if(request.getNotifySms() != null) user.setNotifySms(request.getNotifySms());
        if(request.getNotifyEmail() != null) user.setNotifyEmail(request.getNotifyEmail());


        return userRepository.save(user);
    }
}