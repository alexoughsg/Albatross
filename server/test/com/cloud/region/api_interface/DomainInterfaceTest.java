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

public class DomainInterfaceTest extends TestCase {

	private DomainInterface domainInterface;
	
	
	@Override
    @Before
    public void setUp() {

		domainInterface = new DomainInterface("URL") {

    		public String getSessionKey() {
    			return "XxjzeJWHV3S%2Brwq2m2EsYTSIYNE%3D";
    		}
    		
    		public JSONObject sendApacheGet(String paramStr) throws Exception {
    			
    			JSONObject jsonObj = new JSONObject();
    			
    			JSONArray list = new JSONArray();
    			
    			JSONObject jsonObj1 = new JSONObject();
    			jsonObj1.put("path", "domainPath");
    			
    			list.put(jsonObj1);
    			
    			jsonObj.put("domain", list);

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
	
	  // Test scenario : ListAccounts() 
    @Test
    public void testListDomains() throws Exception {
    	
		JSONArray expected = new JSONArray();
		
		JSONObject jsonObj1 = new JSONObject();
		jsonObj1.put("path", "domainPath/");
		
		expected.put(jsonObj1);
    	
		JSONArray domain = domainInterface.listDomains(true);
    	Assert.assertEquals(expected.toString(), domain.toString());
		
		domain = domainInterface.listDomains(false);
		Assert.assertEquals(expected.toString(), domain.toString());

		
	    // To run this test uncomment the return statement for sendApacheGet() in setup(), it should return an empty JSONObject
		// Test scenario : No Domains
		
		/*JSONArray expected1 = new JSONArray();
    	JSONArray result = domainInterface.listDomains(true);
    	Assert.assertEquals(expected1.toString(), result.toString());*/
		
    }
    
  // Test scenario : findDomain(String uuid) 
    @Test
    public void testfindDomain() throws Exception {
    	
		JSONObject expected = new JSONObject();
		expected.put("path", "domainPath/");
		
		JSONObject domain = domainInterface.findDomain("ds97erwefo0967");
    	Assert.assertEquals(expected.toString(), domain.toString());
    	
    	// To run this test uncomment the return statement for sendApacheGet() in setup(), it should return an empty JSONObject
    	// Test scenario : findDomain(String uuid) : No Domains
    	
    	/*JSONObject domain = domainInterface.findDomain("ds97errrefo0967");
    	Assert.assertNull(domain);*/
		
    }
    
    // Test scenario : findDomain(int level, String name, String path) : Success
    @Test
    public void testfindDomainWithName() throws Exception {
    	
		JSONObject expected = new JSONObject();
		expected.put("path", "domainPath/");
		
		JSONObject domain = domainInterface.findDomain(1, "Domain123", "domainPath");
    	Assert.assertEquals(expected.toString(), domain.toString());
    	
    	// path != pathInJson
    	domain = domainInterface.findDomain(1, "Domain123", "path");
    	Assert.assertNull(domain);
    	
    	// path == null
    	JSONObject expected1 = new JSONObject();
    	expected1.put("path", "domainPath");
    	domain = domainInterface.findDomain(1, "Domain123", null);
    	Assert.assertEquals(expected1.toString(), domain.toString());
    	
    	// To run this test uncomment the return statement for sendApacheGet() in setup(), it should return an empty JSONObject
        // Test scenario : findDomain(int level, String name, String path) : No Domains
    	
    	/*JSONObject domain = domainInterface.findDomain("ds97errrefo0967");
    	Assert.assertNull(domain);*/
    	
    }
    
    
    // Test scenario : listChildDomains(String parentDomainId, boolean isRecursive) : Success
    @Test
    public void testlistChildDomains() throws Exception {
    	
    	JSONArray expected = new JSONArray();
		
		JSONObject jsonObj1 = new JSONObject();
		jsonObj1.put("path", "domainPath/");
		
		expected.put(jsonObj1);
		
		JSONArray domain = domainInterface.listChildDomains("parentDomainId", true);
		
    	Assert.assertEquals(expected.toString(), domain.toString());
    	
    	domain = domainInterface.listChildDomains("parentDomainId", false);
    	Assert.assertEquals(expected.toString(), domain.toString());
    	
    	domain = domainInterface.listChildDomains(null, true);
    	Assert.assertEquals(expected.toString(), domain.toString());
    	
    	// To run this test uncomment the return statement for sendApacheGet() in setup(), it should return an empty JSONObject
    	// Test scenario : listChildDomains(String parentDomainId, boolean isRecursive) : No Domains
    	/*JSONObject domain = domainInterface.findDomain("ds97errrefo0967");
    	Assert.assertNull(domain);*/
    }
    
    // Test scenario : createDomain() : create domain success
    @Test
    public void testcreateDomainSuccess() throws Exception {
    	
		
    	JSONObject expected = new JSONObject();
		
		JSONArray list = new JSONArray();
		
		JSONObject jsonObj1 = new JSONObject();
		jsonObj1.put("path", "domainPath");
		
		list.put(jsonObj1);
		
		expected.put("domain", list);
    	
    	JSONObject result = domainInterface.createDomain("domainName", "parentDomainId", "123", "NetworkDomain");
    	Assert.assertEquals(expected.toString(), result.toString());
    	
    } 
    
    // Test scenario : updateDomain() : update domain success
    @Test
    public void testupdateDomainSuccess() throws Exception {
    	
		
    	JSONObject expected = new JSONObject();
		
		JSONArray list = new JSONArray();
		
		JSONObject jsonObj1 = new JSONObject();
		jsonObj1.put("path", "domainPath");
		
		list.put(jsonObj1);
		
		expected.put("domain", list);
    	
    	JSONObject result = domainInterface.updateDomain("123", "domainName", "networkDomain");
    	Assert.assertEquals(expected.toString(), result.toString());
    	
    } 
    
}
