(function() {
	
	/*
	The core scripts of the API. Currently is public through `window` but will be hidden in production mode.
	*/
	window.Core = {
		
		/*
		Returns a WhatsApp GroupMetadata object from a given group id.
		*/
		group: function(_id) {
			let result = null;
			Store.GroupMetadata.models.forEach(x => {
				if (x.hasOwnProperty("__x_id") && x.__x_id == _id) {
					result = x;
				}
			});
			return result;
		},
		
		/*
		Returns a WhatsApp Contact object from a given contact id.
		*/
		contact: function(_id) {
			let result = null;
			Store.Contact.models.forEach(x => {
				if (x.hasOwnProperty("__x_id") && x.__x_id == _id) {
					result = x;
				}
			});
			return result;
		},
		
		/*
		Returns a WhatsApp Chat object from a given chat id.
		*/
		chat: function(_id) {
			let result = null;
			Store.Chat.models.forEach(x => {
				if (x.hasOwnProperty("__x_id") && x.__x_id == _id) {
					result = x;
				}
			});
			return result;
		},
		
		/*
		Returns a WhatsApp Msg object from a given serialized message id
		*/
		msg: function(_id) {
			let result = null;
			Store.Msg.models.forEach(x => {
				if (x.hasOwnProperty("__x_id") && x.__x_id._serialized == _id) {
					result = x;
				}
			});
			return result;
		},
		
		/*
		Returns the element of a collection that satisfies a predicate condition.
		*/
		find: function(collection, predicate) {
			let result = null;
			collection.forEach(x => {
				if (predicate(x)) {
					result = x;
				}
			});
			return result;
		},
		
		/*
		Calls a callback with an error object.
		*/
		error: function(err, callback) {
			setTimeout(x => { (callback || Core.nop)({error: err}); }, 1);
		},
		
		/*
		Does nothing.
		*/
		nop: function() {}
		
	};
	
	/*
	API Listener - listens for new events (via messages) and handles them.
	*/
    const Listener = function () {

        this.ExternalHandlers = {

            /*
            Parameters:
                1. The user that joined
                2. The user that added them (undefined if they used a link? Should be checked)
                3. The chat the user was added to
            */
            USER_JOIN_GROUP: [],

            /*
            Parameters:
                1. The user that was removed
                2. The user that removed them (undefined if they used a link? Should be checked)
                3. The chat the user was removed from
            */
            USER_LEAVE_GROUP: [],

            /*
            Parameters:
                1. The group ID
                2. The user that changed the title
                3. The new title
                4. The subject type (should be 'subject')
            */
            GROUP_SUBJECT_CHANGE: [],

            /*
            Parameters:
                1. Sender of the message
                2. Chat the message was sent at
                3. Parsed Msg object
            */
            MESSAGE_RECEIVED: [],

            /*
            Parameters:
                1. The chat the message was sent to
                2. Parsed Msg object
            */
            MESSAGE_SENT: []
        };

        /*
        Handlers for different message types
        */
        const handlers = [
            /*
            User join / leave group.
            */
            {
                predicate: msg => msg.__x_isNotification && msg.__x_eventType == "i" && msg.__x_type == "gp2",
                handler: function (msg) {
                    const is_join = msg.__x_subtype == "add" || msg.__x_subtype == "invite";
                    const is_leave = msg.__x_subtype == "leave" || msg.__x_subtype == "remove";
                    const object = msg.__x_recipients[0];
                    const subject = msg.__x_sender;
                    const chat = msg.chat.__x_id;

                    if (is_join) {
                        API.listener.ExternalHandlers.USER_JOIN_GROUP.forEach(x => x(object, subject, chat));
                    } else if (is_leave) {
                        API.listener.ExternalHandlers.USER_LEAVE_GROUP.forEach(x => x(object, subject, chat));
                    }
                }
            },
            /*
            Group subject change.
            */
            {
                predicate: msg => msg.__x_isNotification && msg.__x_eventType == "n",
                handler: function (msg) {
                    const chat = msg.__x_to;
                    const changer = msg.__x_sender;
                    const new_title = msg.__x_body;
                    const subtype = msg.__x_subtype;
                    API.listener.ExternalHandlers.GROUP_SUBJECT_CHANGE.forEach(x => x(chat, changer, new_title, subtype));
                }
            },
            /*
            Message received
            */
            {
                predicate: msg => msg.__x_isUserCreatedType && !msg.__x_isNotification && !msg.__x_isSentByMe,
                handler: function (msg) {
                    let sender = msg.__x_sender;
                    let chat = msg.__x_from;
                    let message = msg.__x_id._serialized;
                    console.log(msg);
                    API.listener.ExternalHandlers.MESSAGE_RECEIVED.forEach(x => x(sender, chat, API.parseMsgObject(msg)));
                }
            },
            /*
            Message sent
            */
            {
                predicate: msg => msg.__x_isUserCreatedType && !msg.__x_isNotification && msg.__x_isSentByMe,
                handler: function (msg) {
                    let to = msg.__x_to;
                    API.listener.ExternalHandlers.MESSAGE_SENT.forEach(x => x(to, API.parseMsgObject(msg), msg));
                }
            }
        ];

        /*
        Handles a new incoming message
        */
        let handle_msg = function (msg) {
            for (let i = 0; i < handlers.length; i++) {
                if (handlers[i].predicate(msg)) {
                    handlers[i].handler(msg);
                    console.log("Firing handler " + i);
                    return;
                }
            }
            console.log("No suitable handlers were found for ", msg);
        };

        /*
        Goes through messages and filters new ones out. Then calls handle_msg on the newly created ones.
        */
        let check_update = function () {

            if (window.Store) {
                Store.Msg.models.forEach(model => {
                    if (model.__x_isNewMsg) {
                        model.__x_isNewMsg = false;
                        handle_msg(model);
                    }
                });
            } else {
                window.makeStore();
            }
        };

        /*
        Clears previously created listeners and starts a new one.
        */
        this.listen = function () {
            if (window.API_LISTENER_TOKEN) {
                clearInterval(window.API_LISTENER_TOKEN);
            }

            window.API_LISTENER_TOKEN = setInterval(check_update, 10);
        };

    };

    /*
    This is the API, which contains functions, literals, constants and utilities to integrate with WhatsApp Web version.
    */
	window.API = {
		
		/*
		Exception constants.
		*/
		Error: {
			OK: true,
			USER_NOT_FOUND: "The specified user ID was not found",
			CHAT_NOT_FOUND: "The specified chat ID was not found",
			GROUP_NOT_FOUND: "The specified group metadata ID was not found",
			USER_NOT_IN_GROUP: "The specified user is not a member of the required group"
		},
		
		/*
		Returns the contact ID from a given phone number.
		Only digits in the phone number. Example: "972557267388" and not "(+972) 055-726-7388"
		*/
		findContactId: function(phone_number) {
            let result = null;
            Store.Contact.models.forEach(x => {
				if (x.hasOwnProperty("__x_id") && (x.__x_id.match(/\d+/g) || []).join("") == phone_number) {
					result = x.__x_id;
				}
			});
			return result || null;
		},
		
		/*
		Returns an array of chat ID's that correspond to chats with the parameter in the title.
		For example, calling it with title='John' may return the ID's of the chats: 'John Smith', 'John from the cafeteria', and 'Johnna\'s birthday party 2016'
		*/
		findChatIds: function(title) {
            const result = [];
            Store.Chat.models.forEach(x => {
				if (x.hasOwnProperty("__x_formattedTitle") && ~(x.__x_formattedTitle.indexOf(title))) {
					result.push(x.__x_id);
				}
			});
			return result;
		},
		
		listener: new Listener(),
		
		/*
		Adds a user to a group.
		Parameters:
			user_id - the ID of the user (NOT the phone number)
			group_id - the ID of the group
			callback - to be invoked after the operation finishes
		*/
		addUserToGroup: function(user_id, group_id, callback) {
            let group = Core.group(group_id);
            let user = Core.contact(user_id);

            if (group == null) {
				Core.error(API.Error.GROUP_NOT_FOUND, callback);
				return;
			}
			if (user == null) {
				Core.error(API.Error.USER_NOT_FOUND, callback);
				return;
			}
			
			group.participants.addParticipant(user).then(callback);
		},
		
		/*
		Removes a user from a group.
		Parameters:
			user_id - the ID of the user (NOT the phone number)
			group_id - the ID of the group
			callback - to be invoked after the operation finishes
		*/
		removeUserFromGroup: function(user_id, group_id, callback) {
            let group = Core.group(group_id);
            if (group == null) {
				Core.error(API.Error.GROUP_NOT_FOUND, callback);
				return;
			}

            let user = Core.find(group.participants, x => x.hasOwnProperty("__x_id") && x.__x_id == user_id);
            if (user == null) {
				Core.error(API.Error.USER_NOT_IN_GROUP, callback || Core.nop);
				return;
			}
			
			group.participants.removeParticipant(user).then(callback);
		},
		
		/*
		Sets a chat's archived status
		Parameters:
			chat_id - the ID of the conversation
			archive_status - true for archiving, false for unarchiving.
			callback - to be invoked after the operation finishes
		*/
		setChatArchiveStatus: function(chat_id, archive_status, callback) {
            let chat = Core.chat(chat_id);
            if (chat == null) {
				Core.error(API.Error.CHAT_NOT_FOUND, callback);
				return;
			}
			
			chat.setArchive(!!archive_status).then(function() {
				(callback || Core.nop)({status: 200});
			});
		},
		
		/*
		Gets the archive status of a chat
		Parameters:
			chat_id - the ID of the conversation
		Return value:
			bool - true if archived, false if not archived.
			null - if chat was not found
		*/
		getChatArchiveStatus: function(chat_id) {
            let chat = Core.chat(chat_id);
            if (chat == null) {
				return null;
			}
			
			return chat.archive;
		},
		
		/*
		Gets the invite link for a group.
		Parameters:
			group_id - the ID of the group
		Return value:
			string - the invite link
			null - if the group was not found
		*/
		getGroupInviteLink: function(group_id) {
            let group = Core.group(group_id);
            if (group == null) {
				return null;
			}
			
			return group.groupInviteLink;
		},
		
		/*
		Revokes a group's invite link.
		Parameters:
			group_id - the ID of the group
			callback - to be invoked after the operation completes
		*/
		revokeGroupInviteLink: function(group_id, callback) {
            let group = Core.group(group_id);
            if (group == null) {
				Core.error(Core.Error.GROUP_NOT_FOUND, callback);
				return;
			}
			
			group.revokeGroupInvite().then(function(e) {
				(callback || Core.nop)({status: e});
			});
		},
		
		/*
		Sets a user's blocked status
		Parameters:
			user_id - the ID of the user to block/unblock
			blocked_status - true - blocked, false - unblocked
			callback - to be invoked after the operation completes
		*/
		setBlockedStatus: function(user_id, blocked_status, callback) {
            let user = Core.contact(user_id);
            if (user == null) {
				Core.error(API.Error.USER_NOT_FOUND, callback);
				return;
			}
			
			user.setBlock(blocked_status).then(function(e) {
				(callback || Core.nop)({status: e});
			});
		},
		
		/*
		Sends a text message in a given chat.
		Parameters:
			chat_id - the chat to send a message to.
			message_text - the plain text of the message.
			callback - to be invoked after the operation completes
		*/
		sendTextMessage: function(chat_id, message_text, callback) {
            let chat = Core.chat(chat_id);
            if (chat == null) {
				Core.error(API.Error.CHAT_NOT_FOUND, callback)
				return;
			}
			
			chat.sendMessage(message_text).then(function(e) {
				(callback || Core.nop)({status: e});
			});
		},
		
		/*
		Creates a new group.
		Parameters:
			group_subject - the title of the newly created group
			participants - an array of ids of users to add to the group
			callback - to be invoked after the operation completes
		
		Needs further testing
		*/
		createGroup: function(group_subject, participants, callback) {
            let p = [];
            for (let x = 0; x < participants.length; x++)
			{
				p.push(Core.contact(participants[x]));
			}
			
			Store.Chat.createGroup(group_subject, null, null, p, undefined).then(callback);
		},
		
		/*
		Retrieves contact info for a certain id
		Parameters:
			user_id - the id of the user to look for
		Return value:
			object - the details
			null - if the user was not found
		*/
		getContactInfo: function(user_id) {
            const contact = Core.contact(user_id);
            if (contact == null || !contact["all"]) {
				return null;
			}
			
			return contact.all;
		},
		
		/*
		Retrieves message info for a certain id
		Parameters:
			message_id - the id of the message to look for
		Return value:
			object - the details
			null - if the message was not found in any chat
		*/
		getMessageInfo: function(message_id) {
			return Core.find(Store.Msg.models, x => x.__x_id._serialized == message_id);
		},
		
		/*
		Returns a list of all contact IDs.
		Return Value:
			array - the array of strings containing the IDs of the clients.
		*/
		getContactList: function() {
            const result = [];
            Store.Contact.models.forEach(x => { result.push(x.__x_id); });
			return result;
		},
		
		/*
		Sends contact(s) to a chat.
		Parameters: 
			chat_id - the ID of the chat to write the contacts to
			contacts - the contact / array of contacts to send
			callback - to be incoked after the operation completes
			
		Needs further testing.
		*/
		sendContactMessage: function(chat_id, contacts, callback) {
			if (contacts.constructor != Array) {
				contacts = [contacts];
			}

            const chat = Core.chat(chat_id);
            if (chat == null) {
				Core.error(API.Error.CHAT_NOT_FOUND, callback);
				return;
			}

            const toSend = [];
            contacts.forEach(x => {
                const c = Core.contact(x);
                if (c != null) {
					toSend.push(c);
				}
			});
			
			if (!toSend.length) {
				Core.error(API.Error.USER_NOT_FOUND, callback);
				return;
			}
			
			if (toSend.length == 1) {
				chat.sendContact(toSend[0]);
			}
			else {
				chat.sendContactList(toSend);
			}
		},
		
		/*
			Send the other side "<you> is typing..."
			chat_id - the chat id
		*/
		sendTyping: function(chat_id) {
            const chat = Core.chat(chat_id);
            if (chat == null) {
				return API.Error.CHAT_NOT_FOUND;
			}
			
			chat.markComposing();
		},
		
		/*
			Send the other side "<you> is recording audio..."
			chat_id - the chat id
		*/
		sendRecording: function(chat_id) {
            const chat = Core.chat(chat_id);
            if (chat == null) {
				return API.Error.CHAT_NOT_FOUND;
			}
			
			chat.markRecording();
		},
		
		/*
			Hides the "<you> is recording audio..." to the other side
			chat_id - the chat id
		*/
		sendStopRecording: function(chat_id) {
            const chat = Core.chat(chat_id);
            if (chat == null) {
				return API.Error.CHAT_NOT_FOUND;
			}
			
			chat.markPaused();
		},
		
		/*
		Initializes a group (required to call before interacting with a group).
		*/
		initGroup(group_id, callback) {
            const group = Core.group(group_id);
            if (group == null) {
				return API.Error.GROUP_NOT_FOUND;
			}
			
			group.update().then(callback);
		},
		
		/*
		Minimizes a message object to a JSON convertable object for sending over network (smaller size than a huge Msg object)
		Parameters:
			msg_object - the message object to convert to JSON compatible type
		*/
		parseMsgObject: function(msg_object) {
            const m = msg_object.all;
            if (msg_object["__x__quotedMsgObj"]) {
				m.quotedMsg = API.parseMsgObject(Core.msg(msg_object.__x__quotedMsgObj.__x_id._serialized));
			}
			m.chat = m.chat.all;
			delete m.msgChunk;
			return m;
		},
		
		/*
		Initializes the API and sets the window.Store object.
		*/
		init: function() {
			window.makeStore();
			window.API.listener.listen();
		}
	};		
})();

window.makeStore = function() {
	if (!window.Store) {
		(function() {
			function getStore(modules) {
				let foundCount = 0;
				let neededObjects = [
					{ id: "Store", conditions: (module) => (module.Chat && module.Msg) ? module : null },
					{ id: "Wap", conditions: (module) => (module.createGroup) ? module : null },
					{ id: "MediaCollection", conditions: (module) => (module.default && module.default.prototype && module.default.prototype.processFiles !== undefined) ? module.default : null },
					{ id: "WapDelete", conditions: (module) => (module.sendConversationDelete && module.sendConversationDelete.length == 2) ? module : null },
					{ id: "Conn", conditions: (module) => (module.default && module.default.ref && module.default.refTTL) ? module.default : null },
					{ id: "WapQuery", conditions: (module) => (module.queryExist) ? module : null },
					{ id: "ProtoConstructor", conditions: (module) => (module.prototype && module.prototype.constructor.toString().indexOf('binaryProtocol deprecated version') >= 0) ? module : null },
					{ id: "UserConstructor", conditions: (module) => (module.default && module.default.prototype && module.default.prototype.isServer && module.default.prototype.isUser) ? module.default : null }
				];

				for (let idx in modules) {
					if ((typeof modules[idx] === "object") && (modules[idx] !== null)) {
						let first = Object.values(modules[idx])[0];
						if ((typeof first === "object") && (first.exports)) {
							for (let idx2 in modules[idx]) {
								let module = modules(idx2);
								if (!module) {
									continue;
								}

								neededObjects.forEach((needObj) => {
									if(!needObj.conditions || needObj.foundedModule) return;
									let neededModule = needObj.conditions(module);
									if(neededModule !== null) {
										foundCount++;
										needObj.foundedModule = neededModule;
									}
								});

								if(foundCount === neededObjects.length) {
									break;
								}
							}

							let neededStore = neededObjects.find((needObj) => needObj.id === "Store");
							window.Store = neededStore.foundedModule ? neededStore.foundedModule : {};
							neededObjects.splice(neededObjects.indexOf(neededStore), 1);
							neededObjects.forEach((needObj) => {
								if(needObj.foundedModule) {
									window.Store[needObj.id] = needObj.foundedModule;
								}
							});

							return window.Store;
						}
					}
				}
			}

			webpackJsonp([], {'parasite': (x, y, z) => getStore(z)}, 'parasite');
		})();
	}
};

API.init();

let success = 0;
let failure = 0;

function sendNext(list, progress, size){
    let entry = list[progress];
    window.API.sendTyping(entry.msisdn+"@c.us");
    window.API.sendTextMessage(entry.msisdn+"@c.us",entry.message,function (event){
        if (event.status!=="success"){
            console.error("Failed sending message: "+entry.message+" to MSISDN: "+entry.msisdn+" Reason: "+event.error);
            failure++;
        }
        else {
            console.log("Successfully sent message: "+entry.message+" to MSISDN: "+entry.msisdn);
            success++;
        }
        progress++;
        if (progress!==size){
            sendNext(list, progress, size);
        }
        else {
            alert("Processed "+(success+failure)+" Entries. Success: "+success+" Failure: "+failure+". See browser console for logs.");
            success = 0;
            failure = 0;
        }
    });
}

let list = [];
let ele = document.createElement("div");
ele.hidden = true;
let csv = document.createElement("input");
csv.type = "file";
csv.id = "csv";
csv.onchange = function(event){
    let file = event.target.files[0], read = new FileReader();

    read.readAsBinaryString(file);

    read.onloadend = function(){
        list = JSON.parse(read.result);
    }
};
let btn = document.createElement("input");
btn.type = "submit";
btn.id = "sender";
btn.innerHTML = "Send";
btn.onclick = function(){
    let size = list.length;
    sendNext(list, 0, size)
};
ele.appendChild(btn);
ele.appendChild(csv);
document.getElementById("app").prepend(ele);
