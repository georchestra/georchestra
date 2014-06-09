package org.georchestra.dlform;

import javax.servlet.http.HttpServletRequest;



public class DownloadQuery {

    private String firstName;
    private String secondName;
    private String company;
    private String email;
    private String tel;
    private String[] dataUse;
    private String comment;
    private boolean ok;
    private String userName;
    private String sessionId;

    private String jsonSpec;

    public String getJsonSpec() {
        return jsonSpec;
    }
    public String getFirstName() {
        return firstName;
    }
    public String getSecondName() {
        return secondName;
    }
    public String getCompany() {
        return company;
    }
    public String getEmail() {
        return email;
    }
    public String getTel() {
        return tel;
    }
    public String[] getDataUse() {
        return dataUse;
    }
    public String getComment() {
        return comment;
    }
    public String getUserName() {
        return userName;
    }
    public String getSessionId() {
        return sessionId;
    }

    public DownloadQuery(HttpServletRequest request) {
        // Rely first on the headers given by the Security-proxy
        // fallback on the form fields (in case of unauthenticated)
        firstName = request.getHeader("sec-firstname") != null ?
                request.getHeader("sec-firstname") : request.getParameter("first_name");

        secondName = request.getHeader("sec-lastname") != null ?
                request.getHeader("sec-lastname") : request.getParameter("last_name");

        company = request.getHeader("sec-org") != null ?
                request.getHeader("sec-org") : request.getParameter("company");

        email = request.getHeader("sec-email") != null ?
                request.getHeader("sec-email") : request.getParameter("email");

        tel = request.getHeader("sec-tel") != null ?
                request.getHeader("sec-tel") : request.getParameter("tel");

        dataUse = request.getParameter("datause") != null ?
                request.getParameter("datause").split(",") : null;

        comment = request.getParameter("comment");

        ok = request.getParameter("ok") != null ?
                request.getParameter("ok").equalsIgnoreCase("on") : false;

        userName = request.getHeader("sec-username");

        sessionId = request.getParameter("sessionid");

        jsonSpec  = request.getParameter("json_spec");
    }
    public boolean isInvalid() {
        return ((firstName == null) || (secondName == null)
                || (company == null) || (email == null)
                || (dataUse == null) || (ok == false)
                || (sessionId == null));
    }
}
