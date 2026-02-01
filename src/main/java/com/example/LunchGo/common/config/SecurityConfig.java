package com.example.LunchGo.common.config;

import com.example.LunchGo.account.helper.JwtFilter;
import com.example.LunchGo.common.exception.JwtAccessDeniedHandler;
import com.example.LunchGo.common.exception.JwtAuthenticationEntryPoint;
import com.example.LunchGo.common.logging.RequestLoggingFilter;
import com.example.LunchGo.common.util.TokenUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final TokenUtils tokenUtils;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, CorsConfigurationSource corsConfigurationSource, JwtFilter jwtFilter) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                .exceptionHandling(exception -> {
                    exception.accessDeniedHandler(jwtAccessDeniedHandler); //403
                    exception.authenticationEntryPoint(jwtAuthenticationEntryPoint); //401
                })

                .sessionManagement(sessionManagement ->
                        sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                ) //세션 사용안할 거니 세션 만들지마라는 코드

                .authorizeHttpRequests(authorizeRequests -> authorizeRequests
                        //각자 권한 걸어주면 됨니다
                        .requestMatchers("/reminders/**").permitAll()
                        .requestMatchers("/api/reminders/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/reminders/**", "/api/reminders/**").permitAll()
                        .requestMatchers("/api/join/**", "/api/auth/**", "/api/sms/**", "/api/login", "/api/login/queue").permitAll()
                        .requestMatchers("/api/refresh").permitAll()
                        .requestMatchers("/error").permitAll()
                        .requestMatchers("/api/reminders/**", "/reminders/**").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/search/email").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/search/loginId").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/search/pwd").permitAll()
                        .requestMatchers(HttpMethod.PUT, "/api/auth/pwd").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/email/*").hasAuthority("ROLE_USER")

                        .requestMatchers(HttpMethod.GET,
                                "/api/restaurants",
                                "/api/restaurants/{id}",
                                "/api/restaurants/*/menus",
                                "/api/restaurants/search"
                        ).permitAll()
                        .requestMatchers(HttpMethod.GET,
                                "/api/restaurants/*/reviews",
                                "/api/restaurants/*/reviews/*",
                                "/api/restaurants/trending"
                        ).permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/weather/current").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/tags/search").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/reviews/tags").hasAuthority("ROLE_USER")
                        .requestMatchers(HttpMethod.GET, "/api/reviews/my").hasAuthority("ROLE_USER")
                        .requestMatchers(HttpMethod.POST, "/api/payments/portone/webhook").permitAll()

                        .requestMatchers(HttpMethod.POST, "/api/restaurants/*/reviews").hasAuthority("ROLE_USER")
                        .requestMatchers(HttpMethod.GET, "/api/restaurants/*/reviews/*/edit").hasAuthority("ROLE_USER")
                        .requestMatchers(HttpMethod.PUT, "/api/restaurants/*/reviews/*").hasAuthority("ROLE_USER")
                        .requestMatchers(HttpMethod.DELETE, "/api/restaurants/*/reviews/*").hasAuthority("ROLE_USER")
                        .requestMatchers(HttpMethod.GET, "/api/restaurants/mapping/**").hasAuthority("ROLE_USER")

                        .requestMatchers(HttpMethod.POST, "/api/v1/images/upload/*")
                        .hasAnyAuthority("ROLE_USER", "ROLE_OWNER")
                        .requestMatchers(HttpMethod.GET, "/api/v1/images/presign").hasAuthority("ROLE_USER")
                        .requestMatchers(HttpMethod.POST, "/api/ocr/receipt").hasAuthority("ROLE_USER")
                        .requestMatchers(HttpMethod.POST, "/api/chatbot/**").hasAuthority("ROLE_USER")

                        .requestMatchers(HttpMethod.POST, "/api/reservations/*/payments").hasAuthority("ROLE_USER")
                        .requestMatchers(HttpMethod.PATCH, "/api/reservations/*/complete").hasAnyAuthority("ROLE_OWNER", "ROLE_STAFF")
                        .requestMatchers(HttpMethod.POST, "/api/payments/portone/complete").hasAuthority("ROLE_USER")
                        .requestMatchers(HttpMethod.POST, "/api/payments/portone/fail").hasAuthority("ROLE_USER")
                        .requestMatchers(HttpMethod.POST, "/api/payments/portone/requested").hasAuthority("ROLE_USER")
                        .requestMatchers(HttpMethod.POST, "/api/reservations/*/payments/expire").hasAuthority("ROLE_USER")
                        .requestMatchers(HttpMethod.GET, "/api/reservations/*/confirmation").hasAuthority("ROLE_USER")
                        .requestMatchers(HttpMethod.GET, "/api/reservations/*/summary").hasAuthority("ROLE_USER")

                        .requestMatchers(HttpMethod.POST, "/api/cafeteria/menus/ocr").hasAuthority("ROLE_USER")
                        .requestMatchers(HttpMethod.POST, "/api/cafeteria/menus/confirm").hasAuthority("ROLE_USER")
                        .requestMatchers(HttpMethod.GET, "/api/cafeteria/menus/week").hasAuthority("ROLE_USER")
                        .requestMatchers(HttpMethod.GET, "/api/cafeteria/recommendations").hasAuthority("ROLE_USER")
                        .requestMatchers(HttpMethod.POST, "/api/map/distance").hasAuthority("ROLE_USER")
                        .requestMatchers(HttpMethod.POST, "/api/map/route").hasAuthority("ROLE_USER")

                        .requestMatchers(HttpMethod.POST, "/api/bookmark-links").hasAuthority("ROLE_USER")
                        .requestMatchers(HttpMethod.PATCH, "/api/bookmark-links/*").hasAuthority("ROLE_USER")
                        .requestMatchers(HttpMethod.GET, "/api/bookmark-links/sent").hasAuthority("ROLE_USER")
                        .requestMatchers(HttpMethod.GET, "/api/bookmark-links/received").hasAuthority("ROLE_USER")
                        .requestMatchers(HttpMethod.GET, "/api/bookmark-links/search").hasAuthority("ROLE_USER")
                        .requestMatchers(HttpMethod.GET, "/api/bookmark-links/search/list").hasAuthority("ROLE_USER")
                        .requestMatchers(HttpMethod.DELETE, "/api/bookmark-links").hasAuthority("ROLE_USER")
                        .requestMatchers(HttpMethod.POST, "/api/bookmark").hasAuthority("ROLE_USER")
                        .requestMatchers(HttpMethod.DELETE, "/api/bookmark").hasAuthority("ROLE_USER")
                        .requestMatchers(HttpMethod.PATCH, "/api/bookmark/visibility").hasAuthority("ROLE_USER")
                        .requestMatchers(HttpMethod.PATCH, "/api/bookmark/promotion").hasAuthority("ROLE_USER")
                        .requestMatchers(HttpMethod.GET, "/api/bookmark/shared").hasAuthority("ROLE_USER")
                        .requestMatchers(HttpMethod.GET, "/api/bookmark/list").hasAuthority("ROLE_USER")
                        .requestMatchers(HttpMethod.GET, "/api/info/user/*").hasAuthority("ROLE_USER")
                        .requestMatchers(HttpMethod.PUT, "/api/info/user/*").hasAuthority("ROLE_USER")

                        .requestMatchers(HttpMethod.POST, "/api/owners/restaurants/*/reviews/*/comments").hasAnyAuthority("ROLE_OWNER", "ROLE_STAFF")
                        .requestMatchers(HttpMethod.DELETE, "/api/owners/restaurants/*/reviews/*/comments/*").hasAnyAuthority("ROLE_OWNER", "ROLE_STAFF")
                        .requestMatchers(HttpMethod.POST, "/api/owners/restaurants/*/reviews/*/blind-requests").hasAuthority("ROLE_OWNER")
                        .requestMatchers(HttpMethod.GET, "/api/owners/restaurants/*/reviews").hasAnyAuthority("ROLE_OWNER", "ROLE_STAFF", "ROLE_ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/owners/restaurants/*/reviews/*").hasAnyAuthority("ROLE_OWNER", "ROLE_STAFF", "ROLE_ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/business/restaurants/*").hasAuthority("ROLE_OWNER")
                        .requestMatchers(HttpMethod.GET, "/api/business/restaurants/*/images").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/business/reservations/**").hasAnyAuthority("ROLE_OWNER", "ROLE_STAFF")
                        .requestMatchers(HttpMethod.POST, "/api/business/reservations/**").hasAuthority("ROLE_OWNER")
                        .requestMatchers(HttpMethod.PATCH, "/api/business/reservations/**").hasAuthority("ROLE_OWNER")
                        .requestMatchers(HttpMethod.GET, "/api/business/restaurants/*/stats/weekly.pdf").hasAuthority("ROLE_OWNER")
                        .requestMatchers(HttpMethod.GET, "/api/business/restaurants/*/stats/weekly").hasAuthority("ROLE_OWNER")

                        .requestMatchers(HttpMethod.POST, "/api/business/staff").hasAuthority("ROLE_OWNER")
                        .requestMatchers(HttpMethod.DELETE, "/api/business/staff").hasAuthority("ROLE_OWNER")
                        .requestMatchers(HttpMethod.GET, "/api/business/staff/*").hasAnyAuthority("ROLE_OWNER", "ROLE_STAFF")
                        .requestMatchers(HttpMethod.POST, "/api/business/promotion/*").hasAuthority("ROLE_OWNER")
                        .requestMatchers(HttpMethod.GET, "/api/info/business/*").hasAuthority("ROLE_OWNER")
                        .requestMatchers(HttpMethod.PUT, "/api/info/business/*").hasAuthority("ROLE_OWNER")

                        .requestMatchers(HttpMethod.GET, "/api/admin/reviews").hasAuthority("ROLE_ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/admin/forbidden-words").hasAuthority("ROLE_ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/admin/forbidden-words").hasAuthority("ROLE_ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/admin/forbidden-words/*").hasAuthority("ROLE_ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/admin/forbidden-words/*").hasAuthority("ROLE_ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/admin/reviews/*/blind-requests").hasAuthority("ROLE_ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/admin/reviews/*/hide").hasAuthority("ROLE_ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/admin/list/owner").hasAuthority("ROLE_ADMIN")
                        .anyRequest().authenticated()
                )

                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(requestLoggingFilter(), JwtFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(
                "http://localhost:5173",
                "http://localhost:8080",
                "http://lunchgo.s3-website.kr.object.ncloudstorage.com",
                "https://lunchgo.s3-website.kr.object.ncloudstorage.com"
        ));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-type", "X-Requested-With"));
        configuration.setExposedHeaders(List.of("Authorization", "Location"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(8); //cost를 낮춰 속도 개선
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public JwtFilter jwtFilter() {
        return new JwtFilter(tokenUtils);
    }

    @Bean
    public RequestLoggingFilter requestLoggingFilter() {
        return new RequestLoggingFilter();
    }
}
