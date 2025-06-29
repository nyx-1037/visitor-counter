package com.nyx.visitorcounter.util;

import org.springframework.security.crypto.password.PasswordEncoder;

public class EncodeGenerater {
	public static void main(String[] args) {
		PasswordEncoder passwordEncoder = new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
		System.out.println(passwordEncoder.encode("123456"));
	}
}
