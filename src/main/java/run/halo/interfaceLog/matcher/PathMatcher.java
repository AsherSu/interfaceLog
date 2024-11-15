package run.halo.interfaceLog.matcher;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import reactor.core.publisher.Mono;
import run.halo.app.extension.ExtensionUtil;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.extension.index.query.QueryFactory;
import run.halo.interfaceLog.extension.InterfaceLogRuleInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class PathMatcher {
    private final ReactiveExtensionClient client;
    private final AntPathMatcher antPathMatcher = new AntPathMatcher();
    private Mono<Map<String, List<String>>> antPathMatchers;
    
    public PathMatcher(ReactiveExtensionClient client) {
        this.client = client;
    }
    
    public void refreshMatcher() {
        antPathMatchers = client.listAll(InterfaceLogRuleInfo.class,
                ListOptions.builder()
                    .andQuery(QueryFactory.all())
                    .build(), 
                Sort.unsorted())
                .mapNotNull(i -> ExtensionUtil.isDeleted(i) ? null : i)
                .collectList()
                .map(this::buildMatcherMap);
    }
    
    private Map<String, List<String>> buildMatcherMap(List<InterfaceLogRuleInfo> rules) {
        Map<String, List<String>> matchers = new HashMap<>();
        List<String> include = new ArrayList<>();
        List<String> exclude = new ArrayList<>();
        matchers.put("include", include);
        matchers.put("exclude", exclude);

        rules.forEach(rule -> {
            if (rule.getSpec().getIsInclude()) {
                include.add(rule.getSpec().getRule());
            } else {
                exclude.add(rule.getSpec().getRule());
            }
        });

        if (include.isEmpty()) {
            exclude.add("/**");
        }

        return matchers;
    }
    
    public Mono<Boolean> matches(String path) {
        if (antPathMatchers == null) {
            refreshMatcher();
        }
        return antPathMatchers.map(matchers -> canMatch(matchers, path));
    }
    
    private boolean canMatch(Map<String, List<String>> matchers, String path) {
        List<String> include = matchers.get("include");
        List<String> exclude = matchers.get("exclude");
        
        boolean isIncluded = include.stream()
                .anyMatch(pattern -> antPathMatcher.match(pattern, path));
        boolean notExcluded = exclude.stream()
                .noneMatch(pattern -> antPathMatcher.match(pattern, path));
                
        return isIncluded && notExcluded;
    }
} 