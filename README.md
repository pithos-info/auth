# Pithos Auth

OAuth 2.0 / OIDC client implementations for the Pithos agent platform. Each module provides an async, Guice-injectable client for a specific identity provider.

## Modules

### auth-api
Interface and abstract base for OAuth operations. Java package: `info.pithos.auth`

| Type | Description |
|---|---|
| `OAuthClient` | Interface extending `ServiceLifeCycle` — token acquisition, refresh, revocation, introspection, and user info |
| `AbstractOAuthClient` | Base class providing `submitAsync` via the platform `ForkJoinExecutor` |

Operations:

| Group | Methods |
|---|---|
| Token acquisition | `clientCredentialsGrant` |
| Token lifecycle | `refreshToken`, `revokeToken` |
| Token validation | `introspectToken` |
| Identity | `getUserInfo` |

Model types (`info.pithos.auth.model`):

| Type | Fields |
|---|---|
| `TokenResponse` | `accessToken`, `refreshToken`, `expiresIn`, `tokenType`, `scope` |
| `TokenIntrospection` | `active`, `subject`, `clientId`, `username`, `expiresAt`, `issuedAt`, `scope`, `roles` |
| `UserInfo` | `subject`, `name`, `email`, `preferredUsername`, `groups` |
| `TokenType` | Enum: `ACCESS`, `REFRESH` |

### auth-keycloak
Keycloak implementation of `OAuthClient`. Java package: `info.pithos.auth.keycloak`

Uses `keycloak-admin-client` (`KeycloakBuilder` + `TokenManager.grantToken()`) for client-credentials token acquisition and `quarkus-oidc-client` (`OidcConstants`) for standard OIDC endpoint paths. All other endpoints (`/token`, `/introspect`, `/userinfo`, `/revoke`) are called via Java's built-in `HttpClient` using the OIDC standard paths provided by `quarkus-oidc-client`.

Config proto: `KeycloakOAuthConfigs` (`serverUrl`, `realm`, `clientId`, `clientSecret`, `timeoutMs`)

`refreshToken` calls the token endpoint directly with the provided refresh token. `revokeToken` accepts a `TokenType` hint (`ACCESS` or `REFRESH`) and posts to the revocation endpoint. `introspectToken` returns realm roles from `realm_access.roles`.

### auth-gcp
GCP Identity implementation of `OAuthClient`. Java package: `info.pithos.auth.gcp`

Uses `google-auth-library-oauth2-http` (`GoogleCredentials` / `ImpersonatedCredentials`) for token acquisition via Application Default Credentials. When `serviceAccountEmail` is set the client uses `ImpersonatedCredentials` to impersonate that service account; otherwise ADC scoped to `defaultScopes` is used directly.

`refreshToken` forces a credential refresh and returns a new access token — GCP service accounts do not use long-lived refresh tokens. `introspectToken` calls the `tokeninfo` endpoint; `getUserInfo` calls the `userinfo` endpoint. Both use Java's built-in `HttpClient`.

Config proto: `GcpIdentityOAuthConfigs` (`projectId`, `serviceAccountEmail`, `defaultScopes`)

---

## Config.proto

Two messages added to `Config.proto` and registered on `ConfigMap` (fields 17 and 18):

```proto
message KeycloakOAuthConfigs {
  string serverUrl = 1;
  string realm = 2;
  string clientId = 3;
  string clientSecret = 4;
  int32 timeoutMs = 5;
}

message GcpIdentityOAuthConfigs {
  string projectId = 1;
  string serviceAccountEmail = 2;
  repeated string defaultScopes = 3;
}
```

Rebuild `runtime-model` after proto changes to regenerate `KeycloakOAuthConfigs` and `GcpIdentityOAuthConfigs` before compiling auth modules.

---

## Usage

All clients follow the same lifecycle pattern used across the Pithos data layer:

```java
// 1. Create and initialise the Guice module
KeycloakOAuthModule module = new KeycloakOAuthModule(applicationContext);
module.init();

// 2. Create an injector
Injector injector = Guice.createInjector(module);

// 3. Start the client before use
OAuthClient client = injector.getInstance(OAuthClient.class);
client.start(10, TimeUnit.SECONDS).join();

// 4. Use the client
TokenResponse token = client.clientCredentialsGrant(requestContext, List.of("openid")).join();

client.introspectToken(requestContext, token.accessToken())
      .thenAccept(info -> System.out.println("active: " + info.active()));

client.getUserInfo(requestContext, token.accessToken())
      .thenAccept(u -> System.out.println("user: " + u.email()));

// 5. Shut down gracefully
client.shutdown(10, TimeUnit.SECONDS).join();
```

Swap `KeycloakOAuthModule` for `GcpIdentityOAuthModule` to switch identity providers with no changes to call sites.

## Build

Requires JDK 23, Maven 3.9.x, and the `pithos-runtime-core-model` and `pithos-runtime-core-context` SNAPSHOTs installed locally. The `pithos-runtime-core-model` module must be rebuilt after the proto changes to regenerate `KeycloakOAuthConfigs` and `GcpIdentityOAuthConfigs`:

```bash
mvn install -f ../runtime-model/pom.xml
mvn compile          # compile all auth modules
mvn install          # install all auth modules to local Maven repository
```

## Dependencies

| Dependency | Version |
|---|---|
| `quarkus-oidc-client` (io.quarkus) | 3.15.1 |
| `keycloak-admin-client` (org.keycloak) | 26.0.7 |
| `google-auth-library-oauth2-http` | 1.30.0 |
| `jackson-databind` | 2.18.2 |
| `guice` | 7.0.0 |
| `slf4j-api` | 2.0.16 |
