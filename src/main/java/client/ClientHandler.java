package client;

import common.CloseEverything;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class ClientHandler implements Runnable, CloseEverything {

    public static final ArrayList<ClientHandler> clientHandlers = new ArrayList<>();
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String clientUserName;

    public ClientHandler(Socket socket) {
        try {
            this.socket = socket;
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.clientUserName = bufferedReader.readLine();
            clientHandlers.add(this);
            broadcastMessage("SERVER: " + clientUserName + " has joined the chat!");
            System.out.println("CLIENT-HANDLER: " + clientUserName + " joined the chat on port " + socket.getPort());
        }catch (IOException e){
            removeClientHandler();
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    @Override
    public void run() {
        String messageFromClient;

        while (true){
            try {
                messageFromClient = bufferedReader.readLine();
                if ((messageFromClient.equalsIgnoreCase(clientUserName + ": exit"))
                        || messageFromClient.equalsIgnoreCase(clientUserName + ": quit")){
                    removeClientHandler();
                    broadcastMessage(clientUserName + " left the chat!");
                    break;
                }else broadcastMessage(messageFromClient);

            }catch (NullPointerException e){
                removeClientHandler();
                System.out.println(clientUserName + " left abruptly!");
                broadcastMessage(clientUserName + " left the chat!");
                break;

            } catch (IOException e){
                removeClientHandler();
                closeEverything(socket, bufferedReader, bufferedWriter);
            }
        }
    }

    public void broadcastMessage (String messageToSend){
        for (ClientHandler clientHandler : clientHandlers){
            try {
                if (!clientHandler.clientUserName.equals(clientUserName)){
                        clientHandler.bufferedWriter.write(messageToSend);
                        clientHandler.bufferedWriter.newLine();
                        clientHandler.bufferedWriter.flush();
                }
            }catch (IOException e){
                removeClientHandler();
                closeEverything(socket, bufferedReader, bufferedWriter);
            }
        }
    }

    public void removeClientHandler(){
        clientHandlers.remove(this);
    }

}
