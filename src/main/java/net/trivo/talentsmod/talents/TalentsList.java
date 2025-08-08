package net.trivo.talentsmod.talents;

public class TalentsList {
    public enum Talents {
        FIGHTING, MINING, FORAGING, ARCHERY, FARMING, SNEAKING,
        SWIMMING, RUNNING, JUMPING, ENDURANCE, RIDING, HEALTHY;


        public String getKey() {
            return "talent_" + this.name().toLowerCase();
        }
    }
    public static String getKeyFromKeyword(String string) {
        return "talent_" + string.toLowerCase();
    }
}
