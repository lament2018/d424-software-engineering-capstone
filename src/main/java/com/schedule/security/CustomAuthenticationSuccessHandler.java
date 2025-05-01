package com.schedule.security;

import com.schedule.repository.UserRepository;
import com.schedule.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import org.springframework.security.core.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import java.io.IOException;


@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
    private final UserRepository userRepository;

    public CustomAuthenticationSuccessHandler(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        if(user.isMustChangePassword()){
            response.sendRedirect("/change-password");
        }else{
            response.sendRedirect("/dashboard");
        }

    }
}
