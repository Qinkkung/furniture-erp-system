package com.furnitureshop.furniture_erp_system.config;

import com.furnitureshop.furniture_erp_system.service.UserDetailsServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Bean นี้คือ "เครื่องมือเข้ารหัสรหัสผ่าน"
     * เราจะใช้ BCrypt ซึ่งปลอดภัยที่สุด
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Bean นี้คือ "ตัวจัดการการยืนยันตัวตน"
     * เราบอกมันว่า:
     * 1. ให้ใช้ UserDetailsService ของเรา (UserDetailsServiceImpl)
     * 2. ให้ใช้เครื่องมือเข้ารหัส (PasswordEncoder) ที่เราสร้าง
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider(UserDetailsServiceImpl userDetailsService) {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * Bean นี้คือ "หัวใจของกฎ"
     * เราจะกำหนดว่า URL path ไหน ใครเข้าได้บ้าง
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authz -> authz
                // 1. อนุญาตให้ทุกคนเข้าถึงหน้า Login และไฟล์ CSS, รูปภาพ
                .requestMatchers("/login", "/css/**", "/images/**").permitAll()

                // 2. กำหนดสิทธิ์ตาม Role (*** แก้ไขลำดับตรงนี้ ***)

                // === (ย้ายขึ้นมา) ===
                // กฎที่ "เจาะจง" ต้องอยู่ก่อนกฎที่ "กว้าง"
                .requestMatchers("/products/variants/**", "/api/inventory/ats/**").hasAnyRole("Sales", "Stock", "Admin")
                .requestMatchers("/api/customers/**").hasRole("Sales") // (API ลูกค้า ให้ Sales ใช้ได้)

                // === (กฎเดิม) ===
                .requestMatchers("/sales-orders/**").hasRole("Sales")
                .requestMatchers("/stock/**", "/purchase-orders/**").hasRole("Stock")
                .requestMatchers("/shipments/**").hasRole("Delivery")
                
                // (กฎนี้ต้องอยู่ "หลัง" กฎ /products/variants/**)
                .requestMatchers("/products/**", "/admin/**").hasRole("Admin")

                // 3. หน้า Dashboard (หน้าแรก) และหน้าอื่นๆ ต้อง "Login ก่อน"
                .requestMatchers("/").authenticated()
                .anyRequest().authenticated() // หน้าอื่นๆ ที่ไม่ได้ระบุ ต้อง Login ทั้งหมด
            )
            .formLogin(form -> form
                .loginPage("/login") 
                .loginProcessingUrl("/login") 
                .defaultSuccessUrl("/", true) 
                .permitAll() 
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/login?logout") 
                .permitAll()
            );

        return http.build();
    }
}