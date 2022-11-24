package ru.miqqra;

/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

import com.cburch.logisim.circuit.CircuitState;
import com.cburch.logisim.gui.hex.HexFile;
import com.cburch.logisim.gui.hex.HexFrame;
import com.cburch.logisim.instance.Instance;
import com.cburch.logisim.proj.Project;
import com.cburch.logisim.tools.MenuExtender;

class BankedMemMenu implements ActionListener, MenuExtender {
    private BankedMem factory;
    private Instance instance;
    private Project proj;
    private Frame frame;
    private CircuitState circState;
    private JMenuItem edit;
    private JMenuItem clear;
    private JMenuItem load;
    private JMenuItem save;

    BankedMemMenu(BankedMem factory, Instance instance) {
        this.factory = factory;
        this.instance = instance;
    }

    public void configureMenu(JPopupMenu menu, Project proj) {
        this.proj = proj;
        this.frame = proj.getFrame();
        this.circState = proj.getCircuitState();

        Object attrs = instance.getAttributeSet();
        if (attrs instanceof BankedRomAttributes) {
            ((BankedRomAttributes) attrs).setProject(proj);
        }

        boolean enabled = circState != null;
        edit = createItem(enabled, BankedStrings.get("ramEditMenuItem"));
        clear = createItem(enabled, BankedStrings.get("ramClearMenuItem"));
        load = createItem(enabled, BankedStrings.get("ramLoadMenuItem"));
        save = createItem(enabled, BankedStrings.get("ramSaveMenuItem"));

        menu.addSeparator();
        menu.add(edit);
        menu.add(clear);
        menu.add(load);
        menu.add(save);
    }

    private JMenuItem createItem(boolean enabled, String label) {
        JMenuItem ret = new JMenuItem(label);
        ret.setEnabled(enabled);
        ret.addActionListener(this);
        return ret;
    }

    public void actionPerformed(ActionEvent evt) {
        Object src = evt.getSource();
        if (src == edit) doEdit();
        else if (src == clear) doClear();
        else if (src == load) doLoad();
        else if (src == save) doSave();
    }

    private void doEdit() {
        BankedMemState s = factory.getState(instance, circState);
        if (s == null) return;
        HexFrame frame = factory.getHexFrame(proj, instance, circState);
        frame.setVisible(true);
        frame.toFront();
    }

    private void doClear() {
        BankedMemState s = factory.getState(instance, circState);
        boolean isAllZero = s.getContents().isClear();
        if (isAllZero) return;

        int choice = JOptionPane.showConfirmDialog(frame,
                BankedStrings.get("ramConfirmClearMsg"),
                BankedStrings.get("ramConfirmClearTitle"),
                JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            s.getContents().clear();
        }
    }

    private void doLoad() {
        JFileChooser chooser = proj.createChooser();
        File oldSelected = factory.getCurrentImage(instance);
        if (oldSelected != null) chooser.setSelectedFile(oldSelected);
        chooser.setDialogTitle(BankedStrings.get("ramLoadDialogTitle"));
        int choice = chooser.showOpenDialog(frame);
        if (choice == JFileChooser.APPROVE_OPTION) {
            File f = chooser.getSelectedFile();
            try {
                factory.loadImage(circState.getInstanceState(instance), f);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(frame, e.getMessage(),
                        BankedStrings.get("ramLoadErrorTitle"), JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void doSave() {
        BankedMemState s = factory.getState(instance, circState);

        JFileChooser chooser = proj.createChooser();
        File oldSelected = factory.getCurrentImage(instance);
        if (oldSelected != null) chooser.setSelectedFile(oldSelected);
        chooser.setDialogTitle(BankedStrings.get("ramSaveDialogTitle"));
        int choice = chooser.showSaveDialog(frame);
        if (choice == JFileChooser.APPROVE_OPTION) {
            File f = chooser.getSelectedFile();
            try {
                HexFile.save(f, s.getContents());
                factory.setCurrentImage(instance, f);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(frame, e.getMessage(),
                        BankedStrings.get("ramSaveErrorTitle"), JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}

