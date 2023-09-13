package io.github.termitepreston.schoolprojects.model;

@Table(name = "lang")
public class Language extends Named {

    public Language(int id, String name) {
        super(id, name);
    }
}
