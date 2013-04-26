// Usage: node js_code_review.js /path/to/some/directory /path/to/other/directory

// For javascript code review, see http://forge.applis-bretagne.fr/issues/4297

// based on the esprima example: http://esprima.org/doc/#nestedternary
// see http://esprima.org/demo/parse.html# for parsed code structure

/*jslint node:true sloppy:true plusplus:true */

var fs = require('fs'),
    esprima = require('esprima'),
    async = require('async'),
    dirs = process.argv.slice(2);

// Executes visitor on the object and its children (recursively).
function traverse(object, visitor) {
    var key, child;

    visitor.call(null, object);
    for (key in object) {
        if (object.hasOwnProperty(key)) {
            child = object[key];
            if (typeof child === 'object' && child !== null) {
                traverse(child, visitor);
            }
        }
    }
}

// http://stackoverflow.com/q/5827612/
function walk(dir, done) {
    var results = [];
    fs.readdir(dir, function (err, list) {
        if (err) {
            return done(err);
        }
        var i = 0;
        (function next() {
            var file = list[i++];
            if (!file) {
                return done(null, {'dir': dir, 'results': results});
            }
            file = dir + '/' + file;
            fs.stat(file, function (err, stat) {
                if (stat && stat.isDirectory()) {
                    walk(file, function (err, res) {
                        results = results.concat(res);
                        next();
                    });
                } else {
                    results.push(file);
                    next();
                }
            });
        }());
    });
}

async.map(dirs, walk, function (err, results) {
    if (err) {
        console.log('Error', err);
        return;
    }

    var errors=0;
    results.forEach(function (result) {
        console.log('Check js code in ' + result.dir);
        result.results.forEach(function (filename) {
            var shortname, dirname, first, content, syntax;

            shortname = filename;
            dirname = result.dir;
            first = true;

            if (shortname.substr(0, dirname.length) === dirname) {
                shortname = shortname.substr(dirname.length + 1, shortname.length);
            }

            function report(node, problem) {
                if (first === true) {
                    console.log('  ' + shortname + ': ');
                    first = false;
                }
                console.log('    Line', node.loc.start.line, ':', problem);
            }

            function checkQuotes(node) {
                // '' quotes are allowed only if the string contains ""
                if ((node.raw[0] !== '"') && (node.value.indexOf('"') < 0)) {
                    report(node, 'Incorrect use of quotes: ' + node.raw);
                    errors++;
                }
            }

            // Find "Property" objects with raw beginning with other thing that "\""
            function checkProperty(node) {
                checkQuotes(node.key);
                checkQuotes(node.value);
            }

            try {
                content = fs.readFileSync(filename, 'utf-8');
                syntax = esprima.parse(content, { tolerant: true, loc: true, range: true, raw: true });
                traverse(syntax, function (node) {
                    if (node.type === 'Property') {
                        checkProperty(node);
                    }
                });
            } catch (e) {
            }
        })
    });
    
    console.log(errors + ' errors');
    process.exit(errors);
});

/*
// How to use an iterator - with async module
function toto(item, callback) {
    console.log(item);
    callback(null, item + ' done');
}

async.map(dirs, toto, function(err, results){
    // results is now an array of the name of each dir + ' done'
    console.log(results);
});
*/
