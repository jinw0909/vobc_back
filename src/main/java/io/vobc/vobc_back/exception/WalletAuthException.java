package io.vobc.vobc_back.exception;

public class WalletAuthException extends RuntimeException {
    public WalletAuthException(String message) {
        super(message);
    }
    public WalletAuthException(String message, Throwable cause) {
        super(message, cause);
    }
}
