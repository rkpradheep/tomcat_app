package com.server.security;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;

import com.server.common.Util;

public class ThrottleHandler
{
	private static final Map<String, ThrottleMeta> ipThrottleMeta = new ConcurrentHashMap<>();
	private static final Map<String, Long> ipLockTimeMap = new ConcurrentHashMap<>();

	static class ThrottleMeta
	{
		int count;
		Long time;

		public int getCount()
		{
			return count;
		}

		public Long getTime()
		{
			return time;
		}

		public int incrementCount()
		{
			return ++this.count;
		}

		public ThrottleMeta()
		{
			count = 0;
			time = System.currentTimeMillis();
		}
	}

	static boolean handleThrottling(HttpServletRequest request)
	{
		String ip = Util.getUserIP(request);
		if(Objects.nonNull(ipLockTimeMap.get(ip)))
		{
			return false;
		}

		ThrottleMeta throttleMeta = ipThrottleMeta.getOrDefault(ip, new ThrottleMeta());
		int count = throttleMeta.incrementCount();
		long time = throttleMeta.getTime();
		long currentTime = System.currentTimeMillis();
		long timeFrameStart = currentTime - (1000 * 60 * 5);

		if(time < timeFrameStart)
		{
			ipThrottleMeta.put(ip, new ThrottleMeta());
			return true;
		}

		if(count > 100)
		{
			ipLockTimeMap.put(ip, System.currentTimeMillis() + (1000 * 60 * 5));
			return false;
		}

		ipThrottleMeta.put(ip, throttleMeta);

		return true;

	}

	public static void removeExpiredIPLocking()
	{
		Long currentTime = System.currentTimeMillis();
		List<String> lockExpiredIP = ipLockTimeMap.entrySet().stream().filter(lockMap -> lockMap.getValue() < currentTime).map(Map.Entry::getKey).collect(Collectors.toList());
		for(String ip : lockExpiredIP)
		{
			ipLockTimeMap.remove(ip);
		}
	}

}
