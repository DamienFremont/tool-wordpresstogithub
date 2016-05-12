package com.damienfremont.blog;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;

public class ReadCVS {

	static String cvsSplitBy = ";";

	public static String[][] run(String csvFile) {
		List<List<String>> lists = toList(csvFile);
		String[][] arrays = toArray(lists);
		return arrays;
	}

	public static String[][] toArray(List<List<String>> tmp) {
		int tmpColSize = tmp.get(0).size();
		int tmpRowSize = tmp.size();

		String[][] res = new String[tmpRowSize][tmpColSize];
		for (int i = 0; i < tmp.size(); i++) {
			List<String> row = tmp.get(i);
			for (int j = 0; j < row.size(); j++) {
				String col = row.get(j);
				res[i][j] = col;
			}
		}
		return res;
	}

	public static List<List<String>> toList(String csvFile) {
		List<List<String>> tmp = new ArrayList<>();
		BufferedReader br = null;
		String line = "";
		try {
			br = new BufferedReader(new FileReader(csvFile));
			while ((line = br.readLine()) != null) {
				String[] columns = line.split(cvsSplitBy);
				tmp.add(ImmutableList.of(columns[0], columns[1]));
			}
			return tmp;
		} catch (Exception e) {
			throw Throwables.propagate(e);
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

}