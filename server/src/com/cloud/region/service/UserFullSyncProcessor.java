package com.cloud.region.service;

import com.amazonaws.util.json.JSONArray;
import com.amazonaws.util.json.JSONObject;
import com.cloud.domain.DomainVO;
import com.cloud.domain.dao.DomainDao;
import com.cloud.rmap.RmapVO;
import com.cloud.user.AccountVO;
import com.cloud.user.User;
import com.cloud.user.UserVO;
import com.cloud.user.dao.AccountDao;
import com.cloud.user.dao.UserDao;
import com.cloud.utils.component.ComponentContext;
import org.apache.cloudstack.region.RegionVO;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class UserFullSyncProcessor extends FullSyncProcessor {

    private static final Logger s_logger = Logger.getLogger(UserFullSyncProcessor.class);

    protected UserDao userDao;
    protected AccountDao accountDao;
    protected DomainDao domainDao;

    protected DomainVO localParentDomain;
    //protected AccountVO localParent;
    protected List<UserVO> localList;
    protected List<UserVO> processedLocalList = new ArrayList<UserVO>();

    protected List<AccountVO> localAccountList;

    //protected JSONObject remoteParentDomain;
    protected List<JSONObject> remoteAccountList;

    private LocalUserManager localUserManager;
    private RemoteUserEventProcessor eventProcessor;

    public UserFullSyncProcessor(RegionVO region, DomainVO parentDomain) throws Exception
    {
        super(region);

        this.userDao = getUserDao();
        this.accountDao = getAccountDao();
        this.domainDao = getDomainDao();

        populateLocalList(parentDomain);
        populateRemoteList();

        localUserManager = allocateLocalUserManager();
        eventProcessor = allocateRemoteUserEventProcessor();
    }

    private void populateLocalList(DomainVO parentDomain)
    {
        localParentDomain = parentDomain;
        localAccountList = accountDao.findActiveAccountsForDomain(localParentDomain.getId());
        localList = new ArrayList<UserVO>();
        for (AccountVO account : localAccountList)
        {
            if (localParentDomain.getName().equals("ROOT") && account.getAccountName().equals("system"))   continue;
            localList.addAll(userDao.listByAccount(account.getId()));
        }
    }

    private void populateRemoteList() throws Exception
    {
        String remoteParentDomainId = null;
        DomainService domainService = allocateDomainService();
        RmapVO rmap = rmapDao.findBySource(localParentDomain.getUuid(), region.getId());
        if (rmap == null)
        {
            JSONObject domainJson = domainService.findDomain(localParentDomain.getLevel(), localParentDomain.getName(), localParentDomain.getPath());
            if (domainJson == null)
            {
                throw new Exception("The parent domain[" + localParentDomain.getPath() + "] cannot be found in the remote region.");
            }
            remoteParentDomainId = BaseService.getAttrValue(domainJson, "id");
        }
        else
        {
            remoteParentDomainId = rmap.getUuid();
        }
        AccountService accountService = allocateAccountService();
        JSONArray remoteAccounts = accountService.list(remoteParentDomainId);
        remoteAccountList = new ArrayList<JSONObject>();
        for(int idx = 0; idx < remoteAccounts.length(); idx++)
        {
            try
            {
                remoteAccountList.add(remoteAccounts.getJSONObject(idx));
            }
            catch(Exception ex)
            {

            }
        }
        UserService userService = allocateUserService();
        JSONArray remoteArray = userService.list(remoteParentDomainId, null);
        remoteList = new ArrayList<JSONObject>();
        for(int idx = 0; idx < remoteArray.length(); idx++)
        {
            try
            {
                remoteList.add(remoteArray.getJSONObject(idx));
            }
            catch(Exception ex)
            {

            }
        }
    }

    private AccountVO getAccount(UserVO user)
    {
        for (AccountVO account : localAccountList)
        {
            if (account.getId() == user.getAccountId()) return account;
        }
        return null;
    }

    private JSONObject getAccount(JSONObject userJson)
    {
        String accountName = BaseService.getAttrValue(userJson, "account");

        for (JSONObject accountJson : remoteAccountList)
        {
            String name = BaseService.getAttrValue(accountJson, "name");
            if (name.equals(accountName)) return accountJson;
        }
        return null;
    }

    private void syncAttributes(UserVO user, JSONObject remoteJson) throws Exception
    {
        try
        {
            if (compare(user, remoteJson))
            {
                return;
            }

            Date localDate = user.getModified();
            Date remoteDate = getDate(remoteJson, "modified");
            if (localDate == null || remoteDate == null)
            {
                s_logger.error("Can't syncAttributes because null date, local modified[" + localDate + "], remote modified[" + remoteDate + "]");
                return;
            }
            if (localDate.equals(remoteDate))   return;
            if (localDate.after(remoteDate))   return;

            localUserManager.update(user, remoteJson, remoteDate);
        }
        catch(Exception ex)
        {
            s_logger.error("Failed to synchronize users : " + ex.getStackTrace());
        }
    }

    protected LocalUserManager allocateLocalUserManager()
    {
        return new LocalUserManager();
    }

    protected RemoteUserEventProcessor allocateRemoteUserEventProcessor()
    {
        return new RemoteUserEventProcessor(hostName, endPoint, userName, password);
    }

    protected void expungeProcessedLocals()
    {
        for (UserVO user : processedLocalList)
        {
            if (!localList.contains(user))    continue;
            localList.remove(user);
        }
    }

    protected boolean compare(Object object, JSONObject jsonObject) throws Exception
    {
        UserVO user = (UserVO)object;

        try
        {
            String remoteName = BaseService.getAttrValue(jsonObject, "username");
            String remoteState = BaseService.getAttrValue(jsonObject, "state");
            if (!user.getUsername().equals(remoteName))   return false;
            if (!user.getState().toString().equals(remoteState)) return false;
            return true;
        }
        catch(Exception ex)
        {
            throw new Exception("Failed to compare users : " + ex.getStackTrace());
        }
    }

    public JSONObject findRemote(Object object)
    {
        UserVO user = (UserVO)object;
        AccountVO account = getAccount(user);

        RmapVO rmap = rmapDao.findBySource(user.getUuid(), region.getId());

        for (JSONObject jsonObject : remoteList)
        {
            JSONObject accountJson = getAccount(jsonObject);
            String accountName = BaseService.getAttrValue(accountJson, "name");
            String remoteName = BaseService.getAttrValue(jsonObject, "username");
            String remoteUuid = BaseService.getAttrValue(jsonObject, "id");

            if (rmap == null)
            {
                if (!account.getAccountName().equals(accountName))  continue;
                if (!user.getUsername().equals(remoteName))  continue;
            }
            else
            {
                if(!rmap.getUuid().equals(remoteUuid))    continue;
            }

            if (rmap == null)
            {
                rmap = new RmapVO(user.getUuid(), region.getId(), remoteUuid);
                rmapDao.create(rmap);
            }

            return jsonObject;
        }

        return null;
    }

    protected boolean synchronize(UserVO user) throws Exception
    {
        JSONObject remoteJson = findRemote(user);
        if (remoteJson == null) return false;

        // synchronize the attributes
        syncAttributes(user, remoteJson);

        processedLocalList.add(user);
        processedRemoteList.add(remoteJson);

        return true;
    }

    protected boolean synchronizeUsingEvent(UserVO user) throws Exception
    {
        JSONObject eventJson = eventProcessor.findLatestRemoteRemoveEvent(user);
        if (eventJson == null)  return false;

        Date eventDate = getDate(eventJson, "created");
        Date created = user.getCreated();
        if (created == null)
        {
            s_logger.error("Can't synchronizeUsingEvent because user created is null");
            return false;
        }
        if (eventDate.before(created))  return false;

        // remove this local
        localUserManager.remove(user, eventDate);

        processedLocalList.add(user);

        return true;
    }

    @Override
    protected void synchronizeByLocal()
    {
        for(UserVO user : localList)
        {
            try
            {
                boolean sync = synchronize(user);
                if (sync)
                {
                    s_logger.info("User[" + user.getUsername() + "] successfully synchronized");
                    continue;
                }
                s_logger.info("User[" + user.getUsername() + "] not synchronized");
            }
            catch(Exception ex)
            {
                s_logger.error("User[" + user.getUsername() + "] failed to synchronize : " + ex.getStackTrace());
            }
        }

        expungeProcessedLocals();
        expungeProcessedRemotes();

        for(UserVO user : localList)
        {
            try
            {
                boolean sync = synchronizeUsingEvent(user);
                if (sync)
                {
                    s_logger.info("User[" + user.getUsername() + "] successfully synchronized using events");

                    continue;
                }
                s_logger.info("User[" + user.getUsername() + "] not synchronized using events");
            }
            catch(Exception ex)
            {
                s_logger.error("User[" + user.getUsername() + "] failed to synchronize using events : " + ex.getStackTrace());
            }
        }

        expungeProcessedLocals();
        expungeProcessedRemotes();

    }

    protected boolean synchronizeUsingRemoved(JSONObject remoteJson) throws Exception
    {
        //String remotePath = BaseService.getAttrValue(remoteJson, "path");
        Date created = getDate(remoteJson, "created");
        if (created == null)
        {
            s_logger.error("Can't synchronizeUsingRemoved because remote created is null");
            return false;
        }

        UserVO removedUser = null;
        for (UserVO user : userDao.listAllIncludingRemoved())
        {
            Date removed = user.getRemoved();
            if (removed == null)    continue;

            AccountVO account = getAccount(user);
            if (account.getDomainId() != localParentDomain.getId())  continue;

            if (removedUser == null)
            {
                removedUser = user;
            }
            else
            {
                Date currentCreated = user.getCreated();
                if (currentCreated == null)
                {
                    s_logger.error("Can't synchronizeUsingRemoved because one of the removed user has null created");
                    return false;
                }
                else if (currentCreated.after(removedUser.getCreated()))
                {
                    removedUser = user;
                }
            }
        }

        Date removed = null;
        if (removedUser != null)
        {
            removed = removedUser.getRemoved();
        }
        if (removed == null || created.after(removed))
        {
            // create this remote in the local region
            String remoteUuid = BaseService.getAttrValue(remoteJson, "id");
            User user = (User)localUserManager.create(remoteJson, created);
            RmapVO rmap = new RmapVO(user.getUuid(), region.getId(), remoteUuid);
            rmapDao.create(rmap);
        }

        processedRemoteList.add(remoteJson);
        return true;
    }

    @Override
    protected void synchronizeByRemote()
    {
        for (JSONObject remoteJson : remoteList)
        {
            String name = BaseService.getAttrValue(remoteJson, "username");

            try
            {
                boolean sync = synchronizeUsingRemoved(remoteJson);
                if (sync)
                {
                    s_logger.info("UserJSON[" + name + "] successfully synchronized using events");
                    continue;
                }
                s_logger.info("UserJSON[" + name + "] not synchronized using events");
            }
            catch(Exception ex)
            {
                s_logger.error("UserJSON[" + name + "] failed to synchronize using events : " + ex.getStackTrace());
            }
        }

        expungeProcessedLocals();
        expungeProcessedRemotes();
    }

    @Override
    public void arrangeLocalResourcesToBeRemoved(FullSyncProcessor syncProcessor)
    {
        UserFullSyncProcessor userProcessor = (UserFullSyncProcessor)syncProcessor;

        for(int idx = localList.size()-1; idx >= 0; idx--)
        {
            UserVO user = localList.get(idx);
            for(UserVO processed : userProcessor.processedLocalList)
            {
                if (user.getId() != processed.getId())  continue;

                // move this user to the processed list
                processedLocalList.add(user);
                localList.remove(user);
                break;
            }
        }
    }

    @Override
    public void arrangeRemoteResourcesToBeCreated(FullSyncProcessor syncProcessor)
    {
        UserFullSyncProcessor userProcessor = (UserFullSyncProcessor)syncProcessor;

        for(int idx = remoteList.size()-1; idx >= 0; idx--)
        {
            JSONObject remoteJson = remoteList.get(idx);
            String name = BaseService.getAttrValue(remoteJson, "name");

            for(JSONObject processed : userProcessor.processedRemoteList)
            {
                String processedName = BaseService.getAttrValue(processed, "name");
                if (!name.equals(processedName))  continue;

                // move this user to the processed list
                processedRemoteList.add(remoteJson);
                remoteList.remove(remoteJson);
                break;
            }
        }
    }
}
