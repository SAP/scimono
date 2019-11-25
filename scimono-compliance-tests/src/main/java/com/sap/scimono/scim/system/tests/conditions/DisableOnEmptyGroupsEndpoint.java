/**
 * Copyright (c) 2019 by SAP Labs Bulgaria, url: http://www.sap.com All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP SE, Walldorf. You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered into with SAP.
 *
 * Created on Nov 6, 2019 by I309486
 * 
 */

package com.sap.scimono.scim.system.tests.conditions;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.extension.ExtendWith;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(GroupsEndpointContainingGroupsCondition.class)
public @interface DisableOnEmptyGroupsEndpoint {

}
