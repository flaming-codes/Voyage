package com.example.thomas.voyage.ContainerClasses;

import android.util.Log;

public class Monster {

    public String name = "", checkout = "", imgRes = "";
    public double evasion = -1, accuracy = -1, critChance = -1;
    public int hp, hpTotal, dmgMin = -1, dmgMax = -1, resistance = 1, block = 0;

    public Monster(){
        MonsterPool monsterPool = new MonsterPool();

        name = monsterPool.getName();
        checkout = monsterPool.getCheckout();
        evasion = monsterPool.getEvasion();
        accuracy = monsterPool.getAccuracy();
        critChance = monsterPool.getCritChance();
        hp = monsterPool.getHp();
        hpTotal = hp;
        dmgMin = monsterPool.getDmgMin();
        dmgMax = monsterPool.getDmgMax();
        imgRes = monsterPool.getImgRes();
    }

    public Monster(String tName, String check, int eva, int acc, int crit, int tHp, int tDmgMin, int tDmgMax){
        name = tName;
        checkout = check;
        evasion = eva;
        accuracy = acc;
        critChance = crit;
        hp = tHp;
        hpTotal = tHp;
        dmgMin = tDmgMin;
        dmgMax = tDmgMax;
    }

    public String getImgRes(){
        return imgRes;
    }

    public void setInt(String id, int val){

        switch (id){

            case "evasion":
                evasion = val; break;
            case "accuracy":
                accuracy = val; break;
            case "critChance":
                critChance = val; break;
            case "hp":
                hp = val; break;
            case "hpTotal":
                hp = val; break;
            case "dmgMin":
                dmgMin = val; break;
            case "dmgMax":
                dmgMax = val; break;
            default:
                Log.e("MONSTER_GET_INT", "Error@ getInt, default called");
        }
    }
}
