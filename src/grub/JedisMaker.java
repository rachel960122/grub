package grub;

import java.io.IOException;

import redis.clients.jedis.Jedis;

public class JedisMaker {
	public static Jedis make() throws IOException {
		String host = "viperfish.redistogo.com";
		int port = 10722;
		String auth = "4caa07f5909e22987d17383a630e0a31";
		
		Jedis jedis = new Jedis(host, port);
		try {
			jedis.auth(auth);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		
		return jedis;
	}
	
	public static void main(String[] args) throws IOException {
		Jedis jedis = make();
		jedis.set("key", "value");
		System.out.println("Got value: " + jedis.get("key"));
		jedis.flushDB();
		jedis.quit();
	}
}
