<%@ page pageEncoding="UTF-8"%>

<c:choose>
    <c:when test='<%= request.getParameter("noheader") == null %>'>
    <div id="go_head">
        <!-- see http://stackoverflow.com/questions/1037839/how-to-force-link-from-iframe-to-be-opened-in-the-parent-window -->
        <iframe src="/static/" style="width:100%;border:none;overflow:none;" scrolling="no" onload="_headerOnLoad(this)"></iframe>
    </div>
    <script type="text/javascript">
        var _headerOnLoad = function(iframe) {
            var base = iframe.contentDocument.createElement('base');
            base.setAttribute('target', '_parent');
            iframe.contentDocument.getElementsByTagName('head')[0].appendChild(base);
        };
    </script>
    </c:when>
</c:choose>