package net.alcaris.plugin.items.enums;

public enum InsuranceType {
    SAFE("安心保険(100%)", 50, 0.0),
    QUALITY("良質保険(95%)", 20, 0.05),
    STANDARD("通常保険(80%)", 5, 0.20),
    PRAY("お祈り保険(50%)", 1, 0.50);

    private final String displayName;
    private final int baseDiamondCost;
    private final double lossProbability;

    InsuranceType(String displayName, int baseDiamondCost, double lossProbability) {
        this.displayName = displayName;
        this.baseDiamondCost = baseDiamondCost;
        this.lossProbability = lossProbability;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getBaseDiamondCost() {
        return baseDiamondCost;
    }

    public double getLossProbability() {
        return lossProbability;
    }

    public static InsuranceType fromDisplayName(String displayName) {
        for (InsuranceType type : values()) {
            if (type.displayName.equals(displayName)) {
                return type;
            }
        }
        return null;
    }
} 