package my.application.helper;

import org.springframework.stereotype.Component;

import java.text.DecimalFormat;

@Component
public class NumberHelper {
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.#");

    public double roundDouble(double number){
        return Double.parseDouble(replaceComma(DECIMAL_FORMAT.format(number)));
    }

    public String replaceComma(String text){
        return text.replace(",", ".");
    }
}
