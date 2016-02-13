package com.example.thomas.voyage.BasicActivities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.thomas.voyage.ContainerClasses.Msg;
import com.example.thomas.voyage.Databases.DBheroesAdapter;
import com.example.thomas.voyage.R;

import java.util.ArrayList;
import java.util.List;

public class HospitalActivity extends Activity {

    private DBheroesAdapter h;
    private List<BrokenHero> brokenHeroList = new ArrayList<>();
    private List<Slot> slotsList = new ArrayList<>();
    private List<Integer> slotIndexForTimerList = new ArrayList<>();
    private List<CountDownTimer> timerList = new ArrayList<>();
    private TextView freeSlotsView, fortuneView, abortMedicationView, boostMedicationView;
    private int lastSelectedSlotIndex = -1;
    private long[] timeUsedArray = {0,0,0};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hospital);
        hideSystemUI();
        prepareActivity();

        /*

        USAGE:

        Um Helden heilen zu lassen, wird dieser über

         -> freie Hospital-Fläche antippen
         -> 'HeroCampActivity' starten und angetippten slotIndex mitgeben
         -> Helden in 'HeroCampActivity' auswählen - "MedSlotIndex"-Eintrag wird angepasst
         -> zurück zu 'HospitalActivity'
         -> Held wird angezeigt

        eingetragen.

        */
    }

    @Override
    protected void onRestart() {
        super.onRestart();  // Always call the superclass method first
        hideSystemUI();
    }


    /**

     onClick-Methoden

     */


    public void onClick(View v){
        switch(v.getId()){
            case R.id.imagebutton_hospital_back_button:

                for(int i = 0; i < brokenHeroList.size(); i++){

                    slotsList.get(brokenHeroList.get(i).getSlotIndex()).cancelCountDownTimer(
                            brokenHeroList.get(i), brokenHeroList.get(i).getTimeToLeave() - timeUsedArray[brokenHeroList.get(i).getSlotIndex()]
                    );

                    /*
                    brokenHeroList.get(i).setTimeToLeave(
                            brokenHeroList.get(i).getBufferTime(-1)
                    );
                    */
                }
                super.onBackPressed();
                finish();
                break;
            case R.id.framelayout_hospital_slot_0:
                checkForAction(0);
                break;
            case R.id.framelayout_hospital_slot_1:
                checkForAction(1);
                break;
            case R.id.framelayout_hospital_slot_2:
                checkForAction(2);
                break;
            case R.id.textview_hospital_abort_medication:
                abortMedication();
                break;
            case R.id.textview_hospital_boost_healing:
                boostMedication();
                break;
            default:
                Msg.msg(this, "ERROR @ HospitalActivity : onClick : switch : default called");
        }
    }


    /**

     Funktionen

     */


    private void initializeBrokenHeroes(){
        try {
            DBheroesAdapter heroesHelper = new DBheroesAdapter(this);
            long sizeDatabase = heroesHelper.getTaskCount();

            for(int i = 1; i <= sizeDatabase; i++){
                Log.v("iniBrokenHeroes", "time to leave from db @ index " + i + ": " + heroesHelper.getTimeToLeave(i));
            }

            List<Integer> placeholderList = new ArrayList<>();
            for(int i = 0; i < 3; i++) placeholderList.add(i);

            for(int i = 1; i <= sizeDatabase; i++){
                int medSlot = heroesHelper.getMedSlotIndex(i);

                if(medSlot != -1){

                    //Msg.msg(getApplicationContext(), "medSlot != -1 @ index " + i);

                    brokenHeroList.add( new BrokenHero(i, medSlot));
                    slotsList.get(heroesHelper.getMedSlotIndex(i)).showHero( brokenHeroList.get( brokenHeroList.size() - 1));

                    for(int j = 0; j < placeholderList.size(); j++){
                        if(placeholderList.get(j) == medSlot) placeholderList.remove(j);
                    }
                }
            }

            for(int i = 0; i < brokenHeroList.size(); i++){
                slotsList.get( brokenHeroList.get(i).getSlotIndex()).showHero(brokenHeroList.get(i));
            }

            for(int i = 0; i < placeholderList.size(); i++){
                slotsList.get( placeholderList.get(i)).showPlaceholder();
            }

            setFreeSlotsView();
            setFortuneView();

        }catch (IndexOutOfBoundsException e){
            Msg.msg(this, e + "");
        }
    }

    private void checkForAction(int slotIndex){
        boolean isUsed = false;
        lastSelectedSlotIndex = slotIndex;

        for(int i = 0; i < brokenHeroList.size(); i++){
            if( brokenHeroList.get(i).getSlotIndex() == slotIndex ) isUsed = true;
        }

        if(!isUsed){

            Intent i = new Intent(getApplicationContext(), HeroCampActivity.class);
            i.putExtra("ORIGIN", "HospitalActivity");
            i.putExtra("SLOT_INDEX", slotIndex);
            startActivity(i);
            finish();

            /*
            lastSelectedSlotIndex = -1;
            brokenHeroList.add(new BrokenHero(slotIndex + 1, slotIndex));
            slotsList.get( slotIndex ).showHero(
                    brokenHeroList.get(brokenHeroList.size() - 1)
            );
            */

        }else{
            abortMedicationView.setTextColor(Color.parseColor("#ffffff"));
            boostMedicationView.setTextColor(Color.parseColor("#ffffff"));
        }

        setFreeSlotsView();
    }

    private void removeBrokenHeroFromList(int slotIndex){

        for(int index = 0; index < brokenHeroList.size(); index++){

            if(brokenHeroList.get(index).getSlotIndex() == slotIndex){
                slotsList.get(slotIndex).cancelCountDownTimer(brokenHeroList.get(index),0);
                slotsList.get(slotIndex).showPlaceholder();
                brokenHeroList.get(index).setHeroMedSlotIndex(-1);
                brokenHeroList.get(index).setHeroHitpoints(brokenHeroList.get(index).getHpNow());
                brokenHeroList.remove(index);
                index = 3;

            }else if(index == 2) Msg.msg(this, "ERROR @ removeBrokenHeroFromList : no matching index");
        }
    }

    private void setFreeSlotsView(){
        freeSlotsView.setText(brokenHeroList.size() + " / " + slotsList.size());
    }

    private void setFortuneView(){
        SharedPreferences prefs = getSharedPreferences("CURRENT_MONEY_PREF", Context.MODE_PRIVATE);
        long money = prefs.getLong("currentMoneyLong", -1);

        fortuneView.setText("$ " + money);
    }

    private void abortMedication(){
        if(lastSelectedSlotIndex == -1){
            Msg.msg(this, "No hero yet choosen!");

        }else{

            for(int i = 0; i < brokenHeroList.size(); i++){
                if( brokenHeroList.get(i).getSlotIndex() == lastSelectedSlotIndex ){

                    i = brokenHeroList.size();
                    Msg.msg(this, "No more medication");

                    removeBrokenHeroFromList(lastSelectedSlotIndex);
                }
            }

            lastSelectedSlotIndex = -1;
            setFreeSlotsView();
            setFortuneView();
            boostMedicationView.setTextColor(Color.parseColor("#707070"));
            abortMedicationView.setTextColor(Color.parseColor("#707070"));
        }
    }

    private void boostMedication(){
        if(lastSelectedSlotIndex == -1){
            Msg.msg(this, "No hero yet choosen!");

        }else{

            for(int i = 0; i < brokenHeroList.size(); i++){
                if( brokenHeroList.get(i).getSlotIndex() == lastSelectedSlotIndex ){
                    int hpNew = brokenHeroList.get(i).getHpTotal();

                    // reutrn-Wert aus Datenbank wird davor in boolean umgewandelt
                    //  und hier betrachtet - wenn 'false', dann Fehler bei update
                    if(!brokenHeroList.get(i).setHeroHitpoints(hpNew)) Msg.msg(this, "ERROR @ setHeroHitpoints");

                    // zum Beenden der Schleife
                    i = brokenHeroList.size();
                    Msg.msg(this, "Heal-A-Hero!");
                    removeBrokenHeroFromList(lastSelectedSlotIndex);
                }
            }

            lastSelectedSlotIndex = -1;
            setFreeSlotsView();
            setFortuneView();

            // beide wieder als inaktiv zeigen, da Auswahl nun beendet ist
            boostMedicationView.setTextColor(Color.parseColor("#707070"));
            abortMedicationView.setTextColor(Color.parseColor("#707070"));
        }
    }

    private long getNewDate(int hpNow, int hpTotal){

        // Wie viel % der HpTotal sind noch nicht geheilt
        // +1, um Rundungsfehler zu korrigieren
        long percentToCompleteHealth = ((hpTotal-hpNow) / hpTotal) * 100 + 1;

        // Jedes Prozent benötigt eine Stunde zur Heilung
        return(System.currentTimeMillis() + (60 * 60 * 1000 * percentToCompleteHealth));
    }


    /**

     Klassen

     */


    private class Slot{
        private int slotIndex;
        private TextView nameView, hpNowView, timeToLeaveView, staticHpView, staticTimeView;
        private ImageView profileResourceView;

        public Slot(int slotIndex){
            this.slotIndex = slotIndex;
            profileResourceView = (ImageView) findViewById(getResources().getIdentifier("imageview_hospital_hero_" + slotIndex, "id", getPackageName()));
            nameView = (TextView) findViewById(getResources().getIdentifier("textview_hospital_hero_name_" + slotIndex,"id",getPackageName()));
            hpNowView = (TextView) findViewById(getResources().getIdentifier("textview_hospital_hero_hp_" + slotIndex,"id",getPackageName()));
            timeToLeaveView = (TextView) findViewById(getResources().getIdentifier("textview_hospital_hero_time_" + slotIndex,"id",getPackageName()));
            staticHpView = (TextView) findViewById(getResources().getIdentifier("static_textview_hospital_hp_" + slotIndex,"id",getPackageName()));
            staticTimeView = (TextView) findViewById(getResources().getIdentifier("static_textview_hospital_time_" + slotIndex,"id",getPackageName()));
        }

        public void showHero(BrokenHero hero){
            //Msg.msg(getApplicationContext(), "showHero called");
            profileResourceView.setImageResource(getResources().getIdentifier(hero.getProfileResource(), "mipmap", getPackageName()));
            nameView.setText(hero.getName() + "");
            hpNowView.setText(hero.getHpNow() + " / " + hero.getHpTotal());
            staticHpView.setVisibility(View.VISIBLE);
            staticTimeView.setText("abreise in min");

            long dateDiff = hero.getTimeToLeave() - System.currentTimeMillis();

            Log.v("showHero", "getTimeToLeave: " + hero.getTimeToLeave());
            Log.v("showHero", "dateDiff: " + dateDiff);
            this.setCountDownTimer(dateDiff, hero);
        }

        public void showPlaceholder(){
            profileResourceView.setImageResource(getResources().getIdentifier("journey_b3", "mipmap", getPackageName()));
            nameView.setText("");
            hpNowView.setText("");
            timeToLeaveView.setText("");
            staticHpView.setVisibility(View.INVISIBLE);
            staticTimeView.setText("hinzufügen");
        }

        private void setCountDownTimer(final long time, final BrokenHero hero){
            try{
                slotIndexForTimerList.add(slotIndex);
                timerList.add(new CountDownTimer(time, 1000) {
                    @Override
                    public void onTick(long l) {
                        timeToLeaveView.setText("" + l / 1000 / 60);

                        // Die verstrichenen Sekunden werden zwischengespeichert
                        // und bei Verlassen der Activity in DB geschrieben
                        timeUsedArray[slotIndex] += 1000;

                        // Es wird mit Modulo-Rechnung geprüft, ob eine ganze Stunde vergangen ist
                        // Wenn ja, dann wird Held um einen Punkt geheilt
                        if(brokenHeroList.get(slotIndex).getHpNow() % (60*60*1000) == 0){
                            brokenHeroList.get(slotIndex).incrementHitpointsByOne();
                        }
                    }

                    @Override
                    public void onFinish() {
                        if(!hero.setTimeToLeave(0)) Msg.msg(getApplicationContext(), "ERROR @ setTimeToLeave : updateTimeToLeave");
                        timeToLeaveView.setText("");

                        //Msg.msg(getApplicationContext(), "onFinish called");

                        for(int index = 0; index < brokenHeroList.size(); index++){

                            if(brokenHeroList.get(index).getSlotIndex() == slotIndex){
                                slotsList.get(slotIndex).showPlaceholder();
                                brokenHeroList.get(index).setHeroMedSlotIndex(-1);
                                brokenHeroList.remove(index);
                                index = 3;

                                Msg.msg(getApplicationContext(), "Held vollständig genesen");

                            }else if(index == 2) Msg.msg(getApplicationContext(), "ERROR @ onFinish : removeBrokenHeroFromList : no matching index");
                        }
                    }
                }.start());

            }catch (Exception e){Msg.msg(getApplicationContext(), e + "");}
        }

        private void cancelCountDownTimer(final BrokenHero hero, long time){
            DBheroesAdapter h = new DBheroesAdapter(getApplicationContext());

            try {
                for(int i = 0; i < slotIndexForTimerList.size(); i++){
                    if(slotIndexForTimerList.get(i) == slotIndex){
                        timerList.get(i).cancel();
                        //hero.setBufferTime(time);
                        if(!hero.setTimeToLeave(time)) Msg.msg(getApplicationContext(), "ERROR @ setTimeToLeave : updateTimeToLeave");
                        Log.v("cancelCountDownTimer","time to leave from db, read: " + h.getTimeToLeave(hero.dbIndex) );
                        slotIndexForTimerList.remove(i);
                        timerList.remove(i);

                        // Gefahr? Größe des Arrays änderst sich zwischen for-Abfrage und if-Zutreffen
                        i = slotIndexForTimerList.size();
                    }
                }
            }catch (Exception e){Msg.msg(getApplicationContext(), String.valueOf(e));}
        }
    }


    private class BrokenHero{
        private int slotIndex, dbIndex, hpNow, hpTotal;
        private long timeToLeave;
        private String name, profileResource;

        public BrokenHero(int dbIndex, int si){
            this.slotIndex = si;
            this.dbIndex = dbIndex;

            /*
            SharedPreferences prefs = getSharedPreferences("HOSPITAL_SLOT_PREFS", Context.MODE_PRIVATE);
            prefs.edit().putInt("DB_INDEX_BY_SLOT_" + si, dbIndex).apply();
            */

            if(dbIndex > 0){
                name = h.getHeroName(dbIndex);
                hpNow = h.getHeroHitpoints(dbIndex);
                profileResource = h.getHeroImgRes(dbIndex);
                hpTotal = h.getHeroHitpointsTotal(dbIndex);

                // 'tempTime' wird verwendet, um weniger Datenbankzugriffe zu haben (nur einen)
                //long tempTime = heroesHelper.getTimeToLeave(dbIndex);

                long tempTime = h.getTimeToLeave(dbIndex);

                if(tempTime == 0) timeToLeave = getNewDate(hpNow, hpTotal);
                else { timeToLeave = tempTime; }

                this.setHeroMedSlotIndex(si);
            }
        }

        public boolean setHeroHitpoints(int hpNew){
            return h.updateHeroHitpoints(dbIndex, hpNew) != -1;
        }

        public boolean incrementHitpointsByOne(){
            return h.updateHeroHitpoints(dbIndex, ++hpNow) != -1;
        }

        public boolean setHeroMedSlotIndex(int slotIndex){
            this.slotIndex = slotIndex;
            return h.updateMedSlotIndex(dbIndex, slotIndex);
        }

        public boolean setTimeToLeave(long time) {
            Log.v("setTimeToLeave",  "setTimeToLeave called with time: " + time);
            return h.updateTimeToLeave(dbIndex, time);
        }

        public String getName(){ return name; }
        public String getProfileResource(){ return profileResource; }
        public int getHpNow(){ return hpNow; }
        public int getHpTotal(){ return hpTotal; }
        public long getTimeToLeave(){ return timeToLeave; }
        public int getSlotIndex(){ return slotIndex; }
    }


    /**

     Funktionen zur Auslagerung von Initialisierungen

     */


    private void prepareActivity(){
        freeSlotsView = (TextView) findViewById(R.id.textview_hospital_slots);
        fortuneView = (TextView) findViewById(R.id.textview_hospital_fortune);
        abortMedicationView = (TextView) findViewById(R.id.textview_hospital_abort_medication);
        boostMedicationView = (TextView) findViewById(R.id.textview_hospital_boost_healing);

        h = new DBheroesAdapter(getApplicationContext());

        for(int i = 0; i < 3; i++) slotsList.add( new Slot(i) );
        initializeBrokenHeroes();
    }

    private void hideSystemUI() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE);
    }
}