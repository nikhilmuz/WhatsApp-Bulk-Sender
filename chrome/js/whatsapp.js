window.ajaxes = {};

/*
Sends a message to the background page (content script -> background page [-> host])
*/
window.serverMessage = function(data) {
	document.getElementById("whatsapp_messaging").dispatchEvent(new CustomEvent("whatsapp_message", { "detail": data }));
};

/*
Receiving messages from the host/background page
*/
window.messageFromServer = function(data) {
	console.log("Received message from server ", data);
	
	if (data.type === "ajax") {
		if (window.ajaxes[data.ajax_id]) {
			if (data.status === "success") {
				window.ajaxes[data.ajax_id].success(data.e);
			}
			else {
				window.ajaxes[data.ajax_id].error(data.a, data.b, data.c);
			}
			delete window.ajaxes[data.ajax_id];
		}
	}
};

/*
For the DOM communication between the page and the content script
*/
document.getElementById("whatsapp_messaging").addEventListener("content_message", function(e) {
	messageFromServer(e.detail);
});

window.$ajax = function(params) {
	let success = null, error = null;
	if (params.success) {
		success = params.success;
		params.success = null;
	}
	if (params.error) {
		error = params.error;
		params.error = null;
	}
	ajax_id = Math.random();
	
	window.ajaxes[ajax_id] = {success: success, error: error};
	
	serverMessage({
		type: "ajax",
		ajax_id: ajax_id,
		ajax: params
	});
};