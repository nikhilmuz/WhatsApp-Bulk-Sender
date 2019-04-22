window.onload = function() {
	document.getElementById("verspan").innerHTML = chrome.runtime.getManifest().version;
	document.getElementById("sender").onclick = function () {
        chrome.tabs.executeScript({code: 'document.getElementById("sender").click();'});
    };
	document.getElementById("csv").onclick = function () {
        chrome.tabs.executeScript({code: 'document.getElementById("csv").click();'});
    }
};
