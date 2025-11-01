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
     * (*** นี่คือเวอร์ชันที่แก้ไขลำดับ และเพิ่ม /pos/** แล้ว ***)
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authz -> authz
                // 1. อนุญาตให้ทุกคนเข้าถึงหน้า Login และไฟล์ CSS, รูปภาพ
                .requestMatchers("/login", "/css/**", "/images/**").permitAll()

                // 2. กำหนดสิทธิ์ตาม Role (*** นี่คือลำดับที่ถูกต้อง ***)

                // (API ที่เจาะจง ต้องอยู่ก่อน)
                .requestMatchers("/products/variants/**", "/api/inventory/ats/**").hasAnyRole("Sales", "Stock", "Admin")
                .requestMatchers("/api/customers/**").hasRole("Sales") 

                // (กฎของ Role หลัก)
                .requestMatchers("/sales-orders/**", "/pos/**").hasRole("Sales") // <-- (เพิ่ม /pos/**)
                .requestMatchers("/stock/**", "/purchase-orders/**").hasRole("Stock")
                .requestMatchers("/shipments/**").hasRole("Delivery")
                
                // (กฎที่กว้างที่สุด ต้องอยู่ล่างสุด)
                .requestMatchers("/products/**", "/admin/**").hasRole("Admin")

                // 3. หน้า Dashboard (หน้าแรก) และหน้าอื่นๆ ต้อง "Login ก่อน"
                .requestMatchers("/").authenticated()
                .anyRequest().authenticated() // หน้าอื่นๆ ที่ไม่ได้ระบุ ต้อง Login ทั้งหมด
            )
            .formLogin(form -> form
                .loginPage("/login") // บอกว่าหน้า Login ของเราคือ URL นี้
                .loginProcessingUrl("/login") // URL ที่ <form> จะส่งข้อมูลไป (ใช้ /login เหมือนกัน)
                .defaultSuccessUrl("/", true) // ถ้า Login สำเร็จ ให้ไปหน้าแรก
                .permitAll() // ทุกคนต้องเห็นหน้า Login
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/login?logout") // ถ้า Logout สำเร็จ ให้กลับไปหน้า Login
                .permitAll()
            );

        return http.build();
    }
}