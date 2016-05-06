package com.damienfremont.blog;

import static org.apache.commons.lang3.StringUtils.isEmpty;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

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
		new Downloader(url, proxy, target).down();
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
		JOptionPane.showConfirmDialog(null, panel, "Wordpress post to GitHub README.md", JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.PLAIN_MESSAGE);

		url = field1.getText();
		proxy = field2.getText();

		if (isEmpty(url)) {
			JOptionPane.showConfirmDialog(null, "Url cannot be empty!", "Error", JOptionPane.DEFAULT_OPTION,
					JOptionPane.PLAIN_MESSAGE);
		}
	}

	private static String arg(final String expectedKey, final String[] args, String... defaultValue) {
		for (int i = 0; i < args.length; i++) {
			String key = args[i];
			if (("-" + expectedKey).equals(key)) {
				String val = args[i + 1];
				System.out.println(key + "=" + val);
				return val;
			}
		}
		return defaultValue != null && defaultValue.length > 0 ? defaultValue[0] : null;
	}

}
