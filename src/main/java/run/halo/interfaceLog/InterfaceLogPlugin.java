package run.halo.interfaceLog;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import run.halo.app.extension.Scheme;
import run.halo.app.extension.SchemeManager;
import run.halo.app.extension.index.IndexSpec;
import run.halo.app.plugin.BasePlugin;
import run.halo.app.plugin.PluginContext;
import run.halo.interfaceLog.extension.InterfaceLogInfo;
import run.halo.interfaceLog.extension.InterfaceLogRuleInfo;
import run.halo.interfaceLog.extension.RetentionDurationInfo;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import static run.halo.app.extension.index.IndexAttributeFactory.simpleAttribute;

/**
 * <p>Plugin main class to manage the lifecycle of the plugin.</p>
 * <p>This class must be public and have a public constructor.</p>
 * <p>Only one main class extending {@link BasePlugin} is allowed per plugin.</p>
 *
 * @author guqing
 * @since 1.0.0
 */
@Component
public class InterfaceLogPlugin extends BasePlugin {

    private final SchemeManager schemeManager;
    private final ApplicationEventPublisher eventPublisher;

    public InterfaceLogPlugin(PluginContext pluginContext, SchemeManager schemeManager,
        ApplicationEventPublisher eventPublisher) {
        super(pluginContext);
        this.schemeManager = schemeManager;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void start() {
        schemeManager.register(RetentionDurationInfo.class);
        schemeManager.register(InterfaceLogRuleInfo.class);
        schemeManager.register(InterfaceLogInfo.class, indexSpecs -> {
                indexSpecs.add(new IndexSpec()
                    .setName("spec.username")
                    .setIndexFunc(simpleAttribute(InterfaceLogInfo.class, interfaceLogInfo -> {
                        String username = interfaceLogInfo.getSpec().getUsername();
                        return username == null ? "" : username;
                    }))
                );
                indexSpecs.add(new IndexSpec()
                    .setName("spec.clientIp")
                    .setIndexFunc(simpleAttribute(InterfaceLogInfo.class, interfaceLogInfo -> {
                        String clientIp = interfaceLogInfo.getSpec().getClientIp();
                        return clientIp == null ? "" : clientIp;
                    }))
                );
                indexSpecs.add(new IndexSpec()
                    .setName("spec.path")
                    .setIndexFunc(simpleAttribute(InterfaceLogInfo.class, interfaceLogInfo -> {
                        String path = interfaceLogInfo.getSpec().getPath();
                        return path == null ? "" : path;
                    }))
                );
                indexSpecs.add(new IndexSpec()
                    .setName("spec.accessTime")
                    .setIndexFunc(simpleAttribute(InterfaceLogInfo.class, interfaceLogInfo -> {
                        Date accessTime = interfaceLogInfo.getSpec().getAccessTime();
                        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        formatter.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
                        return accessTime == null ? null : formatter.format(accessTime);
                    }))
                );
            }
        );
    }

    @Override
    public void stop() {
        schemeManager.unregister(Scheme.buildFromType(InterfaceLogInfo.class));
        schemeManager.unregister(Scheme.buildFromType(InterfaceLogRuleInfo.class));
        schemeManager.unregister(Scheme.buildFromType(RetentionDurationInfo.class));
    }
}
