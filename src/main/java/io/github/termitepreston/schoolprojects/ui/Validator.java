package io.github.termitepreston.schoolprojects.ui;

import java.util.function.Predicate;

record Validator(Predicate<Object> predicate, String message) {

}
