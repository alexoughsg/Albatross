package com.cloud.region.api_interface;

import org.junit.Before;
import org.junit.Test;

import com.amazonaws.util.json.JSONArray;
import com.amazonaws.util.json.JSONObject;

import junit.framework.Assert;
import junit.framework.TestCase;

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
    			
    			return jsonObj;
    		}
    		
    		// Note: Replace this method with the above method to test the negative test cases
    		// 1. If the JSONObject is empty 
    		// 2. No domains
    		/*public JSONObject sendApacheGet(String paramStr) throws Exception {
    			
    			JSONObject jsonObj = new JSONObject();
    			
    			return jsonObj;
    		}*/
    		
    		public JSONObject sendApachePost(String paramStr) throws Exception {
    			
    			JSONObject obj = new JSONObject();
    			obj.put("success", true);
    			
    			return obj;
    		}

        };
    }
	
	  // Test scenario : ListAccounts() : Success
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
    	
    }
    
    // to test this scenario uncomment the sendApacheGet method from the setup
   // Test scenario : No Domains
   /* @Test
    public void testListDomainsNoDomain() throws Exception {
    	
    	JSONArray expected = new JSONArray();
    	
    	JSONArray result = domainInterface.listDomains(true);

    	Assert.assertEquals(expected.toString(), result.toString());
    	
    }*/
	
    
	
}
