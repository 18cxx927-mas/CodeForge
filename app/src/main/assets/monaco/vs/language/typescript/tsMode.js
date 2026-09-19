"use strict";
define("vs/language/typescript/tsMode", ["require", "exports"], function (require, exports) {
    exports.setupTypeScript = function (defaults) {
        return Promise.resolve();
    };
    exports.setupJavaScript = function (defaults) {
        return Promise.resolve();
    };
    exports.getTypeScriptWorker = function () {
        return Promise.resolve(function () { return Promise.resolve(); });
    };
    exports.getJavaScriptWorker = function () {
        return Promise.resolve(function () { return Promise.resolve(); });
    };
});
