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

package com.liferay.portlet.messageboards.service.permission;

import com.liferay.portal.kernel.test.ExecutionTestListeners;
import com.liferay.portal.security.permission.ActionKeys;
import com.liferay.portal.service.permission.BasePermissionTestCase;
import com.liferay.portal.test.LiferayIntegrationJUnitTestRunner;
import com.liferay.portal.test.MainServletExecutionTestListener;
import com.liferay.portlet.messageboards.model.MBCategory;
import com.liferay.portlet.messageboards.model.MBMessage;
import com.liferay.portlet.messageboards.util.MBTestUtil;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Eric Chin
 * @author Shinn Lok
 */
@ExecutionTestListeners(listeners = {MainServletExecutionTestListener.class})
@RunWith(LiferayIntegrationJUnitTestRunner.class)
public class MBMessagePermissionTest extends BasePermissionTestCase {

	@Test
	public void testContains() throws Exception {
		Assert.assertTrue(
			MBMessagePermission.contains(
				permissionChecker, _message, ActionKeys.VIEW));
		Assert.assertTrue(
			MBMessagePermission.contains(
				permissionChecker, _submessage, ActionKeys.VIEW));

		removePortletModelViewPermission();

		Assert.assertFalse(
			MBMessagePermission.contains(
				permissionChecker, _message, ActionKeys.VIEW));
		Assert.assertFalse(
			MBMessagePermission.contains(
				permissionChecker, _submessage, ActionKeys.VIEW));
	}

	@Override
	protected void doSetUp() throws Exception {
		_message = MBTestUtil.addMessage(group.getGroupId());

		MBCategory category = MBTestUtil.addCategory(group.getGroupId());

		_submessage = MBTestUtil.addMessage(
			group.getGroupId(), category.getCategoryId());
	}

	@Override
	protected String getResourceName() {
		return MBPermission.RESOURCE_NAME;
	}

	private MBMessage _message;
	private MBMessage _submessage;

}