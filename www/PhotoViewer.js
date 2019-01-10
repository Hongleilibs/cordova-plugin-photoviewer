
var exec = require('cordova/exec');

exports.showMultiple = function(urlWithTitles, position, options) {
	if(typeof options == "undefined"){
        options = {};
    }
	if(!Array.isArray(urlWithTitles)) {
		throw "url必须提供数组";
	}
	if(!Number.isInteger(position)){
		throw "position必须是数字";
	}
	var positionInt = parseInt(position);
	if(positionInt >= urlWithTitles.length || positionInt < 0 ){
		throw "position超出界限";
	} 
	for (var i = 0; i < urlWithTitles.length; i++) {
		var item = urlWithTitles[i];
		var title = item["title"] = item["title"] || "";
		var description = item["description"] = item["description"] || ""
		var url = item["url"] = item["url"] || "";
		if(typeof(title) !== "string") {
			throw "urls数组包含错误的图片标题";
		}
		if(typeof(title) !== "string") {
        	throw "urls数组包含错误的图片描述";
        }
		if(typeof(url) !== "string") {
			throw "urls数组包含错误的图片url";
		}
	}

	options.img_array = options.img_array || urlWithTitles;
    exec(function(){}, function(){}, "PhotoViewer", "showMultiple", ["", position, options]);
}