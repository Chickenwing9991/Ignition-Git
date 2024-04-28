package com.axone_io.ignition.git.managers;

import com.axone_io.ignition.git.CommitPopup;
import com.axone_io.ignition.git.DesignerHook;
import com.inductiveautomation.ignition.common.Dataset;
import com.inductiveautomation.ignition.common.project.ChangeOperation;
import com.inductiveautomation.ignition.common.project.resource.ProjectResourceId;
import com.inductiveautomation.ignition.common.util.LoggerEx;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

import static com.axone_io.ignition.git.DesignerHook.context;
import static com.axone_io.ignition.git.DesignerHook.rpc;
import static com.axone_io.ignition.git.actions.GitBaseAction.handleCommitAction;

public class GitActionManager {
    static CommitPopup commitPopup;
    private final static LoggerEx logger = LoggerEx.newBuilder().build(GitActionManager.class);

    public static Object[][] getCommitPopupData(String projectName, String userName) {
        List<ChangeOperation> changes = DesignerHook.changes;
        logger.info("GitActionManger: "+changes.toString());

        Dataset ds = rpc.getUncommitedChanges(projectName, userName);
        logger.info("UncommitedChanges: "+ds.toString());

        Object[][] data = new Object[ds.getRowCount()][];

        List<String> resourcesChangedId = new ArrayList<>();
        for (ChangeOperation c : changes) {
            ProjectResourceId pri = ChangeOperation.getResourceIdFromChange(c);
            resourcesChangedId.add(pri.getResourcePath().toString());
        }

        for (int i = 0; i < ds.getRowCount(); i++) {
            String resource = (String) ds.getValueAt(i, "resource");

            boolean toAdd = resourcesChangedId.contains(resource);
            Object[] row = {toAdd, resource, ds.getValueAt(i, "type"), ds.getValueAt(i, "actor")};
            data[i] = row;
        }

        return data;
    }

    public static void showCommitPopup(String projectName, String userName) {
        logger.info("Getting Popup Data");
        Object[][] data = GitActionManager.getCommitPopupData(projectName, userName);
        logger.info("Got Popup Data");
        if (commitPopup != null) {
            commitPopup.setData(data);
            commitPopup.setVisible(true);
            commitPopup.toFront();
        } else {
            commitPopup = new CommitPopup(data, context.getFrame()) {
                @Override
                public void onActionPerformed(List<String> changes, String commitMessage) {
                    handleCommitAction(changes, commitMessage);
                    resetMessage();
                }
            };
        }
    }

    public static void showConfirmPopup(String message, int messageType) {
        JOptionPane.showConfirmDialog(context.getFrame(),
                message, "Info", JOptionPane.DEFAULT_OPTION, messageType);
    }
}
