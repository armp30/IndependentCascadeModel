package com.amirmhd.influenceMaximization.example;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;

class DelimiterChooser extends JFrame implements ActionListener {
    JRadioButton rb1, rb2, rb3;
    JButton b;

    char delimiter='1';

    DelimiterChooser(String text) {
        JLabel label = new JLabel(text);
        label.setBounds(100, 10, 100, 30);
        rb1 = new JRadioButton("\' \'");
        rb1.setBounds(100, 50, 100, 30);
        rb2 = new JRadioButton("\'\\t\'");
        rb2.setBounds(100, 100, 100, 30);
        rb3 = new JRadioButton("\',\'");
        rb3.setBounds(100, 150, 100, 30);
        ButtonGroup bg = new ButtonGroup();
        bg.add(rb1);
        bg.add(rb2);
        bg.add(rb3);
        b = new JButton("click");
        b.setBounds(100, 200, 80, 30);
        b.addActionListener(this);
        add(label);
        add(rb1);
        add(rb2);
        add(rb3);
        add(b);
        setSize(300, 400);
        setLayout(null);
        setVisible(true);
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    public void actionPerformed(ActionEvent e) {
        if (rb2.isSelected()) {
            delimiter='\t';
        } else if (rb3.isSelected()) {
            delimiter=',';
        } else {
            delimiter=' ';
        }
        this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
    }

    public char getDelimiter() {
        return delimiter;
    }


}