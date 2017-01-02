package grub;

import java.io.IOException;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.text.ParseException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

public class BlogFetcher {
	private Jedis jedis;
	
	public BlogFetcher(Jedis jedis) {
		this.jedis = jedis;
	}
	
	public void fetchAll() throws IOException, ParseException {
		fetchAirbnb();
	}

	public void fetchAirbnb() throws IOException, ParseException {
		System.out.println("Fetching new articles from Airbnb...");
		String url = "http://nerds.airbnb.com";
		Document doc = Jsoup.connect(url).get();
		boolean isEnd = false;
		int numNewPosts = 0;
		
		while (!isEnd) {
			Elements posts = doc.select("div[class*=post-tile]");
			for (Element post : posts) {
				String title = post.select("strong").text();
				String ref = post.select("a").first().attr("abs:href").split("/")[3];
				if (!jedis.sismember("airbnb", ref)) {
					numNewPosts++;
					int number = jedis.smembers("airbnb").size() + 1;
					jedis.sadd("airbnb", ref);
					Transaction t = jedis.multi();
					t.hset("airbnb" + number, "title", title);
					t.hset("airbnb" + number, "rating", "");
					t.hset("airbnb" + number, "comment", "");
					
					try {
						Document page = Jsoup.connect(url + "/" + ref).get();
						String[] timePieces = page.select("ul[class=list-unstyled]").first().children().get(2).text().split(" ");
						SimpleDateFormat dt = new SimpleDateFormat("MMMMM dd, yyyy");
						Date date = dt.parse(timePieces[0] + " " + timePieces[1] + " " + timePieces[2]);
						String formatted = new SimpleDateFormat("yyyy-MM-dd").format(date);
						t.hset("airbnb" + number, "date", formatted);
						t.exec();
					} catch (Exception e) {
						t.exec();
						continue;
					}
					
				} else {
					break;
				}
			}
			isEnd = doc.select("a[class=next page-numbers]").isEmpty();
			if (!isEnd) {
				int currentPage = Integer.valueOf(doc.select("span[class=page-numbers current]").text());
				int nextPage = currentPage + 1;
				doc = Jsoup.connect(url + "/page/" + nextPage + "/").get();
			}
		}
		System.out.println("Found " + numNewPosts + " new articles");
	}
	
	public static void main(String[] args) throws IOException, ParseException {
		Jedis jedis = JedisMaker.make();
		BlogFetcher fetcher = new BlogFetcher(jedis);
		fetcher.fetchAirbnb();
		int n = jedis.smembers("airbnb").size();
		for (int i = 1; i <= n; i++) {
			System.out.println(jedis.hgetAll("airbnb" + i));
		}
		//jedis.flushDB();
		jedis.quit();
	}
}
