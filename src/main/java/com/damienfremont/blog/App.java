package com.damienfremont.blog;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNoneEmpty;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class App {

	static String url;
	static String target;

	public static void main(String[] args) throws Exception {
		String csv = ReadArgs.arg("csv", args);
		if(isNoneEmpty(csv)) {
			executeBatch(csv);
		} else {
			url = ReadArgs.arg("url", args);
			target = ReadArgs.arg("target", args, "README.md");
			execute();			
		}		
	}

	private static void executeBatch(String csv) {
		// TODO
//		new Batch().main(wordUrl_gitDir_list);
	}

	private static void execute() throws Exception {
		if (isEmpty(url)) {
			initByUIpopup();
		}
		new Downloader(url, target).down();
	}

	private static void initByUIpopup() {
		System.out.println("no CLI args provided, trying with UI popup!");

		JTextField field1 = new JTextField("");
//		JTextField field2 = new JTextField("");
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.add(new JLabel("Wordpress post URL (*)"));
		panel.add(field1);
//		panel.add(new JLabel("Proxy (optionnal)"));
//		panel.add(field2);
		JOptionPane.showConfirmDialog(null, panel, "Wordpress post to GitHub README.md", JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.PLAIN_MESSAGE);

		url = field1.getText();
//		proxy = field2.getText();

		if (isEmpty(url)) {
			JOptionPane.showConfirmDialog(null, "Url cannot be empty!", "Error", JOptionPane.DEFAULT_OPTION,
					JOptionPane.PLAIN_MESSAGE);
		}
	}

}
