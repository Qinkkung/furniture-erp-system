package com.furnitureshop.furniture_erp_system.service;

import com.furnitureshop.furniture_erp_system.model.User;
import com.furnitureshop.furniture_erp_system.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service // บอก Spring ว่านี่คือ Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    /**
     * นี่คือ Method หลักที่ Spring Security จะเรียกใช้
     * เมื่อมีคนพยายาม Login
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // ใช้ Method ที่เราสร้างใน UserRepository เพื่อค้นหา User
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("ไม่พบผู้ใช้งาน: " + username));
        
        return user; // คืนค่า User ที่ Implement UserDetails
    }
}