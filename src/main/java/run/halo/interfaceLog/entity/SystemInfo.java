package run.halo.interfaceLog.entity;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class SystemInfo {
    private long totalSpace;
    private long unUsableSpace;
    private long totalMemory;
    private long unUsableMemory;
}
