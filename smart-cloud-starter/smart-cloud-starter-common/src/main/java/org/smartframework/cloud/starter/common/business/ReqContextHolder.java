package org.smartframework.cloud.starter.common.business;

import java.util.Objects;

import org.smartframework.cloud.common.pojo.dto.BaseDto;
import org.smartframework.cloud.starter.common.business.exception.DataValidateException;
import org.smartframework.cloud.starter.common.business.exception.confg.ParamValidateMessage;
import org.smartframework.cloud.starter.common.business.security.LoginRedisConfig;
import org.smartframework.cloud.starter.common.business.security.util.ReqHttpHeadersUtil;
import org.smartframework.cloud.starter.common.business.util.SpringContextUtil;
import org.smartframework.cloud.starter.redis.component.RedisComponent;

import com.alibaba.fastjson.TypeReference;

import lombok.experimental.UtilityClass;

/**
 * 请求上下文
 * 
 * @author liyulin
 * @date 2019年6月26日 下午5:07:13
 */
@UtilityClass
public class ReqContextHolder extends BaseDto {
	private static final long serialVersionUID = 1L;

	private static ThreadLocal<LoginCache> loginCacheThreadLocal = new ThreadLocal<>();

	/**
	 * 获取当前用户id
	 * 
	 * @return
	 */
	public static Long getUserId() {
		LoginCache loginCache = getLoginCache();
		Long userId = loginCache.getUserId();
		if (Objects.isNull(userId)) {
			throw new DataValidateException(ParamValidateMessage.GET_USERID_FAIL);
		}
		return userId;
	}

	/**
	 * 获取当前用户信息
	 * 
	 * @return 如果不存在，则会抛异常{@link DataValidateException}
	 */
	public static LoginCache getLoginCache() {
		LoginCache loginCache = getAvailableLoginCache();
		if (Objects.isNull(loginCache)) {
			throw new DataValidateException(ParamValidateMessage.LOGIN_CACHE_MISSING);
		}

		return loginCache;
	}

	/**
	 * 是否已登陆
	 * 
	 * @return true，已登陆；false，未登陆
	 */
	public static boolean isLogin() {
		LoginCache loginCache = getAvailableLoginCache();
		if (loginCache == null) {
			return false;
		}
		return loginCache.getUserId() != null;
	}

	private static LoginCache getAvailableLoginCache() {
		LoginCache loginCache = loginCacheThreadLocal.get();
		if (loginCache != null) {
			return loginCache;
		}

		RedisComponent redisWrapper = SpringContextUtil.getBean(RedisComponent.class);
		String token = ReqHttpHeadersUtil.getTokenMustExist();
		String tokenRedisKey = LoginRedisConfig.getTokenRedisKey(token);
		loginCache = redisWrapper.getObject(tokenRedisKey, new TypeReference<LoginCache>() {
		});
		if (!Objects.isNull(loginCache)) {
			loginCacheThreadLocal.set(loginCache);
		}

		return loginCache;
	}

}