package com.nit.controller;

import com.nit.entity.User;
import com.nit.exceptions.UserNotFoundException;
import com.nit.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.security.Principal;
import java.util.List;

@Controller
public class UserController {

    //Field injection
    @Autowired
    public UserRepository userRepository;

    @Autowired
    public PasswordEncoder passwordEncoder;

    @Autowired
    public AuthenticationManager authenticationManager;

    //Endpoint to load the register page
    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("userObj", new User());
        //returning the register page
        return "Register";
    }

    //Endpoint to handel the register page
    @PostMapping("/register")
    public String handelRegister(Model model, @ModelAttribute("userObj") User user) {

//       model.addAttribute("userObj", new User());
        //Encoding the password
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        //Saving the user in the database
        userRepository.save(user);

        model.addAttribute("msg", "User registered successfully please login");
        //returning the login page
        return "/login";
    }

    //Endpoint to load the login page
    @GetMapping("/login")
    public String showLoginForm(Model model) {
        model.addAttribute("userObj", new User());
        //returning the login page
        return "login";
    }

    //Endpoint to handel the login page
    @PostMapping("/login")
    public String login(Model model, @ModelAttribute("userObj") User user) {

        //Taking authentication token
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword());
        try {
            //Authenticating the user
            Authentication authentication = authenticationManager.authenticate(token);
            //Condition to check user is authenticated or not
            if (authentication.isAuthenticated()) {
                System.out.println("Logged in successfully");
                //returning the dashboard page
                return "/dashboard";
            }
        } catch (Exception e) {
            //If exception occurs
            System.out.println("Login failed: " + e.getMessage());
        }
        //If username or password is incorrect adding Invalid user (string) as a object in model class
        model.addAttribute("msg", "Invalid Credentials");
        //returning the login page
        return "login";
    }

    //Endpoint to get all the users
    @GetMapping("/allUsers")
    public String showAllUsers(Model model) {
        model.addAttribute("users", new User());

        //Fetching list og all the users
        List<User> userList =  userRepository.findAll();

        model.addAttribute("list", userList);

        //returning the all users on page
        return "allUsers";
    }

    //Endpoint to load the dashboard
    @GetMapping("/dashboard")
    public String showDashboard(Model model) {
        //returning the dashboard page
        return "dashboard";
    }

    //Endpoint to load the user data on edit page
    @GetMapping("/edit")
    public String editUser(@RequestParam("id") Long id, Principal principal, Model model) {

        //Taking principle as an argument to the method we can access the currently logged-in user
        User loggedInUser = userRepository.findByUsername(principal.getName());

        //If it is admin
        if (loggedInUser.getRole().equalsIgnoreCase("admin")) {
            User user = userRepository.findById(id).orElseThrow(UserNotFoundException::new);
            model.addAttribute("userObj", user);
            return "edit";
        }
        model.addAttribute("message", "Only Admin can update or delete users.");
        return "message";
    }

    //Endpoint to update the user details
    @PostMapping("/edit")
    public String editUser(@ModelAttribute("userObj") User user, RedirectAttributes redirectAttributes) {

        User existingUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + user.getId()));

        existingUser.setUsername(user.getUsername());
        existingUser.setPassword(passwordEncoder.encode(user.getPassword()));
        existingUser.setRole(user.getRole());
        existingUser.setEmail(user.getEmail());
        System.out.println(user.getUsername());

        userRepository.save(existingUser);
        redirectAttributes.addFlashAttribute("smsg", "User updated successfully");

        return "redirect:/edit?id=" + user.getId();
    }

    //Endpoint to delete the user
    @PostMapping("/deleteUser")
    public String deleteUser(@RequestParam("id") Long id, RedirectAttributes redirectAttributes, Principal principal) {

        User loggedInUser = userRepository.findByUsername(principal.getName());

        if (loggedInUser.getRole().equalsIgnoreCase("admin")) {
            userRepository.findById(id).ifPresentOrElse(
                    userRepository::delete,
                    () -> redirectAttributes.addFlashAttribute("errorMsg", "User not found!")
            );
            redirectAttributes.addFlashAttribute("smsg", "User deleted successfully.");
            return "redirect:/allUsers";
        }

        redirectAttributes.addFlashAttribute("errorMsg", "Only Admins can delete users.");
        return "redirect:/allUsers";
    }

}
