package hr.algebra.nrako.instapound.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class LoggingAspect {

    @Before("within(hr.algebra.nrako.instapound.service..*)")
    public void beforeService(JoinPoint jp) {
        log.debug("Entering {}.{}", jp.getSignature().getDeclaringTypeName(), jp.getSignature().getName());
    }

    @AfterReturning(pointcut = "within(hr.algebra.nrako.instapound.service..*)", returning = "ret")
    public void afterService(JoinPoint jp, Object ret) {
        log.debug("Exiting {}.{} with {}", jp.getSignature().getDeclaringTypeName(), jp.getSignature().getName(), ret);
    }

    @AfterThrowing(pointcut = "within(hr.algebra.nrako.instapound.service..*)", throwing = "ex")
    public void onThrowService(JoinPoint jp, Throwable ex) {
        log.warn("Exception in {}.{}: {}", jp.getSignature().getDeclaringTypeName(), jp.getSignature().getName(), ex.toString());
    }
}

