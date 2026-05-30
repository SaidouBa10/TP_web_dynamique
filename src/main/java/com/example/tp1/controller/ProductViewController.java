package com.example.tp1.controller;

import com.example.tp1.model.Product;
import com.example.tp1.service.ProductService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class ProductViewController {

    @Autowired
    private ProductService productService;

    @GetMapping("/products")
    public String products(Model model, HttpSession session) {
        model.addAttribute("products", productService.getAllProducts());
        model.addAttribute("role", session.getAttribute("role"));
        return "products";
    }

    @PostMapping("/products/add")
    public String addProduct(@ModelAttribute Product product) {
        productService.createProduct(product);
        return "redirect:/products";
    }

    @GetMapping("/products/delete/{id}")
    public String deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return "redirect:/products";
    }
}