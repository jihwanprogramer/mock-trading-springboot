package com.example.mockstalk.common.config;

public class AccountContextHolder {

	private static final ThreadLocal<Long> accountIdHolder = new ThreadLocal<>();

	public static void set(Long accountId) {
		accountIdHolder.set(accountId);
	}

	public static Long getAccountId() {
		return accountIdHolder.get();
	}

	public static void clear() {
		accountIdHolder.remove();
	}
}