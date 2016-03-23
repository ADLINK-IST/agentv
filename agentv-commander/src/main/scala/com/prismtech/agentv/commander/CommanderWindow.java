package com.prismtech.agentv.commander;


import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import com.prismtech.agentv.core.types.MicrosvcRepoEntry;
import com.prismtech.agentv.core.types.NodeError;
import com.prismtech.agentv.core.types.NodeInfo;
import com.prismtech.agentv.core.types.RunningMicrosvc;
import io.nuvo.util.log.ConsoleLogger;
import java.util.List;

public class CommanderWindow extends JDialog  implements AgentvEventListener {
    private JPanel contentPane;
    private JList<String> nodeList;
    private JTextField jarName;
    private JButton browseJar;
    private JButton deployButton;
    private JTextField capsuleName;
    private JButton startButton;
    private JButton stopCapsule;
    private JPanel runningAppsPanel;
    private JList<String> pkgslist;
    private JList<String> runninglist;
    private JTextArea errorLog;
    private JTextField argLine;
    private File jarFile;
    private String selectedNodeValue = null;
    private int selectedNodeId = -1;
    private String selectedMicroSvc = null;
    private String selectedRunningMicrosvc = null;
    private ConsoleLogger logger = new ConsoleLogger("CommanderWindow");
    private Commander commander = Commander.apply(this);

    public void setNodeList(String[] ais) {
        int idx = this.nodeList.getSelectedIndex();
        this.nodeList.setListData(ais);
        this.nodeList.setSelectedIndex(idx);
    }

    public void updateRunningList() {
        logger.debug("Updating Running Microsvc List");
        if (selectedNodeValue != null) {
            String selection = this.runninglist.getSelectedValue();
            this.runninglist.setListData(commander.getRunningMicrosvcs(selectedNodeValue));
            if (selection != null)
                this.runninglist.setSelectedValue(selection, true);

        }
    }

    public void updateRepoEntries() {
        if (selectedNodeValue != null) {
            String selection = this.pkgslist.getSelectedValue();
            this.pkgslist.setListData(commander.getInstalledMicrosvcs(selectedNodeValue));
            if (selection != null)
                this.pkgslist.setSelectedValue(selection, true);

        }
    }

    public void addErrorEntry(String error) {
        this.errorLog.append("\n> " + error);
    }

    public CommanderWindow() {
        setContentPane(contentPane);
        setModal(true);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });


// call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        deployButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                logger.debug("jarFile = " + jarFile + ", nodeId = " + selectedNodeValue);

                if (jarFile != null && selectedNodeValue != null) {
                    try {
                        byte[] buf = Files.readAllBytes(jarFile.toPath());
                        commander.deployPackage(selectedNodeValue, jarFile.getName(), buf);

                    } catch (IOException ioe) {
                        ioe.printStackTrace();
                    }
                } else {
                    JOptionPane.showMessageDialog(contentPane, "Please Select a node and a jar to deploy!");
                }
            }
        });
        browseJar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser chooser = new JFileChooser();
                FileNameExtensionFilter filter = new FileNameExtensionFilter("jar files", "jar");
                chooser.setFileFilter(filter);
                int retVal = chooser.showOpenDialog(null);
                if (retVal == JFileChooser.APPROVE_OPTION) {
                    jarFile = chooser.getSelectedFile();
                    jarName.setText(jarFile.getName());
                }
                logger.debug("Selected jarFile = " + jarFile);
            }
        });

        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                if (selectedMicroSvc != null && selectedNodeValue != null) {
                    String as = argLine.getText();
                    String args[] = null;
                    if (as == null)
                        args = new String[0];
                    else
                        args = as.split(" ");

                    commander.startMicrosvc(selectedNodeValue, selectedMicroSvc, args);
                } else {
                    JOptionPane.showMessageDialog(contentPane, "Please Select a node and a microservice!");
                }
            }
        });
        stopCapsule.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (selectedRunningMicrosvc != null) {
                    commander.stopMicrosvc(selectedRunningMicrosvc);
                } else
                    JOptionPane.showMessageDialog(contentPane, "Please Select a node and a running microservice!");
            }
        });
        nodeList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    selectedNodeId = nodeList.getSelectedIndex();
                    selectedNodeValue = (String) nodeList.getSelectedValue();
                    selectedMicroSvc = null;
                    nodeList.setSelectedIndex(nodeList.getSelectedIndex());
                    String nodeId = (String) nodeList.getSelectedValue();
                    String[] microsvcs = commander.getInstalledMicrosvcs(nodeId);
                    updateRepoEntries();
                    updateRunningList();
                }
            }
        });
        runninglist.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting()) {
                    selectedRunningMicrosvc = (String) runninglist.getSelectedValue();
                }
            }
        });

        pkgslist.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting()) {
                    selectedMicroSvc = (String) pkgslist.getSelectedValue();
                }
            }
        });
    }

    private void onOK() {
// add your code here
        dispose();
    }

    private void onCancel() {
// add your code here if necessary
        dispose();
    }

    public static void main(String[] args) {
        CommanderWindow dialog = new CommanderWindow();
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);

    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        contentPane = new JPanel();
        contentPane.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(3, 2, new Insets(10, 10, 10, 10), -1, -1));
        contentPane.setMinimumSize(new Dimension(660, 480));
        contentPane.setPreferredSize(new Dimension(660, 480));
        runningAppsPanel = new JPanel();
        runningAppsPanel.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(3, 1, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(runningAppsPanel, new com.intellij.uiDesigner.core.GridConstraints(0, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        runningAppsPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Installed Microservices"));
        startButton = new JButton();
        startButton.setText("Start");
        runningAppsPanel.add(startButton, new com.intellij.uiDesigner.core.GridConstraints(2, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        argLine = new JTextField();
        argLine.setToolTipText("microservice command line arguments");
        runningAppsPanel.add(argLine, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JScrollPane scrollPane1 = new JScrollPane();
        runningAppsPanel.add(scrollPane1, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        pkgslist = new JList();
        final DefaultListModel defaultListModel1 = new DefaultListModel();
        pkgslist.setModel(defaultListModel1);
        pkgslist.setSelectionMode(0);
        pkgslist.setToolTipText("microsvcs installed on selected node.");
        scrollPane1.setViewportView(pkgslist);
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel1, new com.intellij.uiDesigner.core.GridConstraints(1, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel1.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Running Microservices"));
        stopCapsule = new JButton();
        stopCapsule.setText("Stop");
        panel1.add(stopCapsule, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JScrollPane scrollPane2 = new JScrollPane();
        panel1.add(scrollPane2, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        runninglist = new JList();
        final DefaultListModel defaultListModel2 = new DefaultListModel();
        runninglist.setModel(defaultListModel2);
        runninglist.setSelectionMode(0);
        runninglist.setToolTipText("microsvcs running on selected node.");
        scrollPane2.setViewportView(runninglist);
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel2, new com.intellij.uiDesigner.core.GridConstraints(2, 0, 1, 2, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel2.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Error Log"));
        final JScrollPane scrollPane3 = new JScrollPane();
        panel2.add(scrollPane3, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        errorLog = new JTextArea();
        errorLog.setBackground(new Color(-14606047));
        errorLog.setCaretColor(new Color(-13107432));
        errorLog.setEditable(false);
        errorLog.setForeground(new Color(-13107432));
        errorLog.setLineWrap(true);
        errorLog.setMinimumSize(new Dimension(0, 1024));
        errorLog.setPreferredSize(new Dimension(0, 425));
        errorLog.setRequestFocusEnabled(false);
        errorLog.setRows(25);
        errorLog.setToolTipText("Error Log.");
        errorLog.setWrapStyleWord(false);
        scrollPane3.setViewportView(errorLog);
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel3, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel3.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Discovered Nodes", TitledBorder.LEFT, TitledBorder.TOP));
        final JScrollPane scrollPane4 = new JScrollPane();
        panel3.add(scrollPane4, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        nodeList = new JList();
        final DefaultListModel defaultListModel3 = new DefaultListModel();
        nodeList.setModel(defaultListModel3);
        nodeList.setSelectionMode(0);
        nodeList.setToolTipText("Discovered nodes.");
        scrollPane4.setViewportView(nodeList);
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(2, 2, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel4, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel4.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Deploy Microservice"));
        jarName = new JTextField();
        panel4.add(jarName, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        browseJar = new JButton();
        browseJar.setText("...");
        panel4.add(browseJar, new com.intellij.uiDesigner.core.GridConstraints(0, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        deployButton = new JButton();
        deployButton.setText("Deploy");
        panel4.add(deployButton, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 2, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }

    @Override
    public void onNodeError(NodeError error) {
        logger.log(">> onNodeError");
        this.addErrorEntry(error.nodeId + "(" + error.errno+ "): " + error.msg);
    }

    @Override
    public void onUpdatedRunningMicrosvcs(List<RunningMicrosvc> rms) {
        logger.log(">> onUpdatedRunningMicrosvcs");
        this.updateRunningList();
    }

    @Override
    public void onUpdatedMicrosvcRepository(List<MicrosvcRepoEntry> res) {
        logger.log(">> onUpdatedMicrosvcRepository");

        this.updateRepoEntries();
    }

    @Override
    public void onNodeJoin(NodeInfo n) {
        this.setNodeList(commander.getNodesList());
    }

    @Override
    public void onNodeLeave(NodeInfo n) {
        if (n.uuid.equals(selectedNodeValue)) {
            selectedNodeValue = null;
            final String el[] = new String[0];
            this.pkgslist.setListData(el);
            this.runninglist.setListData(el);
        }
        this.setNodeList(commander.getNodesList());
    }
}
