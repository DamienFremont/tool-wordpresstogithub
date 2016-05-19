package com.damienfremont.blog;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.google.common.base.Preconditions;

public class Downloader {

	String url;
	String target;

	public Downloader(String url, String target) {
		this.url = url;
		this.target = target;
	}

	public void down() throws Exception {
		System.out.println("url=" + url);
		checkArgument(isNotEmpty(url), "Url cannot be empty!");

		prepare();

		DesiredCapabilities cap = initProxy();
		WebDriver driver = new HtmlUnitDriver(cap);

		System.out.println("read from " + url);
		driver.get(url);

		download(driver, target);
	}

	private DesiredCapabilities initProxy() {
		System.setProperty("jsse.enableSNIExtension", "false");

		DesiredCapabilities cap = new DesiredCapabilities();
		// if (isNotEmpty(proxy)) {
		// String p_host = proxy.split(":")[0];
		// String p_port = proxy.split(":")[1];
		// System.setProperty("http.proxyHost", p_host);
		// System.setProperty("http.proxyPort", p_port);
		// System.setProperty("https.proxyHost", p_host);
		// System.setProperty("https.proxyPort", p_port);
		//
		// org.openqa.selenium.Proxy p = new org.openqa.selenium.Proxy();
		// p.setHttpProxy(proxy).setFtpProxy(proxy).setSslProxy(proxy);
		// cap.setCapability(CapabilityType.PROXY, p);
		// }
		return cap;
	}

	private void prepare() {
		File file = new File(target + "/screenshots");
		System.out.println("clean " + file.getAbsolutePath());
		file.mkdirs();
		final File[] files = file.listFiles();
		for (File f : files) {
			f.delete();
		}
	}

	private void download(WebDriver driver, String target) throws Exception {
		PrintWriter writer = new PrintWriter(target + "/README.md", "UTF-8");
		WebElement article = driver.findElement(By.cssSelector("article"));

		WebElement title = article.findElement(By.cssSelector("header.entry-header h1"));
		writer.println(title.getText());
		writer.println("======");
		writer.println(" ");

		// TODO tags, date, author

		WebElement content = article.findElement(By.cssSelector("div.entry-content"));

		List<WebElement> childs = getChilds(content);
		eval(writer, childs);

		// SOURCE
		writer.println(" ");
		writer.println(format("[%s](%s)", url, url));
		writer.println(" ");

		writer.close();
	}

	private void eval(PrintWriter writer, List<WebElement> childs) throws Exception {
		for (WebElement i : childs) {
			eval(writer, i);
		}
	}

	private void eval(PrintWriter writer, WebElement i) throws Exception {
		String type = i.getTagName();
		switch (type) {
		case "h1":
			writer.println(format("# %s", i.getText()));
			writer.println(" ");
			break;
		case "h2":
			writer.println(format("## %s", i.getText()));
			writer.println(" ");
			break;
		case "h3":
			writer.println(format("## %s", i.getText()));
			writer.println(" ");
			break;
		case "ol":
		case "ul":
			for (WebElement li : getChilds(i)) {
				writer.println(format("* %s", li.getText()));
			}
			writer.println(" ");
			break;
		case "img":
			String src = i.getAttribute("src");
			String uri = downloadImg(src);
			writer.println(format("![alt text](%s)", uri));
			writer.println(" ");
			break;
		case "a":
			if (hasNotChilds(i))
				writer.println(format("[%s](%s)", i.getText(), i.getAttribute("href")));
			else {
				List<WebElement> a_childs = getChilds(i);
				eval(writer, a_childs);
			}
			break;
		case "p":
			// TODO links in p
			if (!hasNotChilds(i)) {
				List<WebElement> p_childs = getChilds(i);
				eval(writer, p_childs);
			}
			writer.println(i.getText());
			writer.println(" ");
			break;
		case "div":
			if (isCode(i)) {
				writeCode(writer, i);
				writer.println(" ");
			} else if (isWordpress(i)) {
				System.out.println("skipping wordpress part: " + i);
			} else {
				List<WebElement> a_childs = getChilds(i);
				eval(writer, a_childs);
			}
			break;
		}
	}

	private boolean isWordpress(WebElement i) {
		try {
			boolean share = i.getAttribute("class").contains("share");
			return share;
		} catch (Exception e) {
			return false;
		}
	}

	private void writeCode(PrintWriter writer, WebElement i) {
		String type = getCodeType(i);
		List<String> lines = getCodeLines(i);
		writer.println("```" + type);
		for (String line : lines) {
			writer.println(line);
		}
		writer.println("```");
	}

	private List<String> getCodeLines(WebElement code) {
		List<String> lines = new ArrayList<>();
		WebElement container = code.findElement(By.cssSelector("table .container"));
		List<WebElement> containerLines = getChilds(container);
		for (WebElement containerLine : containerLines) {
			lines.add(containerLine.getText());
		}
		return lines;
	}

	private String getCodeType(WebElement code) {
		String classes = code.getAttribute("class");
		String type = null;
		if (classes.contains("xml")) {
			type = "xml";
		} else if (classes.contains("java")) {
			type = "java";
		} else if (classes.contains("jscript")) {
			type = "javascript";
		} else if (classes.contains("css")) {
			type = "css";
		} else if (classes.contains("ruby")) {
			type = "ruby";
		} else { // PLAIN
			type = "";
		}
		Preconditions.checkNotNull(type, "checkNotNull codeType: " + classes);
		return type;
	}

	private boolean isCode(WebElement i) {
		try {
			return i.getAttribute("class").contains("syntaxhighlighter");
		} catch (Exception e) {
			return false;
		}
	}

	static SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHHmmssSSS");

	private String downloadImg(String imgUrl) throws MalformedURLException, IOException {
		String uri = String.format("screenshots/%s.%s", //
				sdf.format(new Date()), //
				getExtension(new URL(imgUrl)));
		File file = new File(target + "/" + uri);
		downloadImg(new URL(imgUrl), file);
		return uri;
	}

	private String getExtension(URL url) {
		String extension = FilenameUtils.getExtension(url.toString());
		return extension.substring(0, 3);
	}

	private void downloadImg(URL url, File file) throws IOException, MalformedURLException {
		System.out.println(String.format("saving %s from img %s", file.getAbsolutePath(), url));
		URLConnection conn = url.openConnection();
		conn.setRequestProperty("User-Agent",
				"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:31.0) Gecko/20100101 Firefox/31.0");
		conn.connect();
		FileUtils.copyInputStreamToFile(conn.getInputStream(), file);
	}

	private List<WebElement> getChilds(WebElement webElement) {
		return webElement.findElements(By.xpath("*"));
	}

	private boolean hasNotChilds(WebElement webElement) {
		return webElement.findElements(By.xpath("*")).isEmpty();
	}

}
