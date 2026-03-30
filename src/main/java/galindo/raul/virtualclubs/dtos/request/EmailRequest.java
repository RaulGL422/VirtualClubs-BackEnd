package galindo.raul.virtualclubs.dtos.request;

public record EmailRequest(
    String to,
    String toName,
    String subject,
    String template,
    Object model
) {}