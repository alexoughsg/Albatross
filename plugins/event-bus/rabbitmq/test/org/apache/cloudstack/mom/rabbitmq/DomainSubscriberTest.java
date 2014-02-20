package org.apache.cloudstack.mom.rabbitmq;

import com.cloud.domain.DomainVO;
import com.cloud.domain.dao.DomainDao;
import com.cloud.region.service.DomainService;
import com.cloud.user.dao.AccountDao;
import com.cloud.user.dao.UserDao;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.cloudstack.framework.events.Event;
import junit.framework.TestCase;
import org.apache.cloudstack.region.RegionVO;
import org.apache.cloudstack.region.dao.RegionDao;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DomainSubscriberTest extends TestCase {

    private DomainSubscriber subscriber;

    @Override
    @Before
    public void setUp() {
        subscriber = new DomainSubscriber(1) {

            protected Gson getGson() {
                return new GsonBuilder().create();
            }

            protected DomainDao getDomainDao() {
                DomainDao domainDao = Mockito.mock(DomainDao.class);

                DomainVO domain = new DomainVO();
                Mockito.when(domainDao.findByUuidIncludingRemoved(Mockito.any(String.class))).thenReturn(domain);

                return domainDao;
            }

            protected AccountDao getAccountDao() {
                AccountDao accountDao = Mockito.mock(AccountDao.class);
                return accountDao;
            }

            protected UserDao getUserDao() {
                UserDao userDao = Mockito.mock(UserDao.class);
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

            protected DomainService getDomainService(RegionVO region)
            {
                DomainService domainService = Mockito.mock(DomainService.class);
                return domainService;
            }
        };
    }

    @Test
    public void testProcess() {
        Map<String, String> descMap = new HashMap<String, String>();
        descMap.put("entityuuid", "abcd");
        descMap.put("oldentityname", "oldname");
        subscriber.setDescMap(descMap);

        Event createEvent = new Event("eventSource", "eventCategory", "DOMAIN-CREATE", "resourceType", "resourceUUID");
        subscriber.process(createEvent);
    }
}