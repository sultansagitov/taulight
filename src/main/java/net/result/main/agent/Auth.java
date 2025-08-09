package net.result.main.agent;

import net.result.main.commands.ConsoleContext;
import net.result.sandnode.dto.LogPasswdResponseDTO;
import net.result.sandnode.dto.LoginResponseDTO;
import net.result.sandnode.exception.UnknownSandnodeErrorException;
import net.result.sandnode.exception.error.*;
import net.result.sandnode.hubagent.AgentProtocol;
import net.result.sandnode.serverclient.SandnodeClient;

import java.util.Scanner;

public class Auth {
    public static ConsoleContext authenticateUser(Scanner scanner, SandnodeClient client) throws Exception {
        ConsoleContext context = null;
        while (context == null) {
            String s;
            do {
                System.out.print("[r for register, 'l' for login by password, 't' for login by token]: ");
                s = scanner.nextLine();
            }
            while (s.isEmpty() || (s.charAt(0) != 'r' && s.charAt(0) != 't' && s.charAt(0) != 'l'));

            char choice = s.charAt(0);

            context = switch (choice) {
                case 'r' -> register(scanner, client);
                case 't' -> login(scanner, client);
                case 'l' -> password(scanner, client);
                default -> null;
            };
        }

        return context;
    }

    public static ConsoleContext register(Scanner scanner, SandnodeClient client) throws Exception {
        System.out.print("Nickname: ");
        String nickname = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();
        System.out.print("Device: ");
        String device = scanner.nextLine();

        try {
            var result = AgentProtocol.register(client, nickname, password, device);
            System.out.printf("Token for \"%s\":%n%s%n", nickname, result.token);
            return new ConsoleContext(client);
        } catch (BusyNicknameException e) {
            System.out.println("Nickname is busy");
        } catch (InvalidNicknamePassword e) {
            System.out.println("Invalid nickname or password");
        } catch (SandnodeErrorException | UnknownSandnodeErrorException e) {
            System.out.printf("Unknown sandnode error exception. Please try again. %s%n", e.getClass());
        }

        return null;
    }

    public static ConsoleContext login(Scanner scanner, SandnodeClient client) throws Exception {
        System.out.print("Token: ");
        String token = scanner.nextLine();

        if (token.isEmpty()) return null;

        try {
            LoginResponseDTO result = AgentProtocol.byToken(client, token);
            System.out.printf("You log in as %s%n", result.nickname);
            return new ConsoleContext(client);
        } catch (InvalidArgumentException e) {
            System.out.println("Invalid token. Please try again.");
        } catch (ExpiredTokenException e) {
            System.out.println("Expired token. Please try again.");
        } catch (SandnodeErrorException | UnknownSandnodeErrorException e) {
            System.out.printf("Unknown sandnode error exception. Please try again. %s%n", e.getClass());
        }

        return null;
    }

    public static ConsoleContext password(Scanner scanner, SandnodeClient client) throws Exception {
        System.out.print("Nickname: ");
        String nickname = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();
        System.out.print("Device: ");
        String device = scanner.nextLine();

        if (nickname.isEmpty() || password.isEmpty()) return null;

        try {
            LogPasswdResponseDTO result = AgentProtocol.byPassword(client, nickname, password, device);
            System.out.printf("Token for \"%s\": %n%s%n", nickname, result.token);
            return new ConsoleContext(client);
        } catch (UnauthorizedException e) {
            System.out.println("Incorrect nickname or password. Please try again.");
        } catch (SandnodeErrorException | UnknownSandnodeErrorException e) {
            System.out.printf("Unknown sandnode error exception. Please try again. %s%n", e.getClass());
        }

        return null;
    }
}
