package fincontrol.com.fincontrol.config;

import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;
import com.sun.management.OperatingSystemMXBean;
import java.lang.management.ManagementFactory;

@Aspect
@Component
public class ResourceUsageAspect {

    private final MeterRegistry registry;
    private final OperatingSystemMXBean osBean =
            (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
    private final Runtime runtime = Runtime.getRuntime();

    public ResourceUsageAspect(MeterRegistry registry) {
        this.registry = registry;
    }

    @Around("execution(* fincontrol.com.fincontrol.service.UserService.register(..))")
    public Object aroundRegister(ProceedingJoinPoint pjp) throws Throwable {
        return record("user.register", pjp);
    }
    @Around("execution(* fincontrol.com.fincontrol.service.UserService.findAll(..))")
    public Object aroundList(ProceedingJoinPoint pjp) throws Throwable {
        return record("user.list", pjp);
    }
    @Around("execution(* fincontrol.com.fincontrol.service.UserService.update(..))")
    public Object aroundUpdate(ProceedingJoinPoint pjp) throws Throwable {
        return record("user.update", pjp);
    }
    @Around("execution(* fincontrol.com.fincontrol.service.UserService.delete(..))")
    public Object aroundDelete(ProceedingJoinPoint pjp) throws Throwable {
        return record("user.delete", pjp);
    }

    private Object record(String name, ProceedingJoinPoint pjp) throws Throwable {
        // Estado inicial
        long cpuBefore = osBean.getProcessCpuTime();
        long memBefore = runtime.totalMemory() - runtime.freeMemory();
        try {
            return pjp.proceed();
        } finally {
            // Estado final
            long cpuAfter  = osBean.getProcessCpuTime();
            long memAfter  = runtime.totalMemory() - runtime.freeMemory();
            double cpuSec  = (cpuAfter  - cpuBefore) / 1e9;
            double memDiff = (memAfter  - memBefore);

            // Registra métricas CPU
            DistributionSummary.builder(name + ".cpu")
                    .baseUnit("seconds")
                    .register(registry)
                    .record(cpuSec);

            // Registra métricas memória
            DistributionSummary.builder(name + ".memory")
                    .baseUnit("bytes")
                    .register(registry)
                    .record(memDiff);
        }
    }
}
