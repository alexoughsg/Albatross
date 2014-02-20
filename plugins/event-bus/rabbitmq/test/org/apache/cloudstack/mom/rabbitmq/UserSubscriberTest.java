package org.apache.cloudstack.mom.rabbitmq;

import com.cloud.domain.DomainVO;
import com.cloud.domain.dao.DomainDao;
import com.cloud.region.service.AccountService;
import com.cloud.region.service.UserService;
import com.cloud.user.AccountVO;
import com.cloud.user.UserVO;
import com.cloud.user.dao.AccountDao;
import com.cloud.user.dao.UserDao;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import junit.framework.TestCase;
import org.apache.cloudstack.framework.events.Event;
import org.apache.cloudstack.region.RegionVO;
import org.apache.cloudstack.region.dao.RegionDao;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserSubscriberTest extends TestCase {

    private UserSubscriber subscriber;

    @Override
    @Before
    public void setUp() {
        subscriber = new UserSubscriber(1) {

            protected Gson getGson() {
                return new GsonBuilder().create();
            }

            protected DomainDao getDomainDao() {
                DomainDao domainDao = Mockito.mock(DomainDao.class);

                DomainVO domain = new DomainVO();
                Mockito.when(domainDao.findByIdIncludingRemoved(Mockito.any(Long.class))).thenReturn(domain);

                return domainDao;
            }

            protected AccountDao getAccountDao() {
                AccountDao accountDao = Mockito.mock(AccountDao.class);

                AccountVO account = new AccountVO();
                account.setDomainId(1234);
                Mockito.when(accountDao.findByIdIncludingRemoved(Mockito.any(Long.class))).thenReturn(account);

                return accountDao;
            }

            protected UserDao getUserDao() {
                UserDao userDao = Mockito.mock(UserDao.class);

                UserVO user = new UserVO();
                Mockito.when(userDao.findByUuidIncludingRemoved(Mockito.any(String.class))).thenReturn(user);

                return userDao;
            }

            protected RegionDao getRegionDao() {
                RegionDao regionDao = Mockito.mock(RegionDao.class);

                List<RegionVO> lOfRegions = new ArrayList<RegionVO>();

                RegionVO remoteActiveRegion = new RegionVO();
                remoteActiveRegion.setActive(true);
                remoteActiveRegion.setName("ActiveRemote");
                lOfRegions.add(remoteActiveRegion);

                Mockito.when(regionDao.listAll()).thenReturn(lOfRegions);

                return regionDao;
            }

            protected UserService getUserService(RegionVO region)
            {
                UserService userService = Mockito.mock(UserService.class);
                return userService;
            }
        };
    }

    @Test
    public void testProcess() {
        Map<String, String> descMap = new HashMap<String, String>();
        descMap.put("entityuuid", "abcd");
        descMap.put("oldentityname", "oldname");
        subscriber.setDescMap(descMap);

        Event createEvent = new Event("eventSource", "eventCategory", "ACCOUNT-CREATE", "resourceType", "resourceUUID");
        subscriber.process(createEvent);
    }
}