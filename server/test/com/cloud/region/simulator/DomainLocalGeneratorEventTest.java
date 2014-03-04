/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.cloud.region.simulator;

import org.apache.cloudstack.region.RegionVO;
import org.apache.cloudstack.region.dao.RegionDao;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.cloud.domain.DomainVO;
import com.cloud.domain.Domain.State;
import com.cloud.domain.dao.DomainDao;
import com.cloud.region.service.DomainService;
import com.cloud.region.service.LocalDomainManager;
import com.cloud.user.dao.AccountDao;
import com.cloud.user.dao.UserDao;

import junit.framework.Assert;
import junit.framework.TestCase;


public class DomainLocalGeneratorEventTest extends TestCase {

	private DomainLocalGeneratorEvent domainLocalGeneratorEvent;
	
	@Override
	@Before
	public void setUp() {

		domainLocalGeneratorEvent = new DomainLocalGeneratorEvent() {

			public UserDao getUserDao() {
				UserDao userDao = Mockito.mock(UserDao.class);
				return userDao;
			}

			public AccountDao getAccountDao() {
				AccountDao accountDao = Mockito.mock(AccountDao.class);
				return accountDao;
			}
			
			public DomainDao getDomainDao() {
    			DomainDao domainDao = Mockito.mock(DomainDao.class);
    			long l = 1;
    	    	DomainVO domainVO = Mockito.mock(DomainVO.class);
    	    	Mockito.when(domainVO.getPath()).thenReturn("domainPath/");
    	    	Mockito.when(domainDao.findById(l)).thenReturn(domainVO);
    			return domainDao;
    		}
			
			public RegionDao getRegionDao() {
				RegionDao regionDao = Mockito.mock(RegionDao.class);
				
				RegionVO localRegion = new RegionVO();
	            localRegion.setActive(true);
	            localRegion.setName("Local");
	            localRegion.setPassword("password");
	            localRegion.setEndPoint("endPoint");
	            localRegion.setUserName("userName");
				
				Mockito.when(regionDao.findByName("Local")).thenReturn(localRegion);
				return regionDao;
			}
		 
			public DomainService getDomainService(RegionVO region) {
				DomainService domainService = Mockito.mock(DomainService.class);
				return domainService;
			}
			 
	   		protected DomainVO randDomainSelect(boolean includeRoot) {
	   			DomainVO domainVO = new DomainVO();
	   			domainVO.setPath("/");
	   			domainVO.setName("ROOT");
	   			return domainVO;
	   		}
	 

		};
	}
	
	@Test
	public void testUpdate() {

		// ROOT domain
		DomainVO domainVO = new DomainVO();
		domainVO.setPath("/");
		domainVO.setName("ROOT");

		DomainVO result = domainLocalGeneratorEvent.update(domainVO);
		Assert.assertEquals(domainVO, result);

		// Domain is null
		DomainVO result1 = domainLocalGeneratorEvent.update(null);
		Assert.assertNotNull(result1);

		// Domain is not null and not a ROOT

		DomainVO domainVO1 = new DomainVO();
		domainVO1.setPath("domainPath/");
		domainVO1.setName("ABC");
		domainVO1.setState(State.Active);

		DomainVO result2 = domainLocalGeneratorEvent.update(domainVO1);
		Assert.assertNotNull(result2);

	}
	
	@Test
	public void testRemove() {
		
		// ROOT domain
		DomainVO domainVO = new DomainVO();
		domainVO.setPath("/");
		domainVO.setName("ROOT");
		
		DomainVO result = domainLocalGeneratorEvent.remove(domainVO);
		Assert.assertEquals(domainVO, result);
		
		// Domain is null
		DomainVO result1 = domainLocalGeneratorEvent.remove(null);
		Assert.assertNotNull(result1);


		// Domain is not null and not a ROOT
		DomainVO domainVO1 = new DomainVO();
		domainVO1.setPath("domainPath/");
		domainVO1.setName("ABC");
		domainVO1.setState(State.Active);

		DomainVO result2 = domainLocalGeneratorEvent.remove(domainVO1);
		Assert.assertNotNull(result2);

	}
	
	
	
}
