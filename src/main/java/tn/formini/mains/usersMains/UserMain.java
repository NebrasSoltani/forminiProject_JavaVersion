package tn.formini.mains.usersMains;

import tn.formini.entities.Users.User;
import tn.formini.services.UsersService.UserService;

import java.util.Date;

public class UserMain {
    public static void main(String[] args) {
        UserService us = new UserService();

        User u1 = new User();
        u1.setEmail("test@formini.tn");
        u1.setRoles("[]");
        u1.setPassword("Password123");
        u1.setNom("Soltani");
        u1.setPrenom("Nebras");
        u1.setTelephone("22111333");
        u1.setRole_utilisateur("admin");
        u1.setDate_naissance(new Date());

        System.out.println("--- Ajout d'un utilisateur ---");
        us.ajouter(u1);

        System.out.println("--- Liste des utilisateurs ---");
        us.afficher().forEach(System.out::println);
    }
}
