package net.minespree.skywars.game.loot;

import lombok.Getter;

@Getter
public enum ItemType {
    HELMET,
    CHESTPLATE,
    LEGGINGS,
    BOOTS,
    BLOCK,
    FOOD,
    WEAPON,
    AMMO(false),
    MISCELLANEOUS(false);

    boolean guaranteed;

    ItemType(boolean guaranteed) {
        this.guaranteed = guaranteed;
    }

    ItemType() {
        this.guaranteed = true;
    }
}
