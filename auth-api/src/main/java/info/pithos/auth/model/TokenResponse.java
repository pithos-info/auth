package info.pithos.auth.model;

public record TokenResponse(
    String accessToken,
    String refreshToken,
    long expiresIn,
    String tokenType,
    String scope
) {}
