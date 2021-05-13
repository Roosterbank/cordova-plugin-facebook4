var exec = require('cordova/exec')

exports.logEvent = function logEvent (name, params, valueToSum, s, f) {
  // Prevent NSNulls getting into iOS, messes up our [command.argument count]
  if (!params && !valueToSum) {
    exec(s, f, 'FacebookConnectPlugin', 'logEvent', [name])
  } else if (params && !valueToSum) {
    exec(s, f, 'FacebookConnectPlugin', 'logEvent', [name, params])
  } else if (params && valueToSum) {
    exec(s, f, 'FacebookConnectPlugin', 'logEvent', [name, params, valueToSum])
  } else {
    f('Invalid arguments')
  }
}

exports.userIsChild = function (child, s, f) {
  exec(s, f, 'FacebookConnectPlugin', 'userIsChild', [child])
}

exports.setAdvertiserTracking = function (value, s, f) {
  exec(s, f, 'FacebookConnectPlugin', 'setAdvertiserTracking', [value])
}
