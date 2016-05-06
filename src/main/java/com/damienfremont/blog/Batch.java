package com.damienfremont.blog;

public class Batch {

	public static void main(String[][] wordUrl_gitDir_list) throws Exception {
		System.out.println("starting batch execution");
		for (String[] url_dir : wordUrl_gitDir_list) {
			String url = url_dir[0];
			String dir = url_dir[1];
			new Downloader(url, dir).down();;
		}
	}

}
