package com.damienfremont.blog;

public class ReadArgs {
	
	public static String arg(final String expectedKey, final String[] args, String... defaultValue) {
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
