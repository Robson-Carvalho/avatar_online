package com.avatar.avatar_online.service;

import com.avatar.avatar_online.models.User;
import com.avatar.avatar_online.pubsub.ClientMessageDTO;
import com.avatar.avatar_online.pubsub.SignInDTO;
import com.avatar.avatar_online.pubsub.SignUpDTO;
import com.avatar.avatar_online.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.resource.ResourceUrlProvider;

import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public UUID signUpProcessment(SignUpDTO signUpDTO){
        try {
            User user = new User();

            user.setName(signUpDTO.getName());
            user.setNickname(signUpDTO.getNickname());
            user.setEmail(signUpDTO.getEmail());
            user.setPassword(signUpDTO.getPassword());
            user.setId(UUID.randomUUID());

            userRepository.save(user);
            return user.getId();
        } catch (DataIntegrityViolationException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    @Transactional
    public UUID signInProcessment(SignInDTO signInDTO){
        try {
            User user = userRepository.findByNickname(signInDTO.getNickname());

            if(user == null || !user.getPassword().equals(signInDTO.getPassword())){
                return null;
            }

            return user.getId();

        } catch (DataIntegrityViolationException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }
}
