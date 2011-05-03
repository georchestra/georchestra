from searchaddress.tests import *

class TestOgcproxyController(TestController):

    def test_nourl(self):
        response = self.app.get(url(controller='ogcproxy', action='index'),
                                status=400
                                )

    def test_badurl(self):
        badurl = "http:/toto"
        response = self.app.get(url(controller='ogcproxy',
                                    action='index',
                                    url=badurl
                                    ),
                                status=400
                                )

        badurl = "ftp://toto/"
        response = self.app.get(url(controller='ogcproxy',
                                    action='index',
                                    url=badurl
                                    ),
                                 status=400
                                 )

    def test_wmsgetcaps(self):
        getcaps_url = "http://demo.mapfish.org/mapfishsample/trunk/wms?SERVICE=WMS&VERSION=1.1.0&REQUEST=GetCapabilities"
        response = self.app.get(url(controller='ogcproxy',
                                    action='index',
                                    url=getcaps_url
                                    )
                                 )
        assert 'WMT_MS_Capabilities' in response

    def test_wmsdescribelayer(self):
        gdescribelayer_url = "http://demo.mapfish.org/mapfishsample/trunk/wms?SERVICE=WMS&VERSION=1.1.0&REQUEST=DescribeLayer&LAYERS=summits&WIDTH=10&HEIGHT=10"
        response = self.app.get(url(controller='ogcproxy',
                                    action='index',
                                    url=gdescribelayer_url
                                    )
                                 )
        assert 'WMS_DescribeLayerResponse' in response

    def test_wfsgetcaps(self):
        getcaps_url = "http://demo.mapfish.org/mapfishsample/trunk/wms?SERVICE=WFS&VERSION=1.1.0&REQUEST=GetCapabilities"
        response = self.app.get(url(controller='ogcproxy',
                                    action='index',
                                    url=getcaps_url
                                    )
                                 )
        assert 'WFS_Capabilities' in response
        assert response.charset is None

    def test_wfsgetfeature(self):
        request_body='<wfs:GetFeature xmlns:wfs="http://www.opengis.net/wfs" service="WFS" version="1.0.0" maxFeatures="1" xsi:schemaLocation="http://www.opengis.net/wfs http://schemas.opengis.net/wfs/1.0.0/WFS-transaction.xsd" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"><wfs:Query typeName="summits" srsName="EPSG:4326"></wfs:Query></wfs:GetFeature>'
        getfeature_url = "http://demo.mapfish.org/mapfishsample/trunk/wms"
        response = self.app.post(url(controller='ogcproxy',
                                     action='index',
                                     url=getfeature_url
                                     ),
                                 params=request_body,
                                 content_type='text/xml'
                                 )
