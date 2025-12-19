package in.chaurasiya.moneymanager.Controller;

import in.chaurasiya.moneymanager.Entity.ProfileEntity;
import in.chaurasiya.moneymanager.Repository.ProfileRepository;
import in.chaurasiya.moneymanager.Service.AppUserDetailsService;
import in.chaurasiya.moneymanager.Util.JwtUtil;
import lombok.extern.slf4j.Slf4j;

import org.springframework.util.MultiValueMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/auth/google")
@Slf4j
public class GoogleOAuthController {

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String clientSecret;

    @Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
    private String redirectUri;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private AppUserDetailsService appUserDetailsService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping("/callback")
    public ResponseEntity<?> handleGoogleCallback(@RequestParam String code) {
        try {
            log.info("🔵 Received Google OAuth callback with code");

            String tokenEndpoint = "https://oauth2.googleapis.com/token";

            // Prepare request parameters
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("code", code);
            params.add("client_id", clientId);
            params.add("client_secret", clientSecret);
            params.add("redirect_uri", redirectUri);
            params.add("grant_type", "authorization_code");

            log.info("🔵 Requesting access token from Google");
            log.info("🔵 Redirect URI used: {}", redirectUri);

            // Prepare headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            // Send token request
            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
            ResponseEntity<Map> tokenResponse =
                    restTemplate.postForEntity(tokenEndpoint, request, Map.class);

            String idToken = (String) tokenResponse.getBody().get("id_token");
            log.info("🟢 Received ID token from Google");


            String userInfoUrl = "https://oauth2.googleapis.com/tokeninfo?id_token=" + idToken;
            ResponseEntity<Map> userInfoResponse = restTemplate.getForEntity(userInfoUrl, Map.class);

            if (userInfoResponse.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> userInfo = userInfoResponse.getBody();
                String email = (String) userInfo.get("email");
                String name = (String) userInfo.get("name");
                String picture = (String) userInfo.get("picture");

                log.info("🟢 User info received - Email: {}", email);

                UserDetails userDetails;
                try {
                    userDetails = appUserDetailsService.loadUserByUsername(email);
                    log.info("🟢 Existing user found: {}", email);
                } catch (Exception e) {
                    // Register new user if not found
                    log.info("🔵 Creating new user: {}", email);

                    ProfileEntity profile = ProfileEntity.builder()
                            .email(email)
                            .fullName(name != null ? name : email)
                            .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                            .isActive(true) // ✅ Google users are verified
                            .profileImageUrl(picture)
                            .build();

                    profileRepository.save(profile);
                    log.info("🟢 New user created: {}", email);

                    userDetails = appUserDetailsService.loadUserByUsername(email);
                }

                // Generate JWT token
                String jwtToken = jwtUtil.generateToken(email);
                log.info("🟢 JWT token generated for: {}", email);

                // ✅ Return complete response
                Map<String, Object> response = new HashMap<>();
                response.put("token", jwtToken);
                response.put("email", email);
                response.put("name", name);
                response.put("picture", picture);

                log.info("🟢 Sending success response to frontend");
                return ResponseEntity.ok(response);
            }

            log.error("❌ Failed to get user info from Google");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Collections.singletonMap("error", "Failed to authenticate with Google"));

        } catch (Exception e) {
            log.error("❌ Exception occurred during Google OAuth", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "Authentication failed: " + e.getMessage()));
        }
    }
}