package com.example.tp1.controller;

import com.example.tp1.model.Article;
import com.example.tp1.model.Comment;
import com.example.tp1.service.ArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class ArticleViewController {

    @Autowired
    private ArticleService articleService;

    @GetMapping("/articles")
    public String articles(Model model) {
        model.addAttribute("articles", articleService.getAllArticles());
        return "articles";
    }

    @GetMapping("/articles/{id}")
    public String articleDetail(@PathVariable Long id, Model model) {
        articleService.getArticleById(id).ifPresent(a -> model.addAttribute("article", a));
        return "article-detail";
    }

    @PostMapping("/articles/add")
    public String addArticle(@ModelAttribute Article article) {
        articleService.createArticle(article);
        return "redirect:/articles";
    }

    @PostMapping("/articles/{id}/comments/add")
    public String addComment(@PathVariable Long id,
                             @RequestParam String author,
                             @RequestParam String text) {
        Comment comment = new Comment();
        comment.setAuthor(author);
        comment.setText(text);
        articleService.addComment(id, comment);
        return "redirect:/articles/" + id;
    }

    @GetMapping("/articles/delete/{id}")
    public String deleteArticle(@PathVariable Long id) {
        articleService.deleteArticle(id);
        return "redirect:/articles";
    }
}