/**
 * Copyright (c) 2000-2012 Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.portal.kernel.messaging;

import com.liferay.portal.kernel.cache.Lifecycle;
import com.liferay.portal.kernel.cache.ThreadLocalCacheManager;
import com.liferay.portal.kernel.cluster.ClusterLinkUtil;
import com.liferay.portal.kernel.concurrent.ThreadPoolExecutor;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.messaging.proxy.MessageValuesThreadLocal;
import com.liferay.portal.kernel.util.CentralizedThreadLocal;
import com.liferay.portal.kernel.util.TransientValue;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.model.User;
import com.liferay.portal.security.auth.CompanyThreadLocal;
import com.liferay.portal.security.auth.PrincipalThreadLocal;
import com.liferay.portal.security.permission.PermissionChecker;
import com.liferay.portal.security.permission.PermissionCheckerFactoryUtil;
import com.liferay.portal.security.permission.PermissionThreadLocal;
import com.liferay.portal.service.UserLocalServiceUtil;

import java.util.Set;

/**
 * <p>
 * Destination that delivers a message to a list of message listeners one at a
 * time.
 * </p>
 *
 * @author Michael C. Han
 */
public class SerialDestination extends BaseAsyncDestination {

	public SerialDestination() {
		super();

		setWorkersCoreSize(_WORKERS_CORE_SIZE);
		setWorkersMaxSize(_WORKERS_MAX_SIZE);
	}

	/**
	 * @deprecated
	 */
	public SerialDestination(String name) {
		super(name, _WORKERS_CORE_SIZE, _WORKERS_MAX_SIZE);
	}

	@Override
	protected void dispatch(
		final Set<MessageListener> messageListeners, final Message message) {

		if (!message.contains("companyId")) {
			message.put("companyId", CompanyThreadLocal.getCompanyId());
		}

		if (!message.contains("principalName")) {
			message.put("principalName", PrincipalThreadLocal.getName());
		}

		if (!message.contains("principalPassword")) {
			message.put(
				"principalPassword", PrincipalThreadLocal.getPassword());
		}

		if (!message.contains("permissionChecker")) {
			PermissionChecker permissionChecker =
				PermissionThreadLocal.getPermissionChecker();

			message.put(
				"permissionChecker",
				new TransientValue<PermissionChecker>(permissionChecker));
		}

		ThreadPoolExecutor threadPoolExecutor = getThreadPoolExecutor();

		Runnable runnable = new MessageRunnable(message) {

			public void run() {
				try {
					long messageCompanyId = message.getLong("companyId");

					if (messageCompanyId > 0) {
						CompanyThreadLocal.setCompanyId(messageCompanyId);
					}

					String messagePrincipalName = message.getString(
						"principalName");

					boolean hasPrincipalName = false;

					if (Validator.isNotNull(messagePrincipalName)) {
						hasPrincipalName = true;
						PrincipalThreadLocal.setName(messagePrincipalName);
					}

					String messagePrincipalPassword = message.getString(
						"principalPassword");

					if (Validator.isNotNull(messagePrincipalPassword)) {
						PrincipalThreadLocal.setPassword(
							messagePrincipalPassword);
					}

					TransientValue<PermissionChecker> permissionCheckerValue =
						(TransientValue<PermissionChecker>)message.get(
						"permissionChecker");

					PermissionChecker permissionChecker =
						permissionCheckerValue.getValue();

					if ((permissionChecker == null) && hasPrincipalName) {
						long userId = PrincipalThreadLocal.getUserId();

						try {
							User user = UserLocalServiceUtil.fetchUser(userId);

							permissionChecker =
								PermissionCheckerFactoryUtil.create(user);
						}
						catch (Exception e) {
							throw new RuntimeException(e);
						}
					}

					if (permissionChecker != null) {
						PermissionThreadLocal.setPermissionChecker(
							permissionChecker);
					}

					Boolean clusterForwardMessage = (Boolean)message.get(
						ClusterLinkUtil.CLUSTER_FORWARD_MESSAGE);

					if (clusterForwardMessage != null) {
						MessageValuesThreadLocal.setValue(
							ClusterLinkUtil.CLUSTER_FORWARD_MESSAGE,
							clusterForwardMessage);
					}

					for (MessageListener messageListener : messageListeners) {
						try {
							messageListener.receive(message);
						}
						catch (MessageListenerException mle) {
							_log.error(
								"Unable to process message " + message, mle);
						}
					}
				}
				finally {
					ThreadLocalCacheManager.clearAll(Lifecycle.REQUEST);

					CentralizedThreadLocal.clearShortLivedThreadLocals();
				}
			}

		};

		threadPoolExecutor.execute(runnable);
	}

	private static final int _WORKERS_CORE_SIZE = 1;

	private static final int _WORKERS_MAX_SIZE = 1;

	private static Log _log = LogFactoryUtil.getLog(SerialDestination.class);

}