/**
 * Copyright (c) 2000-2013 Liferay, Inc. All rights reserved.
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

package com.liferay.portal.security.membershippolicy;

import com.liferay.portal.kernel.test.ExecutionTestListeners;
import com.liferay.portal.kernel.transaction.Transactional;
import com.liferay.portal.test.EnvironmentExecutionTestListener;
import com.liferay.portal.test.LiferayIntegrationJUnitTestRunner;
import com.liferay.portal.test.TransactionalExecutionTestListener;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Roberto Díaz
 */
@ExecutionTestListeners(
	listeners = {
		EnvironmentExecutionTestListener.class,
		TransactionalExecutionTestListener.class
	})
@RunWith(LiferayIntegrationJUnitTestRunner.class)
@Transactional
public class UserGroupMembershipPolicyBasicTest
	extends BaseUserGroupMembershipPolicyTestCase {

	@Test
	public void testIsMembershipAllowed() throws Exception {
		long[] userIds = addUsers();
		long[] standardUserGroupIds = addStandardUserGroups();

		Assert.assertTrue(
			UserGroupMembershipPolicyUtil.isMembershipAllowed(
				userIds[0], standardUserGroupIds[0]));
	}

	@Test
	public void testIsMembershipNotAllowed() throws Exception {
		long[] userIds = addUsers();
		long[] forbiddenUserGroupIds = addForbiddenUserGroups();

		Assert.assertFalse(
			UserGroupMembershipPolicyUtil.isMembershipAllowed(
				userIds[0], forbiddenUserGroupIds[0]));
	}

	@Test
	public void testIsMembershipNotRequired() throws Exception {
		long[] userIds = addUsers();
		long[] standardUserGroupIds = addStandardUserGroups();

		Assert.assertFalse(
			UserGroupMembershipPolicyUtil.isMembershipRequired(
				userIds[0], standardUserGroupIds[0]));
	}

	@Test
	public void testIsMembershipRequired() throws Exception {
		long[] userIds = addUsers();
		long[] requiredUserGroupIds = addRequiredUserGroups();

		Assert.assertTrue(
			UserGroupMembershipPolicyUtil.isMembershipRequired(
				userIds[0], requiredUserGroupIds[0]));
	}

}