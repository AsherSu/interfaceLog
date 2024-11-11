package run.halo.interfaceLog.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InterfaceLogRuleFileVO {
    private List<InterfaceLogRule> interfaceLogRules;
}
