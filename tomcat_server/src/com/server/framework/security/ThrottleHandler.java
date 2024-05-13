package com.server.framework.security;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import jakarta.servlet.http.HttpServletRequest;

import com.server.framework.common.DateUtil;

public class ThrottleHandler
{
	public static final Map<String, ThrottleMeta> ipThrottleMeta = new ConcurrentHashMap<>();
	private static final Map<String, Long> ipLockTimeMap = new ConcurrentHashMap<>();

	public static class ThrottleMeta
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
			time = DateUtil.getCurrentTimeInMillis();
		}
	}

	static boolean handleThrottling(HttpServletRequest request)
	{
		synchronized(ipLockTimeMap)
		{
			String ip = SecurityUtil.getOriginatingUserIP();
			String key = ip + "-" + request.getRequestURI();

			if(Objects.nonNull(ipLockTimeMap.get(key)))
			{
				return false;
			}

			ThrottleMeta throttleMeta = ipThrottleMeta.getOrDefault(key, new ThrottleMeta());
			int count = throttleMeta.incrementCount();
			long time = throttleMeta.getTime();
			long currentTime = DateUtil.getCurrentTimeInMillis();
			long timeFrameStart = currentTime - (1000 * 60 * 5);

			if(time < timeFrameStart)
			{
				ipThrottleMeta.put(key, new ThrottleMeta());
				return true;
			}

			if(count > 100)
			{
				ipLockTimeMap.put(key, DateUtil.getCurrentTimeInMillis() + (1000 * 60 * 5));
				return false;
			}

			ipThrottleMeta.put(key, throttleMeta);

			return true;
		}

	}

	public static void removeExpiredIPLockingAndThrottleMeta()
	{
		Long currentTime = DateUtil.getCurrentTimeInMillis();
		List<String> lockExpiredIP = ipLockTimeMap.entrySet().stream().filter(lockMap -> lockMap.getValue() < currentTime).map(Map.Entry::getKey).collect(Collectors.toList());
		for(String ip : lockExpiredIP)
		{
			ipLockTimeMap.remove(ip);
		}

		Long expiredTimeFrame = currentTime -  (1000 * 60 * 5);
		List<String> throttleExpiredIP = ipThrottleMeta.entrySet().stream().filter(throttleMeta -> throttleMeta.getValue().getTime() < expiredTimeFrame).map(Map.Entry::getKey).collect(Collectors.toList());
		for(String ip : throttleExpiredIP)
		{
			ipThrottleMeta.remove(ip);
		}
	}

}
