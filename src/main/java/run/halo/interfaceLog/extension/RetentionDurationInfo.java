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

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@GVK(group = "dailyActive.halo.run", version = "v1alpha1", kind = "RetentionDurationInfo",
    singular = "retentionDurationInfo", plural = "retentionDurationInfos")
@Accessors(chain = true)
public class RetentionDurationInfo extends AbstractExtension {
    public RetentionDurationInfo() {
        this.spec = new RetentionDurationInfoSpec();
    }

    @Schema(requiredMode = REQUIRED)
    private RetentionDurationInfo.RetentionDurationInfoSpec spec;

    @Data
    @ToString
    @AllArgsConstructor
    @NoArgsConstructor
    @Accessors(chain = true)
    public static class RetentionDurationInfoSpec {
        private Integer day;
    }
}
