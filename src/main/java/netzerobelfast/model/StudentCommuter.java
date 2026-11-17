package netzerobelfast.model;

import netzerobelfast.config.Rules;
import netzerobelfast.config.Rules.CharacterDef;

/** The Student Commuter. Ability: Glider rides are free and earn GP. */
public final class StudentCommuter extends Character {

    private final int gliderFare;
    private final int gliderRideGP;

    StudentCommuter(CharacterDef definition, Rules rules) {
        super(definition, rules);
        this.gliderFare = number("gliderFare");
        this.gliderRideGP = number("gliderRideGP");
    }

    @Override
    public int gliderFare() {
        return gliderFare;
    }

    @Override
    public int gliderRideGP() {
        return gliderRideGP;
    }
}