module.exports = config:
  plugins:
    ng_templates:
      module: 'admin_console'
      relativePath: 'app/'
    babel:
      pattern: /\.es6/
      plugins: [ "transform-class-properties" ]
  files:
    javascripts:
      joinTo:
        'app.js': /^app/
        'libraries.js': [
          'vendor/jquery.js',
          'vendor/angular.js',
          'vendor/angular-resource.js',
          'vendor/angular-sanitize.js',
          'vendor/bootstrap-datepicker.js',
          'vendor/router.es5.js',
          'vendor/chosen.jquery.js',
          'vendor/angular-chosen.js',
          'vendor/angular-flash.js',
          'vendor/angular-translate.js',
          'vendor/angular-translate-loader-static-files.js',
          'vendor/dirPagination.js',
          'vendor/chartist.js',
          'vendor/inline.js',
          'vendor/select2.full.js',
          'vendor/moment.min.js',
          'vendor/quill.js',
          'vendor/saveSvgAsPng.js'
        ]
    stylesheets:
      joinTo:
        'app.css': /^app/
        'libraries.css': [
          'vendor/bootstrap.css',
          'vendor/bootstrap-datepicker3.css',
          'vendor/chosen.min.css',
          'vendor/select2.css',
          'vendor/quill.snow.css'
        ]
        'svg.css': [
          'vendor/chartist.min.css',
          'app/styles/svg.scss'
        ]
    templates:
      joinTo:
        'templates.js': [
          /^app/
        ]
