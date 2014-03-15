// auto generated by jp.co.worksap.jax_rs.ApiScriptGenerator
define(['jquery', 'exports'], function($, exports) {
  'use strict';
  var baseURL = $('meta[name="app-data"]').data('context-path') + '/resources';
  exports.load = function (data) {
    return $.ajax({
        cache: false,
        url: baseURL + '/sample/',
        type: 'get',
        data: {'message':data.message}
    }).promise();
  };
  exports.postCat = function (data) {
    return $.ajax({
        cache: false,
        url: baseURL + '/sample/cat',
        type: 'post',
        data: {'foo':data.foo,'bar':data.bar}
    }).promise();
  };
  exports.callDog = function (data) {
    return $.ajax({
        cache: false,
        url: baseURL + '/sample/dog/' + encodeURI(data.name),
        type: 'put',
        data: {}
    }).promise();
  };
});