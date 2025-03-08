package com.hydrospark.hydrospark.controllers;

import com.hydrospark.hydrospark.configuration.EmailService;
import com.hydrospark.hydrospark.entities.Product;
import com.hydrospark.hydrospark.entities.SubProducts;
import com.hydrospark.hydrospark.entities.User;
import com.hydrospark.hydrospark.repositories.ProductRepo;
import com.hydrospark.hydrospark.repositories.SubProdRepo;
import com.hydrospark.hydrospark.repositories.UserRepo;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;
import javax.mail.*;
import javax.mail.internet.*;
import javax.sql.rowset.serial.SerialBlob;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.*;

@Controller
@RequestMapping("/product")
public class Products {

    @Autowired
    private ProductRepo productRepo;

    @Autowired
    private UserRepo userRepo;


    @Autowired
    private SubProdRepo subProdRepo;

    @Autowired
    private EmailService emailService;

    @GetMapping("productdetails/{prodName}")
    public String getProducts(@PathVariable String prodName, Model model, HttpSession session) throws SQLException {
        if(session.getAttribute("user")==null && session.getAttribute("employee")==null){
            String redirectURL="product/productdetails/"+prodName;
            session.setAttribute("redirectURL",redirectURL);
            return "redirect:/signin";
        }
//        session.setAttribute("mainProductName",productRepo);
//        List<SubProducts> subProducts=productRepo.findSubProduct(prodName);
//        System.out.println(subProducts.get(0).getSubTypeName());
//        List<Map<String, String>> base64Images = new ArrayList<>();
//
//        for (SubProducts subProduct : subProducts) {
//            Blob blob = new SerialBlob(subProduct.getProduct().getProdImg());
//            byte[] bytes = blob.getBytes(1, (int) blob.length());
//            String base64Image = Base64.getEncoder().encodeToString(bytes);
//            Map<String, String> prodDetails = new HashMap<>();
//            prodDetails.put("img", base64Image);
//            prodDetails.put("prodName", subProduct.getSubTypeName());
//            prodDetails.put("suburl", "/product/productdescription/" + subProduct.getSubTypeName());
//
//            base64Images.add(prodDetails);
//        }
//
//        model.addAttribute("productDetails", base64Images);  // Send the list of product details to the model
        return "proddetails";  // Thymeleaf template to render
    }

    @GetMapping("productdescription/{subtype}")
    public String getSubType(@PathVariable String subtype,Model model,HttpSession session) throws SQLException {
        if(session.getAttribute("user")==null && session.getAttribute("employee")==null){
            String redirectURL="product/productdescription/"+subtype;
            session.setAttribute("redirectURL",redirectURL);
            return "redirect:/signin";
        }
        session.setAttribute("subProductName",subtype);
        List<SubProducts> subProducts=subProdRepo.findSubProductByName(subtype);
        List<Map<String,String>> base64Images = new ArrayList<>();
        System.out.println(subProducts);
        for(SubProducts subProd:subProducts){
            System.out.println(subProd.getSubTypeName());
            var prdImg=subProd.getSubProdImg();
            if(prdImg==null){
                prdImg=subProd.getProduct().getProdImg();
            }
            Blob blob = new SerialBlob(prdImg);
            byte[] bytes = blob.getBytes(1, (int) blob.length());
            String base64Image = Base64.getEncoder().encodeToString(bytes);
            Map<String,String> prodDetails=new HashMap<>();
            System.out.println(prdImg);
            prodDetails.put("img",base64Image);
            prodDetails.put("prodName",subProd.getSubTypeName());
            prodDetails.put("description",subProd.getDescription());
            prodDetails.put("enquiry","/product/enquiry/"+subProd.getSubTypeName());

//            prodDetails.put("url","/admin/productdetails/"+subProd.getSubTypeName());
            base64Images.add(prodDetails);
        }
        model.addAttribute("product",base64Images);
        return "productDiscription";


    }

    @GetMapping("/enquiry")
    public String prodEnquiry(HttpSession session){
        String product= (String) session.getAttribute("subProductName");
        if (product==null){
            return "redirect:/";
        }
        System.out.println(session.getAttribute("mainProductName"));
        String[] prodArray=product.split("/");
        product=prodArray[prodArray.length-1];
        String user=(String) session.getAttribute("user");
        User userDetails=userRepo.findByEmail(user).get(0);
        System.out.println(product+" "+userDetails.email);
        String redirectURL="product/productdescription/"+product;
        session.setAttribute("redirectURL",redirectURL);
        String subject="Inquiry!! About"+ product;
        String body = "Hello, I am " + userDetails.email + " inquiring about the product " + product + ".\n"
                + "May I get some details about it?\n\n"
                + "Inquirer email: " + userDetails.email + "\n"
                + "Required product: " + product + "\n\n"
                + "Thanks and regards,\n"
                + userDetails.email;
        emailService.sendEmail(session,userDetails.email,subject,body);

        return "redirect:/signin";
    }

    @GetMapping("/{query}")
    public String pp(@PathVariable  String query,Model model) throws SQLException {
        List<Product> products=List.of(productRepo.findByName(query));
        List<Map<String,String>> base64Images = new ArrayList<>();
        for(Product prod:products){
            Blob blob = new SerialBlob(prod.getProdImg());
            byte[] bytes = blob.getBytes(1, (int) blob.length());
            String base64Image = Base64.getEncoder().encodeToString(bytes);
            Map<String,String> prodDetails=new HashMap<>();
            prodDetails.put("img",base64Image);
            prodDetails.put("prodName",prod.getProductName());
            prodDetails.put("url","/product/productdetails/"+prod.getProductName());
            base64Images.add(prodDetails);
        }

        model.addAttribute("product", base64Images);
        System.out.println("base64Images Hereeeeeeeeeee");
        if (base64Images.size()==0){
            System.out.println("Noooooooooooooooo");
            model.addAttribute("error","No Product found with name");
            return "redirect:/admin/error/";
        }
        return "searchProduct.html";
    }

}
