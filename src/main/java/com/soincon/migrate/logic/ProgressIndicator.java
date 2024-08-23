package com.soincon.migrate.logic;

import com.soincon.migrate.command.WarningUtil;

import java.io.IOException;

import static com.soincon.migrate.logic.MigrateSystem.currentStep;

public class ProgressIndicator implements Runnable {
    private static int totalSteps = 1;
    private static long startTime = -1;
    private volatile boolean running = true;
    private MigrateSystem migrateSystem;

    public ProgressIndicator(int totalSteps, MigrateSystem migrateSystem) {
        ProgressIndicator.totalSteps = totalSteps;
        this.migrateSystem=migrateSystem;
    }

    @Override
    public void run() {
        while (running && currentStep <= totalSteps) {
            try {
                showProgressIndicator();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public void stop() {
        running = false;
    }

    private void showProgressIndicator() throws IOException {
        if (startTime == -1) {
            startTime = System.currentTimeMillis();
        }

        int progressBarLength = 85;
        double completedBlocks = (double) currentStep * progressBarLength / totalSteps;
        int remainingBlocks = progressBarLength - (int) completedBlocks;

        int percentage = (int) (currentStep * 100.0 / totalSteps);

        long elapsedTimeMillis = System.currentTimeMillis() - startTime;
        long elapsedTimeSeconds = elapsedTimeMillis / 1000;
        long hours = elapsedTimeSeconds / 3600;
        long minutes = (elapsedTimeSeconds % 3600) / 60;
        long seconds = elapsedTimeSeconds % 60;

//        if(seconds % 3600 == 0) {
//            migrateSystem.updateJwt();
//        }

        String progressBar = WarningUtil.ANSI_GREEN + "█" + WarningUtil.ANSI_GREEN + "█".repeat(Math.max(0, (int) completedBlocks)) +
                WarningUtil.ANSI_RED + "▒".repeat(Math.max(0, remainingBlocks)) + WarningUtil.ANSI_GREEN + "█" + WarningUtil.ANSI_WHITE +
                " " + percentage + "%" +
                " [" +
                String.format("%02d:%02d:%02d]", hours, minutes, seconds) + WarningUtil.ANSI_RESET;

        System.out.print("\r" + progressBar);
    }
}

