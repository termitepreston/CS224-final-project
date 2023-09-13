package io.github.termitepreston.schoolprojects.ui;

import javax.swing.*;

record Attribute(Validator[] validators, JLabel label, JLabel helper,
                 JLabel error) {
}
