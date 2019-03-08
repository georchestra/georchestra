package org.georchestra.console.dto.orgs;

import org.georchestra.console.ds.OrgsDao;

public class OrgDetail extends AbstractOrg {

    private String url;
    private String id;
    private String logo;

    @Override
    public OrgsDao.Extension<OrgDetail> getExtension(OrgsDao orgDao) {
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


    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }
}
