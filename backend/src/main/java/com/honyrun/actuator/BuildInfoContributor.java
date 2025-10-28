package com.honyrun.actuator;

import com.honyrun.util.version.BuildVersionManager;
import com.honyrun.util.version.BuildVersionManager.BuildStatistics;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.boot.actuate.info.Info;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Actuator Info 构建与环境信息贡献者
 *
 * 将构建版本（8位）、构建次数、最后构建时间、构建历史以及应用版本
 * 注入到 /actuator/info 的 "build" 节点；同时提供当前激活的Profile信息。
 */
@Component
public class BuildInfoContributor implements InfoContributor {

    private final BuildVersionManager buildVersionManager;
    private final Environment environment;

    public BuildInfoContributor(BuildVersionManager buildVersionManager, Environment environment) {
        this.buildVersionManager = buildVersionManager;
        this.environment = environment;
    }

    @Override
    public void contribute(Info.Builder builder) {
        Map<String, Object> build = new HashMap<>();
        try {
            BuildStatistics stats = buildVersionManager.getBuildStatistics()
                    .block(Duration.ofMillis(300));
            if (stats != null) {
                build.put("version", stats.getCurrentVersion());
                build.put("count", stats.getBuildCount());
                build.put("lastTime", stats.getLastBuildTime());
                build.put("history", stats.getBuildHistory());
                build.put("appVersion", stats.getAppVersion());
            } else {
                String version = buildVersionManager.getCurrentBuildVersion()
                        .block(Duration.ofMillis(200));
                build.put("version", version != null ? version : "00000001");
            }
        } catch (Exception e) {
            build.put("version", "00000001");
            build.put("error", "build statistics unavailable");
        }
        builder.withDetail("build", build);

        // 环境信息（与 management.info.env.enabled=true 互补，确保最小信息总可见）
        String[] profiles = environment.getActiveProfiles();
        Map<String, Object> envInfo = new HashMap<>();
        envInfo.put("activeProfiles", profiles);
        envInfo.put("primaryProfile", profiles.length > 0 ? profiles[0] : "default");
        builder.withDetail("environment", envInfo);
    }
}
