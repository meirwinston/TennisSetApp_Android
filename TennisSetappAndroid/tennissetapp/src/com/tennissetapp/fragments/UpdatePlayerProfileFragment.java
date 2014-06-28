package com.tennissetapp.fragments;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import com.tennissetapp.Constants;
import com.tennissetapp.R;
import com.tennissetapp.TennisLevelAware;
import com.tennissetapp.TestConstants;
import com.tennissetapp.activities.BaseFragmentActivity;
import com.tennissetapp.activities.SignupActivity;
import com.tennissetapp.activities.TennisMatesActivity;
import com.tennissetapp.form.UpdateAccountPrimaryForm;
import com.tennissetapp.form.UpdateTennisDetailsForm;
import com.tennissetapp.json.GeoCodeResult;
import com.tennissetapp.json.JacksonUtils;
import com.tennissetapp.rest.Client;
import com.tennissetapp.rest.ServiceResponse;
import com.tennissetapp.ui.PlacesAutoCompleteAdapter;
import com.tennissetapp.utils.Utils;

import org.apache.commons.lang.StringUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Map;
import java.util.Random;


public class UpdatePlayerProfileFragment extends BaseFragment implements TennisLevelAware{
    private static final String TAG  = UpdatePlayerProfileFragment.class.getSimpleName();

    private TextView genderTextView,birthdayTextView,firstNameEditText,lastNameEditText;
    private AutoCompleteTextView addressAutoCompleteTextView;
    private GeoCodeResult.Result geoLocation;
    private int year, month, day;
    private DatePickerDialog datePickerDialog;
    private Button submitButton;

    private TextView typeOfMatchTextView,tennisLevelTextView,handTextView,typeOfPlayTextView;
    private TextView availabilityTextView;
    private TennisLevelFragment tennisLevelFragment = new TennisLevelFragment();
    private boolean singles,doubles,fullMatch,points,hittingAround;
    private String hand;
    private Float levelOfPlay;
    private boolean weekdayEveningCheck,weekdayAfternoonCheck, weekdayMorningCheck,
            weekendEveningCheck,weekendAfternoonCheck, weekendMorningCheck;

    private DatePickerDialog.OnDateSetListener onDateSetListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            DateFormat format = SimpleDateFormat.getDateInstance();
            Calendar cal = Calendar.getInstance();
            cal.set(year,monthOfYear,dayOfMonth);
            day = dayOfMonth ;
            month = monthOfYear + 1;
            UpdatePlayerProfileFragment.this.year = year;
            birthdayTextView.setText(format.format(cal.getTime()));

        }
    };

    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_player_profile_update, container, false);

        this.firstNameEditText = (EditText)rootView.findViewById(R.id.firstname_textview);
        this.lastNameEditText = (EditText)rootView.findViewById(R.id.lastname_edittext);
        this.submitButton = (Button)rootView.findViewById(R.id.submit_button);
        this.handTextView = (TextView)rootView.findViewById(R.id.hand_edittext);
        this.tennisLevelTextView = (TextView)rootView.findViewById(R.id.tennis_level_textview);
        this.availabilityTextView = (TextView)rootView.findViewById(R.id.availability_textview);
        this.typeOfPlayTextView = (TextView)rootView.findViewById(R.id.type_of_play_textview);
        this.typeOfMatchTextView = (TextView)rootView.findViewById(R.id.type_of_match_textview);
        this.birthdayTextView = (TextView)rootView.findViewById(R.id.birthdate_textview);
        genderTextView = (TextView)rootView.findViewById(R.id.gender_textview);
        addressAutoCompleteTextView = (AutoCompleteTextView)rootView.findViewById(R.id.location_textview);
        initGender();
        initAutoComplete();
        initBirthDate();
        initTennisLevel();
        initHand();
        initTypeOfMatch();
        initAvailability();
        initTypeOfPlay();
        initTypeOfMatchTextView();

        refresh();
        this.submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submit();
            }
        });
//        if(Constants.env != Constants.Env.PRODUCTION){
//            test();
//        }
        return rootView;
    }

    @SuppressWarnings("unchecked")
    private void refresh(){
        Client.getInstance().getPlayerProfile(0l,new Client.TaskProgress() {
            @Override
            public void onPostExecute(ServiceResponse response) {
                Log.i(TAG, "PLAYER PRIFILE RESPONSE: " + response);

                Map<String,Object> profile = (Map<String,Object>)response.get("profile");
                populateFirstLastNames(profile);
                populateLevelOfPlay(profile);
                populateTypeOfPlay(profile);
                populateAvailability(profile);
                populateGender(profile);
                populateBirthDate(profile);
                populateLocation(profile);
                populateTypeOfMatch(profile);
                populateHand(profile);
            }
        });
    }

    private void setHand(String hand){
    	this.hand = hand;
    	if("RIGHT".equals(hand)){
    		handTextView.setText("Right");
    	}
    	else{
    		handTextView.setText("Left");
    	}
        
    }
    private void populateHand(Map<String,Object> profileInfo){
        setHand((String)profileInfo.get("hand"));
    }
    private void populateTypeOfMatch(Map<String,Object> profileInfo){
        setTypeOfMatch(true,true);
    }
    private void populateBirthDate(Map<String,Object> profileInfo){
        onDateSetListener.onDateSet(this.datePickerDialog.getDatePicker(),1995,3,3);
    }
    private void populateGender(Map<String,Object> profileInfo){
        Log.d(TAG,"populateGender " + profileInfo.get("gender"));
        String gender =(String)profileInfo.get("gender");
        if(Constants.AttributeKeys.Gender.MALE.equals(gender)){
            genderTextView.setText("Male");
        }
        else if(Constants.AttributeKeys.Gender.FEMALE.equals(gender)) {
            genderTextView.setText("Female");
        }
    }
    private void populateLocation(Map<String,Object> profileInfo){
        addressAutoCompleteTextView.setText(profileInfo.get("administrativeAreaLevel1") + ", " + profileInfo.get("country"));
    }
    private void populateFirstLastNames(Map<String,Object> profileInfo){
        firstNameEditText.setText((String)profileInfo.get("firstName"));
        lastNameEditText.setText((String)profileInfo.get("lastName"));
    }
    
    public void setTennisLevel(float level){
        this.levelOfPlay = level;
        this.tennisLevelTextView.setText(String.valueOf(level));
    }

    private void populateLevelOfPlay(Map<String,Object> profileInfo){
    	setTennisLevel(((Number)profileInfo.get("levelOfPlay")).floatValue());
    }

    private void populateTypeOfPlay(Map<String,Object> profileInfo){
        
    	setTypeOfPlay((boolean)profileInfo.get("playHittingAround"), 
    			(boolean)profileInfo.get("playPoints"),
    			(boolean)profileInfo.get("playFullMatch"));
    }

    private void populateAvailability(Map<String,Object> profileInfo){
    	setAvailability((boolean)profileInfo.get("availableWeekdayEvening"), 
    			(boolean)profileInfo.get("availableWeekdayAfternoon"), 
    			(boolean)profileInfo.get("availableWeekdayMorning"), 
    			(boolean)profileInfo.get("availableWeekendEvening"), 
    			(boolean)profileInfo.get("availableWeekendAfternoon"), 
    			(boolean)profileInfo.get("availableWeekendMorning"));
    }

    private void submit2(){
        UpdateTennisDetailsForm form = toUpdateTennisDetailsForm();
        Log.d(getClass().getSimpleName(), "submit " + form);
        ServiceResponse response = Client.getInstance().updatePlayerTennisDetails(form);

        if(response.containsKey("exception")){
            if("com.tennissetapp.exception.NotAuthorizedException".equals(response.get("code"))){
                Intent intent = new Intent(getActivity(), SignupActivity.class);
                this.startActivity(intent);
            }
        }
        else if(response.containsKey("errors")){
            Utils.popupErrors(getActivity(), response);
        }
        else{
            Intent intent = new Intent(getActivity(), TennisMatesActivity.class);
            this.startActivity(intent);
//			Toast.makeText(getActivity(), "TEST SUCCESSFUL - PLAYER PROFILE UPDATED!", Toast.LENGTH_LONG).show();
        }
    }


    private UpdateAccountPrimaryForm toUpdateAccountPrimaryFormForm(){
        UpdateAccountPrimaryForm f = new UpdateAccountPrimaryForm();
//		f.userAccountId = this.userAccountId;
        f.firstName = this.firstNameEditText.getText().toString();
        f.lastName = this.lastNameEditText.getText().toString();
        f.gender = getGender();
        f.agreesToTerms = "true";

        //birthdate
        f.birthDay = String.valueOf(day);
        f.birthMonth = String.valueOf(month);
        f.birthYear = String.valueOf(year);

        if(Constants.env != Constants.Env.PRODUCTION){
            //-----TEST to work offline
            String s = "{\"address_components\" : [{\"long_name\" : \"1600\",\"short_name\" : \"1600\",\"types\" : [ \"street_number\" ]},{\"long_name\" : \"Amphitheatre Parkway\",\"short_name\" : \"Amphitheatre Pkwy\",\"types\" : [ \"route\" ]},{\"long_name\" : \"Mountain View\",\"short_name\" : \"Mountain View\",\"types\" : [ \"locality\", \"political\" ]},{\"long_name\" : \"Santa Clara\",\"short_name\" : \"Santa Clara\",\"types\" : [ \"administrative_area_level_2\", \"political\" ]},{\"long_name\" : \"California\",\"short_name\" : \"CA\",\"types\" : [ \"administrative_area_level_1\", \"political\" ]},{\"long_name\" : \"United States\",\"short_name\" : \"US\",\"types\" : [ \"country\", \"political\" ]},{\"long_name\" : \"94043\",\"short_name\" : \"94043\",\"types\" : [ \"postal_code\" ]}],\"formatted_address\" : \"1600 Amphitheatre Parkway, Mountain View, CA 94043, USA\",\"geometry\" : {\"location\" : {\"lat\" : 37.4219988,\"lng\" : -122.083954},\"location_type\" : \"ROOFTOP\",\"viewport\" : {\"northeast\" : {\"lat\" : 37.42334778029149,\"lng\" : -122.0826050197085},\"southwest\" : {\"lat\" : 37.42064981970849,\"lng\" : -122.0853029802915}}},\"types\" : [ \"street_address\" ]}";
            try {
                this.geoLocation = JacksonUtils.deserializeAs(s, GeoCodeResult.Result.class);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            //-----
        }

        if(this.geoLocation != null){
            f.populate(this.geoLocation);
        }
        return f;
    }

    private UpdateTennisDetailsForm toUpdateTennisDetailsFormForm(){
        UpdateTennisDetailsForm f = new UpdateTennisDetailsForm();
        if(this.levelOfPlay != null){
            f.levelOfPlay = String.valueOf(this.levelOfPlay);
        }
        //type of play
        f.doublesCheck = (this.doubles ? "on" : null);
        f.singlesCheck = (this.singles ? "on" : null);
        f.pointsCheck = (this.points ? "on" : null);
        f.fullMatchCheck = (this.fullMatch ? "on" : null);
        f.hittingAroundCheck = (this.hittingAround ? "on" : null);

        //availability
        f.weekdayAvailabilityAfternoonCheck = (this.weekdayAfternoonCheck ? "on" : null);
        f.weekdayAvailabilityEveningCheck = (this.weekdayEveningCheck ? "on" : null);
        f.weekdayAvailabilityMorningCheck = (this.weekdayMorningCheck ? "on" : null);
        f.weekendAvailabilityAfternoonCheck = (this.weekendAfternoonCheck ? "on" : null);
        f.weekendAvailabilityEveningCheck = (this.weekendEveningCheck ? "on" : null);
        f.weekendAvailabilityMorningCheck = (this.weekendMorningCheck ? "on" : null);

        //hand
        f.hand = this.hand;

        return f;
    }

    private void submit(){
        this.submitButton.setEnabled(false);
        UpdateAccountPrimaryForm form = toUpdateAccountPrimaryFormForm();
        Log.d(getClass().getSimpleName(), "submit " + form);
        ServiceResponse response = Client.getInstance().updateAccountPrimaryFields(form);

        if(response.containsKey("exception")){
            this.submitButton.setEnabled(true);
            if("com.tennissetapp.exception.NotAuthorizedException".equals(response.get("code"))){
                Intent intent = new Intent(getActivity(), SignupActivity.class);
                this.startActivity(intent);
            }
        }
        else if(response.containsKey("errors")){
            this.submitButton.setEnabled(true);
            Utils.popupErrors(getActivity(), response);
        }
        else{
        	UpdateTennisDetailsForm tennisDetailsForm = toUpdateTennisDetailsForm();
        	Log.i(TAG, "tennisDetailsForm: " + tennisDetailsForm);
            response = Client.getInstance().updatePlayerTennisDetails(tennisDetailsForm);
            if(response.containsKey("exception")){
                this.submitButton.setEnabled(true);
                if("com.tennissetapp.exception.NotAuthorizedException".equals(response.get("code"))){
                    Intent intent = new Intent(getActivity(), SignupActivity.class);
                    this.startActivity(intent);
                }
            }
            else if(response.containsKey("errors")){
                this.submitButton.setEnabled(true);
                Utils.popupErrors(getActivity(), response);
            }
            else{
            	showConfirmDialog();
            	this.submitButton.setEnabled(true);
            }
        }
    }
     
    private void showConfirmDialog(){
    	AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		
        builder.setTitle("Profile Updated");
        builder.setMessage("Your profile updated successfully!");
        builder.setPositiveButton(android.R.string.ok,
		new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});
        
        builder.show();
    }

    private void initBirthDate(){
        final Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, c.get(Calendar.YEAR)-20);
        year = c.get(Calendar.YEAR);
        month = c.get(Calendar.MONTH);
        day = c.get(Calendar.DAY_OF_MONTH);

        this.datePickerDialog = new DatePickerDialog(getActivity(), this.onDateSetListener, year, month, day);
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis()-315360000000l); //-10 years
        this.birthdayTextView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                datePickerDialog.show();
            }
        });
    }

    private void initAutoComplete(){
        this.addressAutoCompleteTextView.setAdapter(new PlacesAutoCompleteAdapter(getActivity(), R.layout.listitem));
        addressAutoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                geoLocation = (GeoCodeResult.Result) adapterView.getItemAtPosition(position);
            }
        });
    }

    private void showGenderDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_gender, null);
        builder.setView(view);
        final AlertDialog dialog = builder.show();

        final String[] arr = getResources().getStringArray(R.array.profile_gender);

        view.findViewById(R.id.male_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                genderTextView.setText(arr[0]);
                dialog.dismiss();
            }
        });

        view.findViewById(R.id.female_layout).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                genderTextView.setText(arr[1]);
                dialog.dismiss();
            }
        });
    }

    private void initGender(){
        genderTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showGenderDialog();
            }
        });
    }

    private String getGender(){
        if(this.genderTextView.getText().equals("Male")){
            return "MALE";
        }
        else if(this.genderTextView.getText().equals("Female")){
            return "FEMALE";
        }
        return null;
    }


    private void test(){
        setTennisLevel(2.0f);
        //--
        hand = "RIGHT";
        handTextView.setText("Right");
        //--
        genderTextView.setText(getResources().getStringArray(R.array.profile_gender)[0]);
        //--

        hittingAround = true;
        points = true;
        fullMatch = true;

        ArrayList<String> list = new ArrayList<String>();
        if(hittingAround){
            list.add(getResources().getString(R.string.label_hitting_around));
        }
        if(points){
            list.add(getResources().getString(R.string.label_points));
        }
        if(fullMatch){
            list.add(getResources().getString(R.string.label_full_match));
        }
        if(list.size() > 0){
            typeOfPlayTextView.setText(StringUtils.join(list, ", "));
        }

        //--

        singles = true;
        doubles = false;

        list = new ArrayList<String>();
        if(singles){
            list.add(getResources().getString(R.string.label_singles));
        }
        if(doubles){
            list.add(getResources().getString(R.string.label_doubles));
        }
        if(list.size() > 0){
            typeOfMatchTextView.setText(StringUtils.join(list, ", "));
        }

        //--

        weekdayEveningCheck = true;
        weekdayAfternoonCheck = false;
        weekdayMorningCheck = false;
        weekendEveningCheck = true;
        weekendAfternoonCheck = false;
        weekendMorningCheck = false;

        ArrayList<String> weekdayList = new ArrayList<String>();
        if(weekdayMorningCheck){
            weekdayList.add("Morning");
        }
        if(weekdayAfternoonCheck){
            weekdayList.add("Afternoon");
        }
        if(weekdayEveningCheck){
            weekdayList.add("Evening");
        }

        ArrayList<String> weekendList = new ArrayList<String>();
        if(weekendMorningCheck){
            weekendList.add("Morning");
        }
        if(weekendAfternoonCheck){
            weekendList.add("Afternoon");
        }
        if(weekendEveningCheck){
            weekendList.add("Evening");
        }

        StringBuilder sb = new StringBuilder();
        if(weekdayList.size() > 0){
            sb.append("Weekdays: " + StringUtils.join(weekdayList, ", "));
            if(weekendList.size() > 0){
                sb.append("\n");
            }
        }
        if(weekendList.size() > 0){
            sb.append("Weekends: " + StringUtils.join(weekendList, ", "));
        }
        if(sb.length() > 0){
            availabilityTextView.setText(sb.toString());
        }

        //--

        Random r = new Random();
        int index = r.nextInt(TestConstants.Names.length);
        String[] gen = {"Male","Female"};
        this.firstNameEditText.setText(TestConstants.firstName(index));
        this.lastNameEditText.setText(TestConstants.lastName(index));
        this.genderTextView.setText(gen[r.nextInt(1)]);
        onDateSetListener.onDateSet(null, 1960 + r.nextInt(40), r.nextInt(12), r.nextInt(28));
    }

	@Override
	public void onResume() {
		super.onResume();
		((BaseFragmentActivity)getActivity()).getActivityUtils().setActionBarTitle(R.string.title_edit_profile);
	}

    private void initHand(){
        this.handTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showHandDialog();
            }
        });
    }

    private void initTennisLevel(){

        this.tennisLevelTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG,"tennisLevelTextView.onClick+++");
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction t = fragmentManager.beginTransaction();
                t.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_in_left);
                getActivity().getIntent().putExtra(Constants.ArgumentKey.LAST_FRAGMENT_TAG.toString(), getResources().getString(R.string.tag_player_profile_update));
                t.replace(R.id.content_frarment, tennisLevelFragment,getResources().getString(R.string.tag_player_level)); //
                t.addToBackStack(getResources().getString(R.string.tag_player_profile_update));
                t.commit();
                ((BaseFragmentActivity)getActivity()).getActivityUtils().setActionBarTitle(R.string.title_tennis_level);
            }
        });
    }

    private void initTypeOfPlay(){
        this.typeOfPlayTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTypeOfPlayDialog();
            }
        });
    }

    private void showHandDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_hand, null);
        builder.setView(view);
        final AlertDialog dialog = builder.show();

        view.findViewById(R.id.right_hand_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                hand = "RIGHT";
//                handTextView.setText("Right");
            	setHand("RIGHT");
                dialog.dismiss();
            }
        });

        view.findViewById(R.id.left_hand_layout).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
//                hand = "LEFT";
//                handTextView.setText("Left");
            	setHand("LEFT");
                dialog.dismiss();
            }
        });
    }
    
    private void setTypeOfPlay(boolean hittingAround, boolean points, boolean fullMatch){
        ArrayList<String> list = new ArrayList<String>();
        this.hittingAround = hittingAround;
        this.points = points;
        this.fullMatch = fullMatch;
        if(hittingAround){
            list.add(getResources().getString(R.string.label_hitting_around));
        }
        if(points){
            list.add(getResources().getString(R.string.label_points));
        }
        if(fullMatch){
            list.add(getResources().getString(R.string.label_full_match));
        }
        if(list.size() > 0){
            typeOfPlayTextView.setText(StringUtils.join(list, ", "));
        }

    }
    
    private void showTypeOfPlayDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_type_of_play, null);
        final CheckBox hittingAroundCheck = ((CheckBox)view.findViewById(R.id.hitting_around_checkbox));
        final CheckBox pointsCheck = ((CheckBox)view.findViewById(R.id.points_checkbox));
        final CheckBox fullMatchCheck = ((CheckBox)view.findViewById(R.id.full_match_checkbox));
        builder.setView(view);
        hittingAroundCheck.setChecked(hittingAround);
        pointsCheck.setChecked(points);
        fullMatchCheck.setChecked(fullMatch);

        final AlertDialog dialog = builder.show();

        view.findViewById(R.id.cancel_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        view.findViewById(R.id.ok_button).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                setTypeOfPlay(hittingAroundCheck.isChecked(), pointsCheck.isChecked(),fullMatchCheck.isChecked());
                dialog.dismiss();
            }
        });
    }
    
    private void setAvailability(boolean weekdayEveningCheck,
    		boolean weekdayAfternoonCheck,
    		boolean weekdayMorningCheck,
    		boolean weekendEveningCheck,
    		boolean weekendAfternoonCheck,
    		boolean weekendMorningCheck){
    	
    	this.weekdayMorningCheck = weekdayMorningCheck;
		this.weekdayAfternoonCheck = weekdayAfternoonCheck;
		this.weekdayEveningCheck = weekdayEveningCheck;
		this.weekendAfternoonCheck = weekendAfternoonCheck;
		this.weekendEveningCheck = weekendEveningCheck; 
		this.weekendMorningCheck = weekendMorningCheck;
		
    	ArrayList<String> weekdayList = new ArrayList<String>();
    	if(weekdayMorningCheck){
    		weekdayList.add("Morning");
    	}
    	if(weekdayAfternoonCheck){
    		weekdayList.add("Afternoon");

    	}
    	if(weekdayEveningCheck){
    		weekdayList.add("Evening");
    	}

    	ArrayList<String> weekendList = new ArrayList<String>();
    	if(weekendMorningCheck){
    		weekendList.add("Morning");
    	}
    	if(weekendAfternoonCheck){
    		weekendList.add("Afternoon");
    	}
    	if(weekendEveningCheck){
    		weekendList.add("Evening");
    	}

    	StringBuilder sb = new StringBuilder();
    	if(weekdayList.size() > 0){
    		sb.append("Weekdays: " + StringUtils.join(weekdayList, ", "));
    		if(weekendList.size() > 0){
    			sb.append("\n");
    		}
    	}
    	if(weekendList.size() > 0){
    		sb.append("Weekends: " + StringUtils.join(weekendList, ", "));
    	}
    	if(sb.length() > 0){
    		availabilityTextView.setText(sb.toString());
    	}

    }

    private void showAvailabilityDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_availability, null);
        final CheckBox weekdayEveningCheckbox = ((CheckBox)view.findViewById(R.id.weekday_evening_checkbox));
        final CheckBox weekdayAfternoonCheckbox = ((CheckBox)view.findViewById(R.id.weekday_afternoon_checkbox));
        final CheckBox weekdayMorningCheckbox = ((CheckBox)view.findViewById(R.id.weekday_morning_checkbox));
        final CheckBox weekendEveningCheckbox = ((CheckBox)view.findViewById(R.id.weekend_evening_checkbox));
        final CheckBox weekendAfternoonCheckbox = ((CheckBox)view.findViewById(R.id.weekend_afternoon_checkbox));
        final CheckBox weekendMorningCheckbox = ((CheckBox)view.findViewById(R.id.weekend_morning_checkbox));
        builder.setView(view);

        weekdayEveningCheckbox.setChecked(weekdayEveningCheck);
        weekdayAfternoonCheckbox.setChecked(weekdayAfternoonCheck);
        weekdayMorningCheckbox.setChecked(weekdayMorningCheck);
        weekendEveningCheckbox.setChecked(weekendEveningCheck);
        weekendAfternoonCheckbox.setChecked(weekendAfternoonCheck);
        weekendMorningCheckbox.setChecked(weekendMorningCheck);

        final AlertDialog dialog = builder.show();

        view.findViewById(R.id.cancel_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        view.findViewById(R.id.ok_button).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
            	setAvailability(weekdayEveningCheckbox.isChecked(),
            			weekdayAfternoonCheckbox.isChecked(),
            			weekdayMorningCheckbox.isChecked(),
            			weekendEveningCheckbox.isChecked(),
            			weekendAfternoonCheckbox.isChecked(),
            			weekendMorningCheckbox.isChecked());
                dialog.dismiss();
            }
        });
    }


    private void showTypeOfMatchDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_type_of_match, null);
        final CheckBox singlesCheck = ((CheckBox)view.findViewById(R.id.singles_checkbox));
        final CheckBox doublesCheck = ((CheckBox)view.findViewById(R.id.doubles_checkbox));
        builder.setView(view);
        singlesCheck.setChecked(singles);
        doublesCheck.setChecked(doubles);

        final AlertDialog dialog = builder.show();

        view.findViewById(R.id.cancel_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        view.findViewById(R.id.ok_button).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                setTypeOfMatch(singlesCheck.isChecked(),doublesCheck.isChecked());
                dialog.dismiss();
            }
        });


    }

    private void setTypeOfMatch(boolean singles, boolean doubles){
        this.singles = singles;
        this.doubles = doubles;

        ArrayList<String> list = new ArrayList<String>();
        if(singles){
            list.add(getResources().getString(R.string.label_singles));
        }
        if(doubles){
            list.add(getResources().getString(R.string.label_doubles));
        }
        if(list.size() > 0){
            typeOfMatchTextView.setText(StringUtils.join(list, ", "));
        }
    }

    private void initTypeOfMatchTextView(){
        this.typeOfMatchTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTypeOfMatchDialog();
            }
        });
    }

    private void initAvailability(){
        availabilityTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAvailabilityDialog();
            }
        });
    }

    private void initTypeOfMatch(){
        typeOfMatchTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getActivity(),android.R.layout.select_dialog_singlechoice,getResources().getStringArray(R.array.profile_gender));
                builder.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        typeOfMatchTextView.setText(arrayAdapter.getItem(which));
                        dialog.dismiss();
                    }
                });

                builder.show();
            }
        });
    }
    
    private UpdateTennisDetailsForm toUpdateTennisDetailsForm(){
        UpdateTennisDetailsForm f = new UpdateTennisDetailsForm();
        if(this.levelOfPlay != null){
            f.levelOfPlay = String.valueOf(this.levelOfPlay);
        }
        //type of play
        f.doublesCheck = (this.doubles ? "on" : null);
        f.singlesCheck = (this.singles ? "on" : null);
        f.pointsCheck = (this.points ? "on" : null);
        f.fullMatchCheck = (this.fullMatch ? "on" : null);
        f.hittingAroundCheck = (this.hittingAround ? "on" : null);

        //availability
        f.weekdayAvailabilityAfternoonCheck = (this.weekdayAfternoonCheck ? "on" : null);
        f.weekdayAvailabilityEveningCheck = (this.weekdayEveningCheck ? "on" : null);
        f.weekdayAvailabilityMorningCheck = (this.weekdayMorningCheck ? "on" : null);
        f.weekendAvailabilityAfternoonCheck = (this.weekendAfternoonCheck ? "on" : null);
        f.weekendAvailabilityEveningCheck = (this.weekendEveningCheck ? "on" : null);
        f.weekendAvailabilityMorningCheck = (this.weekendMorningCheck ? "on" : null);

        //hand
        f.hand = this.hand;

        //--
        UpdateAccountPrimaryForm f2 = new UpdateAccountPrimaryForm();
//		f2.userAccountId = this.userAccountId;
        f2.firstName = this.firstNameEditText.getText().toString();
        f2.lastName = this.lastNameEditText.getText().toString();
        f2.gender = getGender();
        f2.agreesToTerms = "true";

        //birthdate
        f2.birthDay = String.valueOf(day);
        f2.birthMonth = String.valueOf(month);
        f2.birthYear = String.valueOf(year);

        if(Constants.env != Constants.Env.PRODUCTION){
            //-----TEST to work offline
            String s = "{\"address_components\" : [{\"long_name\" : \"1600\",\"short_name\" : \"1600\",\"types\" : [ \"street_number\" ]},{\"long_name\" : \"Amphitheatre Parkway\",\"short_name\" : \"Amphitheatre Pkwy\",\"types\" : [ \"route\" ]},{\"long_name\" : \"Mountain View\",\"short_name\" : \"Mountain View\",\"types\" : [ \"locality\", \"political\" ]},{\"long_name\" : \"Santa Clara\",\"short_name\" : \"Santa Clara\",\"types\" : [ \"administrative_area_level_2\", \"political\" ]},{\"long_name\" : \"California\",\"short_name\" : \"CA\",\"types\" : [ \"administrative_area_level_1\", \"political\" ]},{\"long_name\" : \"United States\",\"short_name\" : \"US\",\"types\" : [ \"country\", \"political\" ]},{\"long_name\" : \"94043\",\"short_name\" : \"94043\",\"types\" : [ \"postal_code\" ]}],\"formatted_address\" : \"1600 Amphitheatre Parkway, Mountain View, CA 94043, USA\",\"geometry\" : {\"location\" : {\"lat\" : 37.4219988,\"lng\" : -122.083954},\"location_type\" : \"ROOFTOP\",\"viewport\" : {\"northeast\" : {\"lat\" : 37.42334778029149,\"lng\" : -122.0826050197085},\"southwest\" : {\"lat\" : 37.42064981970849,\"lng\" : -122.0853029802915}}},\"types\" : [ \"street_address\" ]}";
            try {
                this.geoLocation = JacksonUtils.deserializeAs(s, GeoCodeResult.Result.class);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            //-----
        }

        if(this.geoLocation != null){
            f2.populate(this.geoLocation);
        }

        return f;
    }
}

