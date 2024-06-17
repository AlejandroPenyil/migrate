package com.soincon.migrate.command;

import org.fusesource.jansi.AnsiConsole;

import java.util.Scanner;

public class WarningUtil {

    // Códigos de colores ANSI
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";

    static {
        // Inicializa Jansi
        AnsiConsole.systemInstall();
    }

    // Método para mostrar advertencias con colores
    public static void showWarning(String title, String message) {
        System.out.println(ANSI_YELLOW + title + ": " + ANSI_RESET + ANSI_CYAN + message + ANSI_RESET);
    }

    public static String showWarningAndReadInput(String title, String message) {
        System.out.println();
        Scanner scanner = new Scanner(System.in);
        System.out.print(ANSI_YELLOW + title + ": " + ANSI_RESET + ANSI_CYAN + message + ANSI_RESET);
        System.out.print(ANSI_GREEN);  // Cambia el color antes de la entrada del usuario
        String input = scanner.nextLine();
        System.out.print(ANSI_RESET);
        System.out.println();// Resetea el color después de la entrada del usuario
        return input;
    }

    public static void showAlert(String title, String message) {
        System.out.println(ANSI_RED + title + ": " + ANSI_YELLOW + message + ANSI_RESET);
    }

    public static String answer() {
        Scanner scanner = new Scanner(System.in);
        System.out.print(ANSI_GREEN);
        String input = scanner.nextLine();
        System.out.print(ANSI_RESET);  // Resetea el color después de la entrada del usuario
        return input;
    }

    public static void showError() {
        System.out.println(ANSI_RED);
        System.out.println("Un error ha ocurrido");
        System.out.println(ANSI_RESET);
    }
}



