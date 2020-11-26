# header

![header](https://github.com/georchestra/georchestra/workflows/header/badge.svg)

This module publishes a dynamic header, which is by default incorporated into every geOrchestra webapp.

In case the provided header does not suit, it is possible to call another page via the `headerUrl` [config option](https://github.com/georchestra/datadir/blob/19.04/default.properties#L36-L39).

This page should have the `base` tag in the document `head` configured, in order to open links in the iframe's parent:
```html
<base target="_parent" />`
```

If this is not possible, the `header.jsp` files can be customized as such:
```html
<script type="text/javascript">
var _headerOnLoad = function(iframe) {
    var innerDoc = iframe.contentDocument || iframe.contentWindow.document,
        base = innerDoc.createElement('base');
    base.setAttribute('target', '_parent');
    innerDoc.getElementsByTagName('head')[0].appendChild(base);
};
</script>
<iframe src="/my/page" onload="_headerOnLoad(this)"></iframe>
```

