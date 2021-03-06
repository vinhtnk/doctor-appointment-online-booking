/**
 * 
 */

package com.android.daob.activity;

import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;

import com.android.daob.application.AppController;
import com.android.daob.model.DoctorFreeTimeModel;
import com.android.daob.utils.Constants;
import com.android.daob.utils.GlobalStorage;
import com.android.doctor_appointment_online_booking.R;
import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;

public class PatientBookingStep1Activity extends BaseActivity implements
		OnClickListener {

	public static String TAG = PatientBookingStep1Activity.class
			.getSimpleName();

	private ProgressBar loginProgressBar;
	private String url = Constants.URL + "processStep1";

	private String urlEmail = Constants.URL + "bookingStep1/"
			+ MainActivity.patientId;

	RadioButton rbForMe, rbForOther, rbMale, rbFemale;

	Button btnBooking;

	LinearLayout llForOther;

	EditText txtDeName, txtDeOld, txtDePhone, txtNotes, txtEmail, txtAddress;

	TextView tvStartTime, tvEndTime, tvLocation, tvDoctorName, tvBookDate;

	boolean isDelegated = false;

	boolean gender = false;

	String doctorId = "";

	Context context;

	public static String name = "";
	public static String old = "";
	public static String sex = "";
	public static String phone = "";
	public static String address = "";
	public static String doctorname = "";
	public static String meetingDate = "";
	public static String location = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.patient_booking_step1_layout);
		init(getIntent().getExtras());
		context = this;
	}

	void init(Bundle bun) {
		DoctorFreeTimeModel dft = (DoctorFreeTimeModel) bun
				.get(Constants.DATA_KEY);
		// DoctorModel doctor = (DoctorModel)
		// bun.getSerializable(Constants.DATA_KEY);
		// doctorId = doctor.getDoctorId();
		loginProgressBar = (ProgressBar) findViewById(R.id.booking_step1_progress);
		rbForMe = (RadioButton) findViewById(R.id.rb_for_me);
		rbForOther = (RadioButton) findViewById(R.id.rb_for_other);
		rbMale = (RadioButton) findViewById(R.id.rb_male);
		rbFemale = (RadioButton) findViewById(R.id.rb_female);

		llForOther = (LinearLayout) findViewById(R.id.ll_booking_for_other);
		txtDeName = (EditText) findViewById(R.id.ed_booking_name);
		txtDeOld = (EditText) findViewById(R.id.ed_booking_old);
		txtDePhone = (EditText) findViewById(R.id.ed_booking_phone);
		txtNotes = (EditText) findViewById(R.id.edDescription);
		txtEmail = (EditText) findViewById(R.id.tbEmail);
		txtAddress = (EditText) findViewById(R.id.ed_booking_address);

		tvDoctorName = (TextView) findViewById(R.id.tv_doctor_name);
		tvDoctorName.setText(PatientSearchDoctorActivity.doctorName);
		tvStartTime = (TextView) findViewById(R.id.tv_start_time);
		tvStartTime.setText(dft.getStartTime());
		tvEndTime = (TextView) findViewById(R.id.tv_end_time);
		tvEndTime.setText(dft.getEndTime());
		tvLocation = (TextView) findViewById(R.id.tv_location_name);
		tvLocation.setText(dft.getLocation());
		tvBookDate = (TextView) findViewById(R.id.tv_booking_date);
		tvBookDate.setText(dft.getMeetingDate());

		btnBooking = (Button) findViewById(R.id.btn_booking);
		btnBooking.setOnClickListener(this);
		setEmail();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.top_right_menu, menu);
		menu.getItem(0).setTitle(getResources().getString(R.string.home));
		menu.getItem(0).setIcon(
				this.getResources().getDrawable(
						R.drawable.ic_action_go_to_today));
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.top_right_button:
			finish();
			Intent intentHome = new Intent(this, PatientHomeActivity.class);
			startActivity(intentHome);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public void onRadioButtonClicked(View view) {
		// Is the button now checked?
		boolean checked = ((RadioButton) view).isChecked();

		// Check which radio button was clicked
		switch (view.getId()) {
		case R.id.rb_for_me:
			if (checked) {
				llForOther.setVisibility(View.GONE);
				isDelegated = false;
			}
			break;
		case R.id.rb_for_other:
			if (checked) {
				llForOther.setVisibility(View.VISIBLE);
				txtDeName.requestFocus();
				isDelegated = true;
			}
		case R.id.rb_male:
			if (checked) {
				gender = false;
			}
			break;
		case R.id.rb_female:
			if (checked) {
				gender = true;
			}
			break;
		}
	}

	@Override
	public void onClick(View arg0) {
		switch (arg0.getId()) {
		case R.id.btn_booking:
			loginProgressBar.setVisibility(View.VISIBLE);
			String email = txtEmail.getText().toString().trim();
			HashMap<String, String> bookingParams = new HashMap<String, String>();
			bookingParams.put("email", email);
			JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
					Method.POST, url, new JSONObject(bookingParams),
					new Listener<JSONObject>() {

						@Override
						public void onResponse(JSONObject response) {
							Log.i("booking params", " " + response);
							try {
								PatientBookingStep1Activity.this
										.saveBookingToGlobal();
								if (isDelegated) {
									name = txtDeName.getText().toString();
									old = txtDeOld.getText().toString();
									sex = gender ? "Nam" : "Nữ";
									phone = txtDePhone.getText().toString();
									address = txtAddress.getText().toString();
								}
								doctorname = tvDoctorName.getText().toString();
								meetingDate = tvStartTime.getText().toString()
										+ " - "
										+ tvEndTime.getText().toString() + "  "
										+ tvBookDate.getText().toString();
								location = tvLocation.getText().toString();
								Intent i = new Intent(context,
										PatientBookingStep2Activity.class);
								Log.i("aa", " " + response.getInt("confirmKey"));
								i.putExtra(Constants.CONFIRM_KEY,
										response.getInt("confirmKey"));
								context.startActivity(i);
							} catch (JSONException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							loginProgressBar.setVisibility(View.GONE);
						}
					}, new Response.ErrorListener() {
						@Override
						public void onErrorResponse(VolleyError error) {
							VolleyLog.e(TAG, "Error: " + error.getMessage());
							loginProgressBar.setVisibility(View.GONE);
						}
					}) {

			};
			AppController.getInstance().addToRequestQueue(jsonObjectRequest,
					"json_booking_req");

		}

	}

	void setEmail() {
		JsonObjectRequest jsonGetEmail = new JsonObjectRequest(urlEmail, null,
				new Listener<JSONObject>() {
					@Override
					public void onResponse(JSONObject json) {
						try {
							txtEmail.setText(json.getString("email"));
							if (json.has("lastDeApp")) {
								JSONObject delePatient = json.getJSONObject(
										"lastDeApp").getJSONObject(
										"delegatedPatient");
								txtDeName.setText(delePatient.getString("name"));
								txtDePhone.setText(delePatient
										.getString("phone"));
								txtDeOld.setText(delePatient.getString("age"));
								if (delePatient.getString("gender").equals(
										"female")) {
									rbFemale.setChecked(true);
									rbMale.setChecked(false);
								} else {
									rbFemale.setChecked(false);
									rbMale.setChecked(true);
								}
								txtAddress.setText(delePatient
										.getString("address"));
							}
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}, new ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError arg0) {

					}
				});
		AppController.getInstance().addToRequestQueue(jsonGetEmail,
				"json_get_email");
	}

	private void saveBookingToGlobal() throws JSONException {
		JSONObject bookingParams = new JSONObject();
		String deName = txtDeName.getText().toString().trim();
		String deOld = txtDeOld.getText().toString().trim();
		String dePhone = txtDePhone.getText().toString().trim();
		String deAddress = txtAddress.getText().toString().trim();
		String startTime = tvStartTime.getText().toString().trim();
		String endTime = tvEndTime.getText().toString().trim();
		String note = txtNotes.getText().toString().trim();
		String location = tvLocation.getText().toString().trim();
		String meetingDate = tvBookDate.getText().toString().trim();
		String doctorId = PatientSearchDoctorActivity.doctorId;

		bookingParams.put("startTime", startTime);
		bookingParams.put("endTime", endTime);
		bookingParams.put("meetingDate", meetingDate);
		bookingParams.put("isDelegated", isDelegated);
		bookingParams.put("location", location);
		bookingParams.put("notes", note);
		bookingParams.put("doctor", doctorId);
		bookingParams.put("creator", MainActivity.patientId);
		if (isDelegated) {
			JSONObject delPatient = new JSONObject();
			delPatient.put("name", deName);
			delPatient.put("age", deOld);
			delPatient.put("gender", gender ? "male" : "female");
			delPatient.put("phone", dePhone);
			delPatient.put("address", deAddress);
			bookingParams.put("delegatedPatient", delPatient);
		}

		GlobalStorage.sTempBooking = bookingParams;
	}
}
