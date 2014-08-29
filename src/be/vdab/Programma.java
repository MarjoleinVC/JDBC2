/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.vdab;

import static java.lang.String.valueOf;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

/**
 *
 * @author marjolein
 */
public class Programma {

    private static final String URL = "jdbc:mysql://localhost/bank";
    private static final String USER = "cursist";
    private static final String PASSWORD = "cursist";
    private static final String SQL_INSERT
            = "insert into RekeningNr values (?)";
    private static final String SQL_SELECT
            = "select RekeningNr, saldo from bank where RekeningNr =?";
    private static final String SQL_UPDATE
            = "update bank set saldo = saldo + ?";

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try (Scanner keuze = new Scanner(System.in)) {
            System.out.println("Maak uw keuze: 1 (nieuwe rekening, 2 (saldo consulteren"
                    + ", 3 (overschrijven)");
            int choice = keuze.nextInt();
            switch (choice) {
                case 1:
                    try (Scanner keuze1 = new Scanner(System.in)) {
                        System.out.print("Rekeningnummer: ");
                        long rekeningnummer = keuze1.nextLong();
                        if (validateRekeningnummer(rekeningnummer)) {
                            try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
                                    PreparedStatement statement = connection.prepareStatement(SQL_INSERT)) {
                                statement.setLong(1, rekeningnummer);
                            } catch (SQLException ex) {
                                System.out.println(ex);
                            }
                        } else {
                            System.out.println("Geef een geldig rekeningnummer op");
                        }
                    }
                    break;
                case 2:
                    try (Scanner keuze2 = new Scanner(System.in)) {
                        System.out.print("Rekeningnummer: ");
                        long rekeningnummer = keuze2.nextLong();
                        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
                                PreparedStatement statement = connection.prepareStatement(SQL_SELECT)) {
                            statement.setLong(1, rekeningnummer);
                            try (ResultSet resultSet = statement.executeQuery()) {
                                while (resultSet.next()) {
                                    System.out.println(resultSet.getBigDecimal("saldo"));
                                }
                            }
                        } catch (SQLException ex) {
                            System.out.println(ex);
                        }
                    }
                    break;
                case 3: //nog verder uitwerken
                    try (Scanner keuze3 = new Scanner(System.in)) {
                        System.out.print("Rekeningnummer van: ");
                        long rekeningnummerVan = keuze3.nextLong();
                        System.out.print("Rekeningnummer aan: ");
                        long rekeningnummerAan = keuze3.nextLong();
                        System.out.print("Bedrag overschrijving: ");
                        BigDecimal bedrag = keuze3.nextBigDecimal();
                        if (validateRekeningnummer(rekeningnummerVan)) {
                            if (validateRekeningnummer(rekeningnummerAan)) {
                                try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
                                        PreparedStatement statement = connection.prepareStatement(SQL_SELECT)) {
                                    statement.setLong(1, rekeningnummerVan);
                                    statement.setLong(2, rekeningnummerAan);
                                    statement.setBigDecimal(3, bedrag);
                                    try (ResultSet resultSet = statement.executeQuery()) {
                                        while (resultSet.next()) {
                                            System.out.println(resultSet.getBigDecimal("saldo"));
                                        }
                                    }
                                } catch (SQLException ex) {
                                    System.out.println(ex);
                                }
                            } else {
                                System.out.print("Geef een correct rekeningnumer om op te storten.");
                            }
                        } else {
                            System.out.print("Geef een correct rekeningnumer om van over te schrijven.");
                        }
                    }
                    break;

            }
        }
    }

    private static boolean validateRekeningnummer(long rekeningnummer) {
        long deeltal = rekeningnummer / 100;
        byte rest = (byte) (deeltal % 97);
        byte controle = (byte) (rekeningnummer % 100);
        String rekNrAsString = valueOf(rekeningnummer);
        return (rekNrAsString.length() == 12) && (controle == 97 - rest);
    }
}
