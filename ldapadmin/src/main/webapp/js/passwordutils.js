/*
 * Password utilities
 * 
 * This file contains function to manage password.
 */ 


/**
 * Evaluate the password quality
 * 
 * @param password
 */
function scorePassword( password) {
    var score = 0;
    if (!password)
        return score;

    // award every unique letter until 5 repetitions
    var letters = new Object();
    for (var i=0; i<password.length; i++) {
        letters[password[i]] = (letters[password[i]] || 0) + 1;
        score += 5.0 / letters[password[i]];
    }

    // bonus points for mixing it up
    var variations = {
        digits: /\d/.test(password),
        lower: /[a-z]/.test(password),
        upper: /[A-Z]/.test(password),
        nonWords: /\W/.test(password),
    };

    variationCount = 0;
    for (var check in variations) {
        variationCount += (variations[check] == true) ? 1 : 0;
    }
    score += (variationCount - 1) * 10;

    return parseInt(score);
}


/**
 * Sets the color and message based on the password quality.
 * 
 * @param pwdCtrl password control
 * @param messageCtrl the control used to display the password quality
 * @param password 
 */
function feedbackPassStrength(pwdCtrl, messageCtrl, password){

	if((password == null) || (password=="")){
		/* quit color and message if empty */
		messageCtrl.innerHTML="";
		messageCtrl.style="";
		pwdCtrl.style.backgroundColor="";
		return;
	}

	var score = scorePassword(password);

    var message = "very weak";
    var color =  "#e71a1a";
	if (score > 80){
    	
    	message= "strong";
    	color = "#008000"; 
    	
    }else if (score > 60){
    	
        message = "good";
        color =  "#e3cb00";
        
    }else if (score >= 30){
    	
        message = "weak";
        color =  "#Fe3d1a";
        
    } 
	
	messageCtrl.innerHTML = message;
	messageCtrl.style= "color:" + color;
	pwdCtrl.style.backgroundColor= color;
	
}
