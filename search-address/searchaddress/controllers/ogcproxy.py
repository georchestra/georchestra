import logging
import httplib2
from urlparse import urlparse

from pylons import request, response, session, tmpl_context as c
from pylons.controllers.util import abort, redirect_to

from searchaddress.lib.base import BaseController, render

log = logging.getLogger(__name__)

allowed_content_types = (
    "application/xml", "text/xml",
    "application/vnd.ogc.se_xml",           # OGC Service Exception
    "application/vnd.ogc.se+xml",           # OGC Service Exception
    "application/vnd.ogc.success+xml",      # OGC Success (SLD Put)
    "application/vnd.ogc.wms_xml",          # WMS Capabilities
    "application/vnd.ogc.context+xml",      # WMC
    "application/vnd.ogc.gml",              # GML
    "application/vnd.ogc.sld+xml",          # SLD
    "application/vnd.google-earth.kml+xml", # KML
    )

allowed_hosts = (
    # list allowed hosts here (no port limiting)
    )

class OgcproxyController(BaseController):

    def index(self):

        url_scheme = request.environ["wsgi.url_scheme"]
        if url_scheme not in ("http", "https"):
            abort(403) # Forbidden

        if "url" not in request.params:
            abort(400) # Bad Request

        url = request.params["url"]

        # check for full url
        parsed_url = urlparse(url)
        if not parsed_url.netloc or parsed_url.scheme not in ("http", "https"):
            abort(400) # Bad Request

        # get method
        method = request.method

        # get body
        body = None
        if method in ("POST", "PUT"):
            body = request.body

        # forward request to target (without Host Header)
        http = httplib2.Http()
        h = dict(request.headers)
        h.pop("Host", h)
        try:
            resp, content = http.request(url, method=method, body=body, headers=h)
        except:
            abort(502) # Bad Gateway

        # check for allowed content types
        if resp.has_key("content-type"):
            ct = resp["content-type"]
            if not ct.split(";")[0] in allowed_content_types:
                # allow any content type from allowed hosts (any port)
                if not parsed_url.netloc in allowed_hosts:
                    abort(403) # Forbidden
            response.headers["Content-Type"] = ct
        else:
            abort(406) # Not Acceptable

        response.status = resp.status

        return content
