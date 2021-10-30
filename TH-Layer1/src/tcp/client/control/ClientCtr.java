package tcp.client.control;
 
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
 
import model.IPAddress;
import model.ObjectWrapper;
import tcp.client.view.ClientFrm;
import tcp.client.view.LoginFrm;
import tcp.client.view.ClientMainFrm;
import tcp.client.view.ConfirmBillFrm;
import tcp.client.view.HomeFrm;
import tcp.client.view.ProductFrm;
import tcp.client.view.RegisterFrm;
import tcp.client.view.SearchCustomerFrm;
import tcp.client.view.SearchProductFrm;
 
 
public class ClientCtr {
    private Socket mySocket;
    private ClientMainFrm view;
    private ClientListening myListening;                            // thread to listen the data from the server
    private ArrayList<ObjectWrapper> myFunction;                  // list of active client functions
    private IPAddress serverAddress = new IPAddress("localhost",8888);  // default server host and port
     
    public ClientCtr(ClientMainFrm view){
        super();
        this.view = view;
        myFunction = new ArrayList<ObjectWrapper>();  
    }
     
    public ClientCtr(ClientMainFrm view, IPAddress serverAddr) {
        super();
        this.view = view;
        this.serverAddress = serverAddr;
        myFunction = new ArrayList<ObjectWrapper>();
    }
 
 
 
    public boolean openConnection(){        
        try {
            mySocket = new Socket(serverAddress.getHost(), serverAddress.getPort());  
            myListening = new ClientListening();
            myListening.start();
            view.showMessage("Connected to the server at host: " + serverAddress.getHost() + ", port: " + serverAddress.getPort());
        } catch (Exception e) {
            //e.printStackTrace();
            view.showMessage("Error when connecting to the server!");
            return false;
        }
        return true;
    }
     
    public boolean sendData(Object obj){
        try {
            ObjectOutputStream oos = new ObjectOutputStream(mySocket.getOutputStream());
            oos.writeObject(obj);           
             
        } catch (Exception e) {
            //e.printStackTrace();
            view.showMessage("Error when sending data to the server!");
            return false;
        }
        return true;
    }
     
    /*
    public Object receiveData(){
        Object result = null;
        try {
            ObjectInputStream ois = new ObjectInputStream(mySocket.getInputStream());
            result = ois.readObject();
        } catch (Exception e) {
            //e.printStackTrace();
            view.showMessage("Error when receiving data from the server!");
            return null;
        }
        return result;
    }*/
     
    public boolean closeConnection(){
         try {
             if(myListening != null)
                 myListening.stop();
             if(mySocket !=null) {
                 mySocket.close();
                 view.showMessage("Disconnected from the server!");
             }
            myFunction.clear();             
         } catch (Exception e) {
             //e.printStackTrace();
             view.showMessage("Error when disconnecting from the server!");
             return false;
         }
         return true;
    }
     
     
     
    public ArrayList<ObjectWrapper> getActiveFunction() {
        return myFunction;
    }
 
 
    class ClientListening extends Thread{
         
        public ClientListening() {
            super();
        }
         
        public void run() {
            try {
                while(true) {
                    //view.showMessage("Im here");
                    ObjectInputStream ois = new ObjectInputStream(mySocket.getInputStream());
                    Object obj = ois.readObject();
                    if(obj instanceof ObjectWrapper) {
                        ObjectWrapper data = (ObjectWrapper)obj;
                        for(ObjectWrapper fto: myFunction)
                            if(fto.getPerformative() == data.getPerformative()) {
                                switch(data.getPerformative()) {
                                case ObjectWrapper.REPLY_LOGIN_USER:
                                    LoginFrm loginView = (LoginFrm)fto.getData();
                                    loginView.receivedDataProcessing(data); 
                                    break;
                                case ObjectWrapper.REPLY_REGISTER_USER:
                                    RegisterFrm registerView = (RegisterFrm)fto.getData();
                                    registerView.receivedDataProcessing(data);
                                    break;
                                case ObjectWrapper.REPLY_REGISTER_CLIENT:
                                    ClientFrm clientView = (ClientFrm)fto.getData();
                                    clientView.receivedDataProcessing(data);
                                    break;
                                case ObjectWrapper.REPLY_REGISTER_PRODUCT:
                                    ProductFrm productView = (ProductFrm)fto.getData();
                                    productView.receivedDataProcessing(data);
                                    break;
                                case ObjectWrapper.REPLY_SEARCH_CUSTOMER:
                                    SearchCustomerFrm scv = (SearchCustomerFrm)fto.getData();
                                    scv.receivedDataProcessing(data);   
                                    break;
                                case ObjectWrapper.REPLY_SEARCH_PRODUCT:
                                    SearchProductFrm spv = (SearchProductFrm)fto.getData();
                                    spv.receivedDataProcessing(data);
                                    break;
                                case ObjectWrapper.REPLY_CONFIRM_BILL:
                                    ConfirmBillFrm cfv = (ConfirmBillFrm)fto.getData();
                                    cfv.receivedDataProcessing(data);
                                    break;
                                }
                            //view.showMessage("Received an object: " + data.getPerformative());
                            }
                    }
                }   
            } catch (Exception e) {
                e.printStackTrace();
                view.showMessage("Error when receiving data from the server!");
                view.resetClient();
            }
        }
    }
}