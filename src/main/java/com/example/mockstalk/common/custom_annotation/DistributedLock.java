package com.example.mockstalk.common.custom_annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DistributedLock {

	String key();              // 락 키

	long waitTime() default 5; // 락 대기 시간 (초)

	long leaseTime() default 10; // 락 점유 시간 (초)
}
