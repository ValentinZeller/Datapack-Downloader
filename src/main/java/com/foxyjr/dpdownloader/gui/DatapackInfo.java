package com.foxyjr.dpdownloader.gui;

public class DatapackInfo {
	final String title;
	final String description;
	final String author;
	final String slug;
	final String license;
	final String downloads;
	final String follows;
	final String[] display_categories;

	
	public DatapackInfo(String title, String description, String author, String slug, String license, String downloads, String follows, String[] display_categories) {
		this.title = title;
		this.description = description;
		this.author = author;
		this.slug = slug;
		this.license = license;
		this.downloads = downloads;
		this.follows = follows;
		this.display_categories = display_categories;
	}
}
