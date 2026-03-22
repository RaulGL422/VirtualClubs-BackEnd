package galindo.raul.virtualclubs.models;

/**
 * Represents a device connected to the virtual club system.
 *
 * @param deviceId   Unique identifier for the device.
 * @param deviceName Human-readable name of the device.
 * @param deviceType Category or model of the device.
 * @param ipAddress  Network address assigned to the device.
 */
public record Dispositive(
        String deviceId,
        String deviceName,
        String deviceType,
        String ipAddress
) {}
