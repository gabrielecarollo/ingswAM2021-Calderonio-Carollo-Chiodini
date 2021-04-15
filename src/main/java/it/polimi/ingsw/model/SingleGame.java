package it.polimi.ingsw.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SingleGame extends Game {

    private ArrayList<SoloToken> soloTokens;
    private final Player lorenzoIlMagnifico;


    /**
     * this is the constructor of the class, it initialize the three boolean of vatican report
     * at false; set the nickname of players; initialize randomly MarbleMarket and CardsMarket, create all leaderCards and assign four
     * of them at the player, then initialize lorenzoIlMagnifico and all the soloTokens (7) in random order
     *
     * @param nicknames
     */
    public SingleGame(List<String> nicknames) {
        super(nicknames);
        lorenzoIlMagnifico = new Player();
        soloTokens.add(new TrackToken(2, false));
        soloTokens.add(new TrackToken(2, false));
        soloTokens.add(new TrackToken(1, true));
        soloTokens.add(new CardToken(CardColor.BLUE));
        soloTokens.add(new CardToken(CardColor.YELLOW));
        soloTokens.add(new CardToken(CardColor.GREEN));
        soloTokens.add(new CardToken(CardColor.PURPLE));
        Collections.shuffle(soloTokens);
    }

    /**
     * this method call the method action of the last token in the deck, if this return true it shuffles the deck else
     * it inserts the token caught in the first position of the deck
     * @return always false
     */
    @Override
    public boolean endTurn() {
        SoloToken temp;
        if (soloTokens.get(7).action(this, super.getSetOfCard()))
            Collections.shuffle(soloTokens);
        else {
            temp = soloTokens.remove(7);
            soloTokens.add(0, temp);
        }
        return false;
    }

    /**
     *this method verifies if the there are the conditions to end the game: it checks the conditions of the superclass
     * and furthermore and if there is a empty column in CardsMarket
     * @return
     */
    @Override
    protected boolean checkEndGame() {
        return (super.checkEndGame() || super.getSetOfCard().checkMissColumn());
    }

    /**
     * this method is used to add faithPoints to LorenzoIlMagnifico
     * @param actualPlayer is the single player
     * @param toAdd is the number of faithTrack position to add to lorenzoIlMagnifico
     */
    public void addFaithPointsExceptTo (RealPlayer actualPlayer, int toAdd) {
        lorenzoIlMagnifico.addFaithPoints(toAdd);
        checkEndGame();
    }

    /**
     * like in the superclass but at the end it calls checkEndGame to end the game immediately if it needs to be ended
     * @param level this is the level of the card to buy
     * @param color this is the color of the card to buy
     * @param position this is the position in which place the card in the dashboard
     * @param toPayFromWarehouse these are the resources to pay from warehouse
     */
    @Override
    public synchronized void buyCard(int level, CardColor color, int position, CollectionResources toPayFromWarehouse) {
        super.buyCard(level, color, position, toPayFromWarehouse);
        checkEndGame();
    }

    /**
     *this method handle the vatican reports like in the superclass but for only the single player and
     * lorenzoIlMagnifico. At the end it calls checkEndGame to end the game immediately if it needs to be ended
     */
    @Override
    protected synchronized void handleVaticanReport() {
        boolean [] singleVaticanReports = super.getVaticanReports();
        int i=0;
        while((singleVaticanReports[i]) && (i < 3)) i++;
        if (i > 2) return;
        if (getActualPlayer().checkVaticanReport(i) || lorenzoIlMagnifico.checkVaticanReport(i)) {
            setVaticanReports(i);
            getActualPlayer().vaticanReport(i);
            lorenzoIlMagnifico.vaticanReport(i);
        }
        checkEndGame();
    }
}