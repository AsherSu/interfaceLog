package run.halo.interfaceLog;

import org.springframework.context.ApplicationEvent;

public class SchemeRegistered extends ApplicationEvent {
    public SchemeRegistered(Object source) {
        super(source);
    }
}

