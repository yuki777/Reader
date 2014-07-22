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
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlPullParserException;
import com.xxxlabo.reader.RSS20Parser.Entry;

import nl.matshofman.saxrssreader.*;

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

//            //guid
//            tv = (TextView) convertView.findViewById(R.id.guid);
//            tv.setText(guid);

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
				return (ArrayList) loadXmlFromNetwork(urls[0]);
			} catch (IOException e) {
				//return getResources().getString(R.string.connection_error);
			} catch (XmlPullParserException e) {
				//return getResources().getString(R.string.xml_error);
			}catch(Exception e){
				return new ArrayList();
			}
			return null;
		}

        protected void onPostExecute(List result) {
			CardListAdapter adapter = new CardListAdapter(getActivity());
            int size = result.size();
            for(int i=0; i<size; i++){
		        Entry entry = (Entry)result.get(i);
				List entries = new ArrayList();
				entries.add(entry.title);
                entries.add(entry.description);
                entries.add(entry.link);
                entries.add(entry.guid);
                entries.add(entry.pubDate);

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
                    android.util.Log.i("logapp", "6");
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
		private List<Entry> loadXmlFromNetwork(String urlString) throws XmlPullParserException, IOException {
            List<Entry> entries = null;
            try {
                RssFeed feed = RssReader.read(new URL(urlString));
                entries = normalize(feed);
            } catch (SAXException e) {
                e.printStackTrace();
            }

			return entries;
		}

        private List<Entry> normalize(RssFeed feed) throws IOException, XmlPullParserException {
            List entries = new ArrayList();
            ArrayList<RssItem> rssItems = feed.getRssItems();

            for(RssItem rssItem : rssItems) {
                Entry entry = readItem(rssItem);
                entries.add(entry);
            }

            return entries;
        }

        private Entry readItem(RssItem rssItem) throws XmlPullParserException, IOException {
            String entryTitle = rssItem.getTitle();
            String entryDescription = rssItem.getDescription();
            String entryLink = rssItem.getLink();
            String entryGuid = rssItem.getLink();
            String entryPubDate = rssItem.getPubDate().toString();
            Entry entry = new Entry(entryTitle, entryDescription, entryLink, entryGuid, entryPubDate);

            return entry;
        }
    } // private class DownloadXmlTask extends AsyncTask<String, Void, List>
} // public class AppListFragment extends ListFragment
