package com.damienfremont.blog;

import org.junit.Test;

public class AppTest {

	@Test
	public void testMain() throws Exception {

		App.main(new String[] { //
		//
				"-url", //
				"https://damienfremont.com/2015/10/13/javaee-angularjs-bootstrap-integration/" //
				,//
				"-proxy", //
				"proxybc.hld.net:8080" //
				,//
				"-target", //
				"README.md" //
		});
	}

	@Test
	public void testMain_popup() throws Exception {

		App.main(new String[] {});
	}
}
