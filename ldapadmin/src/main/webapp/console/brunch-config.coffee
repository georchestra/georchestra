module.exports = config:
  plugins:
    ng_templates:
      module: 'admin_console'
      relativePath: 'app/'
  files:
    javascripts:
      joinTo:
        'app.js': /^app/
        'libraries.js': [
          'vendor/angular.js',
          'vendor/angular-resource.js',
          'vendor/router.es5.js'
        ]
    stylesheets:
      joinTo:
        'app.css': /^app/
        'libraries.css': [
          'vendor/bootstrap.css'
        ]
    templates:
      joinTo:
        'templates.js': /^app/
