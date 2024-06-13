package com.soincon.migrate.logic;

import com.soincon.migrate.command.WarningUtil;

import static com.soincon.migrate.logic.MigrateSystem.currentStep;

public class ProgressIndicator implements Runnable {
    private static int totalSteps = 1;
    private static long startTime = -1;
    private volatile boolean running = true;

    public ProgressIndicator(int totalSteps) {
        ProgressIndicator.totalSteps = totalSteps;
    }

    @Override
    public void run() {
        while (running && currentStep <= totalSteps) {
            showProgressIndicator();
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

    private void showProgressIndicator() {
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

        String progressBar = WarningUtil.ANSI_GREEN + "█".repeat(Math.max(0, (int) completedBlocks)) +
                WarningUtil.ANSI_RED + "▒".repeat(Math.max(0, remainingBlocks))+ WarningUtil.ANSI_WHITE +
                " " + percentage + "%" +
                " [" +
                String.format("%02d:%02d:%02d]", hours, minutes, seconds) + WarningUtil.ANSI_RESET;

        System.out.print("\r" + progressBar);
    }
}

