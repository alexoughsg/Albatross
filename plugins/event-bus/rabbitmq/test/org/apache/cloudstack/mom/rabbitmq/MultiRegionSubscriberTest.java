package org.apache.cloudstack.mom.rabbitmq;

import java.lang.String;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.apache.cloudstack.region.RegionVO;
import org.apache.cloudstack.region.dao.RegionDao;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

import com.cloud.domain.dao.DomainDao;
import com.cloud.user.dao.AccountDao;
import com.cloud.user.dao.UserDao;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class MultiRegionSubscriberTest extends TestCase {

    private MultiRegionSubscriber subscriber;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Override
    @Before
    public void setUp() {

        subscriber = new MultiRegionSubscriber(1) {

            protected Gson getGson() {
                return new GsonBuilder().create();
            }

            protected DomainDao getDomainDao() {
                DomainDao domainDao = Mockito.mock(DomainDao.class);
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

                RegionVO localRegion = new RegionVO();
                localRegion.setActive(true);
                localRegion.setName("Local");
                lOfRegions.add(localRegion);

                RegionVO remoteInactiveRegion = new RegionVO();
                remoteInactiveRegion.setActive(false);
                remoteInactiveRegion.setName("InactiveRemote");
                lOfRegions.add(remoteInactiveRegion);

                RegionVO remoteActiveRegion = new RegionVO();
                remoteActiveRegion.setActive(true);
                remoteActiveRegion.setName("ActiveRemote");
                lOfRegions.add(remoteActiveRegion);

                Mockito.when(regionDao.listAll()).thenReturn(lOfRegions);

                return regionDao;
            }
        };
    }

    @Test
    public void testFindRemoteRegions() {

        List<RegionVO> result = subscriber.findRemoteRegions();
        Assert.assertEquals("ActiveRemote", result.get(0).getName());
    }

    @Test
    public void testIsCompleted()
    {
        Assert.assertEquals(false, subscriber.isCompleted(null));
        Assert.assertEquals(false, subscriber.isCompleted(""));
        Assert.assertEquals(true, subscriber.isCompleted("Completed"));
    }

    @Test
    public void testIsExecutable()
    {
        Map<String, String> descMap = new HashMap<String, String>();
        subscriber.setDescMap(descMap);

        descMap.put("status", "");
        Assert.assertEquals(false, subscriber.isExecutable());

        descMap.put("status", "Completed");
        Assert.assertEquals(false, subscriber.isExecutable());

        descMap.put("entityuuid", "");
        Assert.assertEquals(false, subscriber.isExecutable());

        descMap.put("entityuuid", "abcd");
        Assert.assertEquals(true, subscriber.isExecutable());
    }
}
