package com.soincon.migrate.command;

public class WarningUtil {

    public static void showWarning(String title, String message) {
        int width = 60; // Ajusta el ancho según sea necesario
        String border = "-".repeat(width);
        String formattedTitle = String.format("| %s |", centerText(title, width - 4));
        String[] formattedMessages = formatMessage(message, width - 4);

        System.out.println(border);
        System.out.println(formattedTitle);
        for (String line : formattedMessages) {
            System.out.println(String.format("| %s |", centerText(line, width - 4)));
        }
        System.out.println(border);
    }

    private static String centerText(String text, int width) {
        if (text.length() >= width) {
            return text.substring(0, width);
        }
        int leftPadding = (width - text.length()) / 2;
        int rightPadding = width - text.length() - leftPadding;
        return " ".repeat(leftPadding) + text + " ".repeat(rightPadding);
    }

    private static String[] formatMessage(String message, int width) {
        String[] words = message.split(" ");
        StringBuilder line = new StringBuilder();
        StringBuilder formattedMessage = new StringBuilder();
        for (String word : words) {
            if (line.length() + word.length() + 1 > width) {
                formattedMessage.append(line.toString().stripTrailing()).append("\n");
                line = new StringBuilder();
            }
            line.append(word).append(" ");
        }
        formattedMessage.append(line.toString().stripTrailing());
        return formattedMessage.toString().split("\n");
    }
}


