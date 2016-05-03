package com.damienfremont.blog;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isEmpty;
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

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.google.common.base.Preconditions;

public class App {

	static String url;
	static String proxy;
	static String target;

	public static void main(String[] args) throws Exception {
		url = arg("url", args);
		proxy = arg("proxy", args);
		target = arg("target", args, "README.md");
		if (isEmpty(url)) {
			initByUIpopup();
		}
		System.out.println("url="+url);
		checkArgument(isNotEmpty(url), "Url cannot be empty!");

		prepare();

		DesiredCapabilities cap = initProxy();
		WebDriver driver = new HtmlUnitDriver(cap);

		System.out.println("read from " + url);
		driver.get(url);

		download(driver, target);
	}

	private static void initByUIpopup() {
		System.out.println("no CLI args provided, trying with UI popup!");

		JTextField field1 = new JTextField("");
		JTextField field2 = new JTextField("");
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.add(new JLabel("Wordpress post URL (*)"));
		panel.add(field1);
		panel.add(new JLabel("Proxy (optionnal)"));
		panel.add(field2);
		JOptionPane.showConfirmDialog(null, panel,
				"Wordpress post to GitHub README.md",
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		
		url = field1.getText();
		proxy = field2.getText();

		if (isEmpty(url)) {
			JOptionPane.showConfirmDialog(null, "Url cannot be empty!", "Error",
					JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE);
		}
	}

	private static DesiredCapabilities initProxy() {
		System.setProperty("jsse.enableSNIExtension", "false");

		DesiredCapabilities cap = new DesiredCapabilities();
		if (isNotEmpty(proxy )) {
			String p_host = proxy.split(":")[0];
			String p_port = proxy.split(":")[1];
			System.setProperty("http.proxyHost", p_host);
			System.setProperty("http.proxyPort", p_port);
			System.setProperty("https.proxyHost", p_host);
			System.setProperty("https.proxyPort", p_port);

			org.openqa.selenium.Proxy p = new org.openqa.selenium.Proxy();
			p.setHttpProxy(proxy).setFtpProxy(proxy).setSslProxy(proxy);
			cap.setCapability(CapabilityType.PROXY, p);
		}
		return cap;
	}

	private static void prepare() {
		File file = new File(OUT + "/screenshots");
		System.out.println("clean " + file.getAbsolutePath());
		file.mkdirs();
		final File[] files = file.listFiles();
		for (File f : files) {
			f.delete();
		}
	}

	static String OUT = "target";

	private static void download(WebDriver driver, String target)
			throws Exception {
		PrintWriter writer = new PrintWriter(OUT + "/" + target, "UTF-8");
		WebElement article = driver.findElement(By.cssSelector("article"));

		WebElement title = article.findElement(By
				.cssSelector("header.entry-header h1"));
		writer.println(title.getText());
		writer.println("======");
		writer.println(" ");

		WebElement content = article.findElement(By
				.cssSelector("div.entry-content"));

		List<WebElement> childs = getChilds(content);
		eval(writer, childs);
		writer.close();
	}

	private static void eval(PrintWriter writer, List<WebElement> childs)
			throws Exception {
		for (WebElement i : childs) {
			eval(writer, i);
		}
	}

	private static void eval(PrintWriter writer, WebElement i) throws Exception {
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
			break;
		case "a":
			if (hasNotChilds(i))
				writer.println(format("[%s](%s)", i.getText(),
						i.getAttribute("href")));
			else {
				List<WebElement> a_childs = getChilds(i);
				eval(writer, a_childs);
			}
			break;
		case "p":
			if (hasNotChilds(i))
				writer.println(i.getText());
			else {
				List<WebElement> p_childs = getChilds(i);
				eval(writer, p_childs);
			}
			writer.println(" ");
			break;
		case "div":
			if (isCode(i)) {
				writeCode(writer, i);
				writer.println(" ");
			}
			break;
		}
	}

	private static void writeCode(PrintWriter writer, WebElement i) {
		WebElement code = i.findElement(By.cssSelector(".syntaxhighlighter"));
		String type = getCodeType(code);
		List<String> lines = getCodeLines(code);
		writer.println("```" + type);
		for (String line : lines) {
			writer.println(line);
		}
		writer.println("```");
	}

	private static List<String> getCodeLines(WebElement code) {
		List<String> lines = new ArrayList<>();
		WebElement container = code.findElement(By
				.cssSelector("table .container"));
		List<WebElement> containerLines = getChilds(container);
		for (WebElement containerLine : containerLines) {
			lines.add(containerLine.getText());
		}
		return lines;
	}

	private static String getCodeType(WebElement code) {
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
		}
		Preconditions.checkNotNull(type, "checkNotNull codeType" + classes);
		return type;
	}

	private static boolean isCode(WebElement i) {
		try {
			return i.findElement(By.cssSelector(".syntaxhighlighter")) != null;
		} catch (Exception e) {
			return false;
		}
	}

	static SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHHmmssSSS");

	private static String downloadImg(String imgUrl)
			throws MalformedURLException, IOException {
		String uri = String.format("screenshots/%s.%s", //
				sdf.format(new Date()), //
				getExtension(new URL(imgUrl)));
		File file = new File(OUT + "/" + uri);
		downloadImg(new URL(imgUrl), file);
		return uri;
	}

	private static String getExtension(URL url) {
		String extension = FilenameUtils.getExtension(url.toString());
		return extension.substring(0, 3);
	}

	private static void downloadImg(URL url, File file) throws IOException,
			MalformedURLException {
		System.out.println(String.format("saving %s from img %s",
				file.getAbsolutePath(), url));
		URLConnection conn = url.openConnection();
		conn.setRequestProperty("User-Agent",
				"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:31.0) Gecko/20100101 Firefox/31.0");
		conn.connect();
		FileUtils.copyInputStreamToFile(conn.getInputStream(), file);
	}

	private static List<WebElement> getChilds(WebElement webElement) {
		return webElement.findElements(By.xpath("*"));
	}

	private static boolean hasNotChilds(WebElement webElement) {
		return webElement.findElements(By.xpath("*")).isEmpty();
	}

	private static String arg(final String expectedKey, final String[] args,
			String... defaultValue) {
		for (int i = 0; i < args.length; i++) {
			String key = args[i];
			if (("-" + expectedKey).equals(key)) {
				String val = args[i + 1];
				System.out.println(key + "=" + val);
				return val;
			}
		}
		return defaultValue != null && defaultValue.length > 0 ? defaultValue[0]
				: null;
	}
}
