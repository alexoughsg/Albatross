// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.
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
import com.cloud.region.service.UserService;
import com.cloud.user.Account.State;
import com.cloud.user.AccountVO;
import com.cloud.user.UserVO;
import com.cloud.user.dao.AccountDao;
import com.cloud.user.dao.UserDao;

public class UserLocalGeneratorEventTest extends TestCase {
	
	private UserLocalGeneratorEvent userLocalGeneratorEvent;

    @Override
    @Before
    public void setUp() {
    	
    	userLocalGeneratorEvent = new UserLocalGeneratorEvent() {

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
    			
    			long l = 1;
    			short s = 1;
    			AccountVO accountVO = new AccountVO("accountName", l, "networkDomain", s, "xvdsgfasdf231");
    			Mockito.when(accountDao.findById(new Long(1))).thenReturn(accountVO);
    			
    			return accountDao;
    		}
    		
    		public UserService getUserService(RegionVO region) {
    			UserService userService = Mockito.mock(UserService.class);
    			return userService;
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
    		
    		protected UserVO randUserSelect() {
    		   	UserVO userVO = new UserVO();
    	    	userVO.setAccountId(1);
    	    	userVO.setState(State.enabled);
    			return userVO;
    		}

        };
    }
   
    @Test
    public void testUpdate() {
    	
    	UserVO userVO = new UserVO();
    	userVO.setAccountId(1);
    	userVO.setState(State.enabled);
    	userVO.setUuid("242134");
    	
    	UserVO result = userLocalGeneratorEvent.update(userVO);
    	Assert.assertNotNull(result);
    	
		// if the input user is null
    	UserVO result1 = userLocalGeneratorEvent.update(null);
		Assert.assertNotNull(result1);
    	
    }
    
    
    @Test
    public void testLock() {
    	
    	UserVO userVO = new UserVO();
    	userVO.setAccountId(1);
    	userVO.setState(State.enabled);
    	
    	UserVO result = userLocalGeneratorEvent.lock(userVO);
    	Assert.assertNotNull(result);
    	
		// if the input user is null
    	UserVO result1 = userLocalGeneratorEvent.lock(null);
		Assert.assertNotNull(result1);

    }
 
    
    @Test
    public void testDisable() {
    	
    	UserVO userVO = new UserVO();
    	userVO.setAccountId(1);
    	userVO.setState(State.enabled);
    	
    	UserVO result = userLocalGeneratorEvent.disable(userVO);
    	Assert.assertNotNull(result);
    	
		// if the input user is null
    	UserVO result1 = userLocalGeneratorEvent.disable(null);
		Assert.assertNotNull(result1);

    	
    }   
    
    @Test
    public void testEnable() {
    	
    	UserVO userVO = new UserVO();
    	userVO.setAccountId(1);
    	userVO.setState(State.enabled);
    	
    	UserVO result = userLocalGeneratorEvent.enable(userVO);
    	Assert.assertNotNull(result);
    	
		// if the input user is null
    	UserVO result1 = userLocalGeneratorEvent.enable(null);
		Assert.assertNotNull(result1);

    	
    }   
    
    @Test
    public void testRemove() {
    	
    	UserVO userVO = new UserVO();
    	userVO.setAccountId(1);
    	userVO.setState(State.enabled);
    	
    	UserVO result = userLocalGeneratorEvent.remove(userVO);
    	Assert.assertNotNull(result);
    	
		// if the input user is null
    	UserVO result1 = userLocalGeneratorEvent.remove(null);
		Assert.assertNotNull(result1);

    } 

}
