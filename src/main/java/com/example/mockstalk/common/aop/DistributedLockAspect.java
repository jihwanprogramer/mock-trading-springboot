package com.example.mockstalk.common.aop;

import java.util.concurrent.TimeUnit;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Aspect
@Component
@RequiredArgsConstructor
public class DistributedLockAspect {

	private final RedissonClient redissonClient;

	@Around("@annotation(distributedLock)")
	public Object lock(ProceedingJoinPoint joinPoint, DistributedLock distributedLock) throws Throwable {
		String key = distributedLock.key();
		long waitTime = distributedLock.waitTime();
		long leaseTime = distributedLock.leaseTime();

		RLock lock = redissonClient.getLock(key);

		boolean isLocked = false;
		try {
			isLocked = lock.tryLock(waitTime, leaseTime, TimeUnit.SECONDS);
			if (!isLocked) {
				throw new IllegalStateException("락 획득 실패: key = " + key);
			}
			return joinPoint.proceed(); // 비즈니스 로직 실행
		} finally {
			if (isLocked && lock.isHeldByCurrentThread()) {
				lock.unlock();
			}
		}
	}

}
