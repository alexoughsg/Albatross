package com.cloud.region.service;

import com.amazonaws.util.json.JSONArray;
import com.amazonaws.util.json.JSONObject;
import com.cloud.domain.DomainVO;
import com.cloud.domain.dao.DomainDao;
import com.cloud.rmap.RmapVO;
import com.cloud.rmap.dao.RmapDao;
import com.cloud.user.Account;
import com.cloud.user.AccountVO;
import com.cloud.user.UserVO;
import com.cloud.user.dao.AccountDao;
import com.cloud.user.dao.UserDao;
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

public class AccountFullSyncProcessorTest extends TestCase {

    private AccountFullSyncProcessor accountFullSyncProcessor;

    @Override
    @Before
    public void setUp() {

        try
        {
            RegionVO region = new RegionVO(1, "region1", "endPoint1");

            DomainVO parentDomain = new DomainVO();
            parentDomain.setName("ROOT");
            parentDomain.setUuid("parent");

            accountFullSyncProcessor = new AccountFullSyncProcessor(region, parentDomain)
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
                    return domainDao;
                }

                protected AccountDao getAccountDao()
                {
                    AccountDao accountDao = Mockito.mock(AccountDao.class);

                    ArrayList<AccountVO> accountList = new ArrayList<AccountVO>();

                    // add the system account first
                    AccountVO system = new AccountVO();
                    system.setAccountName("system");
                    accountList.add(system);

                    // this account exists in both local & remote
                    AccountVO account1 = new AccountVO();
                    account1.setDomainId(0);
                    account1.setAccountName("account1");
                    account1.setState(Account.State.enabled);
                    account1.setUuid("uuid1");
                    account1.setNetworkDomain("networkdomain1");
                    accountList.add(account1);

                    // this account exists in both local & remote
                    AccountVO account2 = new AccountVO();
                    account2.setDomainId(0);
                    account2.setAccountName("account2");
                    account2.setState(Account.State.disabled);
                    account2.setUuid("uuid2");
                    account2.setNetworkDomain("networkdomain2");
                    accountList.add(account2);

                    // this account exists in both local & remote with no rmap
                    AccountVO account3 = new AccountVO();
                    account3.setDomainId(0);
                    account3.setAccountName("account3");
                    account3.setState(Account.State.locked);
                    account3.setUuid("uuid3");
                    account3.setNetworkDomain("networkdomain3");
                    accountList.add(account3);

                    try
                    {
                        // this account has been removed in the remote region
                        long domainId = 0;
                        short type = 0;
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                        Date created = sdf.parse("2014-01-01");
                        AccountVO accountRemovedInRemote = new AccountVO("accountRemoved", domainId, "networkdomainRemoved", type, "uuidRemoved", created);
                        accountList.add(accountRemovedInRemote);
                        Mockito.when(accountDao.findActiveAccountsForDomain(Mockito.any(Long.class))).thenReturn(accountList);
                    }
                    catch(Exception ex)
                    {

                    }

                    try
                    {
                        // this account has been removed in the local region
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                        Date removed = sdf.parse("2014-01-02");
                        List<AccountVO> removedAccounts = new ArrayList<AccountVO>();
                        AccountVO accountRemoveInLocal = new AccountVO();
                        accountRemoveInLocal.setDomainId(0);
                        accountRemoveInLocal.setRemoved(removed);
                        removedAccounts.add(accountRemoveInLocal);
                        Mockito.when(accountDao.listAllIncludingRemoved()).thenReturn(removedAccounts);
                    }
                    catch(Exception ex)
                    {

                    }

                    return accountDao;
                }

                protected DomainService allocateDomainService()
                {
                    DomainService domainService = Mockito.mock(DomainService.class);

                    JSONObject domainJson = new JSONObject();
                    Mockito.when(domainService.findDomain(Mockito.any(String.class))).thenReturn(domainJson);

                    return domainService;
                }

                protected AccountService allocateAccountService()
                {
                    AccountService accountService = Mockito.mock(AccountService.class);

                    JSONArray accountArray = new JSONArray();
                    try
                    {
                        // this account exists in both local & remote
                        JSONObject account1 = new JSONObject();
                        account1.put("id", "uuid1");
                        account1.put("name", "account1");
                        account1.put("state", "enabled");
                        account1.put("networkdomain", "networkdomain1");
                        accountArray.put(account1);

                        // this account exists in both local & remote
                        JSONObject account2 = new JSONObject();
                        account2.put("id", "uuid2");
                        account2.put("name", "account2");
                        account2.put("state", "disabled");
                        account2.put("networkdomain", "networkdomain2");
                        accountArray.put(account2);

                        // this account exists in both local & remote with no rmap
                        JSONObject account3 = new JSONObject();
                        account3.put("id", "uuid3");
                        account3.put("name", "account3");
                        account3.put("state", "locked");
                        account3.put("networkdomain", "networkdomain3");
                        accountArray.put(account3);

                        // this account has been removed in the local
                        JSONObject accountRemovedInLocal = new JSONObject();
                        accountRemovedInLocal.put("created", "2014-01-01T00:00:00-0600");
                        accountArray.put(accountRemovedInLocal);
                    }
                    catch(Exception ex)
                    {

                    }
                    Mockito.when(accountService.list(Mockito.any(String.class))).thenReturn(accountArray);

                    return accountService;
                }

                protected LocalAccountManager allocateLocalAccountManager()
                {
                    LocalAccountManager localAccountManager = Mockito.mock(LocalAccountManager.class);
                    return localAccountManager;
                }

                protected RemoteAccountEventProcessor allocateRemoteAccountEventProcessor()
                {
                    RemoteAccountEventProcessor remoteAccountEventProcessor = Mockito.mock(RemoteAccountEventProcessor.class);

                    try
                    {
                        JSONObject eventJson = new JSONObject();
                        eventJson.put("created", "2014-01-02T00:00:00-0600");
                        Mockito.when(remoteAccountEventProcessor.findLatestRemoteRemoveEvent(Mockito.any(UserVO.class))).thenReturn(eventJson);
                    }
                    catch(Exception ex)
                    {

                    }

                    return remoteAccountEventProcessor;
                }
            };
        }
        catch(Exception ex)
        {
            Assert.assertTrue(1 == 0);
        }
    }

    @Test
    public void testSynchronizeByLocal()
    {
        Assert.assertTrue(accountFullSyncProcessor.localList.size() == 4);
        Assert.assertTrue(accountFullSyncProcessor.remoteList.size() == 4);

        accountFullSyncProcessor.synchronizeByLocal();

        Assert.assertTrue(accountFullSyncProcessor.localList.size() == 0);
        Assert.assertTrue(accountFullSyncProcessor.remoteList.size() == 1);

        accountFullSyncProcessor.synchronizeByRemote();

        Assert.assertTrue(accountFullSyncProcessor.localList.size() == 0);
        Assert.assertTrue(accountFullSyncProcessor.remoteList.size() == 0);
    }
}
