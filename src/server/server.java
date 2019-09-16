/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

/**
 *
 * @author Rima Ghoulam
 */

import java.io.*;
import java.net.*;
import java.util.*;

public class server {
    
    private static Socket clientSocket = null;
    private static ServerSocket serverSocket = null;
    public static ArrayList<ThreadC> users = new ArrayList<ThreadC>();

    public server() {
        int Port = 1491;
    }
    
public static void main(String args[]) {

    if (args.length < 1){
        System.out.println("qlq chose qui cloche!");
        System.exit(0);
    } 

    server s = new server();

    int numeroClient = 1;
    while (true) {
        
        try {

            clientSocket = serverSocket.accept();
            ThreadC clientCourrant =  new ThreadC(clientSocket, users);
            users.add(clientCourrant);
            clientCourrant.start();
            System.out.println(numeroClient + " est connecter");
            numeroClient++;

        } catch (IOException e) {
                    System.out.println("error!");
        }
    }	
}
}

class ThreadC extends Thread {
private String clientName = null;
private ObjectInputStream is = null;
private ObjectOutputStream os = null;
private Socket clientSocket = null;
private final ArrayList<ThreadC> arrayClients;


public ThreadC(Socket clientSocket, ArrayList<ThreadC> clients) {

        this.clientSocket = clientSocket;
        this.arrayClients = clients;

}

public void run() {
    
    ArrayList<ThreadC> clients = this.arrayClients;
    try {
        is = new ObjectInputStream(clientSocket.getInputStream());
        os = new ObjectOutputStream(clientSocket.getOutputStream());
        String nomClient;

        while (true) {                                                                             
            synchronized (this) {
                this.os.writeObject("Entrez votre nom :");
                this.os.flush();
                
                nomClient = ((String) this.is.readObject()).trim();
                
                if ((nomClient.indexOf('@') == -1) || (nomClient.indexOf('!') == -1)) {
                    break;
                } else {
                    this.os.writeObject("invalid name!");
                    this.os.flush();
                }
            }
        }

        this.os.writeObject("أهلا و سهلا ");
        this.os.flush();

        synchronized(this){
        for (ThreadC user : clients){
            if (user == this && user != null) {
                clientName = "@" + nomClient;
                user.os.writeObject(nomClient + "added");
                user.os.flush();
                break;
            }
        }
        }

        // debut conversation:
        while (true) {
            this.os.writeObject("mettez (x) pour quiter et @Nom: message to send msg to Nom");
            this.os.flush();

            String ligne = (String) is.readObject();
            if (ligne.startsWith("x")) { break;}

           
        // si on veut envoyer un msg prive
            if (ligne.startsWith("@")) { unicast(ligne,nomClient); } 
            else{ broadcast(ligne,nomClient); }
        }

        System.out.println(nomClient + " left");
        clients.remove(this);

        synchronized(this) {
            if (! clients.isEmpty()) {
                for (ThreadC c : clients) {

                    if (c != null && c != this && c.clientName != null) {
                            c.os.writeObject(nomClient + "left ");
                            c.os.flush();
                    }
                }
            }
        }

        this.is.close();
        this.os.close();
        clientSocket.close();

} catch (IOException e) {
        System.out.println("session expired");
} catch (ClassNotFoundException e) {
        System.out.println("error class");
}
}


void unicast(String line, String name) throws IOException, ClassNotFoundException {

    String[] words = line.split(":", 2); 

    if (words[1].split(" ")[0].toLowerCase().equals("sendfile")) {
        byte[] file_data = (byte[]) is.readObject();

        for (ThreadC c : arrayClients) {
            if (c != null && c != this && c.clientName != null && c.clientName.equals(words[0])) {
                c.os.writeObject("Sending_File:"+words[1].split(" ",2)[1].substring(words[1].split("\\s",2)[1].lastIndexOf(File.separator)+1));
                c.os.writeObject(file_data);
                c.os.flush();
                System.out.println(this.clientName.substring(1) + " envoyer private msg "+ c.clientName.substring(1));

                break;

            }
        }
    }else {
        if (words.length > 1 && words[1] != null) {
        words[1] = words[1].trim();
        if (!words[1].isEmpty()) {
            for (ThreadC c : arrayClients) {
                if (c != null && c != this && c.clientName != null && c.clientName.equals(words[0])) {
                    c.os.writeObject("<" + name + "> " + words[1]);
                    c.os.flush();
                    System.out.println("sent");
                    this.os.writeObject("sent");
                    this.os.flush();
                    break;
                }
            }
        }
        }
        }
}

//broadcast
void broadcast(String ligne, String name) throws IOException, ClassNotFoundException {
    byte[] file = (byte[]) is.readObject();
    synchronized(this){
        for (ThreadC c : arrayClients) {
            if (c != null && c.clientName != null && c.clientName!=this.clientName){
                c.os.writeObject("Sending:"+ligne.split("\\s",2)[1].substring(ligne.split("\\s",2)[1].lastIndexOf(File.separator)+1));
                c.os.writeObject(file);
                c.os.flush();
            }
        }

        this.os.writeObject("sent");
        this.os.flush();
        System.out.println("from: " + this.clientName.substring(1));
    }
    
}
}
