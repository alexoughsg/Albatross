package com.cloud.region.service;

import com.amazonaws.util.json.JSONArray;
import com.amazonaws.util.json.JSONObject;
import com.cloud.domain.DomainVO;
import com.cloud.domain.dao.DomainDao;
import com.cloud.user.AccountVO;
import com.cloud.user.UserVO;
import com.cloud.user.dao.AccountDao;
import junit.framework.Assert;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.text.SimpleDateFormat;
import java.util.Date;

public class RemoteUserEventProcessorTest extends TestCase {

    private RemoteUserEventProcessor userEventProcessor;

    @Override
    @Before
    public void setUp() {

        userEventProcessor = new RemoteUserEventProcessor("hostName", "endPoint", "userName", "password")
        {
            protected DomainDao getDomainDao()
            {
                DomainDao domainDao = Mockito.mock(DomainDao.class);

                DomainVO domain = new DomainVO();
                domain.setPath("path1");
                Mockito.when(domainDao.findById(Mockito.any(Long.class))).thenReturn(domain);

                return domainDao;
            }

            protected AccountDao getAccountDao()
            {
                AccountDao accountDao = Mockito.mock(AccountDao.class);

                AccountVO account = new AccountVO();
                account.setAccountName("account1");
                Mockito.when(accountDao.findById(Mockito.any(Long.class))).thenReturn(account);

                return accountDao;
            }

            protected BaseService allocateBaseService()
            {
                BaseService baseService = Mockito.mock(BaseService.class);

                JSONArray eventArray = new JSONArray();
                try
                {
                    JSONObject event1 = new JSONObject();
                    String description1 = "completed. User Name : user1, Account Name : account1, Domain Path : path1";
                    event1.put("uuid", "uuid1");
                    event1.put("description", description1);
                    event1.put("created", "2014-01-01T00:00:00-0600");
                    eventArray.put(event1);

                    JSONObject event2 = new JSONObject();
                    String description2 = "completed. User Name : user1, Account Name : account1, Domain Path : path1";
                    event2.put("uuid", "uuid2");
                    event2.put("description", description2);
                    event2.put("created", "2014-01-02T12:00:00-0600");
                    eventArray.put(event2);

                    JSONObject event3 = new JSONObject();
                    String description3 = "completed. User Name : user1, Account Name : account1, Domain Path : path1";
                    event3.put("uuid", "uuid3");
                    event3.put("description", description3);
                    event3.put("created", "2014-01-02T11:00:00-0600");
                    eventArray.put(event3);

                    Mockito.when(baseService.listEvents(Mockito.any(String.class), Mockito.any(String.class), Mockito.any(Date.class), Mockito.any(Date.class))).thenReturn(eventArray);
                }
                catch(Exception ex)
                {

                }

                return baseService;
            }
        };
    }

    @Test
    public void testFindLatestRemoteRemoveEvent()
    {
        try
        {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date created = sdf.parse("2014-01-02");
            long accountId = 1;
            UserVO user = new UserVO(accountId, "user1", "", "", "", "", "", "", created);
            JSONObject event = userEventProcessor.findLatestRemoteRemoveEvent(user);
            Assert.assertTrue(event.getString("uuid").equals("uuid2"));
        }
        catch (Exception ex)
        {
            Assert.assertTrue(1 == 0);
        }
    }
}
