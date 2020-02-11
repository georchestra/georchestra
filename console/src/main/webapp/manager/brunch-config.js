 module.exports = {
    config: {
      plugins: {
        ng_templates: {
          module: 'manager',
          relativePath: 'app/'
        },
        babel: {
          pattern: /\.es6/,
          presets: ['@babel/env']
        }
      },
      files: {
        javascripts: {
          joinTo: {
            'app.js': /^app/,
            'libraries.js': [
              'vendor/jquery.js',
              'vendor/angular.js',
              'vendor/angular-resource.js',
              'vendor/angular-sanitize.js',
              'vendor/auto-complete.min.js',
              'vendor/bootstrap-datepicker.js',
              'vendor/bootstrap.min.js',
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
              'vendor/saveSvgAsPng.js',
              'vendor/ol.js',
              'vendor/FileSaver.js',
              'vendor/promise.min.js',
              'vendor/fetch.js',
              'vendor/moment-locale-de.js',
              'vendor/moment-locale-fr.js',
              'vendor/moment-locale-es.js'
            ]
          }
        },
        stylesheets: {
          joinTo: {
            'app.css': /^app/,
            'libraries.css': [
              'vendor/auto-complete.css',
              'vendor/bootstrap-datepicker3.css',
              'vendor/bootstrap.css',
              'vendor/chosen.min.css',
              'vendor/ol.css',
              'vendor/quill.snow.css',
              'vendor/select2.css'
            ],
            'svg.css': [
              'vendor/chartist.min.css',
              'app/styles/svg.scss'
            ]
          }
        },
        templates: {
          joinTo: {
            'templates.js': [/^app/]
          }
        }
      }
    }
  };
