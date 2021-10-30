package tcp.server.control;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import javax.swing.JOptionPane;
import model.Customer;

import model.IPAddress;
import model.ObjectWrapper;
import model.Product;
import model.User;
import tcp.server.view.ServerMainFrm;

public class ServerCtr {

    private ServerMainFrm view;
    private ServerSocket myServer;
    private ServerListening myListening;
    private ArrayList<ServerProcessing> myProcess;
    //client_udp
    private IPAddress myAddress = new IPAddress("localhost", 8888);  //default server host and port
    private IPAddress serverAddress = new IPAddress("localhost", 5555); //default server address
    private IPAddress myAddressUDP = new IPAddress("localhost", 6666); //default client address
    private DatagramSocket myClient;

    //client_udp
    public ServerCtr(ServerMainFrm view) {
        myProcess = new ArrayList<ServerProcessing>();
        this.view = view;
        openServer();
    }

    public ServerCtr(ServerMainFrm view, int serverPort) {
        myProcess = new ArrayList<ServerProcessing>();
        this.view = view;
        myAddress.setPort(serverPort);
        openServer();

    }

    public ServerCtr(ServerMainFrm view, IPAddress serverAddr) {
        this.view = view;

        serverAddress = serverAddr;
    }

    public ServerCtr(ServerMainFrm view, IPAddress serverAddr, int clientPort) {
        this.view = view;
        serverAddress = serverAddr;
        myAddress.setPort(clientPort);
    }

    public boolean open() {
        try {
            ServerSocket serverSocket = new ServerSocket(0);
            System.out.println(serverSocket.getLocalPort());
            myAddress.setPort(serverSocket.getLocalPort());
            myClient = new DatagramSocket(myAddress.getPort());
            myAddress.setHost(InetAddress.getLocalHost().getHostAddress());
            //view.setServerandClientInfo(serverAddress, myAddress);
            view.showMessage("UDP client is running at the host: " + myAddress.getHost() + ", port: " + myAddress.getPort());
        } catch (Exception e) {
            e.printStackTrace();
            view.showMessage("Error to open the datagram socket!");
            return false;
        }
        return true;
    }

    public boolean close() {
        try {
            myClient.close();
        } catch (Exception e) {
            e.printStackTrace();
            view.showMessage("Error to close the datagram socket!");
            return false;
        }
        return true;
    }

    private void openServer() {
        try {
            myServer = new ServerSocket(myAddress.getPort());
            myListening = new ServerListening();
            myListening.start();
            myAddress.setHost(InetAddress.getLocalHost().getHostAddress());
            view.showServerInfor(myAddress);
            //System.out.println("server started!");
            view.showMessage("TCP server is running at the port " + myAddress.getPort() + "...");
        } catch (Exception e) {
            e.printStackTrace();;
        }
    }

    public void stopServer() {
        try {
            for (ServerProcessing sp : myProcess) {
                sp.stop();
            }
            myListening.stop();
            myServer.close();
            view.showMessage("TCP server is stopped!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    public void publicClientNumber() {
//        ObjectWrapper data = new ObjectWrapper(ObjectWrapper.TABLE_FRIEND_UPDATE, myProcess.size());
//        for (ServerProcessing sp : myProcess) {
//            sp.sendData(data);
//        }
//    }

    /**
     * The class to listen the connections from client, avoiding the blocking of
     * accept connection
     *
     */
    class ServerListening extends Thread {

        public ServerListening() {
            super();
        }

        public void run() {
            view.showMessage("server is listening... ");
            try {
                while (true) {
                    Socket clientSocket = myServer.accept();
                    ServerProcessing sp = new ServerProcessing(clientSocket);
                    sp.start();
                    myProcess.add(sp);
                    boolean ok = open();
                    // System.out.println(myClient.getPort());

                    view.showMessage("Number of client connecting to the server: " + myProcess.size());
                    //publicClientNumber();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * The class to treat the requirement from client
     *
     */
    class ServerProcessing extends Thread {

        private Socket mySocket;
        //private ObjectInputStream ois;
        //private ObjectOutputStream oos;

        public ServerProcessing(Socket s) {
            super();
            mySocket = s;
        }

        public void sendData(Object obj) {
            try {
                ObjectOutputStream oos = new ObjectOutputStream(mySocket.getOutputStream());
                oos.writeObject(obj);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void run() {
            try {
                while (true) {
                    ObjectInputStream ois = new ObjectInputStream(mySocket.getInputStream());
                    ObjectOutputStream oos = new ObjectOutputStream(mySocket.getOutputStream());
                    Object o = ois.readObject();
                    if (o instanceof ObjectWrapper) {
                        ObjectWrapper data = (ObjectWrapper) o;

                        switch (data.getPerformative()) {
                            case ObjectWrapper.LOGIN_USER:
                                User user = (User) data.getData();
                                //System.out.println(player.getAccount()+ " " + player.getPassword());
                                view.showMessage(user.getUsername()+" " + user.getPassword());
                                if (sendDataUDP(data)) {
                                    ObjectWrapper dataRecieveUDP = receiveDataUDP();
                                    if (dataRecieveUDP.getPerformative() == ObjectWrapper.REPLY_LOGIN_USER) {  //udpclient                                        
                                        if (dataRecieveUDP.getData().equals("false"))
                                            oos.writeObject(new ObjectWrapper(ObjectWrapper.REPLY_LOGIN_USER, "false"));
                                        else{
                                            oos.writeObject(new ObjectWrapper(ObjectWrapper.REPLY_LOGIN_USER, dataRecieveUDP.getData()));
                                            view.showMessage("login success");  
                                        }
                                    }
                                }
                                break;
                            case ObjectWrapper.REGISTER_USER:
                                user = (User) data.getData();
                                if(sendDataUDP(data)){
                                    ObjectWrapper dataRecieveUDP = receiveDataUDP();
                                    if (dataRecieveUDP.getPerformative() == ObjectWrapper.REPLY_REGISTER_USER) {  //udpclient                                        
                                        if (dataRecieveUDP.getData().equals("false"))
                                            oos.writeObject(new ObjectWrapper(ObjectWrapper.REPLY_REGISTER_USER, "false"));
                                        else{
                                            oos.writeObject(new ObjectWrapper(ObjectWrapper.REPLY_REGISTER_USER, "true"));
                                            view.showMessage("register user success");  
                                        }
                                    }
                                }
                                break;
                            case ObjectWrapper.REGISTER_CLIENT:
                                Customer customer = (Customer) data.getData();
                                if(sendDataUDP(data)){
                                    ObjectWrapper dataRecieveUDP = receiveDataUDP();
                                    if (dataRecieveUDP.getPerformative() == ObjectWrapper.REPLY_REGISTER_CLIENT) {  //udpclient                                        
                                        if (dataRecieveUDP.getData().equals("false"))
                                            oos.writeObject(new ObjectWrapper(ObjectWrapper.REPLY_REGISTER_CLIENT, "false"));
                                        else{
                                            oos.writeObject(new ObjectWrapper(ObjectWrapper.REPLY_REGISTER_CLIENT, "true"));
                                            view.showMessage("register client success");  
                                        }
                                    }
                                }
                                break;
                            case ObjectWrapper.REGISTER_PRODUCT:
                                Product product = (Product) data.getData();
                                if(sendDataUDP(data)){
                                    ObjectWrapper dataRecieveUDP = receiveDataUDP();
                                    if (dataRecieveUDP.getPerformative() == ObjectWrapper.REPLY_REGISTER_PRODUCT) {  //udpclient                                        
                                        if (dataRecieveUDP.getData().equals("false"))
                                            oos.writeObject(new ObjectWrapper(ObjectWrapper.REPLY_REGISTER_PRODUCT, "false"));
                                        else{
                                            oos.writeObject(new ObjectWrapper(ObjectWrapper.REPLY_REGISTER_PRODUCT, "true"));
                                            view.showMessage("register product success");  
                                        }
                                    }
                                }
                                break;
                            case ObjectWrapper.SEARCH_CUSTOMER_BY_ID:
                                if(sendDataUDP(data)){
                                    ObjectWrapper dataRecieveUDP = receiveDataUDP();
                                    view.showMessage(((ArrayList<Customer>)(dataRecieveUDP.getData())).get(0).getName()+"");                                      
                                    oos.writeObject(new ObjectWrapper(ObjectWrapper.REPLY_SEARCH_CUSTOMER, dataRecieveUDP.getData()));
                                    view.showMessage("search client success"); 
                                }
                                break;
                            case ObjectWrapper.SEARCH_PRODUCT_BY_NAME:
                                if(sendDataUDP(data)){
                                    ObjectWrapper dataRecieveUDP = receiveDataUDP();
//                                    view.showMessage(((ArrayList<Product>)(dataRecieveUDP.getData())).get(0).getName()+"");                                      
                                    oos.writeObject(new ObjectWrapper(ObjectWrapper.REPLY_SEARCH_PRODUCT, dataRecieveUDP.getData()));
                                    view.showMessage("search product success"); 
                                } 
                                break;
                            case ObjectWrapper.CONFIRM_BILL:
                                if(sendDataUDP(data)){
                                    ObjectWrapper dataRecieveUDP = receiveDataUDP();
                                    if (dataRecieveUDP.getData().equals("false"))
                                            oos.writeObject(new ObjectWrapper(ObjectWrapper.REPLY_CONFIRM_BILL, "false"));
                                        else{
                                            oos.writeObject(new ObjectWrapper(ObjectWrapper.REPLY_CONFIRM_BILL, "true"));
                                            view.showMessage("confirm bill success");
                                        }
                                }
                                break;
                        }
//                        ois.reset();
//                        oos.reset();

                    }
                }
            } catch (EOFException | SocketException e) {
                //e.printStackTrace();
                myProcess.remove(this);
                view.showMessage("Number of client connecting to the server: " + myProcess.size());
                //publicClientNumber();
                try {
                    mySocket.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public boolean sendDataUDP(ObjectWrapper data) {
        try {
            //prepare the buffer and write the data to send into the buffer
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(data);
            oos.flush();

            //create data package and send
            byte[] sendData = baos.toByteArray();
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(serverAddress.getHost()), serverAddress.getPort());
            myClient.send(sendPacket);
        } catch (Exception e) {
            e.printStackTrace();
            view.showMessage("Error in sending data package");
            return false;
        }
        return true;
    }

    public ObjectWrapper receiveDataUDP() {
        ObjectWrapper result = null;
        try {
            //prepare the buffer and fetch the received data into the buffer
            byte[] receiveData = new byte[1024];
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            myClient.receive(receivePacket);

            //read incoming data from the buffer 
            ByteArrayInputStream bais = new ByteArrayInputStream(receiveData);
            ObjectInputStream ois = new ObjectInputStream(bais);
            result = (ObjectWrapper) ois.readObject();
        } catch (Exception e) {
            e.printStackTrace();
            view.showMessage("Error in receiving data package");
        }
        return result;
    }
}