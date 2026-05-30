package com.example.tp1.security;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Controller
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    // Register API (Postman)
    @PostMapping("/register")
    @ResponseBody
    public String register(@RequestBody User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole("USER");
        userRepository.save(user);
        return "Utilisateur créé !";
    }

    // Login API (Postman)
    @PostMapping("/login")
    @ResponseBody
    public Map<String, String> login(@RequestBody User user) {
        User found = userRepository.findByUsername(user.getUsername())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé !"));
        if (!passwordEncoder.matches(user.getPassword(), found.getPassword())) {
            throw new RuntimeException("Mot de passe incorrect !");
        }
        String token = jwtUtil.generateToken(found.getUsername());
        return Map.of("token", token);
    }

    // Afficher page login
    @GetMapping("/login-form")
    public String loginForm(Model model) {
        return "login";
    }

    // Traiter le login via formulaire
    @PostMapping("/login-form")
    public String loginFormPost(@RequestParam String username,
                                @RequestParam String password,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        try {
            User found = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé !"));

            if (!passwordEncoder.matches(password, found.getPassword())) {
                redirectAttributes.addFlashAttribute("error", "Mot de passe incorrect !");
                return "redirect:/auth/login-form";
            }

            // Sauvegarde username ET role dans la session
            session.setAttribute("username", found.getUsername());
            session.setAttribute("role", found.getRole());
            session.setMaxInactiveInterval(3600);

            return "redirect:/auth/dashboard";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Utilisateur non trouvé !");
            return "redirect:/auth/login-form";
        }
    }

    // Afficher page register
    @GetMapping("/register-form")
    public String registerForm() {
        return "register";
    }

    // Traiter le register via formulaire
    @PostMapping("/register-form")
    public String registerFormPost(@RequestParam String username,
                                   @RequestParam String password,
                                   @RequestParam String role,
                                   RedirectAttributes redirectAttributes) {
        try {
            User user = new User();
            user.setUsername(username);
            user.setPassword(passwordEncoder.encode(password));
            user.setRole(role);
            userRepository.save(user);
            redirectAttributes.addFlashAttribute("success", "Compte créé ! Connectez-vous.");
            return "redirect:/auth/login-form";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la création !");
            return "redirect:/auth/register-form";
        }
    }

    // Dashboard après login
    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        String username = (String) session.getAttribute("username");
        String role = (String) session.getAttribute("role");
        if (username == null) return "redirect:/auth/login-form";
        model.addAttribute("username", username);
        model.addAttribute("role", role);
        return "dashboard";
    }

    // Logout
    @GetMapping("/logout-form")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/auth/login-form";
    }
}