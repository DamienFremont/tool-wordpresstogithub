package com.damienfremont.blog;

import org.junit.Test;

public class BatchTest {

	@Test
	public void test() throws Exception {
		String ROOT = "C:\\Users\\Damien\\git\\blog";
		String[][] wordUrl_gitDir_list = { //
				{ "https://damienfremont.com/2012/02/27/from-junit-to-junit4/", ROOT + "/20120227-junit3_to_junit4" }
				//
		};

		Batch.main(wordUrl_gitDir_list);
	}

	@Test
	public void test_csv() throws Exception {
		Batch.main(new String[] { "-csv",
				"C:\\Users\\Damien\\git\\tool-wordpresstogithub\\src\\test\\java\\com\\damienfremont\\blog\\AppTest.csv" });
	}

}
