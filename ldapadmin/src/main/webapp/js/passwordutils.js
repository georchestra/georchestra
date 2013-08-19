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
 *  Sets the color and message based on the password quality.
 * 
 * @param ctrlId id of the control used to display the password quality
 * @param password 
 * 
 * TODO: We may use the bootstrap progress bars
 */
function feedbackPassStrength(p, ctrlId, password){
    var message = "";
    var msgclass = "";
    var el = $("#"+ctrlId);
    var pattern = /^label-/;

    $.each( el.attr('class').split(/\s+/), function(index, item){
        if (item.match(pattern)){
            el.removeClass(item);
        }
    });
    if (!password){
        message = "empty";
    } else {
        var score = scorePassword(password);
        if (score > 80){
            message= "strong";
            msgclass = "success";
        } else if (score > 60) {
            message = "good";
            msgclass = "info";
        } else if (score >= 30) {
            message = "weak";
            msgclass = "warning";
       } else {
            message = "very weak";
            msgclass = "important";
        }
    }
    $("#"+ctrlId).html(message);
    $("#"+ctrlId).addClass("label-"+msgclass);
}
