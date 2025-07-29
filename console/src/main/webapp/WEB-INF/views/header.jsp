<%--

 Copyright (C) 2009-2025 by the geOrchestra PSC

 This file is part of geOrchestra.

 geOrchestra is free software: you can redistribute it and/or modify it under
 the terms of the GNU General Public License as published by the Free
 Software Foundation, either version 3 of the License, or (at your option)
 any later version.

 geOrchestra is distributed in the hope that it will be useful, but WITHOUT
 ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
 more details.

 You should have received a copy of the GNU General Public License along with
 geOrchestra.  If not, see <http://www.gnu.org/licenses/>.
 
--%>

<%@ page language="java" pageEncoding="UTF-8" %>


<c:choose>
    <c:when test='${not empty georchestraStylesheet}'>
        <link rel="stylesheet" type="text/css" href="${georchestraStylesheet}" />
    </c:when>
</c:choose>
<c:choose>
    <c:when test='<%= request.getParameter("noheader") == null %>'>
    <div id="go_head">
        <geor-header config-file="${headerConfigFile}" logo-url="${logoUrl}" stylesheet="${georchestraStylesheet}" legacy-header="${useLegacyHeader}" legacy-url="${headerUrl}" height="${headerHeight}" active-app="console"></geor-header>
        <script src="${headerScript}"></script>
    </div>
    </c:when>
</c:choose>
