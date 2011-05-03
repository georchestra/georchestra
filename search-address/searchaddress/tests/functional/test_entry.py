from searchaddress.tests import *

class TestEntryController(TestController):

    def test_index(self):
        response = self.app.get(url(controller='entry', action='index'))
        # Test response...
