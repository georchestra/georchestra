<%@ page pageEncoding="UTF-8"%>

<c:choose>
    <c:when test='<%= request.getParameter("noheader") == null %>'>
    <div id="go_head">
        <!-- see http://stackoverflow.com/questions/1037839/how-to-force-link-from-iframe-to-be-opened-in-the-parent-window -->
        <iframe src="/static/?lang=<%= lang %>&active=mapfishapp" style="width:100%;height:@shared.header.height@px;border:none;overflow:none;" scrolling="no" frameborder="0" onload="_headerOnLoad(this)"></iframe>
    </div>
    <script type="text/javascript">
        var _headerOnLoad = function(iframe) {
            var innerDoc = iframe.contentDocument || iframe.contentWindow.document,
                base = innerDoc.createElement('base');
            base.setAttribute('target', '_parent');
            innerDoc.getElementsByTagName('head')[0].appendChild(base);
        };
    </script>
    </c:when>
</c:choose>