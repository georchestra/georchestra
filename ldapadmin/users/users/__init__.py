from pyramid.config import Configurator


def main(global_config, **settings):
    """ This function returns a Pyramid WSGI application.
    """
    config = Configurator(settings=settings)
    config.add_static_view('static', 'static', cache_max_age=3600)
    config.add_route('users', '/users')
    config.add_route('user', '/users/:user')
    config.scan()
    return config.make_wsgi_app()
