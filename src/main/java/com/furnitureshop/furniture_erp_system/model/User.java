package com.furnitureshop.furniture_erp_system.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * นี่คือคลาส Entity สำหรับตาราง 'Users' (ข้อมูลผู้ใช้งาน)
 * ใช้สำหรับ Login, กำหนดสิทธิ์ และอ้างอิงใน SO
 * มันตรงกับ Data Dictionary 3.9
 */
@Entity
// @Table(name = "users") 
// "user" มักเป็นคำสงวนใน SQL, "users" ปลอดภัยกว่า
@Table(name = "users") 
public class User {

    @Id
    @Column(name = "user_id")
    private String userID; // ตรงกับ 3.9: Field 'UserID'

    public String getUserID() {
		return userID;
	}

	public void setUserID(String userID) {
		this.userID = userID;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPasswordHash() {
		return passwordHash;
	}

	public void setPasswordHash(String passwordHash) {
		this.passwordHash = passwordHash;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	@Column(name = "username", nullable = false, unique = true)
    private String username; // ตรงกับ 3.9: Field 'Username'

    @Column(name = "password_hash", nullable = false)
    private String passwordHash; // ตรงกับ 3.9: Field 'PasswordHash'

    @Column(name = "role", nullable = false)
    private String role; // ตรงกับ 3.9: Field 'Role' (Sales, Stock, Admin)



}