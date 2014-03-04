package com.cloud.region.service;

import com.amazonaws.util.json.JSONArray;
import com.amazonaws.util.json.JSONObject;
import com.cloud.domain.dao.DomainDao;
import com.cloud.user.dao.AccountDao;
import com.cloud.user.dao.UserDao;
import com.cloud.utils.component.ComponentContext;

import java.util.Date;

public abstract class RemoteEventProcessor {

    protected String hostName;
    protected String endPoint;
    protected String userName;
    protected String password;

    public RemoteEventProcessor()
    {

    }

    protected BaseService allocateBaseService()
    {
        return new BaseService(hostName, endPoint, userName, password);
    }

    protected DomainDao getDomainDao()
    {
        return ComponentContext.getComponent(DomainDao.class);
    }

    protected AccountDao getAccountDao()
    {
        return ComponentContext.getComponent(AccountDao.class);
    }

    protected UserDao getUserDao()
    {
        return ComponentContext.getComponent(UserDao.class);
    }

    protected JSONArray listEvents(Date created, String eventType) throws Exception
    {
        BaseService baseService = allocateBaseService();
        return baseService.listEvents(eventType, "completed", created, null);
    }

    protected JSONObject getLatestEvent(JSONObject object1, JSONObject object2)
    {
        if (object1 == null && object2 == null) return null;
        if (object1 == null)    return object2;
        if (object2 == null)    return object1;

        Date date1 = BaseService.parseDateStr(BaseService.getAttrValue(object1, "created"));
        Date date2 = BaseService.parseDateStr(BaseService.getAttrValue(object2, "created"));

        if (date1 == null && date2 == null) return null;
        if (date1 == null)    return object2;
        if (date2 == null)    return object1;

        if (date1.equals(date2))    return null;
        if (date1.before(date2))    return object2;
        return object1;
    }

    abstract public JSONObject findLatestRemoteRemoveEvent(Object object) throws Exception;
}
