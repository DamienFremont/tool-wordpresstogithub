package com.damienfremont.blog;

import org.junit.Test;

public class BatchTest {

	@Test
	public void test() throws Exception {
		String ROOT = "C:\\Users\\Damien\\git\\blog";
		String[][] wordUrl_gitDir_list = { //
				{ "https://damienfremont.com/2017/12/07/howto-js-node-express-rest-api/", 					ROOT + 
					"/20171207-js-node-express-rest-api" }
				//
		};

		Batch.main(wordUrl_gitDir_list);
	}
/*
	@Test
	public void test_csv() throws Exception {
		Batch.main(new String[] { "-csv",
				"C:\\Users\\Damien\\git\\tool-wordpresstogithub\\src\\test\\java\\com\\damienfremont\\blog\\AppTest.csv" });
	}
*/
}
