package com.javafx.experiments.scenicview.utils;

import java.awt.*;
import java.awt.event.*;
import java.io.File;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.JTextComponent;

public class ClassPathDialog extends JDialog {

    public ClassPathDialog(final PathChangeListener listener, final String toolsPath, final String jfxPath) {
        setLayout(new BorderLayout());
        add(new JLabel("Please check/fill the following classpath entries"), BorderLayout.NORTH);

        final JButton launch = new JButton("Launch ScenicView");
        final JTextArea textArea = new JTextArea();

        final JPanel form = new JPanel();
        form.setLayout(new GridBagLayout());
        final GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 5, 5, 5);
        c.gridx = 0;
        c.gridwidth = 2;
        form.add(new JLabel("Tools.jar classpath:"), c);
        final JFileChooser toolsChooser = new JFileChooser();
        toolsChooser.setFileFilter(new FileFilter() {

            @Override public String getDescription() {
                return "tools.jar";
            }

            @Override public boolean accept(final File f) {
                return f.getName().equals("tools.jar") || f.isDirectory();
            }
        });
        final JTextField toolsField = new JTextField();
        toolsField.setPreferredSize(new Dimension(300, 25));
        toolsField.setText(toolsPath);
        c.gridx = 2;
        c.gridwidth = 5;
        form.add(toolsField, c);
        final JTextField jfxField = new JTextField();
        final JButton toolsChange = new JButton("Change");
        toolsChange.addActionListener(new ActionListener() {

            @Override public void actionPerformed(final ActionEvent e) {
                final int option = toolsChooser.showOpenDialog(ClassPathDialog.this);
                if (option == JFileChooser.APPROVE_OPTION) {
                    toolsField.setText(toolsChooser.getSelectedFile().getAbsolutePath());
                    checkValid(launch, toolsField, jfxField, textArea);
                }
            }
        });
        c.gridx = 7;
        c.gridwidth = 1;
        form.add(toolsChange, c);
        c.gridwidth = 2;
        c.gridx = 0;
        c.gridy = 1;
        form.add(new JLabel("jfxrt.jar classpath:"), c);
        c.gridx = 2;
        c.gridwidth = 5;
        final JFileChooser jfxChooser = new JFileChooser();
        jfxChooser.setFileFilter(new FileFilter() {

            @Override public String getDescription() {
                return "jfxrt.jar";
            }

            @Override public boolean accept(final File f) {
                return f.getName().equals("jfxrt.jar") || f.isDirectory();
            }
        });

        jfxField.setText(jfxPath);
        jfxField.setPreferredSize(new Dimension(300, 25));
        form.add(jfxField, c);
        final JButton jfxChange = new JButton("Change");
        jfxChange.addActionListener(new ActionListener() {

            @Override public void actionPerformed(final ActionEvent e) {
                final int option = jfxChooser.showOpenDialog(ClassPathDialog.this);
                if (option == JFileChooser.APPROVE_OPTION) {
                    jfxField.setText(jfxChooser.getSelectedFile().getAbsolutePath());
                    checkValid(launch, toolsField, jfxField, textArea);
                }
            }
        });
        c.gridx = 7;
        c.gridwidth = 1;
        form.add(jfxChange, c);
        add(form, BorderLayout.CENTER);

        final JPanel bottom = new JPanel();
        bottom.setLayout(new GridLayout(2, 1));

        makeMultilineLabel(textArea);
        bottom.add(textArea);

        launch.addActionListener(new ActionListener() {

            @Override public void actionPerformed(final ActionEvent e) {
                listener.onPathChanged(toolsField.getText(), jfxField.getText());
            }
        });
        checkValid(launch, toolsField, jfxField, textArea);
        bottom.add(launch);
        add(bottom, BorderLayout.SOUTH);
        setSize(600, 200);
    }

    private void checkValid(final JButton launch, final JTextField tools, final JTextField jfx, final JTextArea textArea) {
        launch.setEnabled(new File(tools.getText()).exists() && new File(jfx.getText()).exists());
        final boolean windows = System.getProperty("os.name").toLowerCase().indexOf("windows") != -1;
        final char separator = windows ? ';' : ':';
        textArea.setText("This dialog will not be shown if you launch ScenicView with this command:\njava -cp=ScenicView.jar" + separator + "\"" + tools.getText() + "\"" + separator + "\"" + jfx.getText() + "\"");
    }

    private static void makeMultilineLabel(final JTextComponent area) {
        area.setFont(UIManager.getFont("Label.font"));
        area.setEditable(false);
        area.setOpaque(false);
        if (area instanceof JTextArea) {
            ((JTextArea) area).setWrapStyleWord(true);
            ((JTextArea) area).setLineWrap(true);
        }
    }

    interface PathChangeListener {
        public void onPathChanged(String toolsPath, String jfxPath);
    }

    public static void main(final String[] args) {
        final PathChangeListener l = new PathChangeListener() {

            @Override public void onPathChanged(final String toolsPath, final String jfxPath) {
                // TODO Auto-generated method stub

            }
        };
        new ClassPathDialog(l, "c:\\Archivos de programa\\java\\jdk", "c:\\Archivos de programa\\java\\jdk").setVisible(true);
    }

}
