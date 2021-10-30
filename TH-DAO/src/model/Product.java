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
public class Product implements Serializable{
    private static final long serialVersionUID = 20210811004L;
    private int id;
    private String name;
    private String unity;
    private double price;
    private String description;
     
    public Product() {
        super();
    }

    public Product(String name, String unity, double price, String description) {
        super();
        this.name = name;
        this.unity = unity;
        this.price = price;
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getUnity() {
        return unity;
    }

    public double getPrice() {
        return price;
    }

    public String getDescription() {
        return description;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setUnity(String unity) {
        this.unity = unity;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    
    
}
