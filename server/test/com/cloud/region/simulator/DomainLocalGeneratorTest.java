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

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.cloud.domain.DomainVO;
import com.cloud.domain.dao.DomainDao;
import com.cloud.region.service.LocalDomainManager;
import com.cloud.user.dao.AccountDao;
import com.cloud.user.dao.UserDao;

public class DomainLocalGeneratorTest extends TestCase {

	private DomainLocalGenerator domainLocalGenerator;
	
	
	@Override
	@Before
	public void setUp() {

		domainLocalGenerator = new DomainLocalGenerator() {

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
			 return accountDao;
		}
   		
   		
   		protected DomainVO randDomainSelect(boolean includeRoot) {
   			DomainVO domainVO = Mockito.mock(DomainVO.class);
   			Mockito.when(domainVO.getPath()).thenReturn("/");
   			Mockito.when(domainVO.getName()).thenReturn("ROOT");
   			return domainVO;
   		}
   		
   		public LocalDomainManager getLocalDomainManager() { 
   			LocalDomainManager localDomainManager = Mockito.mock(LocalDomainManager.class);
			 return localDomainManager;
		 }
		 

		};
	}
	
	@Test
	public void testUpdate() {
		
		// ROOT domain
		DomainVO domainVO = new DomainVO();
		domainVO.setPath("/");
		domainVO.setName("ROOT");
		
		DomainVO result = domainLocalGenerator.update(domainVO);
		Assert.assertEquals(domainVO, result);
		
		// Domain is null
		DomainVO result1 = domainLocalGenerator.update(null);
		Assert.assertNotNull(result1);


		// Domain is not null and not a ROOT
		DomainVO domainVO1 = Mockito.mock(DomainVO.class);
		Mockito.when(domainVO1.getPath()).thenReturn("domainPath/");
		Mockito.when(domainVO1.getName()).thenReturn("ABC");

		DomainVO result2 = domainLocalGenerator.update(domainVO1);
		Assert.assertNotNull(result2);

	}
	
	
	@Test
	public void testRemove() {
		
		// ROOT domain
		DomainVO domainVO = new DomainVO();
		domainVO.setPath("/");
		domainVO.setName("ROOT");
		
		DomainVO result = domainLocalGenerator.remove(domainVO);
		Assert.assertEquals(domainVO, result);
		
		// Domain is null
		DomainVO result1 = domainLocalGenerator.remove(null);
		Assert.assertNotNull(result1);


		// Domain is not null and not a ROOT
		DomainVO domainVO1 = Mockito.mock(DomainVO.class);
		Mockito.when(domainVO1.getPath()).thenReturn("domainPath/");
		Mockito.when(domainVO1.getName()).thenReturn("ABC");

		DomainVO result2 = domainLocalGenerator.remove(domainVO1);
		Assert.assertNotNull(result2);

	}
	
	
}
