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

import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.amazonaws.util.json.JSONArray;
import com.amazonaws.util.json.JSONException;
import com.amazonaws.util.json.JSONObject;

import junit.framework.Assert;
import junit.framework.TestCase;

public class BaseInterfaceTest extends TestCase {

	private BaseInterface baseInterface;
	
	@Override
    @Before
    public void setUp() {

		baseInterface = new BaseInterface("URL") {

    		public JSONObject sendApacheGet(String paramStr) throws Exception {
    			
    			JSONObject jsonObj = new JSONObject();
    			
    			if (paramStr.contains("projectid")) {
    				
    				jsonObj.put("queryAsyncJob", "queryAsyncJob");
    				return jsonObj;
    			}
    			
    			
    			JSONArray list = new JSONArray();
    			
    			JSONObject jsonObj1 = new JSONObject();
    			jsonObj1.put("event", "event");
    			
    			list.put(jsonObj1);
    			
    			jsonObj.put("event", list);

    			// uncomment to return empty JSONOject
    			/*jsonObj = new JSONObject();
    			return jsonObj;*/

    			
    			return jsonObj;
    		}
    		
    		    		
    		public JSONObject sendApachePost(String paramStr) throws Exception {
    			
    			JSONObject obj = new JSONObject();
    			obj.put("sessionkey", "GNUfHusIyEOsqpgFp/Q9O2zaRFQ=");
    			return obj;
    		}
    		
    		public JSONObject login(String userName, String password) throws Exception {
    			JSONObject obj = new JSONObject();
    			obj.put("sessionkey", "GNUfHusIyEOsqpgFp/Q9O2zaRFQ=");
    			baseInterface.sessionKey = "GNUfHusIyEOsqpgFp/Q9O2zaRFQ=";
    			return obj;
    		}

        };
	}
	
	@Test
	public void testLogin() throws Exception {
		
		JSONObject expected = new JSONObject();
		expected.put("sessionkey", "GNUfHusIyEOsqpgFp/Q9O2zaRFQ=");
		
		JSONObject result = null;
		result = baseInterface.login("userName", "password");
		
		Assert.assertEquals(expected.toString(), result.toString());
		
	}
	
	@Test
	public void testLogout() throws Exception {
		
		baseInterface.login("userName", "password");
		baseInterface.logout();
			
	}

	@Test
	public void testQueryAsyncJob() throws Exception {
		
		baseInterface.login("userName", "password");
		JSONObject result = baseInterface.queryAsyncJob("jobId", "projectId");
		
		JSONObject expected = new JSONObject();
		expected.put("queryAsyncJob", "queryAsyncJob");

		Assert.assertEquals(expected.toString(), result.toString());
		baseInterface.logout();
			
	}
	
	@Test
	public void testListEvents() throws Exception {
		
		baseInterface.login("userName", "password");
		Date date = new Date();
		JSONArray result = baseInterface.listEvents("type", "keyword", date, date);
	
		JSONArray expected = new JSONArray();
		
		JSONObject jsonObj1 = new JSONObject();
		jsonObj1.put("event", "event");
		
		expected.put(jsonObj1);
		
		Assert.assertEquals(expected.toString(), result.toString());
		
		baseInterface.logout();
			
	}
	
}
