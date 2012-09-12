package eu.ttbox.geoping.ui.map.track.bubble;

import java.util.Locale;

import android.content.Context;
import android.location.Address;
import android.location.Location;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import eu.ttbox.geoping.R;
import eu.ttbox.geoping.domain.GeoTrack;

public class GeoTrackBubble extends FrameLayout {

	private final static String TAG = "GeoTrackBubble";

	// Config
	private int DEFAULT_BUBBLE_WIDTH = 300;

	// Datas
	private GeoTrack geoTrack;
	
	// Display
	private LinearLayout layout;
	private TextView coordTextView;
	private TextView accuracyTextView;
	private TextView altitudeTextView;
	private View altitudeBlock;
	private TextView speedTextView;

	private TextView addressTextView;

	private boolean displayGeoLoc = false;

	public GeoTrackBubble(Context context) {
		super(context);
		layout = new LinearLayout(context);
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v = inflater.inflate(R.layout.map_geotrack_bubble, layout);

		// Init fields
		this.coordTextView = (TextView) v.findViewById(R.id.map_geotrack_bubbleView_coord);
		this.accuracyTextView = (TextView) v.findViewById(R.id.map_geotrack_bubbleView_accuracy);
		this.addressTextView = (TextView) v.findViewById(R.id.map_geotrack_bubbleView_address);
		this.altitudeTextView = (TextView) v.findViewById(R.id.map_geotrack_bubbleView_altitude);
		this.altitudeBlock = v.findViewById(R.id.map_geotrack_bubbleView_block_altitude);
		this.speedTextView = (TextView) v.findViewById(R.id.map_geotrack_bubbleView_speed);
		
		// Frame
		FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		params.gravity = Gravity.NO_GRAVITY;
		params.width = DEFAULT_BUBBLE_WIDTH;
		addView(layout, params);
	}
	

	private boolean isSamelocation(Location location, Location other) {
		if (location != null && other != null) {
			// TODO
		}
		return false;
	}

	public void setData(GeoTrack geoTrack) {
		
				Location location = geoTrack.asLocation();
		if (location == null || !isSamelocation(location, location)) {
			this.geoTrack = geoTrack;
			setAddress(null);
		}
		if (location != null) {
			// Coord
			String coordString = String.format(Locale.US, "(%.6f, %.6f)", location.getLatitude(), location.getLongitude());
 			coordTextView.setText(coordString);
			if (displayGeoLoc) {
				coordTextView.setVisibility(VISIBLE);
			} else {
				coordTextView.setVisibility(GONE);
			}
			// Accuracy
			if (location.hasAccuracy()) {
				accuracyTextView.setText( (int) location.getAccuracy() + "m");
				accuracyTextView.setVisibility(VISIBLE);
			} else {
				accuracyTextView.setText("");
				accuracyTextView.setVisibility(GONE);
			}
			// Altitude
			if (location.hasAltitude()) {
				altitudeTextView.setText((int) location.getAltitude() + "m");
				altitudeBlock.setVisibility(VISIBLE);
			} else {
				altitudeTextView.setText("");
				altitudeBlock.setVisibility(GONE);
			}
			// Speed
			if (location.hasSpeed()) {
				speedTextView.setText((int) location.getSpeed() + "m/s");
				speedTextView.setVisibility(GONE);
			} else {
				speedTextView.setText("");
				speedTextView.setVisibility(GONE);
			}
		} else {
			accuracyTextView.setText("");
			altitudeBlock.setVisibility(GONE);
		}

	}

	public void setAddress(Address addr) {
		if (addr != null) {
			StringBuilder addrBuilder = new StringBuilder();
			boolean isNotFist = false;
			for (int i = 0; i < addr.getMaxAddressLineIndex(); i++) {
				if (isNotFist) {
					addrBuilder.append(", ");
				} else {
					isNotFist = true;
				}
				String addrLine = addr.getAddressLine(i);
				addrBuilder.append(addrLine);
			}
			// addrBuilder.append(addr.getPostalCode()).append(", ");
			// addrBuilder.append(addr.getLocality()).append("\n");
			// addrBuilder.append(addr.getCountryName());

			addressTextView.setText(addrBuilder);
			addressTextView.setVisibility(VISIBLE);
		} else {
			addressTextView.setVisibility(GONE);
		}

	}

	public void setDisplayGeoLoc(boolean displayGeoLoc) {
		this.displayGeoLoc = displayGeoLoc;
 	}


}