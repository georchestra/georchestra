from searchaddress.tests import *

class TestAddressesController(TestController):
    def test_index(self):
        response = self.app.get(url_for(controller='addresses'))
        # Test response...
