# UPGRADING from 13.06 to 13.09

 * mapfishapp:
   * default projection changes from EPSG:2154 to EPSG:3857 (aka Spherical Web Mercator). Your users might need to clear their localStorage, or force loading of the new default context.
   * default MAP_SCALES changes to match the OGC WMTS spec,
 * LDAP: see [georchestra/LDAP#2](https://github.com/georchestra/LDAP/pull/2)
   * one group was renamed: ```STAT_USER``` becomes ```MOD_ANALYTICS``` - grants access to the analytics app,
   * an other one was created: ```MOD_LDAPADMIN``` - grants access to the LDAPadmin private UI (/ldapadmin/privateui/).
 * The default application language is now **English**:
   * ```shared.language``` = en
   * ```geonetwork.language``` = eng
   * default email templates [here](https://github.com/georchestra/georchestra/tree/master/config/defaults/ldapadmin/WEB-INF/templates) and [there](https://github.com/georchestra/georchestra/tree/master/config/defaults/extractorapp/WEB-INF/templates): be sure to override them in your own config !
 * Remember also to fill these new global maven filters:
   * ```shared.homepage.url``` - for your SDI home page (might be something like http://my.sdi.org/portal/),
   * ```shared.instance.name``` - will be displayed in page titles (eg: GeoMyCompany),
   * ```shared.email.html``` - whether to send emails in plain text (default) or HTML,
   * ```shared.administrator.email``` - this email receives new account requests (eg: me@mycompany.com)
 * shared maven filters renamed:
   * ```shared.smtp.replyTo``` -> ```shared.email.replyTo```
   * ```shared.smtp.from``` -> ```shared.email.from```
 * frontend webserver:
   * add a proxy rule for `/_static/` subdirectory (see https://github.com/georchestra/georchestra/tree/master/INSTALL.md)
