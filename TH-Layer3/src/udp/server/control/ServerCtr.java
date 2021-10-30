package udp.server.control;
 
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import jdbc.dao.BillDAO;
 
import jdbc.dao.CustomerDAO;
import jdbc.dao.ProductDAO;
import jdbc.dao.UserDAO;
import model.Bill;
import model.IPAddress;
import model.ObjectWrapper;
import model.User;
import model.Customer;
import model.Product;
import udp.server.view.ServerMainFrm;
 
 
public class ServerCtr {
    private ServerMainFrm view;
    private DatagramSocket myServer;    
    private IPAddress myAddress = new IPAddress("localhost", 5555); //default server address
    private UDPListening myListening;
     
    public ServerCtr(ServerMainFrm view){
        this.view = view;       
    }
     
    public ServerCtr(ServerMainFrm view, int port){
        this.view = view;
        myAddress.setPort(port);
    }
     
     
    public boolean open(){
        try {
            myServer = new DatagramSocket(myAddress.getPort());
            myAddress.setHost(InetAddress.getLocalHost().getHostAddress());
            view.showServerInfo(myAddress);
            myListening = new UDPListening();
            myListening.start();
            view.showMessage("UDP server is running at the host: " + myAddress.getHost() + ", port: " + myAddress.getPort());
        }catch(Exception e) {
            e.printStackTrace();
            view.showMessage("Error to open the datagram socket!");
            return false;
        }
        return true;
    }
     
    public boolean close(){
        try {
            myListening.stop();
            myServer.close();
        }catch(Exception e) {
            e.printStackTrace();
            view.showMessage("Error to close the datagram socket!");
            return false;
        }
        return true;
    }
     
    class UDPListening extends Thread{
        public UDPListening() {
             
        }
         
        public void run() {
            while(true) {               
                try {   
                    //prepare the buffer and fetch the received data into the buffer
                    byte[] receiveData = new byte[1024];
                    DatagramPacket receivePacket = new  DatagramPacket(receiveData, receiveData.length);
                    myServer.receive(receivePacket);
                     
                    //read incoming data from the buffer 
                    ByteArrayInputStream bais = new ByteArrayInputStream(receiveData);
                    ObjectInputStream ois = new ObjectInputStream(bais);
                    ObjectWrapper receivedData = (ObjectWrapper)ois.readObject();
                     
                    //processing
                    ObjectWrapper resultData = new ObjectWrapper();
                    switch(receivedData.getPerformative()) {
                        case ObjectWrapper.LOGIN_USER:              // login
                            User user = (User)receivedData.getData();
                            view.showMessage(user.getUsername()+" "+user.getPassword());
                            resultData.setPerformative(ObjectWrapper.REPLY_LOGIN_USER);
                            boolean result =new UserDAO().checkLogin(user);
                            if(result){
                                resultData.setData(user);
                            }
                            else
                                resultData.setData("false");
                            break;
                        case ObjectWrapper.REGISTER_USER:
                            user = (User)receivedData.getData();
                            resultData.setPerformative(ObjectWrapper.REPLY_REGISTER_USER);
                            result = new UserDAO().addUser(user);
                            if(result){
                                resultData.setData("true");
                            }
                            else
                                resultData.setData("false");
                            break;
                        case ObjectWrapper.REGISTER_CLIENT:
                            Customer customer = (Customer)receivedData.getData();
                            resultData.setPerformative(ObjectWrapper.REPLY_REGISTER_CLIENT);
                            result = new CustomerDAO().addClient(customer);
                            if(result){
                                resultData.setData("true");
                            }
                            else
                                resultData.setData("false");
                            break;
                        case ObjectWrapper.REGISTER_PRODUCT:
                            Product product = (Product)receivedData.getData();
                            resultData.setPerformative(ObjectWrapper.REPLY_REGISTER_PRODUCT);
                            result = new ProductDAO().addProduct(product);
                            if(result){
                                resultData.setData("true");
                            }
                            else
                                resultData.setData("false");
                            break;
                        case ObjectWrapper.SEARCH_CUSTOMER_BY_ID:
                            String key = (String)receivedData.getData();
                            CustomerDAO cd = new CustomerDAO();
                            resultData.setPerformative(ObjectWrapper.REPLY_SEARCH_CUSTOMER);
                            ArrayList<Customer> resultId = cd.searchCustomer(key);
                            resultData.setData(resultId);
                            break;
                        case ObjectWrapper.SEARCH_PRODUCT_BY_NAME:
                            key = (String)receivedData.getData();
                            ProductDAO pd = new ProductDAO();
                            resultData.setPerformative(ObjectWrapper.REPLY_SEARCH_PRODUCT);
                            ArrayList<Product> resultname = pd.searchProduct(key);
                            resultData.setData(resultname);
                            break;
                        case ObjectWrapper.CONFIRM_BILL:
                            ArrayList<Bill> listBill =(ArrayList<Bill>) receivedData.getData();
                            resultData.setPerformative(ObjectWrapper.REPLY_CONFIRM_BILL);
                            result = new BillDAO().addProduct(listBill);
                            resultData.setData(result);
                            break;
                    }
                     
                     
                    //prepare the buffer and write the data to send into the buffer
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ObjectOutputStream oos = new ObjectOutputStream(baos);
                    oos.writeObject(resultData);
                    oos.flush();            
                     
                    //create data package and send
                    byte[] sendData = baos.toByteArray();
                    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, receivePacket.getAddress(), receivePacket.getPort());
                    view.showMessage("send!!!");
                    myServer.send(sendPacket);
                } catch (Exception e) {
                    e.printStackTrace();
                    view.showMessage("Error when processing an incoming package");
                }    
            }
        }
    }
}