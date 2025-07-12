package com.imaginamos.farmatodo.backend.util;

import java.text.Normalizer;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FTDUtil {

    /**
     * Order List
     * @param map
     * @param <K>
     * @param <V>
     * @return
     */

    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
        List<Map.Entry<K, V>> list = new ArrayList<>(map.entrySet());
        list.sort(Map.Entry.comparingByValue());

        Map<K, V> result = new LinkedHashMap<>();
        for (Map.Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }

        return result;
    }

    public static String deleteLastWorld(String paragraph) {
        if (Objects.nonNull(paragraph)){
            String[] words = paragraph.split(" ");
            if (words.length > 1){
                words = Arrays.copyOf(words,words.length-1);
                paragraph = String.join(" ", words);
            }
        }
        return paragraph;
    }

    public static boolean isLifeMileValid(final Long lifeMileNumber){
        if( lifeMileNumber != null && lifeMileNumber > 0 ){
            if( String.valueOf(lifeMileNumber).length() > 1 ){
                final int MODULER = 7;
                final String strLifeMileNumber = String.valueOf( lifeMileNumber );
                final int size = strLifeMileNumber.length();
                final String lifeMileWithoutLastDigit = strLifeMileNumber.substring( 0, size - 1 );
                final int lastDigit = Integer.parseInt( strLifeMileNumber.substring( size - 1, size ) );
                final Long module = Long.valueOf(lifeMileWithoutLastDigit ) % MODULER;
                return module == lastDigit;
            }
            return false;
        }
        return false;
    }

    public static Date addSubstractDaysDate(Date date,int days){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_YEAR,days);
        return calendar.getTime();
    }

    public static String replaceStringVar(String largeText, String variableToReplace, String replaceTextFor){
        variableToReplace = "{" + variableToReplace + "}";
        return largeText.replace(variableToReplace, replaceTextFor);
    }
    public static String toTitleCase(String input) {
        StringBuilder titleCase = new StringBuilder(input.length());
        boolean nextTitleCase = true;

        for (char c : input.toCharArray()) {
            if (Character.isSpaceChar(c)) {
                nextTitleCase = true;
            } else if (nextTitleCase) {
                c = Character.toTitleCase(c);
                nextTitleCase = false;
            }

            titleCase.append(c);
        }

        return titleCase.toString();
    }

    public static String cleanString(String texto) {
        texto = Normalizer.normalize(texto, Normalizer.Form.NFD);
        texto = texto.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
        return texto;
    }

    /**
     * Validate if a string matches a given regex pattern
     * @param input the string to validate
     * @param regex the regex pattern to match
     * @return true if the string matches the regex pattern, false otherwise
     */
    public static boolean isValidRegex(String input, String regex) {
        if (Objects.isNull(input) || Objects.isNull(regex)) {
            return false;
        }
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);
        return matcher.matches();
    }
}
