/*
* File : CSWClient.js
* Author : Rob van Swol
* Organisation: National Aerospace Laboratory NLR
* Country : The Netherlands
* email : vanswol@nlr.nl
* Description: Simple AJAX based CSW client
* Depends csw-proxy.php
* Tested on : FireFox 3, Safari, IE 7
* Last Change : 2008-10-22
*/

CSWClient = function(cswhost, host) {
    this.cswhost = null;
    this.use_proxy = true;
    if (typeof cswhost != "undefined") {
        this.cswhost = cswhost;
    }

    this.proxy = "csw-proxy.php";
    if (typeof host != "undefined") {
        this.proxy = host + "csw-proxy.php";
    }

    this.getrecords_xsl = this.loadDocument("lib/xsl/getrecords.xsl");
    this.getrecordbyid_xsl = this.loadDocument("lib/xsl/getrecordbyid.xsl");
    this.defaults_xml = this.loadDocument("lib/xml/defaults.xml");
    this.defaultschema = this.defaults_xml.selectSingleNode("/defaults/outputschema/text()").nodeValue;
}

CSWClient.prototype.setCSWHost = function(host) {
   this.cswhost = host;
}

CSWClient.prototype.useProxy = function(tf) {
   this.use_proxy = tf;
}

CSWClient.prototype.writeClient = function(divId) {
    var client_xml = this.loadDocument("lib/xml/cswclient.xml");
    /* if no default cswhost has been defined we provide the user with optional csw hosts */
    if (this.cswhost == null) {
        var cswhosts_xml = this.loadDocument("lib/xml/csw-hosts.xml");
        var span = client_xml.selectSingleNode("//span[@id='csw-hosts']");
        importNode = client_xml.importNode(cswhosts_xml.documentElement, true);
        span.appendChild(importNode);
    }
    var serializer = new XMLSerializer();
    var output = serializer.serializeToString(client_xml);
    //alert (output);
    var div = document.getElementById(divId);
    div.innerHTML = output;
}

CSWClient.prototype.handleCSWResponse = function(request, xml) {

    var stylesheet = "lib/xsl/prettyxml.xsl";
    if (request == "getrecords" &
       document.theForm.displaymode.value != "xml") {
        stylesheet = "lib/xsl/csw-results.xsl";
    }
    else if (request == "getrecordbyid" &
              document.theForm.displaymode.value != "xml") {
        stylesheet = "lib/xsl/csw-metadata.xsl";
    }

    xslt = this.loadDocument(stylesheet);
    var processor = new XSLTProcessor();
    processor.importStylesheet(xslt);

    var XmlDom = processor.transformToDocument(xml)
    var serializer = new XMLSerializer();
    var output = serializer.serializeToString(XmlDom.documentElement);

    var outputDiv = document.getElementById("csw-output");
    if (request == "getrecordbyid"){
        outputDiv = document.getElementById("metadata");
        //this.positionDiv(document.getElementById('popup'), document.getElementById('results'))
        //this.positionPopUp(document.getElementById('popup'), document.getElementById('results'))
        this.positionPopUp(document.getElementById('popup'), document.getElementById('cswclient'))
        this.showDiv(document.getElementById('popup'));
    }
    outputDiv.innerHTML = output;

    // mdscan addon
    updateTestTagsVisibility();
}


CSWClient.prototype.getRecords = function(start) {

    if (typeof start == "undefined") {
        start = 1;
    }

 if (typeof  document.theForm.cswhost != "undefined") {
   this.setCSWHost(document.theForm.cswhost.value);}

    var queryable = document.theForm.queryable.value;

    /*because geonetwork doen not follow the specs*/
    if(this.cswhost.indexOf('geonetwork') !=-1 & queryable == "anytext")
        queryable = "any";

    var operator = document.theForm.operator.value;
    var query = trim(document.theForm.query.value);
    if (operator == "contains" & query != "") {
        query = "%" + query + "%";
    }

    var schema = "http://www.opengis.net/cat/csw/2.0.2"; // force outputSchema  always  to csw:Record for GetRecords requests
    this.setXpathValue(this.defaults_xml, "/defaults/outputschema", schema + '');
    this.setXpathValue(this.defaults_xml, "/defaults/propertyname", queryable + '');
    this.setXpathValue(this.defaults_xml, "/defaults/literal", query + '');
    //this.setXpathValue(defaults_xml, "/this.defaults/literal", query + '');
    this.setXpathValue(this.defaults_xml, "/defaults/startposition", start + '');
    var sortby = document.theForm.sortby.value;
    this.setXpathValue(this.defaults_xml, "/defaults/sortby", sortby + '');

    var processor = new XSLTProcessor();
    processor.importStylesheet(this.getrecords_xsl);

    var request_xml = processor.transformToDocument(this.defaults_xml);
    var request = new XMLSerializer().serializeToString(request_xml);

    csw_response = this.sendCSWRequest(request);
    var results = "<results><request start=\"" + start + "\"";
    results += " maxrecords=\"";
    results += this.defaults_xml.selectSingleNode("/defaults/maxrecords/text()").nodeValue;
    results += "\"/></results>";

    results_xml = (new DOMParser()).parseFromString(results, "text/xml");
    importNode = results_xml.importNode(csw_response.documentElement, true);
    results_xml.documentElement.appendChild(importNode);
    //alert(new XMLSerializer().serializeToString(results_xml));

    //return handleCSWResponse("getrecords", csw_response);
    return this.handleCSWResponse("getrecords", results_xml);
}


CSWClient.prototype.getRecordById = function(id) {
    var schema = this.defaultschema;
    if (document.theForm.schema != null) {
        schema = document.theForm.schema.value;
    }

    this.setXpathValue(this.defaults_xml, "/defaults/outputschema", schema + '');
    this.setXpathValue(this.defaults_xml, "/defaults/id", id + '');

    var processor = new XSLTProcessor();
    processor.importStylesheet(this.getrecordbyid_xsl);

    var request_xml = processor.transformToDocument(this.defaults_xml);
    var request = new XMLSerializer().serializeToString(request_xml);

    csw_response = this.sendCSWRequest(request);
    //alert(new XMLSerializer().serializeToString(csw_response));
    return this.handleCSWResponse("getrecordbyid", csw_response);
}


CSWClient.prototype.sendCSWRequest = function(request) {

    var xml = Sarissa.getDomDocument();
    xml.async = false;
    var xmlhttp = new XMLHttpRequest();

    var params;
    if (this.use_proxy) {
        params = "csw_host=" + this.cswhost + "&csw_request=" + encodeURIComponent(request);
        xmlhttp.open("POST", this.proxy, false);
        xmlhttp.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
    }
    else {
        params = request;
        xmlhttp.open("POST", this.cswhost, false);
        xmlhttp.setRequestHeader("Content-type", "application/xml");
    }

    //xmlhttp.open("POST", this.proxy, false);
    //xmlhttp.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
    //xmlhttp.open("POST", this.cswhost, false);
    //xmlhttp.setRequestHeader("Content-type", "application/xml");
    xmlhttp.setRequestHeader("Content-length", params.length);
    xmlhttp.setRequestHeader("Connection", "close");
    xmlhttp.send(params); // POST
    //xmlhttp.send(''); //GET

    xml = xmlhttp.responseXML;
    return xml;
}


CSWClient.prototype.loadDocument = function(uri) {

    var xml = Sarissa.getDomDocument();
    var xmlhttp = new XMLHttpRequest();
    xml.async = false;
    xmlhttp.open("GET", uri, false);
    xmlhttp.send('');
    xml = xmlhttp.responseXML;
    return xml;
}


CSWClient.prototype.setXpathValue = function(_a,_b,_c) {

    var _e=_a.selectSingleNode(_b);
    if(_e) {
        if(_e.firstChild) {
            _e.firstChild.nodeValue=_c;
        } else {
            dom=Sarissa.getDomDocument();
            v=dom.createTextNode(_c);
            _e.appendChild(v);
        }
        return true;
    } else {
        return false;
    }
};


CSWClient.prototype.clearPage = function() {
    document.theForm.query.value = "";
    var outputDiv = document.getElementById("csw-output");
    outputDiv.innerHTML = "";
    this.hideDiv(document.getElementById('popup'))
}

CSWClient.prototype.overlayDiv = function(div) {
    while (div.tagName !="DIV") {
        div = div.parentNode
    }

    _width = div.offsetWidth
    _height = div.offsetHeight
    _top = this.findPosY(div);
    _left = this.findPosX(div);

    //overlay = document.createElement("div")
    //overlay.setAttribute("id", "overlay")
    var overlay = document.getElementById('overlay');
    overlay.style.width = _width + "px"
    overlay.style.height = _height + "px"
    overlay.style.position = "absolute"
    overlay.style.background = "#555555"
    overlay.style.top = _top + "px"
    overlay.style.left = _left + "px"

    overlay.style.filter = "alpha(opacity=70)"
    overlay.style.opacity = "0.7"
    overlay.style.mozOpacity = "0.7"
    overlay.style.visibility="visible";

    document.getElementsByTagName("body")[0].appendChild(overlay)
}

CSWClient.prototype.removeDiv = function(div) {
    document.getElementsByTagName("body")[0].removeChild(div)
}

CSWClient.prototype.hideDiv = function(div) {
        document.getElementById('overlay').style.visibility="hidden";
     div.style.visibility="hidden";
}

CSWClient.prototype.showDiv = function(div) {
    //this.overlayDiv(document.getElementById('results-container'));
    this.overlayDiv(document.getElementById('cswclient'));
    div.style.visibility="visible";
}

CSWClient.prototype.positionDiv = function(div1, div2) {
    var width = div2.offsetWidth-100
    var height = div2.offsetHeight-100
    var top = this.findPosY(div2)+50;
    var left = this.findPosX(div2)+50;
    div1.style.width = width + "px"
    div1.style.position = "absolute"
    div1.style.background = "#ffffff"
    div1.style.top = top + "px"
    div1.style.left = left + "px"
}

CSWClient.prototype.positionPopUp = function(div1, div2) {
    var top = this.findPosY(div2)+50+getScrollY();
    div1.style.top = top + "px"
}

CSWClient.prototype.findPosX = function(obj) {
    var curleft = 0;
    if(obj.offsetParent)
        while(1) {
            curleft += obj.offsetLeft;
            if(!obj.offsetParent)
                break;
            obj = obj.offsetParent;
        }
    else if(obj.x)
        curleft += obj.x;
    return curleft;
}

CSWClient.prototype.findPosY = function(obj) {
    var curtop = 0;
    if(obj.offsetParent)
        while(1) {
            curtop += obj.offsetTop;
            if(!obj.offsetParent)
                break;
            obj = obj.offsetParent;
        }
    else if(obj.y)
        curtop += obj.y;
    return curtop;
}

function getScrollY() {
    var scrollY = 0;
    if (typeof window.pageYOffset == "number") scrollY = window.pageYOffset;
    else if (document.documentElement && document.documentElement.scrollTop)
        scrollY = document.documentElement.scrollTop;
    else if (document.body && document.body.scrollTop)
        scrollY = document.body.scrollTop;
    else if (window.scrollY) scrollY = window.scrollY;
    return scrollY;
}

function trim(value) {
    value = value.replace(/^\s+/,'');
    value = value.replace(/\s+$/,'');
    return value;
}
