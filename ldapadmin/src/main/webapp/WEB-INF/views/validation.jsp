<%@ page pageEncoding="UTF-8" %>
<script type="text/javascript">
/*
 * Utilities for form validation
 * 
 * This validation tests are the same as the server-side tests
 */ 

function removeError(id) {
	$("#div-" + id).removeClass("has-error");
	$("#div-" + id + "> div > span#span-error").remove();
}
function addError(id, errCode) {
	$("#div-" + id).addClass("has-error");
	$("#div-" + id + " > div").append('<span id="span-error" class="help-block">' + errCode + '</span>');
}

function testFirstname() {
	var firstname = document.form.firstName.value;
	removeError("firstname");
	if (!isNotEmpty(firstname)) {
		addError("firstname", '<s:message code="firstName.error.required" />');
		return false;
	}
	return true;
}
function testSurname() {
	var surname = document.form.surname.value;
	removeError("surname");
	if (!isNotEmpty(surname)) {
		addError("surname", '<s:message code="surname.error.required" />');
		return false;
	}
	return true;
}
function testEmail() {
	var email = document.form.email.value;
	removeError("firstname");
	if (!isNotEmpty(email)) {
		addError("email", '<s:message code="email.error.required" />');
		return false;
	} else if (!emailCheck(email)) {
		addError("email", '<s:message code="email.error.invalidFormat" />');
		return false;	
	}
	return true;
}
function testUid() {
	var uid = document.form.uid.value;
	removeError("uid");
	if (!isNotEmpty(uid)) {
		addError("uid", '<s:message code="uid.error.required" />');
		return false;
	} else if (!isUidValid(uid)) {
		addError("uid", '<s:message code="uid.error.invalid" />');
		return false;
	}
	return true;
}
function testPassword() {
	var password = document.form.password.value;
	removeError("password");
	if (!isPasswordValid(password)) {
		addError("password", '<s:message code="password.error.sizeError" />');
		return false;
	}
	return true;
}
function testConfirmPassword() {
	var password = document.form.password.value;
	var confirmPassword = document.form.confirmPassword.value;
	removeError("confirmPassword");
	if (password!=confirmPassword) {
		addError("confirmPassword", '<s:message code="confirmPassword.error.pwdNotEquals" />');
		return false;
	}
	return true;
}
function setFormError() {
	$("form#form > #message").remove();
	$("form#form").prepend('<div id="message" class="alert alert-dismissable alert-danger"><button type="button" class="close" data-dismiss="alert" aria-hidden="true">&times;</button><s:message code="form.error" /></div>');
}

/**
 * Test if a uid string is valid
 * 
 * The first character must be a letter, and the uid must contain only
 * letters, digits and/or the '.' character
 * 
 * @param {String} uid
 * @returns {boolean}
 */
function isUidValid(uid) {
	if (uid.charAt(0).match(/[a-z]/ig) && (uid.match(/[a-z0-9\.]/ig).length == uid.length)) {
		return true;
	} else {
		return false;
	}
}

/**
 * Trim and test if a string is not empty
 * 
 * @param {String} str
 * @returns {boolean}
 */
function isNotEmpty(str) {
	if (str.trim().length){
		return true;
	} else {
		return false;
	}
}

/**
 * Test if a password is valid
 * 
 * @param {String} password
 * @returns {boolean}
 */
function isPasswordValid(password) {
	if (!password || password.length < 8){
		return false;
	} else {
		return true;
	}
}

/**
 * Test if an email is valid
 * 
 * @see http://www.javascriptsource.com/forms/email-address-validation.html
 * @param {String} email
 * @returns {boolean}
 */
function emailCheck (emailStr) {

  /* The following variable tells the rest of the function whether or not
  to verify that the address ends in a two-letter country or well-known
  TLD.  1 means check it, 0 means don't. */

  var checkTLD=0;

  /* The following is the list of known TLDs that an e-mail address must end with. */

  var knownDomsPat=/^(com|net|org|edu|int|mil|gov|arpa|biz|aero|name|coop|info|pro|museum)$/;

  /* The following pattern is used to check if the entered e-mail address
  fits the user@domain format.  It also is used to separate the username
  from the domain. */

  var emailPat=/^(.+)@(.+)$/;

  /* The following string represents the pattern for matching all special
  characters.  We don't want to allow special characters in the address. 
  These characters include ( ) < > @ , ; : \ " . [ ] */

  var specialChars="\\(\\)><@,;:\\\\\\\"\\.\\[\\]";

  /* The following string represents the range of characters allowed in a 
  username or domainname.  It really states which chars aren't allowed.*/

  var validChars="\[^\\s" + specialChars + "\]";

  /* The following pattern applies if the "user" is a quoted string (in
  which case, there are no rules about which characters are allowed
  and which aren't; anything goes).  E.g. "jiminy cricket"@disney.com
  is a legal e-mail address. */

  var quotedUser="(\"[^\"]*\")";

  /* The following pattern applies for domains that are IP addresses,
  rather than symbolic names.  E.g. joe@[123.124.233.4] is a legal
  e-mail address. NOTE: The square brackets are required. */

  var ipDomainPat=/^\[(\d{1,3})\.(\d{1,3})\.(\d{1,3})\.(\d{1,3})\]$/;

  /* The following string represents an atom (basically a series of non-special characters.) */

  var atom=validChars + '+';

  /* The following string represents one word in the typical username.
  For example, in john.doe@somewhere.com, john and doe are words.
  Basically, a word is either an atom or quoted string. */

  var word="(" + atom + "|" + quotedUser + ")";

  // The following pattern describes the structure of the user

  var userPat=new RegExp("^" + word + "(\\." + word + ")*$");

  /* The following pattern describes the structure of a normal symbolic
  domain, as opposed to ipDomainPat, shown above. */

  var domainPat=new RegExp("^" + atom + "(\\." + atom +")*$");

  /* Finally, let's start trying to figure out if the supplied address is valid. */

  /* Begin with the coarse pattern to simply break up user@domain into
  different pieces that are easy to analyze. */

  var matchArray=emailStr.match(emailPat);

  if (matchArray==null) {

  /* Too many/few @'s or something; basically, this address doesn't
  even fit the general mould of a valid e-mail address. */

    //alert("Email address seems incorrect (check @ and .'s)");
    return false;
  }
  var user=matchArray[1];
  var domain=matchArray[2];

  // Start by checking that only basic ASCII characters are in the strings (0-127).

  for (i=0; i<user.length; i++) {
    if (user.charCodeAt(i)>127) {
      //alert("Ths username contains invalid characters.");
      return false;
    }
  }
  for (i=0; i<domain.length; i++) {
    if (domain.charCodeAt(i)>127) {
      //alert("Ths domain name contains invalid characters.");
      return false;
    }
  }

  // See if "user" is valid 

  if (user.match(userPat)==null) {

    // user is not valid

    //alert("The username doesn't seem to be valid.");
    return false;
  }

  /* if the e-mail address is at an IP address (as opposed to a symbolic
  host name) make sure the IP address is valid. */

  var IPArray=domain.match(ipDomainPat);
  if (IPArray!=null) {

    // this is an IP address

    for (var i=1;i<=4;i++) {
      if (IPArray[i]>255) {
        //alert("Destination IP address is invalid!");
        return false;
      }
    }
    return true;
  }

  // Domain is symbolic name.  Check if it's valid.

  var atomPat=new RegExp("^" + atom + "$");
  var domArr=domain.split(".");
  var len=domArr.length;
  for (i=0;i<len;i++) {
    if (domArr[i].search(atomPat)==-1) {
      //alert("The domain name does not seem to be valid.");
      return false;
    }
  }

  /* domain name seems valid, but now make sure that it ends in a
  known top-level domain (like com, edu, gov) or a two-letter word,
  representing country (uk, nl), and that there's a hostname preceding 
  the domain or country. */

  if (checkTLD && domArr[domArr.length-1].length!=2 && 
    domArr[domArr.length-1].search(knownDomsPat)==-1) {
    //alert("The address must end in a well-known domain or two letter " + "country.");
    return false;
  }

  // Make sure there's a host name preceding the domain.

  if (len<2) {
    //alert("This address is missing a hostname!");
    return false;
  }

  // If we've gotten this far, everything's valid!
  return true;
}
</script>
