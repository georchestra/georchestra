
// based on the esprima example: http://esprima.org/doc/#nestedternary
// see http://esprima.org/demo/parse.html# for parsed code structure

// Usage: node test_esprima.js /path/to/some/directory

/*jslint node:true sloppy:true plusplus:true */

var fs = require('fs'),
    esprima = require('esprima'),
    dirname = process.argv[2];

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
                return done(null, results);
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

walk(dirname, function (err, results) {
    if (err) {
        console.log('Error', err);
        return;
    }

    var errors=0;
    results.forEach(function (filename) {
        var shortname, first, content, syntax;

        shortname = filename;
        first = true;

        if (shortname.substr(0, dirname.length) === dirname) {
            shortname = shortname.substr(dirname.length + 1, shortname.length);
        }

        function report(node, problem) {
            if (first === true) {
                console.log(shortname + ': ');
                first = false;
            }
            console.log('  Line', node.loc.start.line, ':', problem);
        }

        function checkConditional(node) {
            var condition;

            if (node.consequent.type === 'ConditionalExpression' ||
                    node.alternate.type === 'ConditionalExpression') {

                condition = content.substring(node.test.range[0], node.test.range[1]);
                if (condition.length > 20) {
                    condition = condition.substring(0, 20) + '...';
                }
                condition = '"' + condition + '"';
                report(node, 'Nested ternary for ' + condition);
            }
        }
        
        function checkQuotes(node) {
			// '' quotes are allowed only if the string contains ""
			if ((node.raw[0] !== '"') && (node.value.indexOf('"') < 0)) {
				report(node, 'Incorrect use of quotes: ' + node.raw);
				errors++;
				return 1;
			}
			return 0;
		}
		
		/* Find "Property" objects with raw beginning with other thing that "\"" */
		function checkProperty(node) {
			return checkQuotes(node.key) || checkQuotes(node.value);
		}

        try {
            content = fs.readFileSync(filename, 'utf-8');
            syntax = esprima.parse(content, { tolerant: true, loc: true, range: true, raw: true });
            traverse(syntax, function (node) {
                if (node.type === 'Property') {
                    return checkProperty(node);
                }
            });
        } catch (e) {
        }

    });
    
    console.log('\nnodejs_geor_rules.js - end of the test - number of errors: ' + errors);
    process.exit(errors);
});


