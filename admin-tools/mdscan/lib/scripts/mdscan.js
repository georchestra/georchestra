var TESTLEVELS = [
    { control:"#checkDebug", cssClass:"md-test debug" },
    { control:"#checkInfo", cssClass:"md-test info" },
    { control:"#checkWarning", cssClass:"md-test warning" },
    { control:"#checkError", cssClass:"md-test error" },
    { control:"#checkCritical", cssClass:"md-test critical" }
]

function updateTestTagsVisibility() {
    for (var i=0; i<TESTLEVELS.length; i++) {
        if ($(TESTLEVELS[i].control).attr('checked')) {
            $("span[class='"+TESTLEVELS[i].cssClass+"']").show()
        }
        else {
            $("span[class='"+TESTLEVELS[i].cssClass+"']").hide()
        };
    };
};

$(document).ready(function() {
    $("#checkDebug").change(updateTestTagsVisibility);
    $("#checkInfo").change(updateTestTagsVisibility);
    $("#checkWarning").change(updateTestTagsVisibility);
    $("#checkError").change(updateTestTagsVisibility);
    $("#checkCritical").change(updateTestTagsVisibility);
});


