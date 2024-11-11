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
@GVK(group = "dailyActive.halo.run", version = "v1alpha1", kind = "InterfaceLogRuleInfo",
    singular = "interfaceLogRuleInfo", plural = "interfaceLogRuleInfos")
@Accessors(chain = true)
public class InterfaceLogRuleInfo extends AbstractExtension {

    public InterfaceLogRuleInfo() {
        this.spec = new InterfaceLogRuleInfoSpec();
    }

    @Schema(requiredMode = REQUIRED)
    private InterfaceLogRuleInfoSpec spec;

    @Data
    @ToString
    @AllArgsConstructor
    @NoArgsConstructor
    @Accessors(chain = true)
    public static class InterfaceLogRuleInfoSpec {

        private Long id;
        private Boolean isInclude;
        private String rule;
        private String version;
    }
}
