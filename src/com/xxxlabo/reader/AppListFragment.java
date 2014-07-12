package com.xxxlabo.reader;

import android.app.ListFragment;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.xmlpull.v1.XmlPullParserException;

import com.xxxlabo.reader.RSS20Parser.Entry;


public class AppListFragment extends ListFragment {

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		String title = getString(R.string.blog_name);
		String url = getString(R.string.feed_url);

		new DownloadXmlTask().execute(url);
	}

	public class CardListAdapter extends ArrayAdapter {

		LayoutInflater mInflater;

		public CardListAdapter(Context context) {
			super(context, 0);
			mInflater = LayoutInflater.from(context);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.list_item_card, parent, false);
			}

			List entries = (List) getItem(position);
			String title = (String) entries.get(0);
			String desc = (String) entries.get(1);
			String url = (String) entries.get(2);
            String guid = (String) entries.get(3);
            String pubDate = (String) entries.get(4);

            // title
			TextView tv = (TextView) convertView.findViewById(R.id.title);
			tv.setText(title);

            // url
			tv = (TextView) convertView.findViewById(R.id.url);
			tv.setText(url);

            // pubDate
            tv = (TextView) convertView.findViewById(R.id.pubDate);
            tv.setText(pubDate);

            //guid
            tv = (TextView) convertView.findViewById(R.id.guid);
            tv.setText(guid);

            // desc
            tv = (TextView) convertView.findViewById(R.id.desc);
            desc = desc.replaceAll("\r\n|\r|\n", "");
            desc = stripTags(desc, "someTag");
            if(desc.length() >= 100){
                desc = desc.substring(0, 100) + " ... ";
            }
//            android.util.Log.i("app",  "desc : " + desc);
			tv.setText(desc);

//            icon
//            ImageView iv = (ImageView) convertView.findViewById(R.id.icon);
//            iv.setImageDrawable(info.applicationInfo.loadIcon(packageManager));

			return convertView;
		}

        public String stripTags(String text, String allowedTags) {
            String[] tag_list = allowedTags.split(",");
            Arrays.sort(tag_list);

            final Pattern p = Pattern.compile("<[/!]?([^\\\\s>]*)\\\\s*[^>]*>",
                    Pattern.CASE_INSENSITIVE);
            Matcher m = p.matcher(text);

            StringBuffer out = new StringBuffer();
            int lastPos = 0;
            while (m.find()) {
                String tag = m.group(1);
                // if tag not allowed: skip it
                if (Arrays.binarySearch(tag_list, tag) < 0) {
                    out.append(text.substring(lastPos, m.start())).append(" ");

                } else {
                    out.append(text.substring(lastPos, m.end()));
                }
                lastPos = m.end();
            }
            if (lastPos > 0) {
                out.append(text.substring(lastPos));
                return out.toString().trim();
            } else {
                return text;
            }
        }


    }


	// Implementation of AsyncTask used to download XML feed from stackoverflow.com.
	private class DownloadXmlTask extends AsyncTask<String, Void, List> {

		// TODO:see http://developer.android.com/reference/android/os/AsyncTask.html
		protected List doInBackground(String... urls) {
			try {
				return loadXmlFromNetwork(urls[0]);
			} catch (IOException e) {
				//return getResources().getString(R.string.connection_error);
			} catch (XmlPullParserException e) {
				//return getResources().getString(R.string.xml_error);
			}catch(Exception e){
				return new ArrayList();
			}
			return null;
		}

		@Override
		protected void onPostExecute(List result) {

			CardListAdapter adapter = new CardListAdapter(getActivity());
			int size = result.size();
//            android.util.Log.i("app",  "size : " + size);
			for(int i=0; i<size; i++){
				Entry entry = (Entry)result.get(i);
				final String title = getTitleByEntry(entry);
                final String desc  = getDescriptionByEntry(entry);
				final String url   = getUrlByEntry(entry);
                final String guid   = getGuidByEntry(entry);
                final String pubDate   = getPubDateByEntry(entry);

				List entries = new ArrayList();
				entries.add(title);
				entries.add(desc);
				entries.add(url);
                entries.add(guid);
                entries.add(pubDate);

//                android.util.Log.i("app",  "guid    : " + guid);
//                android.util.Log.i("app",  "pubDate : " + pubDate);

				adapter.add(entries);
			}

			int padding = (int) (getResources().getDisplayMetrics().density * 8); // 8dip
			ListView listView = getListView();
			listView.setPadding(padding, 0, padding, 0);
			listView.setScrollBarStyle(ListView.SCROLLBARS_OUTSIDE_OVERLAY);
			listView.setDivider(null);

			LayoutInflater inflater = LayoutInflater.from(getActivity());
			View header = inflater.inflate(R.layout.list_header_footer, listView, false);
			View footer = inflater.inflate(R.layout.list_header_footer, listView, false);
			listView.addHeaderView(header, null, false);
			listView.addFooterView(footer, null, false);

			listView.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//					// open webview in new browser 
//                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.google.com"));
//                    startActivity(browserIntent);
					
                    // open webview in app 
                    Intent intent = new Intent(view.getContext(), DetailActivity.class);
                    String title = ((TextView) view.findViewById(R.id.title)).getText().toString();
                    String url = ((TextView) view.findViewById(R.id.url)).getText().toString();
                    intent.putExtra("title", title);
                    intent.putExtra("url", url);
                    view.getContext().startActivity(intent);
				}
			});
			
			setListAdapter(adapter);
		}


		// Uploads XML from stackoverflow.com, parses it, and combines it with
		// HTML markup. Returns HTML string.
		private List loadXmlFromNetwork(String urlString) throws XmlPullParserException, IOException {
			InputStream stream = null;
			// Instantiate the parser
			RSS20Parser exciteBlogXMLParser = new RSS20Parser();
			List<Entry> entries = null;
			try {
				stream = downloadUrl(urlString);
				entries = exciteBlogXMLParser.parse(stream);
				// Makes sure that the InputStream is closed after the app is
				// finished using it.
			} finally {
				if (stream != null) {
					stream.close();
				} 
			}

			return entries;
		}

		// Given a string representation of a URL, sets up a connection and gets
		// an input stream.
		private InputStream downloadUrl(String urlString) throws IOException {
			URL url = new URL(urlString);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setReadTimeout(10000 /* milliseconds */);
			conn.setConnectTimeout(15000 /* milliseconds */);
			conn.setRequestMethod("GET");
			conn.setDoInput(true);
			// Starts the query
			conn.connect();
			return conn.getInputStream();
		}
	} // private class DownloadXmlTask extends AsyncTask<String, Void, List>

    private String getTitleByEntry(Entry entry) {
        try {
            return entry.title;
        } catch (Exception e) {
            return "(No Title)";
        }
    }

    private String getDescriptionByEntry(Entry entry) {
        try {
            return Html.fromHtml(entry.description.toString()).toString();
        } catch (Exception e) {
            return "(No Description)";
        }
    }

    private String getUrlByEntry(Entry entry) {
        try {
            return entry.link;
        } catch (Exception e) {
            // TODO:
            return "(No URL)";
        }
    }

    private String getGuidByEntry(Entry entry) {
        try {
            return entry.guid;
        } catch (Exception e) {
            // TODO:
            return "(No Guid)";
        }
    }

    private String getPubDateByEntry(Entry entry) {
        try {
            return entry.pubDate;
        } catch (Exception e) {
            // TODO:
            return "(No PubDate)";
        }
    }
} // public class AppListFragment extends ListFragment
