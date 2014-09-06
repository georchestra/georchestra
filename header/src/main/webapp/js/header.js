var menu = [
    {id: 'geonetwork', name: i18n('catalogue'), url: '/geonetwork/apps/georchestra/', show: true},
    {id: 'mapfishapp', name: i18n('viewer'), url: '/mapfishapp/', show: true},
    {id: 'extractorapp', name: i18n('extractor'), url: '/extractorapp/', show: getExtractor()},
    {id: 'geoserver', name: i18n('services'), url: '/geoserver/web/', show: true}
];
var active;

var req = new XMLHttpRequest();
req.open('GET', document.location, false);
req.send(null);
var headers = req.getAllResponseHeaders().toLowerCase();

var scripts = document.getElementsByTagName('script');
var myScript = scripts[ scripts.length - 1 ];
var query = myScript.src.replace(/^[^\?]+\??/, '');

var pairs = query.split(',');
for (var i = 0; i < pairs.length; i++) {
    var KeyVal = pairs[i].split('=');
    if (!KeyVal || KeyVal.length != 2)
        continue;
    var key = unescape(KeyVal[0]);
    var val = unescape(KeyVal[1]);
    if (key === 'active')
        active = val;
}

var logo = ''+getUrlLogo();
document.write('<div id="go_head">');
document.write('<a id="go_home" href="#">');
document.write('<img src="'+logo+'" height="50"/>');
document.write('</a>');

document.write('<ul>');
for ( var i in menu) {
	if (menu[i].show === true) {
		if (active === menu[i].id) {
			document.write('<li class="active" >');
		} else {
			document.write("<li>");
		}
		document.write('<a href="' + menu[i].url + '">' + menu[i].name + '</a>');
		document.write('</li>');
	}
}
document.write('</ul>');//end ul

if(getAnonymous() === false){
	document.write('<p class="logged">');
	document.write('<a href="/ldapadmin/account/userdetails">'+getUserName()+'</a>');
	document.write('<span class="light"> | </span>');
	document.write('<a href="/j_spring_security_logout">Logout</a>');
	document.write('</p>');
} else {
	document.write('<p class="logged">');
	document.write('<a id="login_a">Login</a>');
	document.write('</p>');
}
document.write('</div>'); //end div go_head

(function(){
	// required to get the correct redirect after login, see https://github.com/georchestra/georchestra/issues/170
	var url,
		a = document.getElementById("login_a");
	if (a !== null) {
		url = parent.window.location.href;
		if (/\/cas\//.test(url)) {
			a.href = "/cas/login";
		} else {
			/* Taken from https://github.com/openlayers/openlayers/blob/master/lib/OpenLayers/Util.js#L557 */
			var paramStr="login", parts = (url + " ").split(/[?&]/);
			a.href = url + (parts.pop() === " " ?
				paramStr :
				parts.length ? "&" + paramStr : "?" + paramStr);
		}
	}
})();
