package com.cloud.region.service;

import com.amazonaws.util.json.JSONArray;
import com.amazonaws.util.json.JSONObject;
import com.cloud.domain.Domain;
import com.cloud.domain.DomainVO;
import com.cloud.domain.dao.DomainDao;
import com.cloud.rmap.RmapVO;
import com.cloud.utils.component.ComponentContext;
import org.apache.cloudstack.region.RegionVO;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DomainFullSyncProcessor extends FullSyncProcessor {

    private static final Logger s_logger = Logger.getLogger(DomainFullSyncProcessor.class);

    protected DomainDao domainDao;

    protected DomainVO localParent;
    protected List<DomainVO> localList;
    protected List<DomainVO> processedLocalList = new ArrayList<DomainVO>();

    private LocalDomainManager localDomainManager;
    private RemoteDomainEventProcessor eventProcessor;

    public DomainFullSyncProcessor(RegionVO region, DomainVO parentDomain) throws Exception
    {
        super(region);

        this.domainDao = ComponentContext.getComponent(DomainDao.class);

        localParent = parentDomain;
        localList = domainDao.findImmediateChildrenForParent(localParent.getId());
        for(int idx = localList.size()-1; idx >= 0; idx--)
        {
            DomainVO domain = localList.get(idx);
            if (!domain.getState().equals(Domain.State.Inactive))   continue;
            localList.remove(domain);
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
        JSONArray remoteArray = domainService.listChildren(remoteParentDomainId, false);
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

        localDomainManager = new LocalDomainManager();
        eventProcessor = new RemoteDomainEventProcessor(hostName, endPoint, userName, password);
    }

    private void syncAttributes(DomainVO domain, JSONObject remoteJson) throws Exception
    {
        try
        {
            if (compare(domain, remoteJson))
            {
                return;
            }

            Date localDate = domain.getModified();
            Date remoteDate = getDate(remoteJson, "modified");
            if (localDate == null || remoteDate == null)
            {
                s_logger.info("Can't syncAttributes because null date, local modified[" + localDate + "], remote modified[" + remoteDate + "]");
                return;
            }
            if (localDate.equals(remoteDate))   return;
            if (localDate.after(remoteDate))   return;

            localDomainManager.update(domain, remoteJson, remoteDate);
        }
        catch(Exception ex)
        {
            s_logger.error("Failed to synchronize domains : " + ex.getStackTrace());
        }
    }

    protected void expungeProcessedLocals()
    {
        for (DomainVO domain : processedLocalList)
        {
            if (!localList.contains(domain))    continue;
            localList.remove(domain);
        }
    }

    protected boolean compare(Object object, JSONObject jsonObject) throws Exception
    {
        DomainVO domain = (DomainVO)object;

        try
        {
            String remoteName = BaseService.getAttrValue(jsonObject, "name");
            String remoteNetworkDomain = BaseService.getAttrValue(jsonObject, "networkdomain");
            if (!domain.getName().equals(remoteName))   return false;
            if (!domain.getState().equals(Domain.State.Active)) return false;
            if (!strCompare(domain.getNetworkDomain(), remoteNetworkDomain))   return false;
            return true;
        }
        catch(Exception ex)
        {
            throw new Exception("Failed to compare domains : " + ex.getStackTrace());
        }
    }

    public JSONObject findRemote(Object object)
    {
        DomainVO domain = (DomainVO)object;
        String localPath = domain.getPath();

        RmapVO rmap = rmapDao.findBySource(domain.getUuid(), region.getId());

        for (JSONObject jsonObject : remoteList)
        {
            String remotePath = BaseService.getAttrValue(jsonObject, "path");
            String remoteUuid = BaseService.getAttrValue(jsonObject, "id");

            if (rmap == null)
            {
                if (!BaseService.compareDomainPath(localPath, remotePath))  continue;
            }
            else
            {
                if(!rmap.getUuid().equals(remoteUuid))    continue;
            }

            if (rmap == null)
            {
                rmap = new RmapVO(domain.getUuid(), region.getId(), remoteUuid);
                rmapDao.create(rmap);
            }

            return jsonObject;
        }

        return null;
    }

    protected boolean synchronize(DomainVO domain) throws Exception
    {
        JSONObject remoteJson = findRemote(domain);
        if (remoteJson == null) return false;

        // synchronize the attributes
        syncAttributes(domain, remoteJson);

        processedLocalList.add(domain);
        processedRemoteList.add(remoteJson);

        return true;
    }

    protected boolean synchronizeUsingEvent(DomainVO domain) throws Exception
    {
        JSONObject eventJson = eventProcessor.findLatestRemoteRemoveEvent(domain);
        if (eventJson == null)  return false;

        Date eventDate = getDate(eventJson, "created");
        Date created = domain.getCreated();
        if (created == null)
        {
            s_logger.error("Can't synchronizeUsingEvent because domain created is null");
            return false;
        }
        if (eventDate.before(created))  return false;

        // remove this local
        localDomainManager.remove(domain, eventDate);

        processedLocalList.add(domain);

        return true;
    }

    protected void synchronizeByLocal()
    {
        for(DomainVO domain : localList)
        {
            try
            {
                boolean sync = synchronize(domain);
                if (sync)
                {
                    s_logger.info("Domain[" + domain.getPath() + "] successfully synchronized");
                    continue;
                }
                s_logger.info("Domain[" + domain.getPath() + "] not synchronized");
            }
            catch(Exception ex)
            {
                s_logger.error("Domain[" + domain.getPath() + "] failed to synchronize : " + ex.getStackTrace());
            }
        }

        expungeProcessedLocals();
        expungeProcessedRemotes();

        for(DomainVO domain : localList)
        {
            try
            {
                boolean sync = synchronizeUsingEvent(domain);
                if (sync)
                {
                    s_logger.info("Domain[" + domain.getPath() + "] successfully synchronized using events");

                    continue;
                }
                s_logger.info("Domain[" + domain.getPath() + "] not synchronized using events");
            }
            catch(Exception ex)
            {
                s_logger.error("Domain[" + domain.getPath() + "] failed to synchronize using events : " + ex.getStackTrace());
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
            s_logger.error("Can't synchronizeUsingRemoved because remote created is null");
            return false;
        }

        DomainVO removedDomain = null;
        for (DomainVO domain : domainDao.listAllIncludingRemoved())
        {
            Date removed = domain.getRemoved();
            if (removed == null)    continue;

            if (!BaseService.compareDomainPath(domain.getPath(), remotePath))  continue;

            if (removedDomain == null)
            {
                removedDomain = domain;
            }
            else
            {
                Date currentCreated = domain.getCreated();
                if (currentCreated == null)
                {
                    s_logger.error("Can't synchronizeUsingRemoved because one of the removed domain has null created");
                    return false;
                }
                else if (currentCreated.after(removedDomain.getCreated()))
                {
                    removedDomain = domain;
                }
            }
        }

        Date removed = null;
        if (removedDomain != null)
        {
            removed = removedDomain.getRemoved();
        }
        if (removed == null || created.after(removed))
        {
            // create this remote in the local region
            String parentPath = BaseService.getAttrValue(remoteParent, "path");
            String remoteUuid = BaseService.getAttrValue(remoteJson, "id");
            DomainVO domain = (DomainVO)localDomainManager.create(remoteJson, parentPath, created);
            RmapVO rmap = new RmapVO(domain.getUuid(), region.getId(), remoteUuid);
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
            String domainPath = BaseService.getAttrValue(remoteJson, "path");

            try
            {
                boolean sync = synchronizeUsingRemoved(remoteJson);
                if (sync)
                {
                    s_logger.info("DomainJSON[" + domainPath + "] successfully synchronized using events");
                    continue;
                }
                s_logger.info("DomainJSON[" + domainPath + "] not synchronized using events");
            }
            catch(Exception ex)
            {
                s_logger.error("DomainJSON[" + domainPath + "] failed to synchronize using events : " + ex.getStackTrace());
            }
        }

        expungeProcessedLocals();
        expungeProcessedRemotes();
    }

    @Override
    public void arrangeLocalResourcesToBeRemoved(FullSyncProcessor syncProcessor)
    {
        DomainFullSyncProcessor domainProcessor = (DomainFullSyncProcessor)syncProcessor;

        for(int idx = localList.size()-1; idx >= 0; idx--)
        {
            DomainVO domain = localList.get(idx);
            for(DomainVO processed : domainProcessor.processedLocalList)
            {
                if (domain.getId() != processed.getId())  continue;

                // move this domain to the processed list
                processedLocalList.add(domain);
                localList.remove(domain);
                break;
            }
        }
    }

    @Override
    public void arrangeRemoteResourcesToBeCreated(FullSyncProcessor syncProcessor)
    {
        DomainFullSyncProcessor domainProcessor = (DomainFullSyncProcessor)syncProcessor;

        for(int idx = remoteList.size()-1; idx >= 0; idx--)
        {
            JSONObject remoteJson = remoteList.get(idx);
            String path = BaseService.getAttrValue(remoteJson, "path");

            for(JSONObject processed : domainProcessor.processedRemoteList)
            {
                String processedPath = BaseService.getAttrValue(processed, "path");
                if (!path.equals(processedPath))  continue;

                // move this domain to the processed list
                processedRemoteList.add(remoteJson);
                remoteList.remove(remoteJson);
                break;
            }
        }
    }
}
