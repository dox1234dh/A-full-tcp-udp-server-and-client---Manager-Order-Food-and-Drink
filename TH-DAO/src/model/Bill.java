/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import java.io.Serializable;

/**
 *
 * @author Asus
 */
public class Bill implements Serializable{
    private static final long serialVersionUID = 20210811010L;
    private int id;
    private Customer customer;
    private Product product;
    private int quantity;
    
    public Bill(){
        super();
    }

    public Bill(Customer customer, Product product, int quantity) {
        super();
        this.customer = customer;
        this.product = product;
        this.quantity = quantity;
    }

    public Customer getCustomer() {
        return customer;
    }

    public Product getProduct() {
        return product;
    }

    public int getQuantity() {
        return quantity;
    }

    public int getId() {
        return id;
    }
    
    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void setId(int id) {
        this.id = id;
    }
    
    
}
