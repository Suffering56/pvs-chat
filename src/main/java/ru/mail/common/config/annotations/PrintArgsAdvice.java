package ru.mail.common.config.annotations;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashSet;
import java.util.Set;

@Aspect
@Component
public class PrintArgsAdvice {

    @PostConstruct
    private void init() {
        System.out.println("PrintArgsAdvice initialized");
    }

    @Around("@annotation(ru.mail.common.config.annotations.PrintArgs)")
    public Object printArgs(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Parameter[] parameters = method.getParameters();

        Set<Integer> loggableParamsIndexes = new HashSet<>();
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            if (parameter.isAnnotationPresent(PrintableArg.class)) {
                loggableParamsIndexes.add(i);
            }
        }

        PrintArgs profile = method.getAnnotation(PrintArgs.class);

        Object[] args = joinPoint.getArgs();
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            if (loggableParamsIndexes.contains(i)) {
                System.out.println(profile.prefix() + arg);
            }
        }

        return joinPoint.proceed();
    }
}
