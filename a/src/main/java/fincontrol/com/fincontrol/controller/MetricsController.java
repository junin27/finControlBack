package fincontrol.com.fincontrol.controller;

import io.micrometer.core.instrument.DistributionSummary;
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

    // Métricas de tempo (Timers)

    @GetMapping("/user-list")
    public Map<String, Object> userListMetrics() {
        return timerMetrics("user.list.time");
    }

    @GetMapping("/user-register")
    public Map<String, Object> userRegisterMetrics() {
        return timerMetrics("user.register.time");
    }

    @GetMapping("/user-update")
    public Map<String, Object> userUpdateMetrics() {
        return timerMetrics("user.update.time");
    }

    @GetMapping("/user-delete")
    public Map<String, Object> userDeleteMetrics() {
        return timerMetrics("user.delete.time");
    }

    private Map<String, Object> timerMetrics(String metricName) {
        Timer t = registry.find(metricName).timer();
        double count = 0, totalTime = 0, max = 0, mean = 0;
        if (t != null) {
            count     = t.count();
            totalTime = t.totalTime(TimeUnit.SECONDS);
            max       = t.max(TimeUnit.SECONDS);
            mean      = count > 0 ? totalTime / count : 0;
        }
        return Map.of(
                "mean",      mean,
                "name",      metricName,
                "max",       max,
                "count",     count,
                "totalTime", totalTime
        );
    }

    // Métricas de uso de recursos (DistributionSummary)

    @GetMapping("/user-list-resources")
    public Map<String, Object> userListResources() {
        return resourcesMetrics("user.list");
    }

    @GetMapping("/user-register-resources")
    public Map<String, Object> userRegisterResources() {
        return resourcesMetrics("user.register");
    }

    @GetMapping("/user-update-resources")
    public Map<String, Object> userUpdateResources() {
        return resourcesMetrics("user.update");
    }

    @GetMapping("/user-delete-resources")
    public Map<String, Object> userDeleteResources() {
        return resourcesMetrics("user.delete");
    }

    private Map<String, Object> resourcesMetrics(String baseName) {
        DistributionSummary cpu       = registry.find(baseName + ".cpu").summary();
        DistributionSummary memory    = registry.find(baseName + ".memory").summary();
        double count      = 0;
        double totalCpu   = 0, maxCpu = 0, meanCpu = 0;
        double totalMem   = 0, maxMem = 0, meanMem = 0;
        if (cpu != null && memory != null) {
            count      = cpu.count();
            totalCpu   = cpu.totalAmount();
            maxCpu     = cpu.max();
            meanCpu    = count > 0 ? totalCpu / count : 0;
            totalMem   = memory.totalAmount();
            maxMem     = memory.max();
            meanMem    = count > 0 ? totalMem / count : 0;
        }
        return Map.of(
                "operation", baseName,
                "count",     count,
                "cpu.total", totalCpu,
                "cpu.max",   maxCpu,
                "cpu.mean",  meanCpu,
                "mem.total", totalMem,
                "mem.max",   maxMem,
                "mem.mean",  meanMem
        );
    }
}
