package eu.ttbox.geoping.ui.pairing;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import eu.ttbox.geoping.GeoPingApplication;
import eu.ttbox.geoping.R;
import eu.ttbox.geoping.core.Intents;
import eu.ttbox.geoping.domain.person.PersonDatabase.PersonColumns;
import eu.ttbox.geoping.ui.smslog.SmsLogListFragment;

public class PairingEditActivity extends SherlockFragmentActivity {

	private static final String TAG = "PairingEditActivity";

	// Binding
	private PairingEditFragment editFragment;
	private SmsLogListFragment smsLogFragment;
	private PairingNotificationFragment notificationFragment;

	private SectionsPagerAdapter mSectionsPagerAdapter;
	private ViewPager mViewPager;
	// Instance
	private static final int VIEW_PAGER_LOADPERS_PAGE_COUNT = 3;
	private int viewPagerPageCount = 1;

	private Uri pairingUri;
	private String pairingPhone;

	// ===========================================================
	// Listener
	// ===========================================================

	private PairingEditFragment.OnPersonSelectListener onPairingSelectListener = new PairingEditFragment.OnPersonSelectListener() {

		@Override
		public void onPersonSelect(Uri id, String phone) {
			// Check Update Phone
			if (!TextUtils.isEmpty(pairingPhone) && !TextUtils.isEmpty(phone)) {
				if (smsLogFragment != null && !pairingPhone.equals(phone)) {
					Bundle args = new Bundle();
					args.putString(eu.ttbox.geoping.ui.smslog.SmsLogListFragment.Intents.EXTRA_SMS_PHONE, pairingPhone);
  					smsLogFragment.refreshLoader(args);
				}
			}
			pairingUri = id;
			pairingPhone = phone;
			// Update Ui Tabs
			if (viewPagerPageCount != VIEW_PAGER_LOADPERS_PAGE_COUNT) {
				viewPagerPageCount = VIEW_PAGER_LOADPERS_PAGE_COUNT;
				mSectionsPagerAdapter.notifyDataSetChanged();
			}
		}

	};

	// ===========================================================
	// Constructors
	// ===========================================================

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pairing_edit_activity);
		// Pagers
		mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
		mViewPager = (ViewPager) findViewById(R.id.pager);
		// Fragment
		editFragment = new PairingEditFragment();
		editFragment.setOnPersonSelectListener(onPairingSelectListener);

		// Analytic
		mViewPager.setAdapter(mSectionsPagerAdapter);
		// Intents
		handleIntent(getIntent());
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (pairingUri != null) {
			outState.putString(PersonColumns.COL_ID, pairingUri.toString());
			outState.putString(PersonColumns.COL_PHONE, pairingPhone);
		}
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		String pariringUriString = savedInstanceState.getString(PersonColumns.COL_ID);
		if (pariringUriString != null) {
			pairingUri = Uri.parse(pariringUriString);
			pairingPhone = savedInstanceState.getString(PersonColumns.COL_PHONE);
		}
	}

	// ===========================================================
	// Life Cycle
	// ===========================================================

	// ===========================================================
	// Menu
	// ===========================================================

	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.menu_pairing_edit, menu);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_save:
			editFragment.onSaveClick();
			return true;
		case R.id.menu_delete:
			editFragment.onDeleteClick();
			return true;
		case R.id.menu_select_contact:
			editFragment.onSelectContactClick(null);
			return true;
		case R.id.menu_cancel:
			editFragment.onCancelClick();
			return true;
//		case R.id.menuQuitter:
//			// Pour fermer l'application il suffit de faire finish()
//			finish();
//			return true;
		}
		return false;
	}

	// ===========================================================
	// Intent Handler
	// ===========================================================

	@Override
	protected void onNewIntent(Intent intent) {
		handleIntent(intent);
	}

	protected void handleIntent(Intent intent) {
		if (intent == null) {
			return;
		}
		String action = intent.getAction();
		Log.d(TAG, "handleIntent for action : " + action);

		if (Intent.ACTION_EDIT.equals(action) || Intent.ACTION_DELETE.equals(action)) {
			mViewPager.setCurrentItem(SectionsPagerAdapter.PAIRING);
			// Prepare Edit
			Uri entityUri = intent.getData();
			// Set Fragment
			Bundle fragArgs = new Bundle();
			fragArgs.putString(Intents.EXTRA_PERSON_ID, entityUri.toString());
			editFragment.setArguments(fragArgs);

			// Tracker
			if (Intent.ACTION_DELETE.equals(action)) {
				GeoPingApplication.getInstance().tracker().trackPageView("/Pairing/delete");
			} else {
				GeoPingApplication.getInstance().tracker().trackPageView("/Pairing/edit");
			}
		} else if (Intent.ACTION_INSERT.equals(action)) {
			mViewPager.setCurrentItem(SectionsPagerAdapter.PAIRING);
			GeoPingApplication.getInstance().tracker().trackPageView("/Pairing/insert");
		}

	}

	// ===========================================================
	// Pages Adapter
	// ===========================================================

	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the primary sections of the app.
	 */
	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		static final int PAIRING = 0;
		static final int NOTIFICATION = 1;
		// static final int PAIRING = 1;
		static final int LOG = 2;

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			Fragment fragment = null;
			switch (position) {
			case PAIRING:
				fragment = editFragment;
				break;
			case NOTIFICATION:
				if (notificationFragment==null) {
					Bundle args = new Bundle();
					args.putString(Intents.EXTRA_DATA_URI, pairingUri.toString());
					notificationFragment = new PairingNotificationFragment();
					notificationFragment.setArguments(args);
				}
				fragment = notificationFragment;
				break;
			case LOG:
				if (smsLogFragment == null) {
					Bundle args = new Bundle();
					args.putString(Intents.EXTRA_SMS_PHONE, pairingPhone);
					smsLogFragment = new SmsLogListFragment();
					smsLogFragment.setArguments(args);
				}
				fragment = smsLogFragment;
				break;
			}
			return fragment;
		}

		@Override
		public int getCount() {
			return viewPagerPageCount;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			switch (position) {
			case PAIRING:
				return getString(R.string.menu_pairing).toUpperCase();
			case NOTIFICATION:
				return getString(R.string.menu_pairing_notification).toUpperCase(); 
			case LOG:
				return getString(R.string.menu_smslog).toUpperCase();
			}
			return null;
		}
	}
	// ===========================================================
	// Listener
	// ===========================================================

}
