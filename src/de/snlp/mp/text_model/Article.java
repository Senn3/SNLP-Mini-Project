package de.snlp.mp.text_model;

import java.util.List;

public class Article {

	private String name;
	private String content;

	public Article(String name, String content) {
		this.setName(name);
		this.setContent(content);
	}

	public Article(String name, List<String> content) {
		this.setName(name);
		StringBuilder builder = new StringBuilder();
		for (String s : content)
			builder.append(s);
		this.setContent(builder.toString());
	}

	public String getName() {
		return name;
	}

	private void setName(String name) {
		this.name = name;
	}

	public String getContent() {
		return content;
	}

	private void setContent(String content) {
		this.content = content;
	}

}
