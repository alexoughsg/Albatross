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

import junit.framework.Assert;
import junit.framework.TestCase;

import org.apache.cloudstack.region.RegionVO;
import org.apache.cloudstack.region.dao.RegionDao;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.cloud.domain.DomainVO;
import com.cloud.domain.dao.DomainDao;
import com.cloud.user.Account.State;
import com.cloud.user.AccountVO;
import com.cloud.user.dao.AccountDao;
import com.cloud.user.dao.UserDao;

public class AccountLocalGeneratorEventTest extends TestCase {

	private AccountLocalGeneratorEvent accountLocalGeneratorEvent;

	@Override
	@Before
	public void setUp() {

		accountLocalGeneratorEvent = new AccountLocalGeneratorEvent() {

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
                localRegion.setPassword("fsdfdsdf");
                localRegion.setEndPoint("endPoint");
                localRegion.setUserName("userName");
    			
    			Mockito.when(regionDao.findByName("Local")).thenReturn(localRegion);
    			return regionDao;
    		}
    		
   		 public UserDao getUserDao() {
			 UserDao userDao = Mockito.mock(UserDao.class);
			 return userDao;
		}


   		public AccountDao getAccountDao() { 
   			AccountDao accountDao = Mockito.mock(AccountDao.class);
			 return accountDao;
		}
   		
   		
   		protected DomainVO randDomainSelect(boolean includeRoot) {
   			DomainVO domainVO = Mockito.mock(DomainVO.class);
   			Mockito.when(domainVO.getPath()).thenReturn("domainPath/");
   			return domainVO;
   		}
   		
		protected AccountVO randAccountSelect(boolean includeSystem) {
			long l = 1;
			short s = 1;
			AccountVO accountVO = new AccountVO("accountName", l, "networkDomain", s, "xvdsgfasdf231");
			return accountVO;
		}

		protected boolean isUsable(AccountVO account) {
			return true;
		}


		};
	}

	
	@Test
	public void testUpdate() throws Exception {
		
		AccountVO accountVO = new AccountVO();
		accountVO.setDomainId(1);
		accountVO.setState(State.enabled);
		accountVO.setUuid("242134");
    	
		AccountVO result = accountLocalGeneratorEvent.update(accountVO);
		
		Assert.assertNotNull(result);
		
		// if the input account is null
		AccountVO result1 = accountLocalGeneratorEvent.update(null);
		Assert.assertNotNull(result1);

		
	}

    @Test
    public void testLock() {
    	
		AccountVO accountVO = new AccountVO();
		accountVO.setDomainId(1);
		accountVO.setState(State.enabled);
    	
		AccountVO result = accountLocalGeneratorEvent.lock(accountVO);
		Assert.assertNotNull(result);

		// if the input account is null
		AccountVO result1 = accountLocalGeneratorEvent.lock(null);
		Assert.assertNotNull(result1);
		
    }
 
    
    @Test
    public void testDisable() {
    	
		AccountVO accountVO = new AccountVO();
		accountVO.setDomainId(1);
		accountVO.setState(State.enabled);
    	
		AccountVO result = accountLocalGeneratorEvent.disable(accountVO);
		Assert.assertNotNull(result);

		// if the input account is null
		AccountVO result1 = accountLocalGeneratorEvent.disable(null);
		Assert.assertNotNull(result1);
    	
    }   
    
    @Test
    public void testEnable() {
    	
		AccountVO accountVO = new AccountVO();
		accountVO.setDomainId(1);
		accountVO.setState(State.enabled);
    	
		AccountVO result = accountLocalGeneratorEvent.enable(accountVO);
		Assert.assertNotNull(result);
		
		// if the input account is null
		AccountVO result1 = accountLocalGeneratorEvent.enable(null);
		Assert.assertNotNull(result1);
    	
    }   
    
    @Test
    public void testRemove() {
    	
		AccountVO accountVO = new AccountVO();
		accountVO.setDomainId(1);
		accountVO.setState(State.enabled);
    	
		AccountVO result = accountLocalGeneratorEvent.remove(accountVO);
		Assert.assertNotNull(result);
		
		// if the input account is null
		AccountVO result1 = accountLocalGeneratorEvent.remove(null);
		Assert.assertNotNull(result1);
    	
    } 

	
	
}
