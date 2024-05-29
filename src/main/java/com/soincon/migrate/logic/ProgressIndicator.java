package com.soincon.migrate.logic;

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

        StringBuilder progressBar = new StringBuilder("");
        progressBar.append("\u2588".repeat(Math.max(0, (int) completedBlocks)));
        progressBar.append(" ".repeat(Math.max(0, remainingBlocks)));
        progressBar.append(" ").append(percentage).append("%");

        progressBar.append(" [");

        progressBar.append(String.format("%02d:%02d:%02d]",hours , minutes, seconds));

        System.out.print("\r" + progressBar.toString());
    }
}

