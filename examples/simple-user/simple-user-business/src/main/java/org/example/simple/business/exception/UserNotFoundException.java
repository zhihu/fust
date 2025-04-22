package org.example.simple.business.exception;

/**
 * 用户不存在异常
 */
public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(Long id) {
        super("User not found with id: " + id);
    }
    
    public UserNotFoundException(String message) {
        super(message);
    }
} 