package com.example.thomas.voyage.CombatActivities;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.thomas.voyage.ContainerClasses.Hero;
import com.example.thomas.voyage.ContainerClasses.Monster;
import com.example.thomas.voyage.Databases.DBplayerItemsAdapter;
import com.example.thomas.voyage.R;
import com.example.thomas.voyage.ResClasses.ImgRes;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CombatActivity extends Activity {

    private static int
            selectedCellPosition = 0,
            monsterHealthConst = -1,
            heroHitpointsConst = -1,
            scoreMultiplier = 1,
            dartCount = 1,
            roundCount = 0,
            monsterDmg = 0;
    private static String monsterName = "", heroClassActive ="", heroImgRes = "", chronicleString = "", eventString = "";
    private static String[] iconArray = {"X 1", "1.", "X 2", "2.", "X 3", "SP", "BULL", "IN", "EYE", "OUT"};
    private static int[]
            tempScoreHistory = {0, 0, 0};
    private static TextView monsterHealthView,
            heroHitpointsView,
            heroNameView,
            monsterNameView,
            eventsView,
            chronicleView;
    private static ImageView
            monsterProfileView,
            healthbarMonsterVital,
            healthBarMonsterDamaged,
            healthBarHeroVital,
            healthBarHeroDamaged,
            heroProfileView;
    private static List<Integer> undoListForHero, selectedItems, invetoryItems;
    private static List<String> eventsList;
    private static List<Hero> heroList;
    private static List<Monster> monsterList;
    private static LinearLayout.LayoutParams
            paramsBarMonsterDamaged,
            paramsBarMonsterVital,
            paramsBarHeroDamaged,
            paramsBarHeroVital;
    private String origin = "";
    private GridView gridViewNumbers, gridViewSpecials;
    private static DBplayerItemsAdapter itemHelper;

    private static int monsterResistance = 1;
    private static int monsterBlock = 0;
    private static int tempScore = 0;
    private static int triggerScore = 0;
    private static int triggerMulti = 0;
    private static int bonusScore = 0;
    private static int bonusBlock = 0;
    private static int playerHealth = 0, bonusHealth = 0, monsterHealth = 300;
    private static int monsterMinDmg = 0, monsterMaxDmg = 0;
    private static String monsterCheckout = "";
    private static int throwCount = 0;
    private static Context context;

    private Monster monster;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getApplicationContext();
        setContentView(R.layout.activity_combat);
        hideSystemUI();
        initializeViews();
        initializeValues();

        monster = new Monster();
        monsterHealth = monster.hp;
        monsterCheckout = monster.checkout;
        monsterNameView.setText(monster.name);
        monsterName = monster.name;
        monsterProfileView.setImageResource(getResources().getIdentifier(monster.getImgRes(), "mipmap", this.getPackageName()));
        monsterMinDmg = monster.dmgMin;
        monsterMaxDmg = monster.dmgMax;
    }

    @Override
    protected void onRestart() {
        super.onRestart();  // Always call the superclass method first
        hideSystemUI();
    }

    private static void resetBonus(){
        tempScore = 0;
        playerHealth = 0;
        bonusScore = 0;
        bonusHealth = 0;
        bonusBlock = 0;
        monsterDmg = 0;
    }

    private static void combat(int scoreField, int multiField){

        switch(heroClassActive){
            case "Waldläufer":
                if(scoreField == 1){
                    bonusScore += 10 - scoreField*scoreMultiplier;
                    applyEffects();
                }
                else if(scoreField == 5 && scoreMultiplier == 1){
                    bonusScore += 5;
                    applyEffects();
                }
                break;
            case "Kneipenschläger":
                break;
            case "Monsterjäger":
                break;
            //Secondaries:
            case "Schurke":
                break;
            case "Spion":
                break;
            case "Glaubenskrieger":
                break;
        }

        tempScore += scoreField * multiField * monsterResistance - monsterBlock;

        String logTopEntry;
        logTopEntry = heroList.get(0).getHeroName() + " attackiert mit " + (tempScore - bonusScore)
                + " und " + bonusScore + " Bonusangriff!";

        logTopEntry = heroList.get(0).getHeroName() + " attackiert mit " + tempScore + ". ThrowCount:" + throwCount;

        if( monsterCheckout.equals("double") && checkOutPossible(2) ){
            if( monsterHealth == (scoreField * multiField) && multiField == 2 ){
                logTopEntry = "DOUBLE WIN!";
                monsterHealth = 0;
            }

        }else if( monsterCheckout.equals("master") && scoreField * multiField == monsterHealth ){
                logTopEntry = "MASTER WIN!";
                monsterHealth = 0;

        }
            monsterHealth -= tempScore;
            tempScoreHistory[throwCount] = tempScore;
            //Toast toast = Toast.makeText(context,  "Health: " + monsterHealth + ", tempScore: " + tempScore, Toast.LENGTH_SHORT);
        //toast.show();

        throwCount++;

            if( monsterHealth <= 0 && monsterCheckout.equals("default")) {
                logTopEntry = "NORMAL WIN!";

            }else if(monsterHealth <= 0) {
                for(int i = 0; i <= 2; i++) {
                    monsterHealth += tempScoreHistory[i];
                    tempScoreHistory[i] = 0;
                    throwCount = 0;
                }
                //Überworfen!

            }
        if( throwCount == 3 ){
                for(int i = 0;i <= 2; i++){
                    tempScoreHistory[i] = 0;
                }
                throwCount = 0;
                Random random = new Random();
                monsterDmg = random.nextInt(monsterMaxDmg-monsterMinDmg) + monsterMinDmg;
                playerHealth -= monsterDmg;
                logTopEntry = monsterName + " erwidert mit " + monsterDmg + ".";
                // Bedingungen für Triggern von Spezial nach 3 Würfen, Spieler und Monster
            }


        monsterHealthView.setText(monsterHealth + "");
        //setHealthBarMonster();
        //setHealthBarHero();

        String tempEventsString = logTopEntry + '\n' + '\n';

        for(int i = eventsList.size() - 1; i >= 0; i--){
            tempEventsString += eventsList.get(i) + '\n';
        }

        eventsView.setText(tempEventsString);
        eventsList.add(logTopEntry);
        resetBonus();
    }

    private static void applyEffects(){
        tempScore += bonusScore;
        playerHealth += bonusHealth;
        monsterDmg -= bonusBlock;
    }

    private static Boolean checkOutPossible(int checkOutType){
        switch(checkOutType){
            case 1:
                for(int i = 1; i < 21; i++) {
                    for (int p = 1; p < 4; p++) {
                        if ((i*p - monsterHealth) == 0 || 25 - monsterHealth == 0 || 50 - monsterHealth == 0) {
                            return true;
                        }
                    }
                }
                break;
            case 2:
                if( (monsterHealth == 50) || ((monsterHealth <= 40) && (monsterHealth % 2 == 0)) ){
                    return true;
                }
                break;
        }

        return false;
    }
/*
    private static void setHealthBarMonster() {
        monsterHealthConst = monsterList.get(0).hp;

        try {
            if (monsterHealth > 0) {

                float x = monsterHealthConst * (float) 0.01;

                float weightRounded = monsterHealth / x * (float) 0.01;

                monsterHealthView.setText(monsterHealth + "");
                paramsBarMonsterDamaged.weight = 1 - weightRounded;
                paramsBarMonsterVital.weight = weightRounded;
                healthBarMonsterDamaged.setLayoutParams(paramsBarMonsterDamaged);
                healthbarMonsterVital.setLayoutParams(paramsBarMonsterVital);

            } else {
                paramsBarMonsterDamaged.weight = 1.0f;
                paramsBarMonsterVital.weight = 0f;
                healthBarMonsterDamaged.setLayoutParams(paramsBarMonsterDamaged);
                healthbarMonsterVital.setLayoutParams((paramsBarMonsterVital));
                monsterHealthView.setText("DU GEWINNER!");
                monsterNameView.setText(monsterName + " K.O.");
                monsterList.get(0).setInt("hp", monsterHealthConst);
            }
        } catch (ArithmeticException a) {
            Log.e("ARITHMETIC EXCEPTION", a + "");
        }
    }

    private static void setHealthBarHero() {
        int heroHitpoints = heroList.get(0).getHp();

        try {
            if (heroHitpoints > 0) {

                float x = heroHitpointsConst * (float) 0.01;

                float weightRounded = heroHitpoints / x * (float) 0.01;

                heroHitpointsView.setText(heroHitpoints + "");
                paramsBarHeroDamaged.weight = 1 - weightRounded;
                paramsBarHeroVital.weight = weightRounded;
                healthBarHeroDamaged.setLayoutParams(paramsBarHeroDamaged);
                healthBarHeroVital.setLayoutParams((paramsBarHeroVital));

            } else {
                paramsBarHeroDamaged.weight = 1.0f;
                paramsBarHeroVital.weight = 0f;
                healthBarHeroDamaged.setLayoutParams(paramsBarHeroDamaged);
                healthBarHeroVital.setLayoutParams((paramsBarHeroVital));
                heroHitpointsView.setText("MONSTER GEWINNER!");
                heroList.get(0).setHp(heroHitpointsConst);
            }
        } catch (ArithmeticException a) {
            Log.e("ARITHMETIC EXCEPTION", a + "");
        }
    }
*/
    private static class ScoreDialAdapter extends BaseAdapter {

        int[] result;
        Context context;
        private LayoutInflater inflater = null;

        public ScoreDialAdapter(CombatActivity combatActivity, int[] list) {
            result = list;
            context = combatActivity;
            inflater = (LayoutInflater) context.
                    getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        }

        @Override
        public int getCount() {
            return result.length;
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, final View convertView, final ViewGroup parent) {

            final Holder holder = new Holder();
            View rowView;

            rowView = inflater.inflate(R.layout.gridview_combat_left_panel, null);
            holder.tv = (TextView) rowView.findViewById(R.id.gridView_combat_textView);
            holder.tv.setText(result[position] + "");

            rowView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {

                    combat(result[position], scoreMultiplier);
                }
            });

            return rowView;
        }

        public class Holder {
            TextView tv;
        }
    }

    private static class ScoreMultiAdapter extends BaseAdapter {

        int[] result;
        Context context;
        private LayoutInflater inflater = null;

        public ScoreMultiAdapter(CombatActivity combatActivity, int[] list) {
            result = list;
            context = combatActivity;
            inflater = (LayoutInflater) context.
                    getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        }

        @Override
        public int getCount() {
            return result.length;
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, final View convertView, final ViewGroup parent) {

            final Holder holder = new Holder();
            View rowView = convertView;

            if(rowView == null){

                if (position < 6) {
                    rowView = inflater.inflate(R.layout.gridview_combat_right_panel, null);
                    holder.tv = (TextView) rowView.findViewById(R.id.gridView_combat_right_panel_textView);
                } else {
                    rowView = inflater.inflate(R.layout.gridview_combat_left_panel, null);
                    holder.tv = (TextView) rowView.findViewById(R.id.gridView_combat_textView);
                }

                holder.tv.setText(iconArray[position]);
            }

            rowView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    //rowView.setBackgroundColor(context.getResources().getColor(R.color.highlight_cherryred));


                    if (selectedCellPosition != -1) {
                        View cellView = parent.getChildAt(selectedCellPosition);
                        cellView.setSelected(false);
                        cellView.postInvalidate();
                    }


                    v.setSelected(true);
                    selectedCellPosition = position;

                    switch (position) {
                        case 0:
                            scoreMultiplier = 1;
                            break;
                        case 1:
                            // Klasse mit 1 = Primärangriff
                            heroClassActive = heroList.get(0).getClassPrimary();
                            break;
                        case 2:
                            scoreMultiplier = 2;
                            break;
                        case 3:
                            // Klasse mit 2 = Sekundärangriff
                            heroClassActive = heroList.get(0).getClassSecondary();
                            break;
                        case 4:
                            scoreMultiplier = 3;
                            break;
                        case 5:
                            // Klasse mit 3 = Spezialangriff
                            heroClassActive = heroList.get(0).getClassPrimary();
                            break;
                        case 6:
                            scoreMultiplier = 1;
                            combat(25, 1);
                            break;
                        case 7:
                            break;
                        case 8:
                            scoreMultiplier = 2;
                            combat(25,2);
                            break;
                        case 9:
                            break;
                        default:
                            holder.tv.setText(":(");
                    }
                }
            });

            return rowView;
        }

        public class Holder {
            TextView tv;
        }
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

    private void initializeValues(){
        heroList = new ArrayList<>();
        monsterList = new ArrayList<>();
        selectedItems = new ArrayList<>();
        eventsList = new ArrayList<>();

        itemHelper = new DBplayerItemsAdapter(this);

        Bundle b = getIntent().getExtras();
        if (b != null) {
            String heroName = b.getString("HEROES_NAME", "???");
            String heroPrimaryClass = b.getString("HEROES_PRIMARY_CLASS", "???");
            String heroSecondaryClass = b.getString("HEROES_SECONDARY_CLASS", "???");
            int heroHitpoints = b.getInt("HEROES_HITPOINTS", -3);
            int heroCosts = b.getInt("HEROES_COSTS", -1);
            String heroImgRes = b.getString("IMAGE_RESOURCE", "R.mipmap.hero_dummy_0");
            origin = b.getString("ORIGIN", "StartActivity");
            int heroEvasion = b.getInt("EVASION", -1);
            int hpTotal = b.getInt("HP_TOTAL", -1);

            heroHitpointsConst = heroHitpoints;


            // .add für Anzahl der Helden sowie Anzahl der Monster
            heroList.add(new Hero(heroName, heroPrimaryClass, heroSecondaryClass, heroImgRes, heroHitpoints, heroHitpoints, heroCosts, heroEvasion));
        }

        monsterList.add(new Monster());
        monsterHealthConst = monsterList.get(0).hp;

        heroNameView.setText(heroList.get(0).getHeroName());
        heroHitpointsView.setText(Integer.toString(heroList.get(0).getHp()));
        monsterHealthView.setText(Integer.toString(monsterList.get(0).hp));

        int[] numbersOfBoardList = new int[20];
        for (int i = 0; i < 20; i++) {
            numbersOfBoardList[i] = i + 1;
        }

        int[] specialSymbolsList = new int[10];
        for (int i = 0; i < 10; i++) {
            specialSymbolsList[i] = i + 1;
        }

        undoListForHero = new ArrayList<>();
        paramsBarMonsterDamaged.weight = 0.0f;
        healthBarMonsterDamaged.setLayoutParams(paramsBarMonsterDamaged);
        paramsBarHeroDamaged.weight = 0.0f;
        healthBarHeroDamaged.setLayoutParams(paramsBarHeroDamaged);
        gridViewNumbers.setAdapter(new ScoreDialAdapter(this, numbersOfBoardList));
        gridViewSpecials.setAdapter(new ScoreMultiAdapter(this, specialSymbolsList));
        heroProfileView.setImageResource(ImgRes.res(this, "hero", heroList.get(0).getImageResource()));
    }

    private void initializeViews() {
        gridViewNumbers = (GridView) findViewById(R.id.activity_combat_gridView_numbers);
        gridViewSpecials = (GridView) findViewById(R.id.activity_combat_gridView_specials);
        healthBarHeroVital = (ImageView) findViewById(R.id.healthbar_hero_vital);
        healthBarHeroDamaged = (ImageView) findViewById(R.id.healthbar_hero_damaged);
        paramsBarHeroDamaged = (LinearLayout.LayoutParams) healthBarHeroDamaged.getLayoutParams();
        paramsBarHeroVital = (LinearLayout.LayoutParams) healthBarHeroVital.getLayoutParams();
        heroNameView = (TextView) findViewById(R.id.heroNameView);
        monsterNameView = (TextView) findViewById(R.id.monsterNameView);
        eventsView = (TextView) findViewById(R.id.combat_events_log);
        monsterHealthView = (TextView) findViewById(R.id.monsterHealthView);
        healthbarMonsterVital = (ImageView) findViewById(R.id.healthbar_monster_vital);
        healthBarMonsterDamaged = (ImageView) findViewById(R.id.healthbar_monster_damaged);
        heroProfileView = (ImageView) findViewById(R.id.combat_hero_profile);
        heroHitpointsView = (TextView) findViewById(R.id.heroHealthView);
        chronicleView = (TextView) findViewById(R.id.textView_hitpoints_chronik);
        paramsBarMonsterDamaged = (LinearLayout.LayoutParams) healthBarMonsterDamaged.getLayoutParams();
        paramsBarMonsterVital = (LinearLayout.LayoutParams) healthbarMonsterVital.getLayoutParams();
        monsterProfileView = (ImageView) findViewById(R.id.imageView_combat_monster);
    }
}