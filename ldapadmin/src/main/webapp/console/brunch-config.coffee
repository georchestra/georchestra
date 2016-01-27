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
          'vendor/jquery.js',
          'vendor/angular.js',
          'vendor/angular-resource.js',
          'vendor/router.es5.js',
          'vendor/chosen.jquery.js',
          'vendor/angular-chosen.js',
          'vendor/angular-translate.js'
        ]
    stylesheets:
      joinTo:
        'app.css': /^app/
        'libraries.css': [
          'vendor/bootstrap.css',
          'vendor/chosen.min.css'
        ]
    templates:
      joinTo:
        'templates.js': /^app/
