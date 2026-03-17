package br.com.fiap.essentia.shared.exception;

public class Exceptions {


    public static class EntityNotFoundException extends RuntimeException {
        public EntityNotFoundException(String message) { super(message); }
    }


    public static class EmailAlreadyInUseException extends RuntimeException {
        public EmailAlreadyInUseException(String message) { super(message); }
    }


    public static class InvalidPasswordException extends RuntimeException {
        public InvalidPasswordException(String message) { super(message); }
    }


    public class UnauthorizedActionException extends RuntimeException {
        public UnauthorizedActionException(String message) { super(message); }
    }


    public static class BusinessException extends RuntimeException {
        public BusinessException(String message) { super(message); }
    }

}
