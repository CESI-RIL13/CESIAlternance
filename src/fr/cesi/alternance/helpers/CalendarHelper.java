package fr.cesi.alternance.helpers;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import fr.cesi.alternance.Constants;

import android.accounts.Account;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Calendars;
import android.provider.CalendarContract.Events;
import android.util.Log;

public class CalendarHelper {

	public static final String TAG 							= Constants.APP_NAME + ".CalendarHelper";
	
	private static Context sContext;
	public static void setContext(Context ctx) {
		sContext = ctx;
	}

	public static Uri asSyncAdapter(Uri uri, String account, String accountType) {
		return uri.buildUpon()
				.appendQueryParameter(
						CalendarContract.CALLER_IS_SYNCADAPTER, "true")
						.appendQueryParameter(Calendars.ACCOUNT_NAME, account)
						.appendQueryParameter(Calendars.ACCOUNT_TYPE, accountType)
						.build();
	}
	
	public static class Planning extends Entity {
		
		private String sourceId;
		private String name;
		private List<Event> events = new ArrayList<Event>();
		
		@Override
		public String getApiPath() {
			return "calendar";
		}
		
		public String getSourceId() {
			return sourceId;
		}
		public void setSourceId(String sourceId) {
			this.sourceId = sourceId;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		
		public List<Event> getEvents() {
			return events;
		}
		public void setEvents(List<Event> events) {
			this.events = events;
		}

		@Override
		public Planning fromJSON(JSONObject json) {
			try {
				sourceId = json.getString("id");
				name = json.getString("name");
				if (json.has("events")) {
					events.clear();
					JSONArray evts = json.getJSONArray("events");
					for (int i=0; i<evts.length(); i++) {
                        Event event = new Event().fromJSON(evts.getJSONObject(i));
						events.add(event);
					}
				}
			} catch (JSONException e) {}
			return this;
		}
		
		@Override
		public JSONObject asJSON() {
			return null;
		}
		
		@Override
		public boolean equals(Object o) {
			if (o == null) return false;
			if (!(o instanceof Planning)) return false;
			return sourceId != null && ((Planning) o).sourceId != null && sourceId.equals(((Planning) o).sourceId);
		}

        public static boolean hasChange(Planning l, Planning r) {
            if (l == null || r == null) return true;
            if (!l.getName().equals(r.getName())) return true;
            return false;
        }
		
		public static String[] FULL_PROJECTION = new String[]{ Calendars._ID, Calendars.CAL_SYNC1, Calendars.CALENDAR_DISPLAY_NAME };
		
		public static Planning cursorToPlanning(Cursor cursor) {
			Planning p = new Planning();
			p.id = cursor.getLong(0);
			p.sourceId = cursor.getString(1);
			p.name = cursor.getString(2);
			return p;
		}

		public static List<Planning> select(Account account) {
			List<Planning> plannings = new ArrayList<Planning>();
			ContentResolver cr = sContext.getContentResolver();
			String selection = Calendars.ACCOUNT_NAME + " = '" + account.name + "'";
            Cursor cur = cr.query(Calendars.CONTENT_URI, FULL_PROJECTION, selection, null, null);
			while (cur.moveToNext()) plannings.add(cursorToPlanning(cur));
			return plannings;
		}
		public static Planning select(Account account, String sourceId) {
			ContentResolver cr = sContext.getContentResolver();
			String selection = Calendars.ACCOUNT_NAME + " = '" + account.name + "'";
			selection += " AND " + Calendars.CAL_SYNC1 + " = '" + sourceId + "'";
            Cursor cur = cr.query(Calendars.CONTENT_URI, FULL_PROJECTION, selection, null, null);
			if (cur.getCount() > 0 && cur.moveToNext()) return cursorToPlanning(cur);
			return null;
		}
		
		public static long insert(Account account, Planning planning) {
			ContentResolver cr = sContext.getContentResolver();
			ContentValues values = new ContentValues();
			values.put(Calendars.ACCOUNT_NAME, account.name);
			values.put(CalendarContract.Calendars.ACCOUNT_TYPE, CalendarContract.ACCOUNT_TYPE_LOCAL);
			values.put(Calendars.NAME, account.name);
			values.put(Calendars.CALENDAR_DISPLAY_NAME, planning.name);
			values.put(Calendars.CALENDAR_COLOR, 0xFFFFFFFF);
			values.put(Calendars.CALENDAR_ACCESS_LEVEL, Calendars.CAL_ACCESS_READ);
			values.put(Calendars.CAL_SYNC1, planning.sourceId);
			values.put(Calendars.SYNC_EVENTS, 1);
			values.put(Calendars.VISIBLE, 1);
			values.put(CalendarContract.Calendars.CALENDAR_TIME_ZONE, TimeZone.getDefault().getID());
			Uri creationUri = asSyncAdapter(Calendars.CONTENT_URI, account.name, account.type);
			Uri created = cr.insert(creationUri, values);
			long id = ContentUris.parseId(created);
			//if (id > 0) Log.d(TAG, "Calendrier ajouté");
			//else Log.d(TAG, "Erreur lors de l'ajouter le calendrier !");
			return id;
		}
		
		public static long update(Account account, Planning planning) {
			ContentResolver cr = sContext.getContentResolver();
			ContentValues values = new ContentValues();
			values.put(Calendars.CALENDAR_DISPLAY_NAME, planning.name);
			values.put(Calendars.CAL_SYNC1, planning.sourceId);
			Uri uri = ContentUris.withAppendedId(Calendars.CONTENT_URI, planning.getId());
			Uri updateUri = asSyncAdapter(uri, account.name, account.type);
			int cnt = cr.update(updateUri, values, null, null);
			long id = ContentUris.parseId(uri);
			//if (cnt > 0) Log.d(TAG, "Calendrier mis à jour");
			//else Log.d(TAG, "Erreur lors de la mise à jour du calendrier !");
			return id;
		}

		public static int delete(Account account, long calId) {
			ContentResolver cr = sContext.getContentResolver();
			cr.delete(Events.CONTENT_URI, Events.CALENDAR_ID + " = " + calId, null);
			Uri deleteUri = ContentUris.withAppendedId(Calendars.CONTENT_URI, calId);
			int cnt = cr.delete(deleteUri, null, null);
			//if (cnt > 0) Log.v(TAG, "Calendar deleted");
			//else Log.d(TAG, "Erreur lors de la suppression du calendrier !");
			return cnt;
		}
	}
	
	public static class Event extends Entity {
		
		private long calId;
		private String sourceId;
		private String title;
		private String description;
		private long begin, end;
		private String where;
		
		@Override
		public String getApiPath() {
			return "/calendar/events";
		}
		
		public long getCalId() {
			return calId;
		}
		public void setCalId(long calId) {
			this.calId = calId;
		}

		public String getSourceId() {
			return sourceId;
		}
		public void setSourceId(String sourceId) {
			this.sourceId = sourceId;
		}

		public String getTitle() {
			return title;
		}
		public void setTitle(String title) {
			this.title = title;
		}

		public String getDescription() {
			return description;
		}
		public void setDescription(String content) {
			this.description = content;
		}

		public long getBegin() {
			return begin;
		}
		public void setBegin(long begin) {
			this.begin = begin;
		}

		public long getEnd() {
			return end;
		}
		public void setEnd(long end) {
			this.end = end;
		}

		public String getWhere() {
			return where;
		}
		public void setWhere(String where) {
			this.where = where;
		}
		
		@Override
		public Event fromJSON(JSONObject json) {
			try {
				sourceId = json.getString("id");
				title = json.getString("title");
				description = json.getString("content");
				begin = convertDateTime(json.getString("startTime"));
				end = convertDateTime(json.getString("endTime"));
				where = json.getString("where");
			} catch (JSONException e) {}
			return this;
		}
		@Override
		public JSONObject asJSON() {
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public boolean equals(Object o) {
			if (o == null) return false;
			if (!(o instanceof Event)) return false;
			return sourceId != null && ((Event) o).sourceId != null && sourceId.equals(((Event) o).sourceId);
		}

        public static boolean hasChange(Event l, Event r) {
            if (l == null || r == null) return true;
            if (!l.getTitle().equals(r.getTitle())) return true;
            if (!l.getDescription().equals(r.getDescription())) return true;
            if (!l.getWhere().equals(r.getWhere())) return true;
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            String dl = sdf.format(new Date(l.getBegin()));
            String dr = sdf.format(new Date(r.getBegin()));
            if (!dl.equals(dr)) return true;
            dl = sdf.format(new Date(l.getEnd()));
            dr = sdf.format(new Date(r.getEnd()));
            if (!dl.equals(dr)) return true;
            return false;
        }
		
		private long convertDateTime(String dateTime) {
			Calendar time = Calendar.getInstance();
			try {
				Calendar cal = Calendar.getInstance();
				Date dt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(dateTime);
				cal.setTime(dt);
				time.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DATE), cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE));
			} catch (ParseException e) {}
			return time.getTimeInMillis();
		}
		
		public static Event cursorToEvent(Cursor cursor) {
			Event event = new Event();
			event.id = cursor.getLong(0);
			event.sourceId = cursor.getString(1);
			event.title = cursor.getString(2);
			event.description = cursor.getString(3);
			event.begin = cursor.getLong(4);
			event.end = cursor.getLong(5);
			event.where = cursor.getString(6);
			return event;
		}
		
		public static final String[] FULL_PROJECTION = new String[]{ Events._ID, Events.SYNC_DATA1, Events.TITLE, Events.DESCRIPTION, Events.DTSTART, Events.DTEND, Events.EVENT_LOCATION };

		public static List<Event> select(Account account, long calId) {
			List<Event> events = new ArrayList<Event>();
			ContentResolver cr = sContext.getContentResolver();
			String selection = Calendars.ACCOUNT_NAME + " = '" + account.name + "' AND " + Events.CALENDAR_ID + " = " + calId;
            Cursor cur = cr.query(Events.CONTENT_URI, FULL_PROJECTION, selection, null, null);
			if (cur.getCount() > 0 && cur.moveToNext()) events.add(cursorToEvent(cur));
			return events;
		}
		
		public static Event select(Account account, long calId, String sourceId) {
			ContentResolver cr = sContext.getContentResolver();
			String selection = Calendars.ACCOUNT_NAME + " = '" + account.name + "' AND " + Events.CALENDAR_ID + " = " + calId + " AND " + Events.SYNC_DATA1 + " = '" + sourceId + "'";
            Cursor cur = cr.query(Events.CONTENT_URI, FULL_PROJECTION, selection, null, null);
			if (cur.getCount() > 0 && cur.moveToNext()) return cursorToEvent(cur);
			return null;
		}

		public static long insert(Account account, long calId, Event event) {
			ContentResolver cr = sContext.getContentResolver();
			ContentValues values = new ContentValues();
			values.put(Events.CALENDAR_ID, calId);
			values.put(Events.SYNC_DATA1, event.sourceId);
			values.put(Events.DTSTART, event.begin);
			values.put(Events.DTEND, event.end);
			values.put(Events.TITLE, event.title);
			values.put(Events.DESCRIPTION, event.description);
			values.put(Events.EVENT_LOCATION, event.where);
			values.put(Events.EVENT_TIMEZONE, TimeZone.getDefault().getID());

			Uri creationUri = asSyncAdapter(Events.CONTENT_URI, account.name, account.type);
			Uri uri = cr.insert(creationUri, values);
			return ContentUris.parseId(uri);
		}

		public static long update(Account account, Event event) {
			ContentResolver cr = sContext.getContentResolver();
			ContentValues values = new ContentValues();
			values.put(Events.SYNC_DATA1, event.sourceId);
			values.put(Events.DTSTART, event.begin);
			values.put(Events.DTEND, event.end);
			values.put(Events.TITLE, event.title);
			values.put(Events.DESCRIPTION, event.description);
			values.put(Events.EVENT_LOCATION, event.where);

			Uri uri = ContentUris.withAppendedId(Events.CONTENT_URI, event.getId());
			Uri updateUri = asSyncAdapter(uri, account.name, account.type);
			int cnt = cr.update(updateUri, values, null, null);
			return cnt > 0 ? ContentUris.parseId(uri) : 0;
		}
		
		public static void delete(Account account, long evtId) {
			ContentResolver cr = sContext.getContentResolver();
			Uri uri = ContentUris.withAppendedId(Events.CONTENT_URI, evtId);
			Uri deleteUri = asSyncAdapter(uri, account.name, account.type);
			cr.delete(deleteUri, null, null);
			Log.v(TAG, "Calendar Event deleted");
		}
		
	}
}
