package io.github.termitepreston.schoolprojects.ui;

import io.github.termitepreston.schoolprojects.DB;

import javax.swing.*;
import java.awt.*;
import java.util.Random;

public class DBConnTestWorker extends SwingWorker<Exception, Void> {
    private final DB db;
    private final ApplicationFrame frame;

    public DBConnTestWorker(DB db, ApplicationFrame appFrame) {
        this.db = db;
        this.frame = appFrame;
    }

    @Override
    protected Exception doInBackground() throws Exception {
        Random random = new Random();
        int progress = 0;
        setProgress(0);
        try {
            Thread.sleep(1000);
            while (progress < 100 && !isCancelled()) {
                //Sleep for up to one second.
                Thread.sleep(random.nextInt(1000));
                //Make random progress.
                progress += random.nextInt(3);
                setProgress(Math.min(progress, 100));
            }
        } catch (InterruptedException ignore) {
        }
        return null;
    }

    @Override
    public void done() {
        Toolkit.getDefaultToolkit().beep();
        frame.getProgressMonitor().setProgress(0);
    }

}
