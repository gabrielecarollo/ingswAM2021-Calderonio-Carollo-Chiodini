package it.polimi.ingsw.view.gui.guiControllers;

import it.polimi.ingsw.controller.commands.Command;
import it.polimi.ingsw.controller.commands.normalCommands.productionCommands.BasicProductionCommand;
import it.polimi.ingsw.controller.responseToClients.ResponseToClient;
import it.polimi.ingsw.model.Resources.*;
import it.polimi.ingsw.network.NetworkUser;
import it.polimi.ingsw.view.gui.Gui;
import it.polimi.ingsw.view.thinModelComponents.ThinModel;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.text.Font;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ResourceBundle;

public class BasicProductionController extends TurnsController{

    private final CollectionResources toPayFromWarehouse = new CollectionResources();
    private final CollectionResources toPayFromStrongbox = new CollectionResources();
    private Label contextAction = new Label();
    private Button payButton = new Button();


    public BasicProductionController(ThinModel model, String nickname, NetworkUser<Command, ResponseToClient> clientNetworkUser, boolean normalActions, boolean leaderActions) {
        super(model, nickname, clientNetworkUser, normalActions, leaderActions);
    }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        setMainWindowSize();

        drawPlayer(getNickname());
        setPlayerOpacity(0.5);
        getMainWindow().getChildren().add(payButton);
        getMainWindow().getChildren().add(contextAction);
        showCard();
        showResourcesToPayFromWarehouse();

    }

    private void showCard(){
        ImageView card = new ImageView(getBasicProductionImage());
        card.setFitWidth(getCardWidth());
        card.setFitHeight(getCardHeight());

        card.setLayoutX(getMainWindow().getPrefWidth() * 3 / 4);
        card.setLayoutY(getMainWindow().getPrefHeight() * 1 / 4);

        getMainWindow().getChildren().add(card);
    }

    private void showResourcesToPayFromWarehouse(){

        setResourcesImages(toPayFromWarehouse);

        payButton.setText("pay");
        payButton.setOnAction(actionEvent -> showResourcesToPayFromStrongbox());

        payButton.setLayoutX(getMainWindow().getPrefWidth() / 2 );
        payButton.setLayoutY(getMainWindow().getPrefHeight() * 3 / 5);

        contextAction.setText("Click in the resources that you want to pay from Warehouse");

        contextAction.setPrefWidth(10000);
        contextAction.setFont(Font.font(31));
        contextAction.setAlignment(Pos.CENTER);
        contextAction.setPrefSize( getMainWindow().getPrefWidth() - contextAction.getText().length(), getMainWindow().getPrefHeight() / 5 );

    }

    private void showResourcesToPayFromStrongbox(){

        setResourcesImages(toPayFromStrongbox);


        payButton.setOnAction(actionEvent -> showResourcesToGainAsOutput());

        payButton.setLayoutX(getMainWindow().getPrefWidth() / 2 );
        payButton.setLayoutY(getMainWindow().getPrefHeight() * 3 / 5);

        contextAction.setText("Click in the resources that you want to pay from Strongbox");

        contextAction.setPrefWidth(10000);
        contextAction.setFont(Font.font(31));
        contextAction.setAlignment(Pos.CENTER);
        contextAction.setPrefSize( getMainWindow().getPrefWidth() - contextAction.getText().length(), getMainWindow().getPrefHeight() / 5 );


    }

    private void setResourcesImages(CollectionResources toPay) {
        double layoutX = getMainWindow().getPrefWidth() * 1 / 4;
        double layoutY = getMainWindow().getPrefHeight() / 2 - getMainWindow().getPrefHeight() / 7;

        double offsetX = getMainWindow().getPrefWidth() / 10;

        int i = 0;

        for (Resource resource : new ArrayList<>(Arrays.asList(new Coin(), new Stone(), new Shield(), new Servant()))){
            ImageView resourceToDraw = new ImageView(getResourceImage(resource));
            resourceToDraw.setLayoutX(layoutX + i * offsetX);
            resourceToDraw.setLayoutY(layoutY);
            resourceToDraw.setOnMouseClicked( mouseEvent -> toPay.add(resource));

            getMainWindow().getChildren().add(resourceToDraw);


            i++;
        }
    }

    private void showResourcesToGainAsOutput(){
        double layoutX = getMainWindow().getPrefWidth() * 1 / 4;
        double layoutY = getMainWindow().getPrefHeight() / 2 - getMainWindow().getPrefHeight() / 7;

        double offsetX = getMainWindow().getPrefWidth() / 10;

        int i = 0;

        for (Resource resource : new ArrayList<>(Arrays.asList(new Coin(), new Stone(), new Shield(), new Servant()))){
            ImageView resourceToDraw = new ImageView(getResourceImage(resource));
            resourceToDraw.setLayoutX(layoutX + i * offsetX);
            resourceToDraw.setLayoutY(layoutY);
            resourceToDraw.setOnMouseClicked( mouseEvent -> getClientNetworkUser().send(new BasicProductionCommand(toPayFromWarehouse, toPayFromStrongbox, resource)));

            getMainWindow().getChildren().add(resourceToDraw);


            i++;
        }

        payButton = new Button();

        contextAction.setText("Click in the resources that you want to gain as output");

        contextAction.setPrefWidth(10000);
        contextAction.setFont(Font.font(31));
        contextAction.setAlignment(Pos.CENTER);
        contextAction.setPrefSize( getMainWindow().getPrefWidth() - contextAction.getText().length(), getMainWindow().getPrefHeight() / 5 );

    }



    @Override
    public void showErrorMessage() {
        super.showErrorMessage();
        rollBack();
    }

    @Override
    public void rollBack() {
        Gui.setRoot("/ProductionWindow",
                new ProductionController(getModel(),
                        getNickname(),
                        getClientNetworkUser(),
                        getNormalAction(),
                        getLeaderAction()));
    }

    @Override
    public void update() {
        setNormalAction(false);
        Gui.setRoot("/ProductionWindow",
                new ProductionController(getModel(),
                        getNickname(),
                        getClientNetworkUser(),
                        false,
                        getLeaderAction()));
    }
}