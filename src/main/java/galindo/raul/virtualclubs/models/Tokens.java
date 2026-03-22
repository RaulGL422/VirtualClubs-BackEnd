package galindo.raul.virtualclubs.models;

/**
 * Represents a pair of authentication tokens.
 * @param accessToken The token used for authenticating requests.
 * @param refreshToken The token used to obtain a new access token.
 */
public record Tokens(
    String accessToken,
    String refreshToken
) {}
