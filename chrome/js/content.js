(function() {
	// For receiving messages from WhatsApp
    let messageFromClient = function (data) {
        chrome.runtime.sendMessage(data);
    };

    // Constructing a new messaging DOM element
    let messaging = document.createElement("div");
    messaging.id = "whatsapp_messaging";
	
	// Attaching a listener for messages from WhatsApp
	messaging.addEventListener("whatsapp_message", function(e) {
		messageFromClient(e.detail);
	});
	
	// Injecting the element
	(document.head || document.documentElement).appendChild(messaging);
	
	// For sending messages to WhatsApp
    let clientMessage = function (data) {
        document.getElementById("whatsapp_messaging").dispatchEvent(new CustomEvent("content_message", {"detail": data}));
    };

    // Listening for messages from background script
	chrome.runtime.onMessage.addListener(function(request, sender) {
		if (!sender.tab) {
			// Message from background script
			clientMessage(request);
		}
	});
	
	// Loading the API
    let el = document.createElement("script");
    el.src = chrome.extension.getURL("js/api.js");
	(document.head || document.documentElement).appendChild(el);
	
	// Loading the WhatsBot script
    el = document.createElement("script");
    el.src = chrome.extension.getURL("js/whatsapp.js");
	(document.head || document.documentElement).appendChild(el);
})();