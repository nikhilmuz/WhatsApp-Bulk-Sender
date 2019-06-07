# WhatsApp-Bulk-Sender
[![Platform](https://img.shields.io/badge/platform-Android%7CChrome-yellow.svg)](https://www.android.com)
![GitHub](https://img.shields.io/github/license/nikhilmuz/WhatsApp-Bulk-Sender.svg)
![Bitcoin](https://img.shields.io/keybase/btc/nikhilkr.svg)

Send bulk messages right from your WhatsApp Android app or WhatsApp Web

This application is meant for sending bulk messages through WhatsApp client on Android or Chrome Web Browser.<br>
<br>
To send messages via WhatsApp Web on Chrome:<br>
<br>
***Deprecated in latest WhatsApp web. Last worked on : 24<sup>th</sup> April, 2019***<br>
<br>
<ol>
<li>Open chrome browser and navigate to chrome://extensions/.</li>
<li>Enable <strong>Developer mode</strong> from top right.</li>
<li>Click "Load unpacked" and browse for chrome folder in this repo.</li>
<li>Open WhatsApp Web at https://web.whatsapp.com and login.</li>
<li>Click the extension icon from right side of omnibar on WhatsApp tab and click Upload and browse for json file containing numbers and messages as in folder samples.</li>
<li>Click on Submit to start sending messages.</li>
<li>Wait for alert on completion of task.</li>
<li>Get logs in the browser console.</li>
</ol>
<br>
You may create json from csv sheet having the column msisdn and message using any online tool like http://www.convertcsv.com/csv-to-json.htm just make sure to enter the contact numbers including country code without any + sign.<br/>
P.S. This method only works with the contacts you have initiated chat with. It will support other contacts too in future versions.<br/>
<p align="center"> <img height="500px" src="/samples/tutorial.gif" alt="Tutorial"/> </p><br/>
<br/>
To send messages using Android Application:<br>
<ol>
<li>Open this app and click on "Settings" button to launch Android Setting app so that you may allow accesibility service for this app which is required to auto send messages. If device is rooted (not recommended) there is no need to do same. Directly go to third step.</li>
<li>Go back to app activity.</li>
<li>Click "Browse" button to browse <strong>*.csv</strong> file having phone number in first column in format <strong>&lt;COUNTRY CODE&gt;&lt;MSISDN&gt;</strong> and message to send in second column.</li>
<li>Hit "Send" button.</li>
<li>Sit back and relax.</li>
</ol>
<br>
P.S. Add the contacts list on your device and sync it with WhatsApp before sending bulk SMSes on non-rooted phones use any utilities like [Google Contacts](https://contacts.google.com) for the same in order to import contacts from *.csv. It is recomended for rooted phones also due to WhatsApp policies.<br/>
<br/>
<strong>Find sample csv and json in "samples" folder after cloning the repository</strong>
