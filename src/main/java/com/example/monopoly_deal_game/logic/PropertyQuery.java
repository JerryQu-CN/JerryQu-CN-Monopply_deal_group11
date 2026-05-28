package com.example.monopoly_deal_game.logic;

import com.example.monopoly_deal_game.model.Player;
import com.example.monopoly_deal_game.model.Property;
import com.example.monopoly_deal_game.model.cards.ActionCardHouse;
import com.example.monopoly_deal_game.model.cards.ActionCardHotel;
import com.example.monopoly_deal_game.model.cards.PropertyCard;

import java.util.ArrayList;
import java.util.List;

public final class PropertyQuery {
    private PropertyQuery() {}

    public static List<PropertyCard> allTableProperties(Player player) {
        List<PropertyCard> out = new ArrayList<>();
        if (player == null) return out;
        for (Property row : player.getProperties()) {
            out.addAll(row.getCards());
        }
        return out;
    }

    public static List<PropertyCard> stealableSingleProperties(Player player) {
        List<PropertyCard> out = new ArrayList<>();
        if (player == null) return out;
        for (Property row : player.getProperties()) {
            if (row.isMonopoly()) continue;
            for (PropertyCard pc : row.getCards()) {
                if (pc.isStealable()) out.add(pc);
            }
        }
        return out;
    }

    public static List<Property> monopolyGroups(Player player) {
        List<Property> out = new ArrayList<>();
        if (player == null) return out;
        for (Property row : player.getProperties()) {
            if (row.isMonopoly()) out.add(row);
        }
        return out;
    }

    public static PropertyCard firstStealableProperty(Player p) {
        for (Property row : p.getProperties()) {
            if (row.isMonopoly()) continue;
            if (!row.getCards().isEmpty()) {
                PropertyCard pc = row.getCards().get(0);
                if (pc.isStealable()) return pc;
            }
        }
        return null;
    }

    public static PropertyCard firstTableProperty(Player p) {
        for (Property row : p.getProperties()) {
            if (!row.getCards().isEmpty()) return row.getCards().get(0);
        }
        return null;
    }

    public static Property firstMonopolyOfPlayer(Player p) {
        if (p == null) return null;
        for (Property row : p.getProperties()) {
            if (row.isMonopoly()) return row;
        }
        return null;
    }

    public static Property findHouseTarget(Player actor, boolean forHouse) {
        for (Property ps : actor.getProperties()) {
            if (!ps.isMonopoly()) continue;
            if (!ps.getEffectiveColor().isBuildable()) continue;
            boolean hasH = ps.getBuildingCards().stream()
                    .anyMatch(c -> c instanceof ActionCardHouse);
            boolean hasT = ps.getBuildingCards().stream()
                    .anyMatch(c -> c instanceof ActionCardHotel);
            if (forHouse && !hasH) return ps;
            if (!forHouse && hasH && !hasT) return ps;
        }
        return null;
    }
}