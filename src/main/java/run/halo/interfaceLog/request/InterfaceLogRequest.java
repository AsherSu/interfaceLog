package run.halo.interfaceLog.request;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class InterfaceLogRequest {
    private List<String> username;
    private List<String> clientIp;
    private List<String> path;
    private String page;
    private String size;
    private String startTime;
    private String endTime;
    private List<String> accessTimes;
}