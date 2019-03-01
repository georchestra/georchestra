package org.georchestra.console.dto.orgs;

import org.georchestra.console.ds.OrgsDao;

public class OrgDetail extends AbstractOrg {

    private String url;
    private String id;


    @Override
    public OrgsDao.Extension getExtension(OrgsDao orgDao) {
        return orgDao.getExtension(this);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }
}
