var menu = [
    {id: 'geonetwork', name: 'catalogue', url: '/geonetwork/apps/georchestra/', show: true},
    {id: 'mapfishapp', name: 'Visualiseur', url: '/mapfishapp/', show: true},
    {id: 'extractorapp', name: 'Extractor', url: '/extractorapp/', show: getExtractor()},
    {id: 'geoserver', name: 'Services', url: '/geoserver/web/', show: true},
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

var logo = '//'+window.location.host+'/IMG/png/logo.png';

document.write('<div id="go_head">');
document.write('<a id="go_home" href="#">');
document.write('<img src="img/logo.png" height="50"/>');
document.write('</a>');

document.write('<ul>');
for ( var i in menu) {
	if (menu[i].show === true) {
		document.write("<li>");
		if (active === menu[i].id) {
			document.write('<a class="active" href="' + menu[i].url	+ '" class="menu">' + menu[i].name + '</a>');
		} else {
			document.write('<a href="' + menu[i].url + '">' + menu[i].name + '</a>');
		}
		document.write('</li>');
	}
}
document.write('</ul>');//end ul

if(getAnonymous() === true){
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
