package giphyproxy;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;

public class AllowedURLs {
	private Set<String> whitelistedSites = new HashSet<String>();

	public AllowedURLs() {
		// in the future use a file and/or a reverse dns lookup
		// maybe should use regex
		this.addURL("api.giphy.com");
		this.addURL("api.giphy.com.");
		// Using an IP doesn't work?
		this.addURL("199.232.34.2");
		this.addURL("151.101.202.2");
		this.addURL("151.101.206.2");
	}

	public void addURL(String url) {
		whitelistedSites.add(url);
	}


	public boolean validURL(String url) {
		if (whitelistedSites.contains(url)) {
			return true;
		}

		try {
			// giphy api has multiple ips if given an ip need to
			InetAddress host = InetAddress.getByAddress(url.getBytes());

			System.out.println(host.getHostName());
			return whitelistedSites.contains(host.getHostName());
			
		} catch (UnknownHostException e) {
			return false;
		}
	}
}

