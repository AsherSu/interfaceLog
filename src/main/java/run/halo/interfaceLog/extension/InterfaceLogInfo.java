package run.halo.interfaceLog.extension;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@GVK(group = "dailyActive.halo.run", version = "v1alpha1", kind = "InterfaceLogInfo",
        singular = "interfaceLogInfo", plural = "interfaceLogInfos")
@Accessors(chain = true)
public class InterfaceLogInfo extends AbstractExtension {

    public InterfaceLogInfo() {
        this.spec = new InterfaceLogInfoSpec();
    }

    @Schema(requiredMode = REQUIRED)
    private InterfaceLogInfoSpec spec;

    @Data
    @ToString
    @AllArgsConstructor
    @NoArgsConstructor
    @Accessors(chain = true)
    public static class InterfaceLogInfoSpec {

        private String username;

        private String clientIp;

        private Date accessTime;

        private String path;

        private String requestType;

        private String requestHeader;

        private Map<String, String> requestParams = new HashMap<>();

        private String requestBody;

        private String responseHeader;

        private String responseBody;

        private String responseStatus;
    }
}

