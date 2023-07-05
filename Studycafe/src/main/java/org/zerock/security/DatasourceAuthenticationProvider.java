package org.zerock.security;


import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import org.apache.commons.codec.binary.Base64;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.zerock.domain.User;
import org.zerock.service.UserService;

@Component
public class DatasourceAuthenticationProvider implements AuthenticationProvider {

	@Autowired
	private HttpServletRequest httpServletRequest;
	
	@Autowired
	private UserService service;
	
	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		String userID = (String) authentication.getPrincipal();
		String password = (String) authentication.getCredentials();
		
		
		
		String hashedPassword = sha256Hash(password);
		
		hashedPassword  = hashedPassword.toUpperCase();
		User user = service.getLoginUser(userID);
		String errMsg = "정상적으로 로그인 되었습니다.";
		
		if(!user.getUserPwd().equals(hashedPassword)){
			errMsg = "아이디 또는 패스워드가 일치하지 않습니다.";
			throw new BadCredentialsException(errMsg);
		}
	        
		List<GrantedAuthority> roles = new ArrayList<GrantedAuthority>();
        roles.add(new SimpleGrantedAuthority("ROLE_USER"));
        
        UsernamePasswordAuthenticationToken result = new UsernamePasswordAuthenticationToken(userID, password, roles);
        result.setDetails(new CustomUserDetails());

		
        return result;
	}

	@Override
	public boolean supports(Class<?> authentication) {
		return true;
	}
	
	
	
	public static String sha256Hash(String input) {
	    try {
	        MessageDigest digest = MessageDigest.getInstance("SHA-256");
	        byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
	
	        // 해시 값을 16진수 문자열로 변환
	        StringBuilder hexString = new StringBuilder();
	        for (byte b : hash) {
	            String hex = Integer.toHexString(0xff & b);
	            if (hex.length() == 1) {
	                hexString.append('0');
	            }
	            hexString.append(hex);
	        }
	
	        return hexString.toString();
	    } catch (NoSuchAlgorithmException e) {
	        e.printStackTrace();
	        return null;
	    }
	}
}
