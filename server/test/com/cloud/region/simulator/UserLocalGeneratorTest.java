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

import org.apache.cloudstack.region.RegionVO;
import org.apache.cloudstack.region.dao.RegionDao;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.amazonaws.util.json.JSONArray;
import com.amazonaws.util.json.JSONException;
import com.amazonaws.util.json.JSONObject;
import com.cloud.domain.DomainVO;
import com.cloud.domain.dao.DomainDao;
import com.cloud.region.service.LocalUserManager;
import com.cloud.region.service.UserService;
import com.cloud.user.AccountVO;
import com.cloud.user.UserVO;
import com.cloud.user.Account.State;
import com.cloud.user.dao.AccountDao;
import com.cloud.user.dao.UserDao;

import junit.framework.Assert;
import junit.framework.TestCase;

public class UserLocalGeneratorTest extends TestCase {

	
	private UserLocalGenerator userLocalGenerator;

    @Override
    @Before
    public void setUp() {
    	
    	userLocalGenerator = new UserLocalGenerator() {
    		
    		public LocalUserManager getLocalUserManager() {
    			LocalUserManager localUserManager = Mockito.mock(LocalUserManager.class);
    			return localUserManager;
    		}

    		public DomainDao getDomainDao() {
    			DomainDao domainDao = Mockito.mock(DomainDao.class);
    			long l = 1;
    	    	DomainVO domainVO = Mockito.mock(DomainVO.class);
    	    	Mockito.when(domainVO.getPath()).thenReturn("domainPath/");
    	    	Mockito.when(domainDao.findById(l)).thenReturn(domainVO);
    			return domainDao;
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
    	
    	UserVO result = userLocalGenerator.update(userVO);
    	Assert.assertEquals(userVO, result);
    	
    }
    
    
    @Test
    public void testLock() {
    	
    	UserVO userVO = new UserVO();
    	userVO.setAccountId(1);
    	userVO.setState(State.enabled);
    	
    	UserVO result = userLocalGenerator.lock(userVO);
    	Assert.assertEquals(userVO, result);
    	
    }
 
    
    @Test
    public void testDisable() {
    	
    	UserVO userVO = new UserVO();
    	userVO.setAccountId(1);
    	userVO.setState(State.enabled);
    	
    	UserVO result = userLocalGenerator.disable(userVO);
    	Assert.assertEquals(userVO, result);
    	
    }   
    
    @Test
    public void testEnable() {
    	
    	UserVO userVO = new UserVO();
    	userVO.setAccountId(1);
    	userVO.setState(State.enabled);
    	
    	UserVO result = userLocalGenerator.enable(userVO);
    	Assert.assertEquals(userVO, result);
    	
    }   
    
    @Test
    public void testRemove() {
    	
    	UserVO userVO = new UserVO();
    	userVO.setAccountId(1);
    	userVO.setState(State.enabled);
    	
    	UserVO result = userLocalGenerator.remove(userVO);
    	Assert.assertEquals(userVO, result);
    	
    } 
	
}
