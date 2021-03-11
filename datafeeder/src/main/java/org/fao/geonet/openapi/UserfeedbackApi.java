package org.fao.geonet.openapi;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.fao.geonet.ApiClient;
import org.fao.geonet.EncodingUtils;
import org.fao.geonet.openapi.model.RatingAverage;
import org.fao.geonet.openapi.model.RatingCriteria;
import org.fao.geonet.openapi.model.ResponseEntity;
import org.fao.geonet.openapi.model.UserFeedbackDTO;

import feign.Headers;
import feign.Param;
import feign.QueryMap;
import feign.RequestLine;

public interface UserfeedbackApi extends ApiClient.Api {

    /**
     * Removes a user feedback Removes a user feedback
     * 
     * @param uuid User feedback UUID. (required)
     * @return ResponseEntity
     */
    @RequestLine("DELETE /srv/api/0.1/userfeedback/{uuid}")
    @Headers({ "Accept: application/json", })
    ResponseEntity deleteUserFeedback(@Param("uuid") String uuid);

    /**
     * Provides an average rating for a metadata record
     * 
     * @param metadataUuid Metadata record UUID. (required)
     * @return RatingAverage
     */
    @RequestLine("GET /srv/api/0.1/records/{metadataUuid}/userfeedbackrating")
    @Headers({ "Accept: application/json", })
    RatingAverage getMetadataUserComments(@Param("metadataUuid") String metadataUuid);

    /**
     * Get list of rating criteria
     * 
     * @return List&lt;RatingCriteria&gt;
     */
    @RequestLine("GET /srv/api/0.1/userfeedback/ratingcriteria")
    @Headers({ "Accept: application/json", })
    List<RatingCriteria> getRatingCriteria();

    /**
     * Finds a list of user feedback records. This list will include also the draft
     * user feedback if the client is logged as reviewer.
     * 
     * @param metadataUuid Metadata record UUID. (optional)
     * @param size         Maximum number of feedback to return. (optional, default
     *                     to -1)
     * @return List&lt;UserFeedbackDTO&gt;
     */
    @RequestLine("GET /srv/api/0.1/userfeedback?metadataUuid={metadataUuid}&size={size}")
    @Headers({ "Accept: application/json", })
    List<UserFeedbackDTO> getUserComments(@Param("metadataUuid") String metadataUuid, @Param("size") Integer size);

    /**
     * Finds a list of user feedback records. This list will include also the draft
     * user feedback if the client is logged as reviewer. Note, this is equivalent
     * to the other <code>getUserComments</code> method, but with the query
     * parameters collected into a single Map parameter. This is convenient for
     * services with optional query parameters, especially when used with the
     * {@link GetUserCommentsQueryParams} class that allows for building up this map
     * in a fluent style.
     * 
     * @param queryParams Map of query parameters as name-value pairs
     *                    <p>
     *                    The following elements may be specified in the query map:
     *                    </p>
     *                    <ul>
     *                    <li>metadataUuid - Metadata record UUID. (optional)</li>
     *                    <li>size - Maximum number of feedback to return.
     *                    (optional, default to -1)</li>
     *                    </ul>
     * @return List&lt;UserFeedbackDTO&gt;
     */
    @RequestLine("GET /srv/api/0.1/userfeedback?metadataUuid={metadataUuid}&size={size}")
    @Headers({ "Accept: application/json", })
    List<UserFeedbackDTO> getUserComments(@QueryMap(encoded = true) Map<String, Object> queryParams);

    /**
     * A convenience class for generating query parameters for the
     * <code>getUserComments</code> method in a fluent style.
     */
    public static class GetUserCommentsQueryParams extends HashMap<String, Object> {
        public GetUserCommentsQueryParams metadataUuid(final String value) {
            put("metadataUuid", EncodingUtils.encode(value));
            return this;
        }

        public GetUserCommentsQueryParams size(final Integer value) {
            put("size", EncodingUtils.encode(value));
            return this;
        }
    }

    /**
     * Finds a list of user feedback for a specific records. This list will include
     * also the draft user feedback if the client is logged as reviewer.
     * 
     * @param metadataUuid Metadata record UUID. (required)
     * @param size         Maximum number of feedback to return. (optional, default
     *                     to -1)
     * @return List&lt;UserFeedbackDTO&gt;
     */
    @RequestLine("GET /srv/api/0.1/records/{metadataUuid}/userfeedback?size={size}")
    @Headers({ "Accept: application/json", })
    List<UserFeedbackDTO> getUserCommentsOnARecord(@Param("metadataUuid") String metadataUuid,
            @Param("size") Integer size);

    /**
     * Finds a list of user feedback for a specific records. This list will include
     * also the draft user feedback if the client is logged as reviewer. Note, this
     * is equivalent to the other <code>getUserCommentsOnARecord</code> method, but
     * with the query parameters collected into a single Map parameter. This is
     * convenient for services with optional query parameters, especially when used
     * with the {@link GetUserCommentsOnARecordQueryParams} class that allows for
     * building up this map in a fluent style.
     * 
     * @param metadataUuid Metadata record UUID. (required)
     * @param queryParams  Map of query parameters as name-value pairs
     *                     <p>
     *                     The following elements may be specified in the query map:
     *                     </p>
     *                     <ul>
     *                     <li>size - Maximum number of feedback to return.
     *                     (optional, default to -1)</li>
     *                     </ul>
     * @return List&lt;UserFeedbackDTO&gt;
     */
    @RequestLine("GET /srv/api/0.1/records/{metadataUuid}/userfeedback?size={size}")
    @Headers({ "Accept: application/json", })
    List<UserFeedbackDTO> getUserCommentsOnARecord(@Param("metadataUuid") String metadataUuid,
            @QueryMap(encoded = true) Map<String, Object> queryParams);

    /**
     * A convenience class for generating query parameters for the
     * <code>getUserCommentsOnARecord</code> method in a fluent style.
     */
    public static class GetUserCommentsOnARecordQueryParams extends HashMap<String, Object> {
        public GetUserCommentsOnARecordQueryParams size(final Integer value) {
            put("size", EncodingUtils.encode(value));
            return this;
        }
    }

    /**
     * Finds a specific user feedback
     * 
     * @param uuid User feedback UUID. (required)
     * @return UserFeedbackDTO
     */
    @RequestLine("GET /srv/api/0.1/userfeedback/{uuid}")
    @Headers({ "Accept: application/json", })
    UserFeedbackDTO getUserFeedback(@Param("uuid") String uuid);

    /**
     * Creates a user feedback Creates a user feedback in draft status if the user
     * is not logged in.
     * 
     * @param uf userFeedbackDto (optional)
     * @return ResponseEntity
     */
    @RequestLine("POST /srv/api/0.1/userfeedback")
    @Headers({ "Content-Type: application/json", "Accept: application/json", })
    ResponseEntity newUserFeedback(UserFeedbackDTO uf);

    /**
     * Publishes a feedback For reviewers
     * 
     * @param uuid User feedback UUID. (required)
     * @return ResponseEntity
     */
    @RequestLine("GET /srv/api/0.1/userfeedback/{uuid}/publish")
    @Headers({ "Accept: application/json", })
    ResponseEntity publishFeedback(@Param("uuid") String uuid);

    /**
     * Send an email to catalogue administrator or record&#39;s contact
     * 
     * @param metadataUuid  Metadata record UUID. (required)
     * @param name          User name. (required)
     * @param org           User organisation. (required)
     * @param email         User email address. (required)
     * @param comments      A comment or question. (required)
     * @param recaptcha     Recaptcha validation key. (optional)
     * @param phone         User phone number. (optional)
     * @param subject       Email subject. (optional, default to User feedback)
     * @param function      User function. (optional, default to -)
     * @param type          Comment type. (optional, default to -)
     * @param category      Comment category. (optional, default to -)
     * @param metadataEmail List of record&#39;s contact to send this email.
     *                      (optional)
     * @return ResponseEntity
     */
    @RequestLine("POST /srv/api/0.1/records/{metadataUuid}/alert?recaptcha={recaptcha}&name={name}&org={org}&email={email}&comments={comments}&phone={phone}&subject={subject}&function={function}&type={type}&category={category}&metadataEmail={metadataEmail}")
    @Headers({ "Accept: application/json", })
    ResponseEntity sendEmailToContact(@Param("metadataUuid") String metadataUuid, @Param("name") String name,
            @Param("org") String org, @Param("email") String email, @Param("comments") String comments,
            @Param("recaptcha") String recaptcha, @Param("phone") String phone, @Param("subject") String subject,
            @Param("function") String function, @Param("type") String type, @Param("category") String category,
            @Param("metadataEmail") String metadataEmail);

    /**
     * Send an email to catalogue administrator or record&#39;s contact
     * 
     * Note, this is equivalent to the other <code>sendEmailToContact</code> method,
     * but with the query parameters collected into a single Map parameter. This is
     * convenient for services with optional query parameters, especially when used
     * with the {@link SendEmailToContactQueryParams} class that allows for building
     * up this map in a fluent style.
     * 
     * @param metadataUuid Metadata record UUID. (required)
     * @param queryParams  Map of query parameters as name-value pairs
     *                     <p>
     *                     The following elements may be specified in the query map:
     *                     </p>
     *                     <ul>
     *                     <li>recaptcha - Recaptcha validation key. (optional)</li>
     *                     <li>name - User name. (required)</li>
     *                     <li>org - User organisation. (required)</li>
     *                     <li>email - User email address. (required)</li>
     *                     <li>comments - A comment or question. (required)</li>
     *                     <li>phone - User phone number. (optional)</li>
     *                     <li>subject - Email subject. (optional, default to User
     *                     feedback)</li>
     *                     <li>function - User function. (optional, default to
     *                     -)</li>
     *                     <li>type - Comment type. (optional, default to -)</li>
     *                     <li>category - Comment category. (optional, default to
     *                     -)</li>
     *                     <li>metadataEmail - List of record&#39;s contact to send
     *                     this email. (optional)</li>
     *                     </ul>
     * @return ResponseEntity
     */
    @RequestLine("POST /srv/api/0.1/records/{metadataUuid}/alert?recaptcha={recaptcha}&name={name}&org={org}&email={email}&comments={comments}&phone={phone}&subject={subject}&function={function}&type={type}&category={category}&metadataEmail={metadataEmail}")
    @Headers({ "Accept: application/json", })
    ResponseEntity sendEmailToContact(@Param("metadataUuid") String metadataUuid,
            @QueryMap(encoded = true) Map<String, Object> queryParams);

    /**
     * A convenience class for generating query parameters for the
     * <code>sendEmailToContact</code> method in a fluent style.
     */
    public static class SendEmailToContactQueryParams extends HashMap<String, Object> {
        public SendEmailToContactQueryParams recaptcha(final String value) {
            put("recaptcha", EncodingUtils.encode(value));
            return this;
        }

        public SendEmailToContactQueryParams name(final String value) {
            put("name", EncodingUtils.encode(value));
            return this;
        }

        public SendEmailToContactQueryParams org(final String value) {
            put("org", EncodingUtils.encode(value));
            return this;
        }

        public SendEmailToContactQueryParams email(final String value) {
            put("email", EncodingUtils.encode(value));
            return this;
        }

        public SendEmailToContactQueryParams comments(final String value) {
            put("comments", EncodingUtils.encode(value));
            return this;
        }

        public SendEmailToContactQueryParams phone(final String value) {
            put("phone", EncodingUtils.encode(value));
            return this;
        }

        public SendEmailToContactQueryParams subject(final String value) {
            put("subject", EncodingUtils.encode(value));
            return this;
        }

        public SendEmailToContactQueryParams function(final String value) {
            put("function", EncodingUtils.encode(value));
            return this;
        }

        public SendEmailToContactQueryParams type(final String value) {
            put("type", EncodingUtils.encode(value));
            return this;
        }

        public SendEmailToContactQueryParams category(final String value) {
            put("category", EncodingUtils.encode(value));
            return this;
        }

        public SendEmailToContactQueryParams metadataEmail(final String value) {
            put("metadataEmail", EncodingUtils.encode(value));
            return this;
        }
    }
}
