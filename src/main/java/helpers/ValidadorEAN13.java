/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package helpers;



public class ValidadorEAN13 {
    
    public static boolean validarEAN13(String codigo) {
        if (codigo == null || codigo.length() != 13) {
            return false;
        }
        
        try {
            long l = Long.parseLong(codigo);
        } catch (NumberFormatException e) {
            return false;
        }

        int sum = 0;
        for (int i = 0; i < 12; i++) {
            int digit = Character.getNumericValue(codigo.charAt(i));
            if (digit < 0 || digit > 9) {
                return false;
            }
            sum += (i % 2 == 0) ? digit * 1 : digit * 3;
        }
        
        int checksum = (10 - (sum % 10)) % 10;
        int lastDigit = Character.getNumericValue(codigo.charAt(12));
        
        return checksum == lastDigit;
    }
}