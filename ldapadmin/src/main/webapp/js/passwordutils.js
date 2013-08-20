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
 * TODO: i18n of the tags (empty, weak, etc.)
 */
function feedbackPassStrength(ctrlId, password){
    var message = "";
    var msgLabelClass = "";
    var msgOffsetClass = "";
    var el = $("#"+ctrlId);
    var patternLabel = /^label-/;
    var patternOffset = /^col-lg-offset-/;
    $.each( el.attr('class').split(/\s+/), function(index, item){
        if (item.match(patternLabel) || item.match(patternOffset)){
            el.removeClass(item);
        }
    });
    if (!password){
        message = "empty";
        msgLabelClass = "default";
    } else {
        var score = scorePassword(password);
        if (score > 80){
            message= "strong";
            msgLabelClass = "success";
            msgOffsetClass = "8";
        } else if (score > 60) {
            message = "good";
            msgLabelClass = "info";
            msgOffsetClass = "6";
        } else if (score >= 30) {
            message = "weak";
            msgLabelClass = "warning";
            msgOffsetClass = "4";
       } else {
            message = "very weak";
            msgLabelClass = "danger";
            msgOffsetClass = "2";
        }
    }
    $("#"+ctrlId).html(message);
    $("#"+ctrlId).addClass("label-"+msgLabelClass);
    $("#"+ctrlId).addClass("col-lg-offset-"+msgOffsetClass);
}
