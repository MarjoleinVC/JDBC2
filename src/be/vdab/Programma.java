/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.vdab;

import static java.lang.String.valueOf;
import java.math.BigDecimal;
import java.sql.CallableStatement;
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

    private static final String URL = "jdbc:mysql://localhost/bank?noAccessToProcedureBodies=true";
    private static final String USER = "cursist";
    private static final String PASSWORD = "cursist";
    private static final String SQL_INSERT
            = "insert into rekeningen (RekeningNr, Saldo) values (?, 0) "; //saldo waarde 0, omdat bij rekening aanmaken nog niets op de rekening kan staan!
    private static final String SQL_SELECT
            = "select RekeningNr, Saldo from rekeningen where RekeningNr =?";
    private static final String SQL_CALLUPDATE
            = "{call Overschrijven (?, ?, ?)}";

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try (Scanner keuze = new Scanner(System.in)) {
            String choice = null;
            //while (!"1".equals(choice) || !"2".equals(choice) || !"3".equals(choice)) { //invoer controleren
            System.out.println("Maak uw keuze: \n 1 (nieuwe rekening) \n 2 (saldo consulteren) \n 3 (overschrijven)");
            choice = keuze.next();
            switch (choice) {
                case "1":
                    try (Scanner rekeningnr = new Scanner(System.in)) {
                        System.out.print("Rekeningnummer: ");
                        long RekeningNr = rekeningnr.nextLong();
                        if (validateRekeningnummer(RekeningNr)) {
                            try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
                                    PreparedStatement statement = connection.prepareStatement(SQL_INSERT)) {
                                statement.setLong(1, RekeningNr);
                                statement.executeUpdate();
                            } catch (SQLException ex) {
                                System.out.println(ex);
                            }
                        } else {
                            System.out.println("Geef een geldig rekeningnummer op.");
                        }
                    }
                    break;
                case "2":
                    try (Scanner rekeningnr = new Scanner(System.in)) {
                        System.out.print("Rekeningnummer: ");
                        long RekeningNr = rekeningnr.nextLong();
                        if (validateRekeningnummer(RekeningNr)) {
                            try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
                                    PreparedStatement statement = connection.prepareStatement(SQL_SELECT)) {
                                statement.setLong(1, RekeningNr);
                                try (ResultSet resultSet = statement.executeQuery()) {
                                    if (resultSet.next()) {
                                        System.out.println(resultSet.getLong(1) + " " + resultSet.getBigDecimal(2));
                                    } else {
                                        System.out.println("Het rekeningnummer werd nog niet aangemaakt.");
                                    }
                                }
                            } catch (SQLException ex) {
                                System.out.println(ex);
                            }
                        }
                    }
                    break;
                case "3":
                    try (Scanner rekeningnr = new Scanner(System.in)) {
                        System.out.print("Rekeningnummer overschrijver: ");
                        long rekeningnummerVan = rekeningnr.nextLong();
                        System.out.print("Rekeningnummer ontvanger: ");
                        long rekeningnummerAan = rekeningnr.nextLong();
                        System.out.print("Bedrag van de overschrijving: ");
                        BigDecimal bedrag = rekeningnr.nextBigDecimal();
                        if (validateRekeningnummer(rekeningnummerVan) && (validateRekeningnummer(rekeningnummerAan)) && (rekeningnummerVan != rekeningnummerAan)) {
                            try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
                                    CallableStatement statement = connection.prepareCall(SQL_CALLUPDATE)) {
                                statement.setLong(1, rekeningnummerVan);
                                statement.setLong(2, rekeningnummerAan);
                                statement.setBigDecimal(3, bedrag);
                                if (statement.executeUpdate() == 0) {
                                    System.out.println("Er staat niet genoeg geld op de rekening van de overschrijver. De overschrijving werd niet uitgevoerd.");
                                } else {
                                    System.out.println("De overschrijving van €" + bedrag
                                            + " werd uitgevoerd van rekening " + rekeningnummerVan
                                            + " naar rekening " + rekeningnummerAan + ".");
                                }
                            } catch (SQLException ex) {
                                System.out.println(ex);
                            }
                        } else {
                            if (!validateRekeningnummer(rekeningnummerVan)) {
                                System.out.print("Geef een correct rekeningnumer om van over te schrijven.");
                            } else if (!validateRekeningnummer(rekeningnummerAan)) {
                                System.out.print("Geef een correct rekeningnumer om op te storten.");
                            } else if ((rekeningnummerVan == rekeningnummerAan)) {
                                System.out.print("Geef twee verschillende rekeningnummers op.");
                            }
                        }
                        break;
                    }
            }
            //}
        }
    }

    private static boolean validateRekeningnummer(long rekeningnummer) {
        long deeltal = rekeningnummer / 100;
        byte rest = (byte) (deeltal % 97);
        if (rest == 0) {
            rest = 97;
        }
        byte controle = (byte) (rekeningnummer % 100);
        String rekNrAsString = valueOf(rekeningnummer);
        return (rekNrAsString.length() == 12) && (controle == rest);
    }
}
