/**
 * Copyright (c) 2008 The Open Planning Project
 * 
 * @requires Styler.js
 */
Ext.namespace("Styler");

/**
 * Function: Styler.dispatch
 * Allows multiple asynchronous sequences to be called in parallel.  A final
 *     callback is called when all other sequences report that they are done.
 * 
 * Parameters:
 * functions - {Array(Function)} List of functions to be called.  All
 *     functions will be called with two arguments - a callback to call when
 *     the sequence is done and a storage object
 * complete - {Function} A function that will be called when all other
 *     functions report that they are done.  The final callback will be
 *     called with the storage object passed to all other functions.
 * scope - {Object} Optional object to be set as the scope of all functions
 *     called.
 */
Styler.dispatch = function(functions, complete, scope) {
    var requests = functions.length;
    var responses = 0;
    var storage = {};
    function respond() {
        ++responses;
        if(responses === requests) {
            complete.call(scope, storage);
        }
    }
    function trigger(index) {
        window.setTimeout(function() {
            functions[index].apply(scope, [respond, storage]);
        });
    }
    for(var i=0; i<requests; ++i) {
        trigger(i);
    }
};
