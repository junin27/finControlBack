// src/main/java/fincontrol/com/fincontrol/controller/MetricsController.java
package fincontrol.com.fincontrol.controller;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/metrics/custom")
public class MetricsController {

    private final MeterRegistry registry;

    public MetricsController(MeterRegistry registry) {
        this.registry = registry;
    }

    @GetMapping("/user-list")
    public Map<String,Object> userListMetrics() {
        Timer t = registry.find("user.list.time").timer();
        double count     = t.count();
        double totalTime = t.totalTime(TimeUnit.SECONDS);
        double max       = t.max(TimeUnit.SECONDS);
        double mean      = count > 0 ? totalTime / count : 0;
        return Map.of(
                "mean",      mean,
                "name",      "user.list.time",
                "max",       max,
                "count",     count,
                "totalTime", totalTime
        );
    }

    @GetMapping("/user-register")
    public Map<String,Object> userRegisterMetrics() {
        Timer t = registry.find("user.register.time").timer();
        double count     = t.count();
        double totalTime = t.totalTime(TimeUnit.SECONDS);
        double max       = t.max(TimeUnit.SECONDS);
        double mean      = count > 0 ? totalTime / count : 0;
        return Map.of(
                "mean",      mean,
                "name",      "user.register.time",
                "max",       max,
                "count",     count,
                "totalTime", totalTime
        );
    }

    @GetMapping("/user-update")
    public Map<String,Object> userUpdateMetrics() {
        Timer t = registry.find("user.update.time").timer();
        double count     = t.count();
        double totalTime = t.totalTime(TimeUnit.SECONDS);
        double max       = t.max(TimeUnit.SECONDS);
        double mean      = count > 0 ? totalTime / count : 0;
        return Map.of(
                "mean",      mean,
                "name",      "user.update.time",
                "max",       max,
                "count",     count,
                "totalTime", totalTime
        );
    }

    @GetMapping("/user-delete")
    public Map<String,Object> userDeleteMetrics() {
        Timer t = registry.find("user.delete.time").timer();
        double count     = t.count();
        double totalTime = t.totalTime(TimeUnit.SECONDS);
        double max       = t.max(TimeUnit.SECONDS);
        double mean      = count > 0 ? totalTime / count : 0;
        return Map.of(
                "mean",      mean,
                "name",      "user.delete.time",
                "max",       max,
                "count",     count,
                "totalTime", totalTime
        );
    }
}
