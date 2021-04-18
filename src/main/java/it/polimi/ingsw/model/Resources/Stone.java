package it.polimi.ingsw.model.Resources;

import it.polimi.ingsw.model.Marble.GreyMarble;
import it.polimi.ingsw.model.Marble.Marble;

/**
 * this class represent the stone resource
 */

public class Stone implements Resource {

    /**
     * this method returns a stone type
     * @return a stone type
     */
    @Override
    public ResourceType getType() {
        return ResourceType.STONE;
    }
    /**
     * this method convert the resource to his marble associated
     * @return a grey marble
     */
    @Override
    public Marble convertInMarble(){return new GreyMarble();}

    /**
     * this method verify if the argument toCompare has the same type of the resource that calls the method
     * @param toCompare this is the resource to verify
     * @return true if toCompare is a stone, false if it isn't
     */
    @Override
    public boolean equals(Object toCompare) {
        return toCompare instanceof Stone;
    }
}