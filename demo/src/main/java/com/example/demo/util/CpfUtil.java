package com.example.demo.util;

public final class CpfUtil {

    private CpfUtil() {
    }

    public static String somenteNumeros(String cpf) {
        return cpf == null ? "" : cpf.replaceAll("\\D", "");
    }

    public static String formatar(String cpf) {
        String numeros = somenteNumeros(cpf);
        if (numeros.length() != 11) {
            return numeros;
        }

        return numeros.substring(0, 3) + "."
            + numeros.substring(3, 6) + "."
            + numeros.substring(6, 9) + "-"
            + numeros.substring(9, 11);
    }

    public static boolean valido(String cpf) {
        String numeros = somenteNumeros(cpf);

        if (numeros.length() != 11 || numeros.matches("(\\d)\\1{10}")) {
            return false;
        }

        int primeiroDigito = calcularDigito(numeros.substring(0, 9), 10);
        int segundoDigito = calcularDigito(numeros.substring(0, 9) + primeiroDigito, 11);

        return numeros.equals(numeros.substring(0, 9) + primeiroDigito + segundoDigito);
    }

    private static int calcularDigito(String base, int pesoInicial) {
        int soma = 0;
        for (int i = 0; i < base.length(); i++) {
            soma += Character.getNumericValue(base.charAt(i)) * (pesoInicial - i);
        }

        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }
}
