<?php
// PHP CSW Proxy for CSW services. 
// Responds to both HTTP GET and POST requests
//
// Author: Rob van Swol, NLR
// October 15th, 2008
//

$logging = false;
/* Optionally write all requests in a log file */
if ($logging) {
  $fh = fopen("lib/proxy.log", "ab+");
  $timestamp = strftime("[%Y-%m-%d %H:%M:%S]");
}

$allowed_hosts = array();

$doc = new DOMDocument();
$doc->load('./lib/xml/csw-hosts.xml');

$hosts = $doc->getElementsByTagName("option");
foreach ($hosts as $host) {
  //$csw_host_id = trim($host->nodeValue);
  $csw_host = trim($host->getAttribute("value"));
  //echo $csw_host."\n";
  array_push($allowed_hosts, $csw_host);
}


// Get the REST call path from the AJAX application
// Is it a POST or a GET?
$url = ($_POST['csw_host']) ? $_POST['csw_host'] : $_GET['csw_host'];

// Check if $url is a known host
if (!in_array($url, $allowed_hosts)) {
   echo "not allowed";
   if ($logging) {
     fwrite($fh, $timestamp.": refused request...\n");
     fwrite($fh, $timestamp.": HOST NOT ALLOWED> ".$url."\n");
   }
} else {

if ($logging) {
  fwrite($fh, $timestamp.": incoming request...\n");
  fwrite($fh, $timestamp.": HOST> ".$url."\n");
}

// Open the Curl session
$session = curl_init($url);

// If it's a POST, put the POST data in the body
if ($_POST['csw_request']) {

   //if (substr($_POST['csw_request'],0,5) == "<?xml") {
   if (substr($_POST['csw_request'],0,1) == "<") {
      
      // Is magic quotes on? 
      if (get_magic_quotes_gpc())
        $xmlpost = stripslashes($_POST['csw_request']);
      else
        $xmlpost = $_POST['csw_request'];
            
      curl_setopt ($session, CURLOPT_POST, true);
      curl_setopt ($session, CURLOPT_POSTFIELDS, $xmlpost);
      curl_setopt ($session, CURLOPT_HTTPHEADER, Array("Content-Type: application/xml"));
      //curl_setopt ($session, CURLOPT_SSL_VERIFYPEER, 0);
	  if ($logging)
        fwrite($fh, $timestamp.": POST> ".$xmlpost."\n");

    } else {
	$postvars = '';
	while ($element = current($_POST)) {
		if (key($_POST) != "csw_request")
	            $postvars .= key($_POST).'='.$element.'&';
		else 
		    $postvars .= $element.'&';
		next($_POST);
	}
        curl_setopt ($session, CURLOPT_POST, true);
        curl_setopt ($session, CURLOPT_POSTFIELDS, $_POST['csw_request']);
        curl_setopt ($session, CURLOPT_HTTPHEADER, Array("Content-Type: application/x-www-form-urlencoded"));
		if ($logging)
          fwrite($fh, $timestamp.": POST> ".$_POST['csw_request']."\n"); 
    }       

} else if ($_GET['csw_request']) {
    curl_setopt ($session, CURLOPT_POST, true);
    curl_setopt ($session, CURLOPT_POSTFIELDS, $_GET['csw_request']);
    curl_setopt ($session, CURLOPT_HTTPHEADER, Array("Content-Type: application/x-www-form-urlencoded"));
	if ($logging)
      fwrite($fh, $timestamp.": GET> ".$_GET['csw_request']."\n"); 
}



// Don't return HTTP headers. Do return the contents of the call
curl_setopt($session, CURLOPT_HEADER, false);
curl_setopt($session, CURLOPT_RETURNTRANSFER, true);


// Make the call
$xml = curl_exec($session);

// The web service returns XML. Set the Content-Type appropriately
header("Content-Type: text/xml");

if ($logging) {
  fwrite($fh, $timestamp.": RESPONSE> ".$xml."\n");
  fclose($fh);
}

echo $xml;
curl_close($session);
}
?>
