import logging

from pylons import request, response, session, tmpl_context as c
from pylons.controllers.util import abort, redirect_to

from searchaddress.lib.base import BaseController, render

log = logging.getLogger(__name__)

class EntryController(BaseController):

    def index(self):
        c.debug = "debug" in request.params
        c.lang = str(request.params.get("lang", "fr"))

        return render("index.html")
