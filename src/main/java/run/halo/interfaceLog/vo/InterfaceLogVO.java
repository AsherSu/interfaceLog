package run.halo.interfaceLog.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import run.halo.interfaceLog.extension.InterfaceLogInfo;

import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.TimeZone;

@Data
@AllArgsConstructor
public class InterfaceLogVO {
    private String id;

    private String username;

    private String clientIp;

    private String accessTime;

    private String path;

    private String requestType;

    private String responseStatus;

    public InterfaceLogVO(InterfaceLogInfo info) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        formatter.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
        this.username = info.getSpec().getUsername();
        this.clientIp = info.getSpec().getClientIp();
        this.accessTime = formatter.format(info.getSpec().getAccessTime());
        this.path = generatePath(info);
        this.requestType = info.getSpec().getRequestType();
        this.responseStatus = info.getSpec().getResponseStatus();
        this.id = info.getMetadata().getName();
    }

    private String generatePath(InterfaceLogInfo info) {
        Map<String, String> singleValueMap = info.getSpec().getRequestParams();
        if (!singleValueMap.isEmpty()) {
            StringBuilder pathBuilder = new StringBuilder(info.getSpec().getPath()).append("?");
            singleValueMap.forEach((key, value) ->
                    pathBuilder.append(key).append("=").append(value).append("&")
            );
            // Remove the trailing '&'
            pathBuilder.setLength(pathBuilder.length() - 1);
            return pathBuilder.toString();
        }
        return info.getSpec().getPath();
    }
}
