package info.pithos.auth.model;

import java.util.List;

public record TokenIntrospection(
    boolean active,
    String subject,
    String clientId,
    String username,
    long expiresAt,
    long issuedAt,
    String scope,
    List<String> roles
) {}
