var VISISWITCHES = [
    { control:"#checkDebug", selector:"span[class='md-test debug']" },
    { control:"#checkInfo", selector:"span[class='md-test info']" },
    { control:"#checkWarning", selector:"span[class='md-test warning']" },
    { control:"#checkError", selector:"span[class='md-test error']" },
    { control:"#checkCritical", selector:"span[class='md-test critical']" }
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
    $("*[class='layersmenu']").tooltip({
        position: 'center left'
    });


};

$(document).ready(function() {
    for (var i=0; i<VISISWITCHES.length; i++) {
        $(VISISWITCHES[i].control).change(updateTestTagsVisibility);
    }
});


