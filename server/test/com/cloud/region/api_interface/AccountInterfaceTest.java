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
package com.cloud.region.api_interface;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;

import com.amazonaws.util.json.JSONArray;
import com.amazonaws.util.json.JSONObject;

public class AccountInterfaceTest extends TestCase {

	private AccountInterface accountInterface;

	@Override
	@Before
	public void setUp() {

		accountInterface = new AccountInterface("URL") {

			public String getSessionKey() {
				return "XxjzeJWHV3S%2Brwq2m2EsYTSIYNE%3D";
			}

			public JSONObject sendApacheGet(String paramStr) throws Exception {

				JSONObject jsonObj = new JSONObject();

				JSONArray list = new JSONArray();

				JSONObject jsonObj1 = new JSONObject();
				jsonObj1.put("domainid", "1");

				list.put(jsonObj1);

				jsonObj.put("account", list);
				
				// uncomment to return empty JSONOject
    			/*jsonObj = new JSONObject();
    			return jsonObj;*/

				return jsonObj;
			}

			public JSONObject sendApachePost(String paramStr) throws Exception {

				JSONObject obj = new JSONObject();
				obj.put("success", true);

				return obj;
			}

		};
	}

	// Test scenario : ListAccounts() : Success
	@Test
	public void testListAccounts() throws Exception {

		JSONArray expected = new JSONArray();

		JSONObject jsonObj1 = new JSONObject();
		jsonObj1.put("domainid", "1");

		expected.put(jsonObj1);

		JSONArray result = accountInterface.listAccounts("1");

		Assert.assertEquals(expected.toString(), result.toString());
		
		result = accountInterface.listAccounts(null);

		Assert.assertEquals(expected.toString(), result.toString());
		
		// To run this test uncomment the return statement for sendApacheGet() in setup(), it should return an empty JSONObject
		// Test scenario : No accounts

		/*JSONArray expected = new JSONArray();
		JSONArray result = accountInterface.listAccounts(null);
		Assert.assertEquals(expected.toString(), result.toString());*/

	}


	// Test scenario : findAccount() : With Accounts
	@Test
	public void testfindAccount() throws Exception {

		JSONObject expected = new JSONObject();
		expected.put("domainid", "1");

		JSONObject result = accountInterface.findAccount("1", "account1");

		Assert.assertEquals(expected.toString(), result.toString());
		
		// To run this test uncomment the return statement for sendApacheGet() in setup(), it should return an empty JSONObject
		// Test scenario : No accounts
		
		/* JSONObject result = accountInterface.findAccount("1", "account1");
		Assert.assertNull(result); */		
	}


	// Test scenario : createAccount() : create account success
	@Test
	public void testCreateAccount() throws Exception {

		JSONObject expected = new JSONObject();
		expected.put("success", true);

		JSONObject result = accountInterface.createAccount("cust1", "QWERTY",
				"cust1@abc.com", "cust1", "one", "accType", "1", "accName",
				"Details", "NWDomain", "IST");

		Assert.assertEquals(expected.toString(), result.toString());

	}

	// Test scenario : updateAccount() : update account success
	@Test
	public void testUpdateAccount() throws Exception {

		JSONObject expected = new JSONObject();

		JSONArray list = new JSONArray();

		JSONObject jsonObj1 = new JSONObject();
		jsonObj1.put("domainid", "1");

		list.put(jsonObj1);

		expected.put("account", list);

		JSONObject result = accountInterface.updateAccount("123", "cust1",
				"cust2", "Details", "1", "NWDomain");

		Assert.assertEquals(expected.toString(), result.toString());

	}

}