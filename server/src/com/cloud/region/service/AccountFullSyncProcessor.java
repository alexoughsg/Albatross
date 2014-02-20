package com.cloud.region.service;

import com.amazonaws.util.json.JSONArray;
import com.amazonaws.util.json.JSONObject;
import com.cloud.domain.DomainVO;
import com.cloud.domain.dao.DomainDao;
import com.cloud.rmap.RmapVO;
import com.cloud.user.Account;
import com.cloud.user.AccountVO;
import com.cloud.user.dao.AccountDao;
import com.cloud.utils.component.ComponentContext;
import org.apache.cloudstack.region.RegionVO;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AccountFullSyncProcessor extends FullSyncProcessor {

    private static final Logger s_logger = Logger.getLogger(AccountFullSyncProcessor.class);

    protected AccountDao accountDao;
    protected DomainDao domainDao;

    protected DomainVO localParent;
    protected List<AccountVO> localList;
    protected List<AccountVO> processedLocalList = new ArrayList<AccountVO>();

    private LocalAccountManager localAccountManager;
    private RemoteAccountEventProcessor eventProcessor;

    public AccountFullSyncProcessor(RegionVO region, DomainVO parentDomain) throws Exception
    {
        super(region);

        this.accountDao = ComponentContext.getComponent(AccountDao.class);
        this.domainDao = ComponentContext.getComponent(DomainDao.class);

        localParent = parentDomain;
        localList = accountDao.findActiveAccountsForDomain(localParent.getId());
        if (localParent.getName().equals("ROOT"))
        {
            for(int idx = localList.size()-1; idx >= 0; idx--)
            {
                AccountVO account = localList.get(idx);
                if (!account.getAccountName().equals("system"))   continue;
                localList.remove(account);
            }
        }

        String remoteParentDomainId = null;
        DomainService domainService = new DomainService(hostName, endPoint, userName, password);
        RmapVO rmap = rmapDao.findBySource(localParent.getUuid(), region.getId());
        if (rmap == null)
        {
            remoteParent = domainService.findDomain(localParent.getLevel(), localParent.getName(), localParent.getPath());
            if (remoteParent == null)
            {
                throw new Exception("The parent domain[" + localParent.getPath() + "] cannot be found in the remote region[" + hostName + "].");
            }
            remoteParentDomainId = BaseService.getAttrValue(remoteParent, "id");
            rmap = new RmapVO(localParent.getUuid(), region.getId(), remoteParentDomainId);
            rmapDao.create(rmap);
        }
        else
        {
            remoteParentDomainId = rmap.getUuid();
            remoteParent = domainService.findDomain(remoteParentDomainId);
            if (remoteParent == null)
            {
                throw new Exception("The parent domain[" + remoteParentDomainId + "] cannot be found in the remote region[" + hostName + "].");
            }
        }
        AccountService accountService = new AccountService(hostName, endPoint, userName, password);
        JSONArray remoteArray = accountService.list(remoteParentDomainId);
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

        localAccountManager = new LocalAccountManager();
        eventProcessor = new RemoteAccountEventProcessor(hostName, endPoint, userName, password);
    }

    private void syncAttributes(AccountVO account, JSONObject remoteJson) throws Exception
    {
        try
        {
            if (compare(account, remoteJson))
            {
                return;
            }

            Date localDate = account.getModified();
            Date remoteDate = getDate(remoteJson, "modified");
            if (localDate == null || remoteDate == null)
            {
                s_logger.info("Can't syncAttributes because null date, local modified[" + localDate + "], remote modified[" + remoteDate + "]");
                return;
            }
            if (localDate.equals(remoteDate))   return;
            if (localDate.after(remoteDate))   return;

            localAccountManager.update(account, remoteJson, remoteDate);
        }
        catch(Exception ex)
        {
            s_logger.error("Failed to synchronize accounts : " + ex.getStackTrace());
        }
    }

    protected void expungeProcessedLocals()
    {
        for (AccountVO account : processedLocalList)
        {
            if (!localList.contains(account))    continue;
            localList.remove(account);
        }
    }

    //@Override
    protected boolean compare(Object object, JSONObject jsonObject) throws Exception
    {
        AccountVO account = (AccountVO)object;

        try
        {
            String remoteName = BaseService.getAttrValue(jsonObject, "name");
            String remoteState = BaseService.getAttrValue(jsonObject, "state");
            String remoteNetworkDomain = BaseService.getAttrValue(jsonObject, "networkdomain");
            if (!account.getAccountName().equals(remoteName))   return false;
            if (!account.getState().toString().equals(remoteState)) return false;
            if (!strCompare(account.getNetworkDomain(), remoteNetworkDomain))   return false;
            return true;
        }
        catch(Exception ex)
        {
            throw new Exception("Failed to compare accounts : " + ex.getStackTrace());
        }
    }

    public JSONObject findRemote(Object object)
    {
        AccountVO account = (AccountVO)object;

        RmapVO rmap = rmapDao.findBySource(account.getUuid(), region.getId());

        for (JSONObject jsonObject : remoteList)
        {
            String remoteName = BaseService.getAttrValue(jsonObject, "name");
            String remoteUuid = BaseService.getAttrValue(jsonObject, "id");

            if (rmap == null)
            {
                if (!account.getAccountName().equals(remoteName))  continue;
            }
            else
            {
                if(!rmap.getUuid().equals(remoteUuid))    continue;
            }

            if (rmap == null)
            {
                rmap = new RmapVO(account.getUuid(), region.getId(), remoteUuid);
                rmapDao.create(rmap);
            }

            return jsonObject;
        }

        return null;
    }

    protected boolean synchronize(AccountVO account) throws Exception
    {
        JSONObject remoteJson = findRemote(account);
        if (remoteJson == null) return false;

        // synchronize the attributes
        syncAttributes(account, remoteJson);

        processedLocalList.add(account);
        processedRemoteList.add(remoteJson);

        return true;
    }

    protected boolean synchronizeUsingEvent(AccountVO account) throws Exception
    {
        JSONObject eventJson = eventProcessor.findLatestRemoteRemoveEvent(account);
        if (eventJson == null)  return false;

        Date eventDate = getDate(eventJson, "created");
        Date created = account.getCreated();
        if (created == null)
        {
            s_logger.info("Can't synchronizeUsingEvent because account created is null");
            return false;
        }
        if (eventDate.before(created))  return false;

        // remove this local
        localAccountManager.remove(account, eventDate);

        processedLocalList.add(account);

        return true;
    }

    @Override
    protected void synchronizeByLocal()
    {
        for(AccountVO account : localList)
        {
            try
            {
                boolean sync = synchronize(account);
                if (sync)
                {
                    s_logger.info("Account[" + account.getAccountName() + "] successfully synchronized");
                    continue;
                }
                s_logger.info("Account[" + account.getAccountName() + "] not synchronized");
            }
            catch(Exception ex)
            {
                s_logger.error("Account[" + account.getAccountName() + "] failed to synchronize : " + ex.getStackTrace());
            }
        }

        expungeProcessedLocals();
        expungeProcessedRemotes();

        for(AccountVO account : localList)
        {
            try
            {
                boolean sync = synchronizeUsingEvent(account);
                if (sync)
                {
                    s_logger.info("Account[" + account.getAccountName() + "] successfully synchronized using events");

                    continue;
                }
                s_logger.info("Account[" + account.getAccountName() + "] not synchronized using events");
            }
            catch(Exception ex)
            {
                s_logger.error("Account[" + account.getAccountName() + "] failed to synchronize using events : " + ex.getStackTrace());
            }
        }

        expungeProcessedLocals();
        expungeProcessedRemotes();
    }

    protected boolean synchronizeUsingRemoved(JSONObject remoteJson) throws Exception
    {
        String remotePath = BaseService.getAttrValue(remoteJson, "path");
        Date created = getDate(remoteJson, "created");
        if (created == null)
        {
            s_logger.info("Can't synchronizeUsingRemoved because remote created is null");
            return false;
        }

        AccountVO removedAccount = null;
        for (AccountVO account : accountDao.listAllIncludingRemoved())
        {
            Date removed = account.getRemoved();
            if (removed == null)    continue;

            if (account.getDomainId() != localParent.getId())  continue;

            if (removedAccount == null)
            {
                removedAccount = account;
            }
            else
            {
                Date currentCreated = account.getCreated();
                if (currentCreated == null)
                {
                    s_logger.info("Can't synchronizeUsingRemoved because one of the removed account has null created");
                    return false;
                }
                else if (currentCreated.after(removedAccount.getCreated()))
                {
                    removedAccount = account;
                }
            }
        }

        Date removed = null;
        if (removedAccount != null)
        {
            removed = removedAccount.getRemoved();
        }
        if (removed == null || created.after(removed))
        {
            // create this remote in the local region
            String remoteUuid = BaseService.getAttrValue(remoteJson, "id");
            AccountVO account = (AccountVO)localAccountManager.create(remoteJson, created);
            RmapVO rmap = new RmapVO(account.getUuid(), region.getId(), remoteUuid);
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
            String name = BaseService.getAttrValue(remoteJson, "name");

            try
            {
                boolean sync = synchronizeUsingRemoved(remoteJson);
                if (sync)
                {
                    s_logger.info("AccountJSON[" + name + "] successfully synchronized using events");
                    continue;
                }
                s_logger.info("AccountJSON[" + name + "] not synchronized using events");
            }
            catch(Exception ex)
            {
                s_logger.error("AccountJSON[" + name + "] failed to synchronize using events : " + ex.getStackTrace());
            }
        }

        expungeProcessedLocals();
        expungeProcessedRemotes();
    }

    @Override
    public void arrangeLocalResourcesToBeRemoved(FullSyncProcessor syncProcessor)
    {
        AccountFullSyncProcessor accountProcessor = (AccountFullSyncProcessor)syncProcessor;

        for(int idx = localList.size()-1; idx >= 0; idx--)
        {
            AccountVO account = localList.get(idx);
            for(AccountVO processed : accountProcessor.processedLocalList)
            {
                if (account.getId() != processed.getId())  continue;

                // move this account to the processed list
                processedLocalList.add(account);
                localList.remove(account);
                break;
            }
        }
    }

    @Override
    public void arrangeRemoteResourcesToBeCreated(FullSyncProcessor syncProcessor)
    {
        AccountFullSyncProcessor accountProcessor = (AccountFullSyncProcessor)syncProcessor;

        for(int idx = remoteList.size()-1; idx >= 0; idx--)
        {
            JSONObject remoteJson = remoteList.get(idx);
            String name = BaseService.getAttrValue(remoteJson, "name");

            for(JSONObject processed : accountProcessor.processedRemoteList)
            {
                String processedName = BaseService.getAttrValue(processed, "name");
                if (!name.equals(processedName))  continue;

                // move this account to the processed list
                processedRemoteList.add(remoteJson);
                remoteList.remove(remoteJson);
                break;
            }
        }
    }
}
