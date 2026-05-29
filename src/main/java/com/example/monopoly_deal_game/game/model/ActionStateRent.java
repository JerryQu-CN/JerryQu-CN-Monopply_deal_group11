package com.example.monopoly_deal_game.game.model;

import com.example.monopoly_deal_game.model.Player;

import java.io.Serial;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Rent collection state: waits for targets to pay rent.
 * Targets can use JSN to refuse before accepting.
 */
public class ActionStateRent extends ActionState {
    @Serial
    private static final long serialVersionUID = 1L;

    private final Map<Player, Integer> charges = new LinkedHashMap<>();
    private final String renterName;
    private final Player renter;

    public ActionStateRent(Player renter, Player rented, int amount) {
        super(renter != null ? renter : rented, rented, renter != null ? renter.getName() + " charged rent" : "Bank charged rent");
        this.renter = renter;
        this.renterName = renter != null ? renter.getName() : "Bank";
        charges.put(rented, amount);
        updateStatus();
    }

    public ActionStateRent(Player renter, List<Player> rented, int amount) {
        super(renter, new ArrayList<>(rented), renter != null ? renter.getName() + " charged rent" : "Bank charged rent");
        this.renter = renter;
        this.renterName = renter != null ? renter.getName() : "Bank";
        for (Player p : rented) {
            charges.put(p, amount);
        }
        updateStatus();
    }

    public Player getRenter() {
        return renter;
    }

    public int getCharge(Player player) {
        return charges.getOrDefault(player, 0);
    }

    public Map<Player, Integer> getCharges() {
        return charges;
    }

    private SerializablePlayerAction onAccepted;

    public void setOnAccepted(SerializablePlayerAction action) {
        this.onAccepted = action;
    }

    public boolean hasOnAccepted() {
        return onAccepted != null;
    }

    /** Execute the post-acceptance business logic (payment, etc.). With multiple targets, called once per target. */
    public void executeOnAccepted(Player player) {
        if (onAccepted != null) {
            onAccepted.execute(player);
        }
    }

    /** @deprecated Use {@link #setAccepted(Player, boolean)} + {@link #executeOnAccepted(Player)} */
    @Deprecated
    public void playerPaid(Player player) {
        setAccepted(player, true);
    }

    @Override
    public void setTargetState(Player player, TargetState state) {
        super.setTargetState(player, state);
        if (state == TargetState.NOT_TARGETED) {
            charges.remove(player);
        }
    }

    private void updateStatus() {
        StringBuilder sb = new StringBuilder(renterName + " charges ");
        List<Player> targets = getTargetPlayers();
        for (int i = 0; i < targets.size(); i++) {
            if (i > 0 && i == targets.size() - 1) {
                sb.append(" and ");
            } else if (i > 0) {
                sb.append(", ");
            }
            sb.append(targets.get(i).getName());
            sb.append(" ").append(charges.get(targets.get(i))).append("M");
        }
        setStatus(sb.toString());
    }
}