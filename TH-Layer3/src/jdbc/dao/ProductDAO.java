/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdbc.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import static jdbc.dao.DAO.con;
import model.Product;

/**
 *
 * @author Asus
 */
public class ProductDAO extends DAO{
    public boolean addProduct(Product product){
        boolean result = false;
        String sql = "INSERT INTO tblproduct(name, unity, price, description) VALUES(?,?,?,?)";
        try{
            PreparedStatement ps = con.prepareStatement(sql,
                              Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, product.getName());
            ps.setString(2, product.getUnity());
            ps.setDouble(3, product.getPrice());
            ps.setString(4, product.getDescription());
 
            ps.executeUpdate();
             
            //get id of the new inserted client
            ResultSet generatedKeys = ps.getGeneratedKeys();
            if (generatedKeys.next()) {
                product.setId(generatedKeys.getInt(1));
            }
            result = true;
        }catch(Exception e){
            e.printStackTrace();
        }
        return result;
    }
    public ArrayList<Product> searchProduct(String key){
        ArrayList<Product> result = new ArrayList<Product>();
        String sql = "SELECT * FROM tblproduct WHERE name LIKE ?";
        try{
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, "%" + key + "%");
            ResultSet rs = ps.executeQuery();
 
            while(rs.next()){
                Product product = new Product();
                product.setId(rs.getInt("id"));
                product.setName(rs.getString("name"));
                product.setUnity(rs.getString("unity"));
                product.setPrice(Double.parseDouble(rs.getString("price")));
                product.setDescription(rs.getString("description"));
                result.add(product);
            }
        }catch(Exception e){
            e.printStackTrace();
        }   
        return result;
    }
}
