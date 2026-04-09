package tn.formini.services;


import java.util.List;

public interface service<T> {
        void ajouter(T t);
        void modifier(T t);
        void supprimer(int id);
        List<T> afficher();
    }

