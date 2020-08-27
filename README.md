GIF Search Proxy
=================

Usage
-------------
mvn package
mvn exec:java

A SSL server socket listening on port 7427 will start. Optionally the first argument will be interpretted as a custom port.
The server utilizes a self signed certificate. A PKCS12 formatted keystore is at ./giphyProxyKeystore. The passphrase is "password".
