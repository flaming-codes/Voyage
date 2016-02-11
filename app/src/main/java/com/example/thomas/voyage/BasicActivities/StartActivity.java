package com.example.thomas.voyage.BasicActivities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.thomas.voyage.CombatActivities.CombatMonsterHeroActivity;
import com.example.thomas.voyage.CombatActivities.PrepareCombatActivity;
import com.example.thomas.voyage.CombatActivities.QuickCombatActivity;
import com.example.thomas.voyage.CombatActivities.WorldMapQuickCombatActivity;
import com.example.thomas.voyage.ContainerClasses.Hero;
import com.example.thomas.voyage.ContainerClasses.Item;
import com.example.thomas.voyage.ContainerClasses.Msg;
import com.example.thomas.voyage.Databases.DBheroesAdapter;
import com.example.thomas.voyage.Databases.DBmerchantHeroesAdapter;
import com.example.thomas.voyage.Databases.DBmerchantItemsAdapter;
import com.example.thomas.voyage.Databases.DBplayerItemsAdapter;
import com.example.thomas.voyage.Databases.DBscorefieldAndMultiAmountAdapter;
import com.example.thomas.voyage.R;
import com.example.thomas.voyage.ResClasses.ConstRes;
import com.opencsv.CSVReader;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class StartActivity extends Activity {

    private DBheroesAdapter heroesHelper;
    private DBmerchantHeroesAdapter merchantHelper;
    private DBplayerItemsAdapter itemPlayerHelper;
    private DBmerchantItemsAdapter itemMerchantHelper;
    private TextView textViewSlaveMarket, textViewHeroesParty, textViewItemMarket, textViewHospital;
    private ConstRes c;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        hideSystemUI();

        heroesHelper = new DBheroesAdapter(this);
        merchantHelper = new DBmerchantHeroesAdapter(this);
        itemPlayerHelper = new DBplayerItemsAdapter(this);
        itemMerchantHelper = new DBmerchantItemsAdapter(this);
        c = new ConstRes();

        isAppFirstStarted();
        isQuickCombatfirstStarted();

        textViewSlaveMarket = (TextView) findViewById(R.id.start_textView_slave_market);
        textViewHeroesParty = (TextView) findViewById(R.id.start_textView_manage_heroes);
        textViewItemMarket = (TextView) findViewById(R.id.start_textView_inventory_merchant);
        textViewHospital = (TextView) findViewById(R.id.start_textView_hospital);

        setSlaveMarketWindow();
        setHeroesPartyWindow();
        setItemMarketWindow();
        setHospitalWindows();


        /*

        CSV-Testgebiet

         */


        try{
            CSVReader reader = new CSVReader(new InputStreamReader(getResources().openRawResource(R.raw.test)));
            String [] nextLine;
            while ((nextLine = reader.readNext()) != null) {
                // nextLine[] is an array of values from the line
                Msg.msgShort(this, nextLine[0] + nextLine[1] + "etc...");
            }

        }catch(java.io.FileNotFoundException f){
            Msg.msg(this, "Error : " + String.valueOf(f));
        }catch(java.io.IOException i){
            Msg.msg(this, "Error : " + String.valueOf(i));
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();  // Always call the superclass method first
        setSlaveMarketWindow();
        setHeroesPartyWindow();
        setItemMarketWindow();
        setHospitalWindows();
        hideSystemUI();
    }

    public void isAppFirstStarted() {
        // vor Datenbank-Upgrade durchgeführt -> zuerst letztes 'false' durch 'true' ersetzen
        //  -> App starten -> 'true' wieder auf 'false' & Versionsnummer erhöhen -> starten

        SharedPreferences prefs = getPreferences(MODE_PRIVATE);
        Boolean isFirstRun = prefs.getBoolean(c.IS_FIRST_RUN, true);

        if (isFirstRun) {
            Msg.msg(this, "IS_FIRST_RUN: " + isFirstRun);

            SharedPreferences money_pref = getSharedPreferences("CURRENT_MONEY_PREF", Context.MODE_PRIVATE);
            money_pref.edit().putLong("currentMoneyLong", 5000).apply();

            long validation = prepareHeroesDatabaseForGame(c.TOTAL_HEROES_PLAYER);
            if (validation < 0) {
                Msg.msg(this, "ERROR @ insertToHeroesDatabase");
            }

            // Datenbankplätze für HeroMerchant reservieren
            validation = insertToMerchantDatabase(c.TOTAL_HEROES_MERCHANT);
            if (validation < 0) {
                Msg.msg(this, "ERROR @ insertToMerchantHeroesDatabase");
            }

            preparePlayersItemDatabase(c.TOTAL_ITEMS_PLAYER_LV1);
            insertToItemMerchantDatabase(c.TOTAL_ITEMS_MERCHANT_LV1);

            SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();
            editor.putBoolean(c.IS_FIRST_RUN, false);
            editor.apply();
        }
    }

    private void isQuickCombatfirstStarted(){
        c = new ConstRes();
        SharedPreferences prefs = getPreferences(MODE_PRIVATE);

        if(prefs.getBoolean(c.QUICK_COMBAT_FIRST_STARTED, true)){
            Msg.msg(this, "SCOREFIELDS AND MULTIS DB CREATED");
            DBscorefieldAndMultiAmountAdapter scoreHelper = new DBscorefieldAndMultiAmountAdapter(this);

            long validation = 0;
            for(int i = 0; i <= 20; i++){
                validation = scoreHelper.insertData(i, 0, 0, 0);
            }

            scoreHelper.insertData(25, 0, 0, 0);
            scoreHelper.insertData(50, 0, 0, 0);

            if(validation < 0) Msg.msg(this, "ERROR @ insert for 20 values in scoreDatabase");

            SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();
            editor.putBoolean(c.QUICK_COMBAT_FIRST_STARTED, false);
            editor.apply();
        }
    }

    private void setSlaveMarketWindow(){
        int countNewHeroes = 0;

        for(int i = 1; i <= merchantHelper.getTaskCount(); i++){
            if( !(merchantHelper.getHeroName(i)
                    .equals(getResources().getString(R.string.indicator_unused_row))) ){

                countNewHeroes++;
            }
        }

        if(countNewHeroes == 1) textViewSlaveMarket.setText("1 neuer Held");
        else if(countNewHeroes == 0) textViewSlaveMarket.setText("Nichts anzubieten...");
        else textViewSlaveMarket.setText(countNewHeroes + " neue Helden");
    }

    private void setHeroesPartyWindow(){
        long size = heroesHelper.getTaskCount();
        long count = 0;

        for(int i = 1; i <= size; i++){
            if(!heroesHelper.getHeroName(i).equals(c.NOT_USED)){
                count++;
            }
        }

        if(count == size){
            textViewHeroesParty.setText( "Volles Haus!");
        }else{
            textViewHeroesParty.setText( count + " / " + size + " belegt");
        }

    }

    private void setItemMarketWindow(){
        int countUsed = 0;

        for(int i = 1; i <= itemMerchantHelper.getTaskCount(); i++){
            if(! itemMerchantHelper.getItemName(i).equals(c.NOT_USED) ){
                countUsed++;
            }
        }

        if(countUsed > 0) textViewItemMarket.setText(countUsed + " Waren zu kaufen");
        else if(countUsed == 0) textViewItemMarket.setText("Nichts mehr zu kaufen...");
    }

    private void setHospitalWindows(){
        DBheroesAdapter h = new DBheroesAdapter(this);
        int sum = 0;

        for(int i = 1; i <= 10; i++){
            if(h.getTimeToLeave(i) > 0) sum++;
        }

        if(sum == 0) textViewHospital.setText("Niemand in Behandlung...");
        else if(sum == 1) textViewHospital.setText("Ein Held wird versorgt");
        else textViewHospital.setText(sum + " Helden in Behandlung");
    }

    private long prepareHeroesDatabaseForGame(int rows) {

        long validation = 0;

        for (int i = rows; i > 0; i--) {
            validation = heroesHelper.insertData("NOT_USED", 0, "", "", 0, "");

            if(validation < 0){
                Toast.makeText(this, "ERROR @ prepareHeroesDatabaseForGame with index " + i, Toast.LENGTH_SHORT).show();
            }

            if (i == 1 && validation > 0)
                Msg.msg(this, "9 blank rows in heroes database inserted, 10th under way");
        }

        return validation;
    }

    private void preparePlayersItemDatabase(int rows){

        for (int i = rows; i > 0; i--) {
            long validation = itemPlayerHelper.insertData("NOT_USED", 0, "", "", 0, 0, "");

            if(validation < 0){
                Toast.makeText(this, "ERROR @ preparePlayersItemDatabaseForGame with index " + i, Toast.LENGTH_SHORT).show();
            }else if (i == 1 && validation > 0)
                Msg.msg(this, "PlayersItemDatabase prepared for game!");
        }
    }

    private void insertToItemMerchantDatabase(int rows){
        Msg.msg(this, "'insertToItemMerchant' called");

        for(int i = 1; i <= rows; i++){

            Item item = new Item(this);
            long id = itemMerchantHelper.insertData(
                    item.getStrings("ITEM_NAME"),
                    item.getInts("SKILL_ID"),
                    item.getStrings("DES_MAIN"),
                    item.getStrings("DES_ADD"),
                    item.getInts("BUY_COSTS"),
                    item.getInts("SPELL_COSTS"),
                    item.getStrings("RARITY")
            );

            if(id < 0) Msg.msg(this, "ERROR@insertToItemMerchantDatabase");
        }
    }

    private long insertToMerchantDatabase(int numberOfInserts) {
        Msg.msg(this, "'insertToMerchantDatabase'");
        List<Hero> heroList = new ArrayList<>();
        long id = 0;

        for (int i = 0; i < numberOfInserts; i++) {
            heroList.add(new Hero(this));
            heroList.get(i).Initialize("Everywhere");

            // noch Vorgänger-unabhängig -> neue Zeilen werden einfach an Ende angehängt
            id = merchantHelper.insertData(
                    heroList.get(i).getHeroName(),
                    heroList.get(i).getHp(),
                    heroList.get(i).getClassPrimary(),
                    heroList.get(i).getClassSecondary(),
                    heroList.get(i).getCosts(),
                    heroList.get(i).getImageResource(),
                    heroList.get(i).getEvasion(),
                    heroList.get(i).getHpTotal());

            if (id < 0) Msg.msg(this, "ERROR @ insert of hero " + i + 1);

        }

        return id;
    }

    public void clickToHeroMerchant(View view) {
        Intent i = new Intent(getApplicationContext(), MerchantHeroActivity.class);
        startActivity(i);
    }

    public void clickToPrepareCombat(View view) {
        Intent i = new Intent(getApplicationContext(), PrepareCombatActivity.class);
        startActivity(i);
    }

    public void clickToQuickCombat(View view){
        Intent i = new Intent(getApplicationContext(), QuickCombatActivity.class);
        startActivity(i);
    }
    public void toScreenSlideActivity(View view) {
        Intent i = new Intent(getApplicationContext(), WorldMapQuickCombatActivity.class);
        startActivity(i);
    }

    public void clickToStats(View view) {
        Intent i = new Intent(getApplicationContext(), CombatMonsterHeroActivity.class);
        startActivity(i);
    }

    public void clickToMerchantInventory(View view){
        Intent i = new Intent(getApplicationContext(), MerchantInventoryActivity.class);
        startActivity(i);
    }

    public void clickToCamp(View view){
        Intent i = new Intent(getApplicationContext(), HeroCampActivity.class);
        i.putExtra("ORIGIN", "StartActivity");
        startActivity(i);
    }

    public void onClickHospital(View view){
        Intent i = new Intent(getApplicationContext(), HospitalActivity.class);
        startActivity(i);
    }

    private void hideSystemUI() {
        // Set the IMMERSIVE flag.
        // Set the content to appear under the system bars so that the content
        // doesn't resize when the system bars hide and show.
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE);
    }
}



/* CODE SNIPPETS 2 LEARN

        //View viewLandscape = findViewById(R.id.activity_start);
        //viewLandscape.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

        // zuerst der Activity eine id geben -> danach direkt ansprechbar
        // in Manifest zur activity ".StartActivity" "android:screenOrientation="landscape"" einfügen -> immer Landschaftsmodus


        heroesHelper = new DBheroesAdapter(this);

        long id = heroesHelper.insertData("Thomas", 100, "eins", "zwei");

        if (id < 0) Msg.msg(this, "error@insert");
        else Msg.msg(this, "success@insert");

        String data = heroesHelper.getAllData();
        Msg.msg(this, data);
        */



