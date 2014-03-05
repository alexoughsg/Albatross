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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.cloud.domain.DomainVO;
import com.cloud.domain.dao.DomainDao;
import com.cloud.user.AccountVO;
import com.cloud.user.dao.AccountDao;
import com.cloud.user.dao.UserDao;

public class LocalGeneratorTest extends TestCase {

	private LocalGenerator localGenerator;
	
	@Override
	@Before
	public void setUp() {

		localGenerator = new LocalGenerator() {

			public DomainDao getDomainDao() {
    			DomainDao domainDao = Mockito.mock(DomainDao.class);
    			List<DomainVO> lOfDomains = new ArrayList<DomainVO>();
    			DomainVO domainVO = new DomainVO("name", 1, new Long(1), "networkDomain");
    			domainVO.setUuid("ac8cc522-5e6c-45e9-a7bc-175392a974e2");
    			DomainVO domainVO1 = new DomainVO("name2", 2, new Long(2), "networkDomain1");
    			domainVO1.setUuid("0c9ef45f-6778-41fc-a64d-2f0d6e0874d3");
    			lOfDomains.add(domainVO);
    			lOfDomains.add(domainVO1);
    			Mockito.when(domainDao.listAll()).thenReturn(lOfDomains);
    			return domainDao;
    		}

			public UserDao getUserDao() {
				 UserDao userDao = Mockito.mock(UserDao.class);
				 return userDao;
			}


	   		public AccountDao getAccountDao() { 
	   			AccountDao accountDao = Mockito.mock(AccountDao.class);
	   			List<AccountVO> lOfAccounts = new ArrayList<AccountVO>();
	   			short s = 1;
	   			long l = 1;
	   			Date date = new Date();
	   			AccountVO account1 = new AccountVO("accountName", l, "networkDomain", s, "3jkj34lkjk324", date);
	   			AccountVO account2 = new AccountVO("accountName1", l, "networkDomain1", s, "5645vsdgsgsd", date);
	   			lOfAccounts.add(account1);
	   			lOfAccounts.add(account2);
	   			Mockito.when(accountDao.listAll()).thenReturn(lOfAccounts);
				 return accountDao;
			}
		};
	}
	
	@Test
	public void testRandDomainSelect() {
		
		DomainVO result = localGenerator.randDomainSelect(true);
		Assert.assertNotNull(result);
		
		// input parameter includeRoot is false
		result = localGenerator.randDomainSelect(false);
		Assert.assertNotNull(result);
	}
	
	@Test
	public void testRandAccountSelect() {
		
		AccountVO result = localGenerator.randAccountSelect(true);
		Assert.assertNotNull(result);
		
		// input parameter includeSystem is false
		result = localGenerator.randAccountSelect(false);
		Assert.assertNotNull(result);

	}
	
	@Test
	public void testIsUsable() {
		
		// ACCOUNT_TYPE_PROJECT
		short type = 5;
		AccountVO account = new AccountVO();
		account.setType(type);
		Assert.assertFalse(localGenerator.isUsable(account));
		
		// ACCOUNT_ID_SYSTEM
		long id = 1;
		account = new AccountVO();
		account.setId(id);
		Assert.assertFalse(localGenerator.isUsable(account));
		
		// domain id = ACCOUNT_ID_SYSTEM and account type  = ACCOUNT_TYPE_ADMIN
		long domainId = 1;
		short accountType = 1;
		account = new AccountVO();
		account.setDomainId(domainId);
		account.setType(accountType);
		Assert.assertFalse(localGenerator.isUsable(account));
		
		// ACCOUNT_TYPE_PROJECT != 5 return true
		type = 2;
		account = new AccountVO();
		account.setType(type);
		Assert.assertTrue(localGenerator.isUsable(account));
		
	}
	
	
}
