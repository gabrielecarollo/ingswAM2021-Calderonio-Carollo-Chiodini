package it.polimi.ingsw.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * this class represent the main
 */
public class ClientMain {
    public static void main(String[] args) throws IOException {

        String hostName = "127.0.0.1";
        int portNumber = 1234;
        try (
                Socket echoSocket = new Socket(hostName, portNumber);
                PrintWriter out =
                        new PrintWriter(echoSocket.getOutputStream(), true);
                BufferedReader in =
                        new BufferedReader(
                                new InputStreamReader(echoSocket.getInputStream()));
                BufferedReader stdIn =
                        new BufferedReader(
                                new InputStreamReader(System.in))
        ) {
            String userInput;
            while ((userInput = stdIn.readLine()) != null) {
                out.println(userInput);
                String recived = in.readLine();
                System.out.println("echo: " + recived);
                switch (recived){
                    case "Communication ended":
                        System.exit(0);
                    case "Invalid command, try again" :
                        break;
                    case "Ok" :
                        System.exit(0);
                }
                //if(recived.equals("Communication ended")) System.exit(1);

            }
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host " + hostName);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to " +
                    hostName);
            System.exit(1);
        }
    }
}