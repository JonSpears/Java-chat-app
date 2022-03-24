package client;

import common.CloseEverything;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.Scanner;

public class Client implements CloseEverything {
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String userName;
//    private volatile boolean isConnected;


    public Client(Socket socket, String userName) {
        try {
            this.socket = socket;
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.userName = userName;
//            isConnected = true;
        }catch (IOException e){
            System.err.println("Client error " + e.getMessage());
        }
    }

    public void sendMessage (){
        try {
            bufferedWriter.write(userName);
            bufferedWriter.newLine();
            bufferedWriter.flush();

            Scanner scanner = new Scanner(System.in);
            while (socket.isConnected()){
                String messageToSend = scanner.nextLine();
                if (messageToSend.equalsIgnoreCase(userName + ": exit")){
                    bufferedWriter.write(messageToSend);
                    closeEverything(socket, bufferedReader, bufferedWriter);
//                    isConnected = false;
                }
                bufferedWriter.write(userName + ": " + messageToSend);
                bufferedWriter.newLine();
                bufferedWriter.flush();
            }
        }catch (IOException e){
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    public void listenForMessage () {
        new Thread(() -> {
            String messageFromChat;
            while (true) {
                try {
                    messageFromChat = bufferedReader.readLine();
                    if (messageFromChat != null){
                        System.out.println(messageFromChat);
                    }else {
                        System.out.println("Server is down!");
                        closeEverything(socket, bufferedReader, bufferedWriter);
//                        isConnected = false;
                    }
                } catch (IOException e) {
                    closeEverything(socket, bufferedReader, bufferedWriter);
                }
            }
        }).start();
    }

    public static void main(String[] args) throws IOException {
        try {
            Runtime.getRuntime().addShutdownHook(new Thread(()->
                    System.out.println("You have been disconnected from the chat")));
            Scanner scanner = new Scanner(System.in);
            System.out.println("Enter your username for the chat: ");
            String userName = scanner.nextLine();
            Socket socket = new Socket("localhost", 1234);
            Client client = new Client(socket, userName);
            client.listenForMessage();
            client.sendMessage();
        } catch (SocketException e){
            System.err.println("Server timed out " + e.getMessage());

        }catch (IOException e){
            System.err.println(e.getMessage());
        }
    }
}
