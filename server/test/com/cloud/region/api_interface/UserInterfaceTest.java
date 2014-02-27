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

import org.junit.Before;
import org.junit.Test;

import com.amazonaws.util.json.JSONArray;
import com.amazonaws.util.json.JSONObject;

import junit.framework.Assert;
import junit.framework.TestCase;

public class UserInterfaceTest extends TestCase {
	
	private UserInterface userInterface;
	
	@Override
    @Before
    public void setUp() {

		userInterface = new UserInterface("URL") {

    		public String getSessionKey() {
    			return "XxjzeJWHV3S%2Brwq2m2EsYTSIYNE%3D";
    		}
    		
    		public JSONObject sendApacheGet(String paramStr) throws Exception {
    			
    			JSONObject jsonObj = new JSONObject();
    			
    			JSONArray list = new JSONArray();
    			
    			JSONObject jsonObj1 = new JSONObject();
    			jsonObj1.put("domainid", "2");
    			jsonObj1.put("domain", "ROOT");
    			jsonObj1.put("account", "system");
    			
    			JSONObject jsonObj2 = new JSONObject();
    			jsonObj2.put("domainid", "1");
    			jsonObj2.put("domain", "xyz");
    			jsonObj2.put("account", "abc");
    			
    			list.put(jsonObj1);
    			list.put(jsonObj2);
    			
    			jsonObj.put("user", list);
    			
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
	
	
    // Test scenario : listUsers()
    @Test
    public void testListUsers() throws Exception {
    	
    	JSONArray expected = new JSONArray();
			
		JSONObject jsonObj1 = new JSONObject();
		jsonObj1.put("domainid", "1");
		jsonObj1.put("domain", "xyz");
		jsonObj1.put("account", "abc");

		expected.put(jsonObj1);

		JSONArray list = new JSONArray();
		
		JSONObject jsonObj3 = new JSONObject();
		jsonObj3.put("domainid", "2");
		jsonObj3.put("domain", "ROOT");
		jsonObj3.put("account", "system");
		
		JSONObject jsonObj4 = new JSONObject();
		jsonObj4.put("domainid", "1");
		jsonObj4.put("domain", "xyz");
		jsonObj4.put("account", "abc");
		
		list.put(jsonObj3);
		list.put(jsonObj4);
		
    	JSONArray result = userInterface.listUsers("1", "accName");
    	
    	Assert.assertEquals(expected.toString(), result.toString());
    	
    	JSONArray result1 = userInterface.listUsers(null, "accName");
    	
		Assert.assertEquals(list.toString(), result1.toString());
    	
    	JSONArray result2 = userInterface.listUsers("1", null);
    	
    	Assert.assertEquals(expected.toString(), result2.toString());
    	
    	JSONArray result3 = userInterface.listUsers(null, null);
    	
    	Assert.assertEquals(list.toString(), result3.toString());
    	
    }
    
    
    // Test scenario : findUser() : Success
    @Test
    public void testfindUser() throws Exception {
    	
    	JSONObject expected = new JSONObject();
    	expected.put("domainid", "2");
    	expected.put("domain", "ROOT");
    	expected.put("account", "system");

		JSONObject result = userInterface.findUser("123sdhdfsod1213");
		
		Assert.assertEquals(expected.toString(), result.toString());
		
		// To run this test uncomment the return statement for sendApacheGet() in setup(), it should return an empty JSONObject
		// Test scenario : No user

		/*JSONObject result = userInterface.findUser("123sdhdfsod1213");
		Assert.assertNull(result);*/
    	
    }
    
    // Test scenario : createUser() : create user success
    @Test
    public void testCreateUser() throws Exception {
    	
		JSONObject expected = new JSONObject();
		expected.put("success", true);
    	
    	JSONObject result = userInterface.createUser("cust1", "QWERTY", "cust1@abc.com", "cust1", "one", "accName", "1", "IST");
    	
    	Assert.assertEquals(expected.toString(), result.toString());
    	
    } 
    
    // Test scenario : updateUser() : update user success
    @Test
    public void testUpdateUser() throws Exception {
    	
		
    	JSONObject expected = new JSONObject();
		
		JSONArray list = new JSONArray();
		
		JSONObject jsonObj1 = new JSONObject();
		jsonObj1.put("domainid", "2");
		jsonObj1.put("domain", "ROOT");
		jsonObj1.put("account", "system");
		
		JSONObject jsonObj2 = new JSONObject();
		jsonObj2.put("domainid", "1");
		jsonObj2.put("domain", "xyz");
		jsonObj2.put("account", "abc");
		
		list.put(jsonObj1);
		list.put(jsonObj2);
		
		expected.put("user", list);
    	
    	JSONObject result = userInterface.updateUser("123", "cust@abc.com", "cust1", "cust2", "password", "EST", "UserAPIKey", "cust", "userSecretKey");
    	
    	Assert.assertEquals(expected.toString(), result.toString());
    	
    } 
    
    

}
