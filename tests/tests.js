/**
 * Jasmine Based test suites
 *
 * Several of facebookConnectPlugin APIs cannot be automatically tested, because 
 * they depend on user interaction in order to call success or fail
 * handlers. For most of them, there is a basic test that assert the presence of 
 * the API.
 *
 * There are some cases that test automation can be applied, i.e in "getLoginStatus", 
 * "logEvent" and "logPurchase" methods. For those cases, there is some level 
 * of automatic test coverage.
 */
exports.defineAutoTests = function () {
    'use strict';
    
    describe('facebookConnectPlugin', function () {
    	it('should be defined', function () {
            expect(facebookConnectPlugin).toBeDefined();
        });        
                
        describe('logEvent', function () {
            it('should be defined', function () {
                expect(facebookConnectPlugin.logEvent).toBeDefined();
            });
            
            it('should be a function', function () {
                expect(typeof facebookConnectPlugin.logEvent).toEqual('function');
            });
            
            it('should succeed when called with valid arguments', function (done) {
                function onSuccess(data){
                    expect(data).toBeDefined();
                    done();
                }
                
                function onError(error){
                    expect(true).toEqual(false); // to make it fail
                    done();
                }
                
                facebookConnectPlugin.logEvent('test-event',{},0,onSuccess,onError);
            });
        });
    
    });
};

/**
 * Manual tests suites
 *
 * Some actions buttons to execute facebookConnectPlugin methods
 */
exports.defineManualTests = function (contentEl, createActionButton) {
    'use strict';
    
    /** helper function to log a messages in the log widget */
    function logMessage(message, color) {
        var log = document.getElementById('info'),
            logLine = document.createElement('div');
        
        if (color) {
            logLine.style.color = color;
        }
        
        logLine.innerHTML = message;
        log.appendChild(logLine);
    }

    /** helper function to clear the log widget */
    function clearLog() {
        var log = document.getElementById('info');
        log.innerHTML = '';
    }
    
    /** helper class to log a non implemented event */
    function testNotImplemented(testName) {
        return function () {
            console.error(testName, 'test not implemented');
        };
    }
    
    /** function called on deviceready event */
    function init() {}
        
    /** object to hold properties and configs */
    var TestSuite = {};
    
    TestSuite.$markup = '' +
        '<fieldset>' +
        
        '<fieldset>' +
            '<legend>Event</legend>' +
            
            'Event Name: <input type="text" id="eventNameInput"><br>' +
        
            '<h3>Log Event</h3>' +
            '<div id="buttonLogEvent"></div>' +
            'Expected result: should log an event with the given Event Name' +
        '</fieldset>' +        
        '';
        
    contentEl.innerHTML = '<div id="info"></div>' + TestSuite.$markup;
    
    TestSuite.getEventName = function () {
        return document.getElementById('eventNameInput').value;
    };
    
        
    createActionButton('logEvent', function () {
        clearLog();
        
        var eventName = TestSuite.getEventName();
        
        function onSuccess(data) {
            console.log('logEvent success, data written in console');
            var message = 'logEvent data: ' + JSON.stringify(data);
            logMessage(message,'green');
        }
        
        function onError (error) {
            console.error('logEvent fail, error written in console');
            var message = 'logEvent error: ' + JSON.stringify(error);
            logMessage(message,'red');
        }
        
        facebookConnectPlugin.logEvent(eventName,{},0,onSuccess,onError);  
    }, 'buttonLogEvent');
        
    document.addEventListener('deviceready', init, false);
};