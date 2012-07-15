package com.javafx.experiments.scenicview.utils;

import com.javafx.experiments.scenicview.images.ui.Images;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;

public class ClassPathDialog extends JFrame {
    
    private static final int PADDING = 10;
    private static final Color INVALID_COLOR = new Color(246, 157, 160);
    private static final Color VALID_COLOR = new Color(188,222,172);
    
    private static final String TOOLS_JAR_TOOLTIP = 
            "<html>The tools.jar file is located within your Java JDK folder.<br/><br/>" +
            "<b>For example</b>: a common location for the tools.jar file in a Java 7u5 install is:<br/>" +
            "C:\\Program Files (x86)\\Java\\jdk1.7.0_05\\lib\\tools.jar";
    
    private static final String JFXRT_JAR_TOOLTIP = 
            "<html>The jfxrt.jar file is located within your JavaFX runtime folder.<br/><br/>" +
            "<b>For example</b>: a common location for the jfxrt.jar file in a JavaFX install is:<br/>" +
            "rt\\lib";
    
    private final ImageIcon buttonImage = new ImageIcon(Images.class.getResource("mglass.gif"));
    
    private final JTextField toolsField;
    private final JTextField jfxField;
    
    private final JButton actionButton;
    
    private PathChangeListener pathChangeListener;
    
    public ClassPathDialog(final String toolsPath, final String jfxPath, boolean isBootTime) {
        // install a native look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(ClassPathDialog.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            Logger.getLogger(ClassPathDialog.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(ClassPathDialog.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnsupportedLookAndFeelException ex) {
            Logger.getLogger(ClassPathDialog.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setTitle("Scenic View :: Required Libraries");
        setLayout(new BorderLayout());
        setIconImage(buttonImage.getImage());
        
        JPanel outerPanel = new JPanel(new BorderLayout());
        outerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(PADDING, PADDING, PADDING, PADDING), 
                BorderFactory.createCompoundBorder(
                    BorderFactory.createTitledBorder("Required Libraries"),
                    BorderFactory.createEmptyBorder(PADDING, PADDING, PADDING, PADDING))));
        
        outerPanel.add(new JLabel("Please find the following jar files on your system:"), BorderLayout.NORTH);

        actionButton = new JButton(isBootTime ? "Launch Scenic View" : "Save");

        final JPanel form = new JPanel();
        form.setLayout(new GridBagLayout());
        final GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 5, 5, 5);
        c.gridx = 0;
        c.gridwidth = 2;
        JLabel label = new JLabel("tools.jar classpath:");
        label.setToolTipText(TOOLS_JAR_TOOLTIP);
        form.add(label, c);
        
        toolsField = new JTextField();
        toolsField.setToolTipText(TOOLS_JAR_TOOLTIP);
        toolsField.setEditable(false);
        toolsField.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                show("tools.jar", toolsField);
            }
        });
        toolsField.setPreferredSize(new Dimension(300, 25));
        toolsField.setText(toolsPath);
        c.gridx = 2;
        c.gridwidth = 5;
        form.add(toolsField, c);
        final JButton toolsChange = new JButton(buttonImage);
        toolsChange.setToolTipText(TOOLS_JAR_TOOLTIP);
        toolsChange.addActionListener(new ActionListener() {
            @Override public void actionPerformed(final ActionEvent e) {
                show("tools.jar", toolsField);
            }
        });
        c.gridx = 7;
        c.gridwidth = 1;
        form.add(toolsChange, c);
        c.gridwidth = 2;
        c.gridx = 0;
        c.gridy = 1;
        label = new JLabel("jfxrt.jar classpath:");
        label.setToolTipText(JFXRT_JAR_TOOLTIP);
        form.add(label, c);
        c.gridx = 2;
        c.gridwidth = 5;

        jfxField = new JTextField();
        jfxField.setEditable(false);
        jfxField.setToolTipText(JFXRT_JAR_TOOLTIP);
        jfxField.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                show("jfxrt.jar", jfxField);
            }
        });
        jfxField.setText(jfxPath);
        jfxField.setPreferredSize(new Dimension(300, 25));
        form.add(jfxField, c);
        final JButton jfxChange = new JButton(buttonImage);
        jfxChange.setToolTipText(JFXRT_JAR_TOOLTIP);
        jfxChange.addActionListener(new ActionListener() {
            @Override public void actionPerformed(final ActionEvent e) {
                show("jfxrt.jar", jfxField);
            }
        });
        c.gridx = 7;
        c.gridwidth = 1;
        form.add(jfxChange, c);
        outerPanel.add(form, BorderLayout.CENTER);

        actionButton.addActionListener(new ActionListener() {
            @Override public void actionPerformed(final ActionEvent e) {
                HashMap<String, String> map = new HashMap<String, String>();
                map.put(PathChangeListener.TOOLS_JAR_KEY, toolsField.getText());
                map.put(PathChangeListener.JFXRT_JAR_KEY, jfxField.getText());
                pathChangeListener.onPathChanged(map);
            }
        });
        checkValid();
        outerPanel.add(actionButton, BorderLayout.SOUTH);
        
        add(outerPanel, BorderLayout.CENTER);
        setSize(600, 220);
        setResizable(false);
        setLocationRelativeTo(null);
    }
    
    public void setPathChangeListener(final PathChangeListener listener) {
        this.pathChangeListener = listener;
    }
    
    private void show(final String desiredFile, final JTextField textField) {
        final JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileFilter() {
            @Override public String getDescription() {
                return desiredFile;
            }

            @Override public boolean accept(final File f) {
                return f.getName().equals(desiredFile) || f.isDirectory();
            }
        });
        
        final int option = fileChooser.showOpenDialog(ClassPathDialog.this);
        if (option == JFileChooser.APPROVE_OPTION) {
            textField.setText(fileChooser.getSelectedFile().getAbsolutePath());
            checkValid();
        }
    }

    private void checkValid() {
        boolean toolsJarExists = new File(toolsField.getText()).exists();
        boolean javafxJarExists = new File(jfxField.getText()).exists();
        actionButton.setEnabled(toolsJarExists && javafxJarExists);
        
        // update the UI to indicate whether the selected paths are valid
        toolsField.setBackground(toolsJarExists ? VALID_COLOR : INVALID_COLOR);
        jfxField.setBackground(javafxJarExists ? VALID_COLOR : INVALID_COLOR);
    }
}
