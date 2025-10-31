package com.furnitureshop.furniture_erp_system.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "users") 
// Implement UserDetails เพื่อให้ Spring Security รู้จัก
public class User implements UserDetails {

    @Id
    @Column(name = "user_id")
    private String userID; 

    @Column(name = "username", nullable = false, unique = true)
    private String username; 

    @Column(name = "password_hash", nullable = false)
    private String passwordHash; // Field นี้จะเก็บรหัสผ่านที่ *เข้ารหัสแล้ว*

    @Column(name = "role", nullable = false)
    private String role; // (Sales, Stock, Admin, Delivery)

    // --- Constructor (ควรมี) ---
    public User() {}

    // --- Getters and Setters (ของเดิม) ---
	public String getUserID() { return userID; }
	public void setUserID(String userID) { this.userID = userID; }
    public String getRole() { return role; }
	public void setRole(String role) { this.role = role; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    // --- Method ที่ Implement จาก UserDetails ---

    /**
     * ดึง "สิทธิ์" ของ User
     * เราจะแปลง String "Sales" ให้เป็น "ROLE_Sales"
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // เพิ่ม "ROLE_" นำหน้า role ของเราเสมอ นี่คือกฎของ Spring Security
        return List.of(new SimpleGrantedAuthority("ROLE_" + this.role));
    }

    /**
     * Spring Security จะเรียกใช้ Method นี้เพื่อเอารหัสผ่าน
     */
    @Override
    public String getPassword() {
        return this.passwordHash; // คืนค่าจาก field ที่เก็บรหัสผ่าน
    }

    /**
     * Spring Security จะเรียกใช้ Method นี้เพื่อเอา Username
     */
    @Override
    public String getUsername() {
        return this.username;
    }

    // --- ตั้งค่า Account Status (สำหรับตอนนี้ตั้งเป็น true ทั้งหมด) ---
    @Override
    public boolean isAccountNonExpired() {
        return true; // บัญชีไม่หมดอายุ
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // บัญชีไม่ถูกล็อค
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // รหัสผ่านไม่หมดอายุ
    }

    @Override
    public boolean isEnabled() {
        return true; // บัญชีเปิดใช้งาน
    }
    
    // --- Setterสำหรับ Username (ของเดิม) ---
    public void setUsername(String username) {
		this.username = username;
	}
}