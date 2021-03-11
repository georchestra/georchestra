package org.fao.geonet.openapi;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.fao.geonet.ApiClient;
import org.fao.geonet.EncodingUtils;
import org.fao.geonet.openapi.model.PageJSONWrapper;

import feign.Headers;
import feign.Param;
import feign.QueryMap;
import feign.RequestLine;

public interface PagesApi extends ApiClient.Api {

    /**
     * Add a new Page object in DRAFT section in status HIDDEN &lt;p&gt;Is not
     * possible to load a link and a file at the same time.&lt;/p&gt; &lt;a
     * href&#x3D;&#39;http://geonetwork-opensource.org/manuals/trunk/eng/users/user-guide/define-static-pages/define-pages.html&#39;&gt;More
     * info&lt;/a&gt;
     * 
     * @param language language (required)
     * @param pageId   pageId (required)
     * @param format   format (required)
     * @param data     data (optional)
     * @param link     link (optional)
     */
    @RequestLine("POST /srv/api/0.1/pages/?language={language}&pageId={pageId}&link={link}&format={format}")
    @Headers({ "Content-Type: multipart/form-data", "Accept: */*", })
    void addPage(@Param("language") String language, @Param("pageId") String pageId, @Param("format") String format,
            @Param("data") File data, @Param("link") String link);

    /**
     * Add a new Page object in DRAFT section in status HIDDEN &lt;p&gt;Is not
     * possible to load a link and a file at the same time.&lt;/p&gt; &lt;a
     * href&#x3D;&#39;http://geonetwork-opensource.org/manuals/trunk/eng/users/user-guide/define-static-pages/define-pages.html&#39;&gt;More
     * info&lt;/a&gt; Note, this is equivalent to the other <code>addPage</code>
     * method, but with the query parameters collected into a single Map parameter.
     * This is convenient for services with optional query parameters, especially
     * when used with the {@link AddPageQueryParams} class that allows for building
     * up this map in a fluent style.
     * 
     * @param data        data (optional)
     * @param queryParams Map of query parameters as name-value pairs
     *                    <p>
     *                    The following elements may be specified in the query map:
     *                    </p>
     *                    <ul>
     *                    <li>language - language (required)</li>
     *                    <li>pageId - pageId (required)</li>
     *                    <li>link - link (optional)</li>
     *                    <li>format - format (required)</li>
     *                    </ul>
     */
    @RequestLine("POST /srv/api/0.1/pages/?language={language}&pageId={pageId}&link={link}&format={format}")
    @Headers({ "Content-Type: multipart/form-data", "Accept: */*", })
    void addPage(@Param("data") File data, @QueryMap(encoded = true) Map<String, Object> queryParams);

    /**
     * A convenience class for generating query parameters for the
     * <code>addPage</code> method in a fluent style.
     */
    public static class AddPageQueryParams extends HashMap<String, Object> {
        public AddPageQueryParams language(final String value) {
            put("language", EncodingUtils.encode(value));
            return this;
        }

        public AddPageQueryParams pageId(final String value) {
            put("pageId", EncodingUtils.encode(value));
            return this;
        }

        public AddPageQueryParams link(final String value) {
            put("link", EncodingUtils.encode(value));
            return this;
        }

        public AddPageQueryParams format(final String value) {
            put("format", EncodingUtils.encode(value));
            return this;
        }
    }

    /**
     * Adds the page to a section. This means that the link to the page will be
     * shown in the list associated to that section. &lt;a
     * href&#x3D;&#39;http://geonetwork-opensource.org/manuals/trunk/eng/users/user-guide/define-static-pages/define-pages.html&#39;&gt;More
     * info&lt;/a&gt;
     * 
     * @param language language (required)
     * @param pageId   pageId (required)
     * @param section  section (required)
     */
    @RequestLine("POST /srv/api/0.1/pages/{language}/{pageId}/{section}")
    @Headers({ "Accept: */*", })
    void addPageToSection(@Param("language") String language, @Param("pageId") String pageId,
            @Param("section") String section);

    /**
     * Delete a Page object &lt;a
     * href&#x3D;&#39;http://geonetwork-opensource.org/manuals/trunk/eng/users/user-guide/define-static-pages/define-pages.html&#39;&gt;More
     * info&lt;/a&gt;
     * 
     * @param language language (required)
     * @param pageId   pageId (required)
     * @param format   format (required)
     */
    @RequestLine("DELETE /srv/api/0.1/pages/{language}/{pageId}?format={format}")
    @Headers({ "Accept: */*", })
    void deletePage(@Param("language") String language, @Param("pageId") String pageId, @Param("format") String format);

    /**
     * Delete a Page object &lt;a
     * href&#x3D;&#39;http://geonetwork-opensource.org/manuals/trunk/eng/users/user-guide/define-static-pages/define-pages.html&#39;&gt;More
     * info&lt;/a&gt; Note, this is equivalent to the other <code>deletePage</code>
     * method, but with the query parameters collected into a single Map parameter.
     * This is convenient for services with optional query parameters, especially
     * when used with the {@link DeletePageQueryParams} class that allows for
     * building up this map in a fluent style.
     * 
     * @param language    language (required)
     * @param pageId      pageId (required)
     * @param queryParams Map of query parameters as name-value pairs
     *                    <p>
     *                    The following elements may be specified in the query map:
     *                    </p>
     *                    <ul>
     *                    <li>format - format (required)</li>
     *                    </ul>
     */
    @RequestLine("DELETE /srv/api/0.1/pages/{language}/{pageId}?format={format}")
    @Headers({ "Accept: */*", })
    void deletePage(@Param("language") String language, @Param("pageId") String pageId,
            @QueryMap(encoded = true) Map<String, Object> queryParams);

    /**
     * A convenience class for generating query parameters for the
     * <code>deletePage</code> method in a fluent style.
     */
    public static class DeletePageQueryParams extends HashMap<String, Object> {
        public DeletePageQueryParams format(final String value) {
            put("format", EncodingUtils.encode(value));
            return this;
        }
    }

    /**
     * Edit a Page content and format &lt;a
     * href&#x3D;&#39;http://geonetwork-opensource.org/manuals/trunk/eng/users/user-guide/define-static-pages/define-pages.html&#39;&gt;More
     * info&lt;/a&gt;
     * 
     * @param language language (required)
     * @param pageId   pageId (required)
     * @param format   format (required)
     * @param data     data (optional)
     * @param link     link (optional)
     */
    @RequestLine("POST /srv/api/0.1/pages/{language}/{pageId}?link={link}&format={format}")
    @Headers({ "Content-Type: multipart/form-data", "Accept: */*", })
    void editPage(@Param("language") String language, @Param("pageId") String pageId, @Param("format") String format,
            @Param("data") File data, @Param("link") String link);

    /**
     * Edit a Page content and format &lt;a
     * href&#x3D;&#39;http://geonetwork-opensource.org/manuals/trunk/eng/users/user-guide/define-static-pages/define-pages.html&#39;&gt;More
     * info&lt;/a&gt; Note, this is equivalent to the other <code>editPage</code>
     * method, but with the query parameters collected into a single Map parameter.
     * This is convenient for services with optional query parameters, especially
     * when used with the {@link EditPageQueryParams} class that allows for building
     * up this map in a fluent style.
     * 
     * @param language    language (required)
     * @param pageId      pageId (required)
     * @param data        data (optional)
     * @param queryParams Map of query parameters as name-value pairs
     *                    <p>
     *                    The following elements may be specified in the query map:
     *                    </p>
     *                    <ul>
     *                    <li>link - link (optional)</li>
     *                    <li>format - format (required)</li>
     *                    </ul>
     */
    @RequestLine("POST /srv/api/0.1/pages/{language}/{pageId}?link={link}&format={format}")
    @Headers({ "Content-Type: multipart/form-data", "Accept: */*", })
    void editPage(@Param("language") String language, @Param("pageId") String pageId, @Param("data") File data,
            @QueryMap(encoded = true) Map<String, Object> queryParams);

    /**
     * A convenience class for generating query parameters for the
     * <code>editPage</code> method in a fluent style.
     */
    public static class EditPageQueryParams extends HashMap<String, Object> {
        public EditPageQueryParams link(final String value) {
            put("link", EncodingUtils.encode(value));
            return this;
        }

        public EditPageQueryParams format(final String value) {
            put("format", EncodingUtils.encode(value));
            return this;
        }
    }

    /**
     * Edit a Page name and language &lt;a
     * href&#x3D;&#39;http://geonetwork-opensource.org/manuals/trunk/eng/users/user-guide/define-static-pages/define-pages.html&#39;&gt;More
     * info&lt;/a&gt;
     * 
     * @param language    language (required)
     * @param pageId      pageId (required)
     * @param newLanguage newLanguage (optional)
     * @param newPageId   newPageId (optional)
     */
    @RequestLine("PUT /srv/api/0.1/pages/{language}/{pageId}?newLanguage={newLanguage}&newPageId={newPageId}")
    @Headers({ "Accept: */*", })
    void editPage1(@Param("language") String language, @Param("pageId") String pageId,
            @Param("newLanguage") String newLanguage, @Param("newPageId") String newPageId);

    /**
     * Edit a Page name and language &lt;a
     * href&#x3D;&#39;http://geonetwork-opensource.org/manuals/trunk/eng/users/user-guide/define-static-pages/define-pages.html&#39;&gt;More
     * info&lt;/a&gt; Note, this is equivalent to the other <code>editPage1</code>
     * method, but with the query parameters collected into a single Map parameter.
     * This is convenient for services with optional query parameters, especially
     * when used with the {@link EditPage1QueryParams} class that allows for
     * building up this map in a fluent style.
     * 
     * @param language    language (required)
     * @param pageId      pageId (required)
     * @param queryParams Map of query parameters as name-value pairs
     *                    <p>
     *                    The following elements may be specified in the query map:
     *                    </p>
     *                    <ul>
     *                    <li>newLanguage - newLanguage (optional)</li>
     *                    <li>newPageId - newPageId (optional)</li>
     *                    </ul>
     */
    @RequestLine("PUT /srv/api/0.1/pages/{language}/{pageId}?newLanguage={newLanguage}&newPageId={newPageId}")
    @Headers({ "Accept: */*", })
    void editPage1(@Param("language") String language, @Param("pageId") String pageId,
            @QueryMap(encoded = true) Map<String, Object> queryParams);

    /**
     * A convenience class for generating query parameters for the
     * <code>editPage1</code> method in a fluent style.
     */
    public static class EditPage1QueryParams extends HashMap<String, Object> {
        public EditPage1QueryParams newLanguage(final String value) {
            put("newLanguage", EncodingUtils.encode(value));
            return this;
        }

        public EditPage1QueryParams newPageId(final String value) {
            put("newPageId", EncodingUtils.encode(value));
            return this;
        }
    }

    /**
     * Return the page object details except the content &lt;a
     * href&#x3D;&#39;http://geonetwork-opensource.org/manuals/trunk/eng/users/user-guide/define-static-pages/define-pages.html&#39;&gt;More
     * info&lt;/a&gt;
     * 
     * @param language language (required)
     * @param pageId   pageId (required)
     * @return PageJSONWrapper
     */
    @RequestLine("GET /srv/api/0.1/pages/{language}/{pageId}")
    @Headers({ "Accept: application/json", })
    PageJSONWrapper getPage(@Param("language") String language, @Param("pageId") String pageId);

    /**
     * Return the static html content identified by pageId &lt;a
     * href&#x3D;&#39;http://geonetwork-opensource.org/manuals/trunk/eng/users/user-guide/define-static-pages/define-pages.html&#39;&gt;More
     * info&lt;/a&gt;
     * 
     * @param language language (required)
     * @param pageId   pageId (required)
     * @return String
     */
    @RequestLine("GET /srv/api/0.1/pages/{language}/{pageId}/content")
    @Headers({ "Accept: text/plain;charset&#x3D;UTF-8", })
    String getPage1(@Param("language") String language, @Param("pageId") String pageId);

    /**
     * List all pages according to the filters &lt;a
     * href&#x3D;&#39;http://geonetwork-opensource.org/manuals/trunk/eng/users/user-guide/define-static-pages/define-pages.html&#39;&gt;More
     * info&lt;/a&gt;
     * 
     * @param language language (optional)
     * @param section  section (optional)
     * @param format   format (optional)
     * @return List&lt;PageJSONWrapper&gt;
     */
    @RequestLine("GET /srv/api/0.1/pages/list?language={language}&section={section}&format={format}")
    @Headers({ "Accept: application/json", })
    List<PageJSONWrapper> listPages(@Param("language") String language, @Param("section") String section,
            @Param("format") String format);

    /**
     * List all pages according to the filters &lt;a
     * href&#x3D;&#39;http://geonetwork-opensource.org/manuals/trunk/eng/users/user-guide/define-static-pages/define-pages.html&#39;&gt;More
     * info&lt;/a&gt; Note, this is equivalent to the other <code>listPages</code>
     * method, but with the query parameters collected into a single Map parameter.
     * This is convenient for services with optional query parameters, especially
     * when used with the {@link ListPagesQueryParams} class that allows for
     * building up this map in a fluent style.
     * 
     * @param queryParams Map of query parameters as name-value pairs
     *                    <p>
     *                    The following elements may be specified in the query map:
     *                    </p>
     *                    <ul>
     *                    <li>language - language (optional)</li>
     *                    <li>section - section (optional)</li>
     *                    <li>format - format (optional)</li>
     *                    </ul>
     * @return List&lt;PageJSONWrapper&gt;
     */
    @RequestLine("GET /srv/api/0.1/pages/list?language={language}&section={section}&format={format}")
    @Headers({ "Accept: application/json", })
    List<PageJSONWrapper> listPages(@QueryMap(encoded = true) Map<String, Object> queryParams);

    /**
     * A convenience class for generating query parameters for the
     * <code>listPages</code> method in a fluent style.
     */
    public static class ListPagesQueryParams extends HashMap<String, Object> {
        public ListPagesQueryParams language(final String value) {
            put("language", EncodingUtils.encode(value));
            return this;
        }

        public ListPagesQueryParams section(final String value) {
            put("section", EncodingUtils.encode(value));
            return this;
        }

        public ListPagesQueryParams format(final String value) {
            put("format", EncodingUtils.encode(value));
            return this;
        }
    }

    /**
     * Changes the status of a page. &lt;a
     * href&#x3D;&#39;http://geonetwork-opensource.org/manuals/trunk/eng/users/user-guide/define-static-pages/define-pages.html&#39;&gt;More
     * info&lt;/a&gt;
     * 
     * @param language language (required)
     * @param pageId   pageId (required)
     * @param status   status (required)
     */
    @RequestLine("PUT /srv/api/0.1/pages/{language}/{pageId}/{status}")
    @Headers({ "Accept: */*", })
    void removePageFromSection(@Param("language") String language, @Param("pageId") String pageId,
            @Param("status") String status);

    /**
     * Removes the page from a section. This means that the link to the page will
     * not be shown in the list associated to that section. &lt;a
     * href&#x3D;&#39;http://geonetwork-opensource.org/manuals/trunk/eng/users/user-guide/define-static-pages/define-pages.html&#39;&gt;More
     * info&lt;/a&gt;
     * 
     * @param language language (required)
     * @param pageId   pageId (required)
     * @param section  section (required)
     */
    @RequestLine("DELETE /srv/api/0.1/pages/{language}/{pageId}/{section}")
    @Headers({ "Accept: */*", })
    void removePageFromSection1(@Param("language") String language, @Param("pageId") String pageId,
            @Param("section") String section);
}
