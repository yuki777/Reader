
// see... http://developer.android.com/training/basics/network-ops/xml.html

package com.xxxlabo.reader;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Xml;

public class RSS20Parser {
	// We don't use namespaces
	private static final String ns = null;

	public List parse(InputStream in) throws XmlPullParserException, IOException {
		try {
			XmlPullParser parser = Xml.newPullParser();
			parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
			parser.setInput(in, null);
			parser.nextTag();
			List rss = readRss(parser);
			return rss;
		} finally {
			in.close();
		}
	}

	private List readRss(XmlPullParser parser) throws XmlPullParserException, IOException {
		List entries = null;
		parser.require(XmlPullParser.START_TAG, ns, "rss");
		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}
			String name = parser.getName();
			if (name.equals("channel")) {
				entries = readChannel(parser);
			}else {
				skip(parser);
			}
		}  
		return entries;
	}


	// Parses the contents of an entry. If it encounters a title, summary, or link tag, hands them off
	// to their respective "read" methods for processing. Otherwise, skips the tag.
	//private Blog readChannel(XmlPullParser parser) throws XmlPullParserException, IOException {
	private List readChannel(XmlPullParser parser) throws XmlPullParserException, IOException {
		parser.require(XmlPullParser.START_TAG, ns, "channel");
		List entries = new ArrayList();
		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}
			String name = parser.getName();
			if(name.equals("item")){
				entries.add(readItem(parser));
			} else {
				skip(parser);
			}
		}
		
		return entries;
	}

	
	// Parses the contents of an entry. If it encounters a title, summary, or link tag, hands them off
	// to their respective "read" methods for processing. Otherwise, skips the tag.
	private Entry readItem(XmlPullParser parser) throws XmlPullParserException, IOException {
		parser.require(XmlPullParser.START_TAG, ns, "item");
		String entryTitle = null;
		String entryDescription = null;
		String entryLink = null;
        String entryGuid = null;
        String entryPubDate = null;
		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}
			String name = parser.getName();
//            android.util.Log.i("parser",  "name : " + name);
			if (name.equals("title")) {
				entryTitle = readTitle(parser);
			} else if (name.equals("description")) {
				entryDescription = readDescription(parser);
            } else if (name.equals("link")) {
                entryLink = readLink(parser);
            } else if (name.equals("guid")) {
                entryGuid = readGuid(parser);
            } else if (name.equals("pubDate")) {
                entryPubDate = readPubDate(parser);
			} else {
				skip(parser);
			}
		}
		Entry entry = new Entry(entryTitle, entryDescription, entryLink, entryGuid, entryPubDate);
		return entry;
	}
	
	// Processes title tags in the feed.
	private String readTitle(XmlPullParser parser) throws IOException, XmlPullParserException {
		parser.require(XmlPullParser.START_TAG, ns, "title");
		String val = readText(parser);
		parser.require(XmlPullParser.END_TAG, ns, "title");
		return val;
	}

	// Processes link tags in the feed.
	private String readLink(XmlPullParser parser) throws IOException, XmlPullParserException {
//		TODO:hrefとか取得したいときはこう。
//	    String link = "";
//		parser.require(XmlPullParser.START_TAG, ns, "link");
//		String tag = parser.getName();
//		String relType = parser.getAttributeValue(null, "rel");
//		if (tag.equals("link")) {
//			if (relType.equals("alternate")){
//				link = parser.getAttributeValue(null, "href");
//				parser.nextTag();
//			}
//		}
//		parser.require(XmlPullParser.END_TAG, ns, "link");
//		return link;
		parser.require(XmlPullParser.START_TAG, ns, "link");
		String val = readText(parser);
		parser.require(XmlPullParser.END_TAG, ns, "link");
		return val;
	}

    // Processes summary tags in the feed.
    private String readDescription(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "description");
        String val = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "description");
        return val;
    }

    // Processes summary tags in the feed.
    private String readGuid(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "guid");
        String val = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "guid");
        return val;
    }

    // Processes summary tags in the feed.
    private String readPubDate(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "pubDate");
        String val = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "pubDate");
        return val;
    }

    // For the tags title and summary, extracts their text values.
	private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
		String result = "";
		if (parser.next() == XmlPullParser.TEXT) {
			result = parser.getText();
			parser.nextTag();
		}
		return result;
	}
	
	private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
		if (parser.getEventType() != XmlPullParser.START_TAG) {
			throw new IllegalStateException();
		}
		int depth = 1;
		while (depth != 0) {
			switch (parser.next()) {
			case XmlPullParser.END_TAG:
				depth--;
				break;
			case XmlPullParser.START_TAG:
				depth++;
				break;
			}
		}
	}

	// Blog
	public static class Blog {
		public final String title;
		public final String link;
		public final String description;
        public final String guid;
        public final String pubDate;

		private Blog(String title, String description, String link, String guid, String pubDate) {
			this.title = title;
			this.description = description;
			this.link = link;
            this.guid = guid;
            this.pubDate = pubDate;
		}
	}
	
	// Blog -> Entry
	public static class Entry {
		public final String title;
		public final String link;
		public final String description;
        public final String guid;
        public final String pubDate;

		private Entry(String title, String description, String link, String guid, String pubDate) {
			this.title = title;
			this.description = description;
			this.link = link;
            this.guid = guid;
            this.pubDate = pubDate;
		}
	}
}