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

public class UserFullSyncProcessorTest extends TestCase {

    private UserFullSyncProcessor userFullSyncProcessor;

    @Override
    @Before
    public void setUp() {

        try
        {
            RegionVO region = new RegionVO(1, "region1", "endPoint1");

            DomainVO parentDomain = new DomainVO();
            parentDomain.setName("ROOT");
            parentDomain.setUuid("parent");

            userFullSyncProcessor = new UserFullSyncProcessor(region, parentDomain)
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
                    AccountVO system = new AccountVO();
                    system.setAccountName("system");
                    accountList.add(system);
                    AccountVO account1 = new AccountVO();
                    account1.setId(1);
                    account1.setAccountName("account1");
                    account1.setUuid("uuid1");
                    accountList.add(account1);
                    Mockito.when(accountDao.findActiveAccountsForDomain(Mockito.any(Long.class))).thenReturn(accountList);

                    return accountDao;
                }

                protected UserDao getUserDao()
                {
                    UserDao userDao = Mockito.mock(UserDao.class);

                    List<UserVO> users = new ArrayList<UserVO>();

                    // this user exists in both local & remote
                    UserVO user1 = new UserVO();
                    user1.setAccountId(1);
                    user1.setUsername("user1");
                    user1.setState(Account.State.enabled);
                    user1.setUuid("uuid1");
                    users.add(user1);

                    // this user exists in both local & remote
                    UserVO user2 = new UserVO();
                    user2.setAccountId(1);
                    user2.setUsername("user2");
                    user2.setState(Account.State.disabled);
                    user2.setUuid("uuid2");
                    users.add(user2);

                    // this user exists in both local & remote with no rmap
                    UserVO user3 = new UserVO();
                    user3.setAccountId(1);
                    user3.setUsername("user3");
                    user3.setState(Account.State.locked);
                    user3.setUuid("uuid3");
                    users.add(user3);

                    try
                    {
                        // this user has been removed in the remote region
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                        Date created = sdf.parse("2014-01-01");
                        UserVO userRemovedInRemote = new UserVO(4, "userRemoved", "", "", "", "", "", "uuidRemoved", created);
                        users.add(userRemovedInRemote);
                        Mockito.when(userDao.listByAccount(Mockito.any(Long.class))).thenReturn(users);
                    }
                    catch(Exception ex)
                    {

                    }

                    try
                    {
                        // this user has been removed in the local region
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                        Date removed = sdf.parse("2014-01-02");
                        List<UserVO> removedUsers = new ArrayList<UserVO>();
                        UserVO userRemoveInLocal = new UserVO();
                        userRemoveInLocal.setAccountId(1);
                        userRemoveInLocal.setRemoved(removed);
                        removedUsers.add(userRemoveInLocal);
                        Mockito.when(userDao.listAllIncludingRemoved()).thenReturn(removedUsers);
                    }
                    catch(Exception ex)
                    {

                    }

                    return userDao;
                }

                protected DomainService allocateDomainService()
                {
                    DomainService domainService = Mockito.mock(DomainService.class);
                    return domainService;
                }

                protected AccountService allocateAccountService()
                {
                    AccountService accountService = Mockito.mock(AccountService.class);

                    JSONArray accountArray = new JSONArray();
                    try
                    {
                        JSONObject account1 = new JSONObject();
                        account1.put("id", "uuid1");
                        account1.put("name", "account1");
                        accountArray.put(account1);
                    }
                    catch(Exception ex)
                    {

                    }
                    Mockito.when(accountService.list(Mockito.any(String.class))).thenReturn(accountArray);

                    return accountService;
                }

                protected UserService allocateUserService()
                {
                    UserService userService = Mockito.mock(UserService.class);

                    JSONArray userArray = new JSONArray();
                    try
                    {
                        // this user exists in both local & remote
                        JSONObject user1 = new JSONObject();
                        user1.put("accountId", "uuid1");
                        user1.put("account", "account1");
                        user1.put("id", "uuid1");
                        user1.put("username", "user1");
                        user1.put("state", "enabled");
                        userArray.put(user1);

                        // this user exists in both local & remote
                        JSONObject user2 = new JSONObject();
                        user2.put("accountId", "uuid1");
                        user2.put("account", "account1");
                        user2.put("id", "uuid2");
                        user2.put("username", "user2");
                        user2.put("state", "disabled");
                        userArray.put(user2);

                        // this user exists in both local & remote with no rmap
                        JSONObject user3 = new JSONObject();
                        user3.put("accountId", "uuid1");
                        user3.put("account", "account1");
                        user3.put("id", "uuid3");
                        user3.put("username", "user3");
                        user3.put("state", "locked");
                        userArray.put(user3);

                        // this user has been removed in the local
                        JSONObject userRemovedInLocal = new JSONObject();
                        userRemovedInLocal.put("created", "2014-01-01T00:00:00-0600");
                        userArray.put(userRemovedInLocal);
                    }
                    catch(Exception ex)
                    {

                    }
                    Mockito.when(userService.list(Mockito.any(String.class), Mockito.any(String.class))).thenReturn(userArray);

                    return userService;
                }

                protected LocalUserManager allocateLocalUserManager()
                {
                    LocalUserManager localUserManager = Mockito.mock(LocalUserManager.class);
                    return localUserManager;
                }

                protected RemoteUserEventProcessor allocateRemoteUserEventProcessor()
                {
                    RemoteUserEventProcessor remoteUserEventProcessor = Mockito.mock(RemoteUserEventProcessor.class);

                    try
                    {
                        JSONObject eventJson = new JSONObject();
                        eventJson.put("created", "2014-01-02T00:00:00-0600");
                        Mockito.when(remoteUserEventProcessor.findLatestRemoteRemoveEvent(Mockito.any(UserVO.class))).thenReturn(eventJson);
                    }
                    catch(Exception ex)
                    {

                    }

                    return remoteUserEventProcessor;
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
        Assert.assertTrue(userFullSyncProcessor.localList.size() == 4);
        Assert.assertTrue(userFullSyncProcessor.remoteList.size() == 4);

        userFullSyncProcessor.synchronizeByLocal();

        Assert.assertTrue(userFullSyncProcessor.localList.size() == 0);
        Assert.assertTrue(userFullSyncProcessor.remoteList.size() == 1);

        userFullSyncProcessor.synchronizeByRemote();

        Assert.assertTrue(userFullSyncProcessor.localList.size() == 0);
        Assert.assertTrue(userFullSyncProcessor.remoteList.size() == 0);
    }
}
