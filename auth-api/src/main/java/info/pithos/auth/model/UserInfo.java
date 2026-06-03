package info.pithos.auth.model;

import java.util.List;

public record UserInfo(
    String subject,
    String name,
    String email,
    String preferredUsername,
    List<String> groups
) {}
