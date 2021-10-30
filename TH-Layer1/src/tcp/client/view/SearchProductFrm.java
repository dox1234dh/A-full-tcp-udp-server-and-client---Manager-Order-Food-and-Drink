package tcp.client.view;
 
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
 
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import model.Bill;
 
import model.Customer;
import model.ObjectWrapper;
import model.Product;
import tcp.client.control.ClientCtr;
 
public class SearchProductFrm extends JFrame implements ActionListener{
    private ArrayList<Product> listProduct;
    private JTextField txtKey;
    private JButton btnSearch,btnPrint;
    private JTable tblResult;
    private ClientCtr mySocket;
    private Product product;
    private ArrayList<Bill> listBill = new ArrayList<Bill>();
    public SearchProductFrm(ClientCtr socket, Customer customer){
        super("Search product to selling");
        SearchProductFrm parent = this;
        mySocket = socket;
        listProduct = new ArrayList<Product>();
         
        JPanel pnMain = new JPanel();
        pnMain.setSize(this.getSize().width-5, this.getSize().height-20);       
        pnMain.setLayout(new BoxLayout(pnMain,BoxLayout.Y_AXIS));
        pnMain.add(Box.createRigidArea(new Dimension(0,10)));
         
        JLabel lblHome = new JLabel("Search a product to buying");
        lblHome.setAlignmentX(Component.CENTER_ALIGNMENT);  
        lblHome.setFont (lblHome.getFont ().deriveFont (20.0f));
        pnMain.add(lblHome);
        pnMain.add(Box.createRigidArea(new Dimension(0,20)));
         
        JPanel pn1 = new JPanel();
        pn1.setLayout(new BoxLayout(pn1,BoxLayout.X_AXIS));
        pn1.setSize(this.getSize().width-5, 20);
        pn1.add(new JLabel("Product name: "));
        txtKey = new JTextField();
        pn1.add(txtKey);
        btnSearch = new JButton("Search");
        btnSearch.addActionListener(this);
        pn1.add(btnSearch);
        btnPrint = new JButton("Print");
        btnPrint.addActionListener(this);
        pn1.add(btnPrint);
        pnMain.add(pn1);
        pnMain.add(Box.createRigidArea(new Dimension(0,10)));
 
        JPanel pn2 = new JPanel();
        pn2.setLayout(new BoxLayout(pn2,BoxLayout.Y_AXIS));     
        tblResult = new JTable();
        JScrollPane scrollPane= new  JScrollPane(tblResult);
        tblResult.setFillsViewportHeight(false); 
        scrollPane.setPreferredSize(new Dimension(scrollPane.getPreferredSize().width, 250));
        
        tblResult.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int column = tblResult.getColumnModel().getColumnIndexAtX(e.getX()); // get the coloum of the button
                int row = e.getY() / tblResult.getRowHeight(); // get the row of the button
 
                // *Checking the row or column is valid or not
                if (row < tblResult.getRowCount() && row >= 0 && column < tblResult.getColumnCount() && column >= 0) {
                    //search and delete all existing previous view
//                    ObjectWrapper existed = null;
//                    for(ObjectWrapper func: mySocket.getActiveFunction())
//                        if(func.getData() instanceof SearchProductFrm) {
//                            ((SearchProductFrm)func.getData()).dispose();
//                            existed = func;
//                        }
//                    if(existed != null)
//                        mySocket.getActiveFunction().remove(existed);
                     
                    //create new instance
                    product = listProduct.get(row);
                    BillFrm bill = (new BillFrm(mySocket, product, customer,parent));
                    bill.setVisible(true);
//                    listBill.add(bill.getBill());
//                    System.out.println(listBill.size() + listBill.get(listBill.size()-1).getQuantity());
                }
            }
        });

        pn2.add(scrollPane);
        pnMain.add(pn2);    
        this.add(pnMain);
        this.setSize(600,300);              
        this.setLocation(200,10);
        this.setVisible(true);
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        mySocket.getActiveFunction().add(new ObjectWrapper(ObjectWrapper.REPLY_SEARCH_PRODUCT, this));
    }
 
    @Override
    public void actionPerformed(ActionEvent e) {
        // TODO Auto-generated method stub
        JButton btnClicked = (JButton)e.getSource();
        if(btnClicked.equals(btnSearch)){
            if((txtKey.getText() == null)||(txtKey.getText().length() == 0))
                return;
            //send data to the server
            mySocket.sendData(new ObjectWrapper(ObjectWrapper.SEARCH_PRODUCT_BY_NAME, txtKey.getText().trim()));
        }
        if(btnClicked.equals(btnPrint)){
            for(ObjectWrapper func: mySocket.getActiveFunction())
                    if(func.getData() instanceof ConfirmBillFrm) {
                        ((ConfirmBillFrm)func.getData()).setVisible(true);
                        return;
                    }
                ConfirmBillFrm cbv = new ConfirmBillFrm(mySocket,listBill);
                cbv.setVisible(true);
                this.dispose();
        }
    }
     
    /**
     * Treatment of search result received from the server
     * @param data
     */
    public void receivedDataProcessing(ObjectWrapper data) {
        if(data.getData() instanceof ArrayList<?>) {
            listProduct = (ArrayList<Product>)data.getData();
 
            String[] columnNames = {"Id", "Name", "Unity", "Price", "Description"};
            String[][] value = new String[listProduct.size()][columnNames.length];
            for(int i=0; i<listProduct.size(); i++){
                value[i][0] = listProduct.get(i).getId() +"";
                value[i][1] = listProduct.get(i).getName();
                value[i][2] = listProduct.get(i).getUnity();
                value[i][3] = listProduct.get(i).getPrice() +"";
                value[i][4] = listProduct.get(i).getDescription();
            }
            DefaultTableModel tableModel = new DefaultTableModel(value, columnNames) {
                @Override
                public boolean isCellEditable(int row, int column) {
                   //unable to edit cells
                   return false;
                }
            };
            tblResult.setModel(tableModel);
        }else {
            tblResult.setModel(null);
        }
    }
    public void addBill(Bill bill){
        listBill.add(bill);
        for(Bill func: listBill)
            System.out.println(func.getProduct().getName() +" "+ func.getQuantity());
    }

}