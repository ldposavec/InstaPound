package hr.algebra.nrako.instapound.aspect;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class ControllerMetricsAspect {

    private final MeterRegistry meterRegistry;

    @Around("within(@org.springframework.web.bind.annotation.RestController *)")
    public Object aroundController(ProceedingJoinPoint pjp) throws Throwable {
        MethodSignature sig = (MethodSignature) pjp.getSignature();
        String className = sig.getDeclaringType().getSimpleName();
        String methodName = sig.getName();

        Tags tags = Tags.of("controller", className, "method", methodName);
        meterRegistry.counter("controller.calls.total", tags).increment();

        Timer.Sample sample = Timer.start(meterRegistry);
        try {
            Object result = pjp.proceed();
            return result;
        } catch (Throwable t) {
            meterRegistry.counter("controller.calls.exceptions", tags).increment();
            throw t;
        } finally {
            sample.stop(Timer.builder("controller.calls.latency")
                    .tags(tags)
                    .publishPercentiles(0.5, 0.95)
                    .register(meterRegistry));
        }
    }
}

