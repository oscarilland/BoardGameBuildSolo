package netzerobelfast.model;

import netzerobelfast.config.CardDef.Source;

import java.util.List;
import java.util.Map;

/**
 * What a disaster did, for the end-of-round pop-up (FR-12, FR-17).
 *
 * @param disaster    which disaster
 * @param band        the danger band that set its cost
 * @param district    the flooded district's code, or null
 * @param square      the storm-closed square's id, or -1
 * @param payments    what each player actually paid, after Resilience and any amount written off
 * @param bankrupted  players this disaster made bankrupt
 * @param explanation the one-line climate explanation from the data
 * @param sources     where the explanation comes from
 */
public record DisasterResult(
        Disaster disaster,
        int band,
        String district,
        int square,
        Map<Player, Integer> payments,
        List<Player> bankrupted,
        String explanation,
        List<Source> sources) {

    /** True if a Flood or Storm found nothing to hit, so nothing happened (Q-17). */
    public boolean skipped() {
        return (disaster == Disaster.FLOOD && district == null) || (disaster == Disaster.STORM && square == -1);
    }
}
