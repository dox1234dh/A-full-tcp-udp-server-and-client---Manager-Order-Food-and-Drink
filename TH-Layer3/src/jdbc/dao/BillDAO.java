/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdbc.dao;

import java.util.ArrayList;
import model.Bill;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 *
 * @author Asus
 */
public class BillDAO extends DAO{
    public boolean addProduct(ArrayList<Bill> listBill){
        double totalbill=0;
        Bill bill = new Bill();
        for(int i=0;i<listBill.size();++i){
            totalbill += listBill.get(i).getQuantity()*listBill.get(i).getProduct().getPrice();
        }
        boolean result = false;
        String sql = "INSERT INTO tblorder(idclient, totalproduct) VALUES(?,?)";
        try{
            PreparedStatement ps = con.prepareStatement(sql,
                              Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, listBill.get(0).getCustomer().getId());
            ps.setDouble(2, totalbill);
 
            ps.executeUpdate();
             
            //get id of the new inserted client
            ResultSet generatedKeys = ps.getGeneratedKeys();
            if (generatedKeys.next()) {
                bill.setId(generatedKeys.getInt(1));
                System.out.println("ok");
            }
            System.out.println(bill.getId());
            result = true;
        }catch(Exception e){
            e.printStackTrace();
        }
        return result;
    }
}
