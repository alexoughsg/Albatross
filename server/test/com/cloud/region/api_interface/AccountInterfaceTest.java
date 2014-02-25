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
    			
    			return jsonObj;
    		}
    		
    		// Note: Replace this method with the above method to test the negative test cases
    		// 1. If the JSONObject is empty 
    		// 2. No accounts
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
    public void testListAccountsDomainIdNotNull() throws Exception {
    	
    	JSONArray expected = new JSONArray();
		
		JSONObject jsonObj1 = new JSONObject();
		jsonObj1.put("domainid", "1");
		
		expected.put(jsonObj1);
    	
    	JSONArray result = accountInterface.listAccounts("1");
    	
    	Assert.assertEquals(expected.toString(), result.toString());
    	
    }
    
    // Test scenario : ListAccounts() : If domainId passed is null
    @Test
    public void testListAccountsDomainIDisNull() throws Exception {
    	
    	JSONArray expected = new JSONArray();
		
		JSONObject jsonObj1 = new JSONObject();
		jsonObj1.put("domainid", "1");
		
		expected.put(jsonObj1);
    	
    	JSONArray result = accountInterface.listAccounts(null);
    	
    	Assert.assertEquals(expected.toString(), result.toString());
    	
    }
    
    
    // to test this scenario uncomment the sendApacheGet method from the setup
    // Test scenario : ListAccounts() : No accounts
/*    @Test
    public void testListAccountsEmptyAccount() throws Exception {
    	
    	JSONArray expected = new JSONArray();
    	
    	JSONArray result = accountInterface.listAccounts(null);
    	
    	Assert.assertEquals(expected.toString(), result.toString());
    	
    }
*/	
    
    // Test scenario : findAccount() : With Accounts
    @Test
    public void testfindAccountSuccess() throws Exception {
    	
		
		JSONObject expected = new JSONObject();
		expected.put("domainid", "1");
    	
    	JSONObject result = accountInterface.findAccount("1", "account1");
    	
    	Assert.assertEquals(expected.toString(), result.toString());
    	
    }
    
    // Test scenario :findAccount() : No Accounts
/*    @Test
    public void testfindAccountNoAccounts() throws Exception {
    	
    	JSONObject result = accountInterface.findAccount("1", "account1");
    	
    	Assert.assertNull(result);
    	
    }*/
    
    
    // Test scenario : createAccount() : create account success
    @Test
    public void testCreateAccountSuccess() throws Exception {
    	
		
		JSONObject expected = new JSONObject();
		expected.put("success", true);
    	
    	JSONObject result = accountInterface.createAccount("cust1", "QWERTY", "cust1@abc.com", "cust1", "one", "accType", "1", "accName", "Details", "NWDomain", "IST");
    	
    	Assert.assertEquals(expected.toString(), result.toString());
    	
    } 
    
    // Test scenario : updateAccount() : update account success
    @Test
    public void testUpdateAccountSuccess() throws Exception {
    	
		
    	JSONObject expected = new JSONObject();
		
		JSONArray list = new JSONArray();
		
		JSONObject jsonObj1 = new JSONObject();
		jsonObj1.put("domainid", "1");
		
		list.put(jsonObj1);
		
		expected.put("account", list);
    	
    	JSONObject result = accountInterface.updateAccount("123", "cust1", "cust2", "Details", "1", "NWDomain");
    	
    	Assert.assertEquals(expected.toString(), result.toString());
    	
    } 
    
    
}
