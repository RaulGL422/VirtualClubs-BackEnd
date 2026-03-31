package galindo.raul.virtualclubs.utils;

import galindo.raul.virtualclubs.models.Dispositive;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

class CommonUtilsTest {

    @Test
    void getDispositiveInfo_sinHeaders_devuelveDispositiveConRemoteAddr() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("192.168.1.1");
        request.addHeader("User-Agent", "TestAgent/1.0");

        Dispositive disp = CommonUtils.getDispositiveInfo(request);

        assertThat(disp).isNotNull();
        assertThat(disp.ipAddress()).isEqualTo("192.168.1.1");
        assertThat(disp.deviceId()).isNotBlank();
    }

    @Test
    void getDispositiveInfo_conXForwardedFor_usaIpForwarded() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Forwarded-For", "10.0.0.1, 10.0.0.2");
        request.addHeader("User-Agent", "TestAgent/1.0");

        Dispositive disp = CommonUtils.getDispositiveInfo(request);

        // Debe tomar el primer valor del header (el cliente real)
        assertThat(disp.ipAddress()).isEqualTo("10.0.0.1, 10.0.0.2");
    }

    @Test
    void getDispositiveInfo_conXDeviceId_loUsaEnElHashDelDeviceId() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("1.2.3.4");
        request.addHeader("User-Agent", "UA");
        request.addHeader("X-Device-ID", "mi-uuid-fijo");

        MockHttpServletRequest requestSinDeviceId = new MockHttpServletRequest();
        requestSinDeviceId.setRemoteAddr("1.2.3.4");
        requestSinDeviceId.addHeader("User-Agent", "UA");

        Dispositive conId    = CommonUtils.getDispositiveInfo(request);
        Dispositive sinId    = CommonUtils.getDispositiveInfo(requestSinDeviceId);

        // El deviceId calculado debe ser distinto si el X-Device-ID es distinto
        assertThat(conId.deviceId()).isNotEqualTo(sinId.deviceId());
    }

    @Test
    void getDispositiveInfo_mismosInputs_producenMismoDeviceId() {
        MockHttpServletRequest r1 = new MockHttpServletRequest();
        r1.setRemoteAddr("1.2.3.4");
        r1.addHeader("User-Agent", "UA");
        r1.addHeader("X-Device-ID", "uuid-123");

        MockHttpServletRequest r2 = new MockHttpServletRequest();
        r2.setRemoteAddr("1.2.3.4");
        r2.addHeader("User-Agent", "UA");
        r2.addHeader("X-Device-ID", "uuid-123");

        assertThat(CommonUtils.getDispositiveInfo(r1).deviceId())
                .isEqualTo(CommonUtils.getDispositiveInfo(r2).deviceId());
    }

    @Test
    void getDispositiveInfo_conHeadersDeDispositivo_losMapea() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("1.2.3.4");
        request.addHeader("User-Agent", "UA");
        request.addHeader("X-Device-Name", "Mi Movil");
        request.addHeader("X-Device-Type", "Android");

        Dispositive disp = CommonUtils.getDispositiveInfo(request);

        assertThat(disp.deviceName()).isEqualTo("Mi Movil");
        assertThat(disp.deviceType()).isEqualTo("Android");
    }
}
