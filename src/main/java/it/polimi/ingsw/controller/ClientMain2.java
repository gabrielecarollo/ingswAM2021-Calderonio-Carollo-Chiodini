package it.polimi.ingsw.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.typeadapters.RuntimeTypeAdapterFactory;
import it.polimi.ingsw.model.DevelopmentCards.CardColor;
import it.polimi.ingsw.model.DevelopmentCards.DevelopmentCard;
import it.polimi.ingsw.model.LeaderCard.*;
import it.polimi.ingsw.model.Marble.Marble;
import it.polimi.ingsw.model.Resources.*;
import it.polimi.ingsw.model.SingleGame.CardToken;
import it.polimi.ingsw.model.SingleGame.SoloToken;
import it.polimi.ingsw.model.SingleGame.TrackToken;
import it.polimi.ingsw.view.ThinLeaderCard;
import it.polimi.ingsw.view.ThinPlayer;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * this class represent the main
 */
public class ClientMain2 implements Runnable {

    private final String hostName;
    private final Socket echoSocket;
    private final Gson gson;
    private int position = 0;
    private ThinPlayer myself;
    private List<ThinPlayer> opponents;
    private List<LeaderCard> leaderCards;
    private List<LeaderCard> allLeaderCards;
    private final DevelopmentCard[] productionPower = new DevelopmentCard[3];
    private DevelopmentCard[][] cardsMarket;
    private Marble[][] marbleMarket;
    private Marble lonelyMarble;
    private SoloToken solotoken;


    public static void main(String[] args) throws IOException{
        ClientMain2 client = new ClientMain2("127.0.0.1", 1234);
        client.start();
    }

    public ClientMain2(String hostName, int portNumber) throws IOException {
        this.hostName = hostName;
        this.echoSocket = new Socket(hostName, portNumber);
        allLeaderCards = createLeaderCards();
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapterFactory(
                RuntimeTypeAdapterFactory.
                        of(SoloToken.class, "type").
                        registerSubtype(TrackToken.class, "trackToken").
                        registerSubtype(CardToken.class, "cardToken"));
        builder.registerTypeAdapter(Resource.class, new ResourceInterfaceAdapter());
        builder.registerTypeAdapter(Marble.class, new MarbleInterfaceAdapter());
        gson = builder.create();
    }


    private void start() {

        Thread t = new Thread(this);
        t.start();

        try (
                PrintWriter out =
                        new PrintWriter(echoSocket.getOutputStream(), true);
                BufferedReader stdIn =
                        new BufferedReader(
                                new InputStreamReader(System.in))
        ) {

            String command;

            while ((command = stdIn.readLine()) != null) {

                Command jsonCommand = new Command();

                switch (command){

                    case "set_players" :
                        System.out.println("Scrivere il numero di giocatori");
                        String numberOfPlayers = stdIn.readLine();
                        jsonCommand.cmd = "set_players";
                        jsonCommand.size = Integer.parseInt(numberOfPlayers);
                        send(out, jsonCommand);
                        break;

                    case "login":
                        System.out.println("Scrivere il nickname");
                        jsonCommand.cmd = "login";
                        jsonCommand.nickname = stdIn.readLine();
                        send(out, jsonCommand);
                        break;

                    case "initialise_leaderCards":
                        initialiseLeaderCard(out, stdIn, jsonCommand);
                        break;

                    case "initialise_resources":
                        initialiseResources(out, stdIn, jsonCommand);
                        break;

                    case "quit":
                        jsonCommand.cmd = "quit";
                        send(out, jsonCommand);
                        break;

                    default:
                        System.out.println("Comando non riconosciuto");
                        jsonCommand.cmd = "";
                        send(out, jsonCommand);
                        break;
                }
            }

        } catch (UnknownHostException e) {
            System.err.println("Don't know about host " + hostName);
            System.exit(1);
        } catch (IOException e) {
            System.out.println("Someone left, the game finish, we are sorry...");
            System.exit(1);
        }
    }

    /**
     * When an object implementing interface {@code Runnable} is used
     * to create a thread, starting the thread causes the object's
     * {@code run} method to be called in that separately executing
     * thread.
     * <p>
     * The general contract of the method {@code run} is that it may
     * take any action whatsoever.
     *
     * @see Thread#run()
     */

    @Override
    public void run() {
        BufferedReader in =
                null;
        try {
            in = new BufferedReader(
                    new InputStreamReader(echoSocket.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        ResponseToClient response;
        String recived;

        try {
            while (true){
                assert in != null;
                if ((recived = in.readLine()) == null) break;
                response = gson.fromJson(recived, ResponseToClient.class);
                if (response.serialize){ // if the internal state of the game is changed
                    processObject(response.code, response);
                }
                if (response.message != null){
                    if (response.ignorePossibleCommands)
                        System.out.println(response.message);
                    else{
                        response.possibleCommands.add("quit");
                        System.out.println(response.message + ", Possible commands:" + response.possibleCommands);
                    }

                    if (response.position > 0){
                        this.position = response.position;
                        System.out.println("You are the" + position + "° player");
                    }


                }
            }
            in.close();
            System.err.println("Disconnessione in corso...");
            System.exit(1);
        } catch (IOException e){
            System.err.println("Qualcosa è andato storto con IOexception...");
            System.err.println(e.getMessage());
        }catch (NullPointerException e){
            System.err.println("Un client si è NullDisconnesso");
            System.err.println(e.getMessage());
            System.exit(1);
        }catch (JsonSyntaxException e){
            System.err.println("Disconnessione in corso per json errore...");
            System.err.println(e.getMessage());

        }
    }

    /**
     * this method send a message to the server, the message
     * have to be in the format class Command
     * @param out this is the printWriter associated with the socket
     * @param command this is the socket to send
     */
    private void send(PrintWriter out, Command command){
        out.println(gson.toJson(command, Command.class));
    }

    private boolean isValidResource(String toVerify){
        List<String> resources = new ArrayList<>();
        resources.add("COIN");
        resources.add("STONE");
        resources.add("SHIELD");
        resources.add("SERVANT");
        return !resources.contains(toVerify);
    }

    private Resource convert(String s){
        return ResourceType.valueOf(ResourceType.class, s).getResource();
    }

    private synchronized void initialiseResources(PrintWriter out, BufferedReader stdIn, Command jsonCommand) throws IOException {
        String firstResource;
        String secondResource;
        switch (position){
            case 1:
                jsonCommand.cmd = "initialise_resources";
                break;
            case 2:
            case 3:
                System.out.println("Scrivere la risorsa da voler ottenere[coin, stone, shield, servant]");
                firstResource = stdIn.readLine().toUpperCase();
                if(isValidResource(firstResource)){
                    System.out.println("Risorsa non valida");
                    return;
                }
                jsonCommand.cmd = "initialise_resources";
                jsonCommand.firstResource = convert(firstResource);
                break;
            case 4:
                System.out.println("Scrivere la prima risorsa da voler ottenere[coin, stone, shield, servant]");
                firstResource = stdIn.readLine().toUpperCase();
                if(isValidResource(firstResource)){
                    System.out.println("Risorsa non valida");
                    return;
                }
                System.out.println("Scrivere la seconda risorsa da voler ottenere[coin, stone, shield, servant]");
                secondResource = stdIn.readLine();
                if(isValidResource(secondResource)){
                    System.out.println("Risorsa non valida");
                    return;
                }
                jsonCommand.cmd = "initialise_resources";
                jsonCommand.firstResource = convert(firstResource);
                jsonCommand.secondResource = convert(secondResource);
                break;
        }

        send(out, jsonCommand);
    }

    private void initialiseLeaderCard(PrintWriter out, BufferedReader stdIn, Command jsonCommand) throws IOException {
        jsonCommand.cmd = "initialise_leaderCards";
        System.out.println("Scrivere la prima leader card da voler scartare");
        String firstCard = stdIn.readLine();
        try {
            jsonCommand.firstCard = Integer.parseInt(firstCard);
        }catch (NumberFormatException e){
            System.err.println("Devi inserire un numero valido");
            return;
        }
        System.out.println("Scrivere la seconda leader card da voler scartare");
        String secondCard = stdIn.readLine();
        try {
            jsonCommand.secondCard = Integer.parseInt(secondCard);
        }catch (NumberFormatException e){
            System.err.println("Devi inserire un numero valido");
            return;
        }
        send(out, jsonCommand);
    }


    private synchronized void processObject( int code, ResponseToClient response){
        switch (code){
            case 1:
                leaderCards = response.leaderCards.stream().
                        map(this::recreate).
                        collect(Collectors.toList());
                System.out.println(leaderCards);
                break;
            case 2:
                cardsMarket = response.cardsMarket;
                marbleMarket = response.marbleMarket;
                lonelyMarble = response.lonelyMarble;
                solotoken = response.soloToken;
                myself = response.actualPlayer;
                opponents = response.opponents;
                System.out.println(Arrays.deepToString(cardsMarket));
                System.out.println(Arrays.deepToString(marbleMarket));
                System.out.println(lonelyMarble);
                System.out.println(solotoken);
                System.out.println(myself);
                System.out.println(opponents);
                break;

        }
    }

    private List<LeaderCard> createLeaderCards(){
        List<LeaderCard> leaders = new ArrayList<>(); // create an arraylist of leaderCard
        LeaderCardRequirements requirements1 = new ColorRequired(new ArrayList<>(Arrays.asList(CardColor.BLUE, CardColor.BLUE, CardColor.YELLOW)));
        LeaderCardRequirements requirements2 = new ColorRequired(new ArrayList<>(Arrays.asList(CardColor.PURPLE, CardColor.PURPLE, CardColor.GREEN)));
        LeaderCardRequirements requirements3 = new ColorRequired(new ArrayList<>(Arrays.asList(CardColor.GREEN, CardColor.GREEN, CardColor.PURPLE)));
        LeaderCardRequirements requirements4 = new ColorRequired(new ArrayList<>(Arrays.asList(CardColor.YELLOW, CardColor.YELLOW, CardColor.BLUE)));

        //NewWhiteMarble leaderCards
        leaders.add(new NewWhiteMarble(requirements2,5,new Coin())); leaders.add(new NewWhiteMarble(requirements1 ,5,new Stone())); leaders.add(new NewWhiteMarble(requirements4,5,new Servant())); leaders.add(new NewWhiteMarble(requirements3,5,new Shield())); //NewWhiteMarble leaderCard
        //NewShelf leaderCards
        leaders.add(new NewShelf(new ResourcesRequired(new Shield()),3,new Coin())); leaders.add(new NewShelf(new ResourcesRequired(new Servant()),3,new Shield())); leaders.add(new NewShelf(new ResourcesRequired(new Stone()),3,new Servant())); leaders.add(new NewShelf(new ResourcesRequired(new Coin()),3,new Stone())); //NewShelf leaderCard
        //NewProduction leaderCards
        leaders.add(new NewProduction(new LevelRequired(CardColor.GREEN) ,4 , new Coin())); leaders.add(new NewProduction(new LevelRequired(CardColor.PURPLE) ,4 , new Stone())); leaders.add(new NewProduction(new LevelRequired(CardColor.BLUE) ,4 , new Servant())); leaders.add(new NewProduction(new LevelRequired(CardColor.YELLOW) ,4 , new Shield())); //NewProduction leaderCard

        LeaderCardRequirements requirement1 = new ColorRequired(new ArrayList<>(Arrays.asList(CardColor.YELLOW, CardColor.PURPLE)));
        LeaderCardRequirements requirement2 = new ColorRequired(new ArrayList<>(Arrays.asList(CardColor.GREEN , CardColor.BLUE)));
        LeaderCardRequirements requirement3 = new ColorRequired(new ArrayList<>(Arrays.asList(CardColor.BLUE , CardColor.PURPLE)));
        LeaderCardRequirements requirement4 = new ColorRequired(new ArrayList<>(Arrays.asList(CardColor.YELLOW, CardColor.GREEN)));

        //NewDiscount leaderCards
        leaders.add(new NewDiscount(requirement1,2,new Coin())); leaders.add(new NewDiscount(requirement2,2,new Stone())); leaders.add(new NewDiscount(requirement3,2,new Shield())); leaders.add(new NewDiscount(requirement4,2,new Servant())); //NewDiscount leaderCard
        return leaders;
    }

    /**
     * this method build the leader cards starting from the victory points and the resource associated
     * @param leaderCard this is the leader card to recreate
     * @return the card
     */
    private LeaderCard recreate(ThinLeaderCard leaderCard){
        Resource resource = leaderCard.getResource();
        int victoryPoints = leaderCard.getVictoryPoints();
        for (LeaderCard card : allLeaderCards){
            if (card.getResource().equals(resource) && card.getVictoryPoints() == victoryPoints){
                if (leaderCard.isActive())
                    card.setActive();
                return card;
            }
        }
        return null;
    }

    /**
     * this method will show the game
     */
    private void show(){

    }

    /**
     * this method get all the leader cards of the game
     * @return all the leader cards of the game
     */
    public List<LeaderCard> getAllLeaderCards() {
        return allLeaderCards;
    }
}
