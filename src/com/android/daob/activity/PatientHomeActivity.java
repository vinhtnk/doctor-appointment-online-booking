package com.android.daob.activity;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.daob.application.AppController;
import com.android.daob.model.UserMeetingModel;
import com.android.daob.utils.Constants;
import com.android.doctor_appointment_online_booking.R;
import com.android.volley.Response;
import com.android.volley.Request.Method;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;

public class PatientHomeActivity extends BaseActivity {
	public static String TAG = PatientHomeActivity.class.getSimpleName();

	String url = Constants.URL + "patientDashboard/" + MainActivity.username;

	String cancelApp = Constants.URL + "patientCancelAppointment/";

	ListView lvMeeting;

	TextView tvNoResult;

	ArrayAdapter<UserMeetingModel> listMeetingAdapter;

	ArrayList<UserMeetingModel> listUserMeetingModels = new ArrayList<UserMeetingModel>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.patient_home_layout);
		lvMeeting = (ListView) findViewById(R.id.lvMeeting);
		tvNoResult = (TextView) findViewById(R.id.tv_no_result_patient_meeting);
		lvMeeting.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				final UserMeetingModel um = (UserMeetingModel) parent
						.getItemAtPosition(position);
				if (um.getStatus().equalsIgnoreCase(Constants.STATUS_NEW) ||
						um.getStatus().equalsIgnoreCase(Constants.STATUS_CONFIRMED)) {
					AlertDialog.Builder builder1 = new AlertDialog.Builder(
							PatientHomeActivity.this);
					final EditText input = new EditText(
							PatientHomeActivity.this);
					input.setInputType(InputType.TYPE_CLASS_TEXT);
					builder1.setView(input);
					builder1.setTitle(getApplicationContext().getResources()
							.getString(R.string.reason_canceled));

					builder1.setPositiveButton(getApplicationContext()
							.getResources().getString(R.string.btn_cancel),
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									dialog.cancel();
								}
							});
					builder1.setNegativeButton(getApplicationContext()
							.getResources().getString(R.string.cancled_app),
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									String urlUpdate  = cancelApp + um.getId();
									InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
									imm.hideSoftInputFromWindow(
											input.getWindowToken(), 0);
									HashMap<String, String> updateStatus = new HashMap<String, String>();
									updateStatus.put("status", "canceled");
									updateStatus.put("message", input.getText().toString());
									String content = PatientHomeActivity.this
											.getResources().getString(
													R.string.processing);
									showProgressDialog(content, false);
									JsonObjectRequest jsonObbjectReq = new JsonObjectRequest(
											Method.PUT, urlUpdate, new JSONObject(updateStatus),
											new Listener<JSONObject>() {

												@Override
												public void onResponse(
														JSONObject response) {
													// TODO Auto-generated
													// method stub
													try {
														if (response.getString(
																"message").equals(
																"success")) {
															getDashboard();
															Toast.makeText(
																	PatientHomeActivity.this,
																	getResources()
																			.getString(
																					R.string.update_success),
																	Toast.LENGTH_SHORT)
																	.show();
														} else {
															Toast.makeText(
																	PatientHomeActivity.this,
																	"Cuộc hẹn đã được cập nhật trước đó",
																	Toast.LENGTH_SHORT)
																	.show();
														}
													} catch (JSONException e) {
														// TODO Auto-generated catch
														// block
														e.printStackTrace();
													}
													closeProgressDialog();
												}

											}, new Response.ErrorListener() {
												@Override
												public void onErrorResponse(
														VolleyError arg0) {
													// TODO Auto-generated
													// method stub
													VolleyLog.e(TAG, "Error: "
															+ arg0.getMessage());
												}

											});
									AppController.getInstance().addToRequestQueue(
											jsonObbjectReq, "update to canceled");
								}
							});

					builder1.show();
				}
				return true;
			}
			
		});
		lvMeeting.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				final UserMeetingModel um = (UserMeetingModel) parent
						.getItemAtPosition(position);
				Intent i = new Intent(PatientHomeActivity.this, PatientAppointmentDetailActivity.class);
				Bundle bun = new Bundle();
		        bun.putString("appointmentId", um.getId());
				i.putExtras(bun);
				VolleyLog.e(TAG, "sendBun: " , bun);
				startActivity(i);
			}
		});
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		getDashboard();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.top_right_menu, menu);
		menu.getItem(0).setTitle(
				getResources().getString(R.string.search_doctor));
		menu.getItem(0).setIcon(
				this.getResources().getDrawable(R.drawable.ic_action_search));
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.top_right_button:
			Intent intentSearch = new Intent(this,
					PatientSearchDoctorActivity.class);
			startActivity(intentSearch);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}

	}

	@Override
	public void onBackPressed() {
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_HOME);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
	}

	void getDashboard() {
		String tag_json_getDashboard = "json_getDashboard_req";
		String content = PatientHomeActivity.this.getResources().getString(
				R.string.loading);
		showProgressDialog(content, false);
		Log.i("aa", url);
		JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(url,
				new Listener<JSONArray>() {

					@Override
					public void onResponse(JSONArray jsonArr) {
						listUserMeetingModels.clear();
						for (int i = 0; i < jsonArr.length(); i++) {
							UserMeetingModel umm = new UserMeetingModel();
							JSONObject jsonObjDoctor = new JSONObject();
							try {
								jsonObjDoctor = jsonArr.getJSONObject(i)
										.getJSONObject("doctor");
								umm.setDoctorName(jsonObjDoctor
										.getString("name"));
								umm.setHospital(jsonArr.getJSONObject(i)
										.getString("location"));
								umm.setDate(jsonArr.getJSONObject(i).getString(
										"meetingDate"));
								umm.setHour(jsonArr.getJSONObject(i).getString(
										"startTime"));
								umm.setStatus(jsonArr.getJSONObject(i)
										.getString("status"));
								umm.setId(jsonArr.getJSONObject(i).getString("id"));
								listUserMeetingModels.add(umm);
							} catch (JSONException e) {
								e.printStackTrace();
							}
						}
						if (listUserMeetingModels != null
								&& listUserMeetingModels.size() > 0) {
							tvNoResult.setVisibility(View.GONE);
							lvMeeting.setVisibility(View.VISIBLE);
							listMeetingAdapter = new UserMeetingViewAdapter(
									listUserMeetingModels);
							lvMeeting.setAdapter(listMeetingAdapter);
							listMeetingAdapter.notifyDataSetChanged();
						} else {
							tvNoResult.setVisibility(View.VISIBLE);
							lvMeeting.setVisibility(View.GONE);
						}
						closeProgressDialog();
					}
				}, new Response.ErrorListener() {

					@Override
					public void onErrorResponse(VolleyError error) {
						VolleyLog.e(TAG, "Error: " + error.getMessage());
						closeProgressDialog();
					}
				});
		AppController.getInstance().addToRequestQueue(jsonArrayRequest,
				tag_json_getDashboard);
	}

	class UserMeetingViewAdapter extends ArrayAdapter<UserMeetingModel> {
		ViewHolder holder;

		ArrayList<UserMeetingModel> listData;

		public UserMeetingViewAdapter(ArrayList<UserMeetingModel> listData) {
			super(PatientHomeActivity.this,
					R.layout.patient_meeting_item_layout, listData);
			this.listData = listData;
		}

		public ArrayList<UserMeetingModel> getListData() {
			return listData;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				LayoutInflater inflater = (LayoutInflater) getContext()
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = inflater.inflate(
						R.layout.patient_meeting_item_layout, parent, false);
				holder = new ViewHolder();
				holder.tvDoctorName = (TextView) convertView
						.findViewById(R.id.tv_doctor_name);
				holder.tvHospital = (TextView) convertView
						.findViewById(R.id.tv_hospital_name);
				holder.tvDate = (TextView) convertView
						.findViewById(R.id.tv_date_meeting);
				holder.tvHour = (TextView) convertView
						.findViewById(R.id.tv_hour_meeting);
				holder.tvStatus = (TextView) convertView
						.findViewById(R.id.tv_status_meeting);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			UserMeetingModel umm = listData.get(position);

			holder.tvDoctorName.setText(umm.getDoctorName());
			holder.tvHospital.setText(umm.getHospital());
			holder.tvDate.setText(umm.getDate());
			holder.tvHour.setText(umm.getHour());

			if (umm.getStatus().equalsIgnoreCase(Constants.STATUS_NEW)) {
				holder.tvStatus.setText(PatientHomeActivity.this.getResources()
						.getString(R.string.status_new));
				holder.tvStatus.setTextColor(PatientHomeActivity.this
						.getResources().getColor(R.color.app_new));
			} else if (umm.getStatus().equalsIgnoreCase(
					Constants.STATUS_CONFIRMED)) {
				holder.tvStatus.setText(PatientHomeActivity.this.getResources()
						.getString(R.string.status_confirmed));
				holder.tvStatus.setTextColor(PatientHomeActivity.this
						.getResources().getColor(R.color.app_confirmed));
			} else if (umm.getStatus().equalsIgnoreCase(Constants.STATUS_DONE)) {
				holder.tvStatus.setText(PatientHomeActivity.this.getResources()
						.getString(R.string.status_done));
				holder.tvStatus.setTextColor(PatientHomeActivity.this
						.getResources().getColor(R.color.app_done));
			} else if (umm.getStatus().equalsIgnoreCase(
					Constants.STATUS_CANCELED)) {
				holder.tvStatus.setText(PatientHomeActivity.this.getResources()
						.getString(R.string.status_canceled));
				holder.tvStatus.setTextColor(PatientHomeActivity.this
						.getResources().getColor(R.color.app_canceled));
			} else if (umm.getStatus().equalsIgnoreCase(
					Constants.STATUS_REJECTED)) {
				holder.tvStatus.setText(PatientHomeActivity.this.getResources()
						.getString(R.string.status_rejected));
				holder.tvStatus.setTextColor(PatientHomeActivity.this
						.getResources().getColor(R.color.app_rejected));
			} else if (umm.getStatus()
					.equalsIgnoreCase(Constants.STATUS_MISSED)) {
				holder.tvStatus.setText(PatientHomeActivity.this.getResources()
						.getString(R.string.status_missed));
				holder.tvStatus.setTextColor(PatientHomeActivity.this
						.getResources().getColor(R.color.app_missed));
			}

			return convertView;
		}
	}

	private class ViewHolder {
		TextView tvDoctorName;

		TextView tvHospital;

		TextView tvDate;

		TextView tvHour;

		TextView tvStatus;
	}
}
