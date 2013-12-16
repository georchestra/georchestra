var _headerOnLoad = function(iframe) {
    var innerDoc = iframe.contentDocument || iframe.contentWindow.document,
        base = innerDoc.createElement('base');
    base.setAttribute('target', '_parent');
    innerDoc.getElementsByTagName('head')[0].appendChild(base);
};