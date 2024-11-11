package run.halo.interfaceLog.service;

import java.io.File;
import java.lang.management.ManagementFactory;
import com.sun.management.OperatingSystemMXBean;
import run.halo.interfaceLog.entity.SystemInfo;

public class SystemInfoService {
    public static SystemInfo systemInfo() {
        File root = new File("/");
        long totalSpace = root.getTotalSpace();
        long freeSpace = root.getFreeSpace();
        // 获取内存信息
        OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
        long totalMemory = osBean.getTotalMemorySize();
        long freeMemory = osBean.getFreeMemorySize();
        return new SystemInfo()
            .setTotalSpace(totalSpace)
            .setUnUsableSpace(totalSpace-freeSpace)
            .setTotalMemory(totalMemory)
            .setUnUsableMemory(totalMemory-freeMemory);
    }
}
