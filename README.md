GIF Search Proxy
=================

Usage
-------------
mvn package
mvn exec:java

A SSL server socket listening on port 7427 will start. Optionally the first argument will be interpretted as a custom port.
The server utilizes a self signed certificate. A PKCS12 formatted keystore is at ./giphyProxyKeystore. The passphrase is "password".

Notes
-------------
Current implementation uses the RateLimiter class from Google Guava. A previous implementation utilized a thread pool instead.

TODO
-------------
Whitelist should be improved to utilize an external source for population.
Want to improve upon handling of stuck sockets. Currently just timing out after 10 seconds.
If expanded upon would like to add an additional dependency for a better logging system. 
Currently utilizing curl to test, but given more complexity and time more formal testing tools would be a good idea.
