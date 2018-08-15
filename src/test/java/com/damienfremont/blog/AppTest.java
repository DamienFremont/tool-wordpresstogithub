package com.damienfremont.blog;

import org.junit.Test;

public class AppTest {

	@Test
	public void testMain_popup() throws Exception {

		App.main(new String[] {});
	}
	
	@Test
	public void testMain() throws Exception {
		App.main(new String[] { //
				//
				"-url", //
				"https://damienfremont.com/2015/09/01/git-the-simple-guide/" //
				, //
				"-target", //
				"target" //
		});
	}

	@Test
	public void testMain_batch() throws Exception {
		App.main(new String[] { //
				//
				"-csv", //
				"https://damienfremont.com/2015/10/13/javaee-angularjs-bootstrap-integration/" //
		});
	}
}
