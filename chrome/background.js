/*
This variable is responsible for keeping track of the active WhatsApp tab.
It is updated once the tab communicates with the background page (sends a type:start request)
*/
let whatsapp_tab_id = -1;

/*
Injecting the content script into the WhatsApp webpage and resetting the connection settings to the host.
*/
chrome.webNavigation.onCompleted.addListener(function(details) {
	
	if (~details.url.indexOf("https://web.whatsapp.com/")) {
		console.log("Injecting");
		
		whatsapp_tab_id = details.tabId;
		console.log(whatsapp_tab_id);
		
		// Injecting the content script into the page.
		chrome.tabs.executeScript(details.tabId, {file: "js/content.js"});
	}
});

/*
Passes a message to the client (dict-obj)
Full journey from here - to content script, then to the webpage through the DOM
*/
function clientMessage(data) {
	chrome.tabs.sendMessage(whatsapp_tab_id, data);
}

/*
Listening to messages from the content script (webpage -> content script -> *background page* -> host)
*/
chrome.runtime.onMessage.addListener(function(request, sender) {
	if (sender.tab) {
		// From content script
		console.log("Background page received: ", request, sender);
		
		if (request.type === "ajax") {
            let m = request.ajax;
            m.success = function(e) {
				clientMessage({type: "ajax", ajax_id: request.ajax_id, status: "success", e: e});
			};
			m.error = function(a, b, c) {
				clientMessage({type: "ajax", ajax_id: request.ajax_id, status: "error", a: a, b: b, c: c});
			};
			$.ajax(m);
		}
	}
});

