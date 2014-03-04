package com.cloud.region.service;

import com.amazonaws.util.json.JSONArray;
import com.amazonaws.util.json.JSONObject;
import com.cloud.domain.Domain;
import com.cloud.domain.DomainVO;
import com.cloud.domain.dao.DomainDao;
import com.cloud.rmap.RmapVO;
import com.cloud.rmap.dao.RmapDao;
import com.cloud.user.UserVO;
import junit.framework.Assert;
import junit.framework.TestCase;
import org.apache.cloudstack.region.RegionVO;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DomainFullSyncProcessorTest extends TestCase {

    private DomainFullSyncProcessor domainFullSyncProcessor;

    @Override
    @Before
    public void setUp() {

        try
        {
            RegionVO region = new RegionVO(1, "region1", "endPoint1");

            DomainVO parentDomain = new DomainVO();
            parentDomain.setName("ROOT");
            parentDomain.setUuid("parent");

            domainFullSyncProcessor = new DomainFullSyncProcessor(region, parentDomain)
            {
                protected RmapDao getRmapDao()
                {
                    RmapDao rmapDao = Mockito.mock(RmapDao.class);

                    RmapVO parentRmap = new RmapVO();
                    parentRmap.setUuid("abcd");
                    Mockito.when(rmapDao.findBySource("parent", 1)).thenReturn(parentRmap);

                    Mockito.when(rmapDao.findBySource("uuid1", 1)).thenReturn(new RmapVO("uuid1", 1, "uuid1"));
                    Mockito.when(rmapDao.findBySource("uuid2", 1)).thenReturn(new RmapVO("uuid2", 1, "uuid2"));
                    Mockito.when(rmapDao.findBySource("uuid3", 1)).thenReturn(null);

                    return rmapDao;
                }

                protected DomainDao getDomainDao()
                {
                    DomainDao domainDao = Mockito.mock(DomainDao.class);

                    ArrayList<DomainVO> domainList = new ArrayList<DomainVO>();

                    // this domain exists in both local & remote
                    DomainVO domain1 = new DomainVO();
                    domain1.setName("domain1");
                    domain1.setState(Domain.State.Active);
                    domain1.setUuid("uuid1");
                    domain1.setNetworkDomain("networkdomain1");
                    domain1.setPath("path1");
                    domainList.add(domain1);

                    // this domain exists in both local & remote
                    DomainVO domain2 = new DomainVO();
                    domain2.setState(Domain.State.Active);
                    domain2.setUuid("uuid2");
                    domain2.setNetworkDomain("networkdomain2");
                    domain2.setPath("path2");
                    domainList.add(domain2);

                    // this domain exists in both local & remote with no rmap
                    DomainVO domain3 = new DomainVO();
                    domain3.setState(Domain.State.Active);
                    domain3.setUuid("uuid3");
                    domain3.setNetworkDomain("networkdomain3");
                    domain3.setPath("path3");
                    domainList.add(domain3);

                    // this domain exists only in local with inactive state
                    DomainVO domainInactive = new DomainVO();
                    domainInactive.setState(Domain.State.Inactive);
                    domainInactive.setUuid("uuidInactive");
                    domainInactive.setNetworkDomain("networkdomainInactive");
                    domainInactive.setPath("pathInactive");
                    domainList.add(domainInactive);

                    try
                    {
                        // this domain has been removed in the remote region
                        long parentId = 0;
                        long owner = 0;
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                        Date created = sdf.parse("2014-01-01");
                        DomainVO domainRemovedInRemote = new DomainVO("domainRemoved", owner, parentId, "networkdomainRemoved", "uuidRemoved", created);
                        domainRemovedInRemote.setPath("pathRemovedInRemote");
                        domainList.add(domainRemovedInRemote);
                        Mockito.when(domainDao.findImmediateChildrenForParent(Mockito.any(Long.class))).thenReturn(domainList);
                    }
                    catch(Exception ex)
                    {

                    }

                    try
                    {
                        // this domain has been removed in the local region
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                        Date removed = sdf.parse("2014-01-02");
                        List<DomainVO> removedDomains = new ArrayList<DomainVO>();
                        DomainVO domainRemoveInLocal = new DomainVO();
                        domainRemoveInLocal.setPath("pathRemovedInLocal");
                        domainRemoveInLocal.setRemoved(removed);
                        removedDomains.add(domainRemoveInLocal);
                        Mockito.when(domainDao.listAllIncludingRemoved()).thenReturn(removedDomains);
                    }
                    catch(Exception ex)
                    {

                    }

                    return domainDao;
                }

                protected DomainService allocateDomainService()
                {
                    DomainService domainService = Mockito.mock(DomainService.class);

                    try
                    {
                        JSONObject domainJson = new JSONObject();
                        domainJson.put("name", "ROOT");
                        domainJson.put("uuid", "parent");
                        Mockito.when(domainService.findDomain(Mockito.any(String.class))).thenReturn(domainJson);
                    }
                    catch (Exception ex)
                    {

                    }

                    JSONArray domainArray = new JSONArray();
                    try
                    {
                        // this domain exists in both local & remote
                        JSONObject domain1 = new JSONObject();
                        domain1.put("id", "uuid1");
                        domain1.put("name", "domain1");
                        domain1.put("path", "path1");
                        domain1.put("networkdomain", "networkdomain1");
                        domainArray.put(domain1);

                        // this domain exists in both local & remote
                        JSONObject domain2 = new JSONObject();
                        domain2.put("id", "uuid2");
                        domain2.put("name", "domain2");
                        domain2.put("path", "path2");
                        domain2.put("networkdomain", "networkdomain2");
                        domainArray.put(domain2);

                        // this domain exists in both local & remote with no rmap
                        JSONObject domain3 = new JSONObject();
                        domain3.put("id", "uuid3");
                        domain3.put("name", "domain3");
                        domain3.put("path", "path3");
                        domain3.put("networkdomain", "networkdomain3");
                        domainArray.put(domain3);

                        // this domain has been removed in the local
                        JSONObject domainRemovedInLocal = new JSONObject();
                        domainRemovedInLocal.put("path", "pathRemovedInLocal");
                        domainRemovedInLocal.put("created", "2014-01-01T00:00:00-0600");
                        domainArray.put(domainRemovedInLocal);
                    }
                    catch(Exception ex)
                    {

                    }
                    Mockito.when(domainService.listChildren(Mockito.any(String.class), Mockito.anyBoolean())).thenReturn(domainArray);

                    return domainService;
                }

                protected LocalDomainManager allocateLocalDomainManager()
                {
                    LocalDomainManager localDomainManager = Mockito.mock(LocalDomainManager.class);
                    return localDomainManager;
                }

                protected RemoteDomainEventProcessor allocateRemoteDomainEventProcessor()
                {
                    RemoteDomainEventProcessor remoteDomainEventProcessor = Mockito.mock(RemoteDomainEventProcessor.class);

                    try
                    {
                        JSONObject eventJson = new JSONObject();
                        eventJson.put("created", "2014-01-02T00:00:00-0600");
                        Mockito.when(remoteDomainEventProcessor.findLatestRemoteRemoveEvent(Mockito.any(UserVO.class))).thenReturn(eventJson);
                    }
                    catch(Exception ex)
                    {

                    }

                    return remoteDomainEventProcessor;
                }
            };
        }
        catch(Exception ex)
        {
            Assert.assertTrue(1 == 0);
        }
    }

    @Test
    public void testSynchronize()
    {
        Assert.assertTrue(domainFullSyncProcessor.localList.size() == 4);
        Assert.assertTrue(domainFullSyncProcessor.remoteList.size() == 4);

        domainFullSyncProcessor.synchronizeByLocal();

        Assert.assertTrue(domainFullSyncProcessor.localList.size() == 0);
        Assert.assertTrue(domainFullSyncProcessor.remoteList.size() == 1);

        domainFullSyncProcessor.synchronizeByRemote();

        Assert.assertTrue(domainFullSyncProcessor.localList.size() == 0);
        Assert.assertTrue(domainFullSyncProcessor.remoteList.size() == 0);
    }
}
