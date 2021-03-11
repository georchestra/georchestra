package org.fao.geonet.openapi;

import java.util.List;

import org.fao.geonet.ApiClient;
import org.fao.geonet.openapi.model.IsoLanguage;
import org.fao.geonet.openapi.model.Language;

import feign.Headers;
import feign.Param;
import feign.RequestLine;

public interface LanguagesApi extends ApiClient.Api {

    /**
     * Add a language Add all default translations from all *Desc tables in the
     * database. This operation will only add translations for a default catalog
     * installation. Defaults can be customized in SQL scripts located in
     * WEB-INF/classes/setup/sql/data/_*.
     * 
     * @param langCode ISO 3 letter code (required)
     */
    @RequestLine("PUT /srv/api/0.1/languages/{langCode}")
    @Headers({ "Accept: */*", })
    void addLanguage(@Param("langCode") String langCode);

    /**
     * Remove a language Delete all translations from all *Desc tables in the
     * database. Warning: This will also remove all translations you may have done
     * to those objects (eg. custom groups).
     * 
     * @param langCode ISO 3 letter code (required)
     */
    @RequestLine("DELETE /srv/api/0.1/languages/{langCode}")
    @Headers({ "Accept: */*", })
    void deleteLanguage(@Param("langCode") String langCode);

    /**
     * Get ISO languages ISO languages provides a list of all languages (eg. used
     * for autocompletion in metadata editor).
     * 
     * @return List&lt;IsoLanguage&gt;
     */
    @RequestLine("GET /srv/api/0.1/isolanguages")
    @Headers({ "Accept: application/json", })
    List<IsoLanguage> getIsoLanguages();

    /**
     * Get languages Languages for the application having translations in the
     * database. All tables with &#39;Desc&#39; suffix contains translation for some
     * domain objects like groups, tags, ...
     * 
     * @return List&lt;Language&gt;
     */
    @RequestLine("GET /srv/api/0.1/languages")
    @Headers({ "Accept: application/json", })
    List<Language> getLanguages();
}
