var VISISWITCHES = [
    { control:"#checkDebug", selector:"span[class='md-test debug']" },
    { control:"#checkInfo", selector:"span[class='md-test info']" },
    { control:"#checkWarning", selector:"span[class='md-test warning']" },
    { control:"#checkError", selector:"span[class='md-test error']" },
    { control:"#checkCritical", selector:"span[class='md-test critical']" },
    { control:"#btAdmin", selector:"*[class='md-admin']" }
]

function updateTestTagsVisibility() {
    for (var i=0; i<VISISWITCHES.length; i++) {
        if ($(VISISWITCHES[i].control).attr('checked')) {
            $(VISISWITCHES[i].selector).show()
        }
        else {
            $(VISISWITCHES[i].selector).hide()
        };
    };
};

$(document).ready(function() {
    for (var i=0; i<VISISWITCHES.length; i++) {
        $(VISISWITCHES[i].control).change(updateTestTagsVisibility);
    }
});


