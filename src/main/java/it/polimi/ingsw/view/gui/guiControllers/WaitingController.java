package it.polimi.ingsw.view.gui.guiControllers;

import it.polimi.ingsw.controller.commands.Command;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

import java.net.URL;
import java.util.ResourceBundle;

public class WaitingController implements Initializable , GuiController {

    @FXML
    private Label waitingMessage;


    private String message;


    public WaitingController(String message){
        this.message = message;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        waitingMessage.setText(message);
    }

    @Override
    public void update(){

    }

    @Override
    public void sendNewCommand(Command toSend) {

    }
}