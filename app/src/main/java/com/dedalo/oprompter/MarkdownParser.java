package com.dedalo.oprompter;

import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MarkdownParser {

    public static SpannableString parse(String text) {
        // Lista para almacenar los spans que aplicaremos
        List<SpanInfo> spans = new ArrayList<SpanInfo>();

        // Negrita: *texto*
        findAndMarkSpans(text, "\\*([^*]+)\\*", spans, new StyleSpan(Typeface.BOLD));

        // Cursiva: _texto_
        findAndMarkSpans(text, "_([^_]+)_", spans, new StyleSpan(Typeface.ITALIC));

        // Subrayado: +texto+
        findAndMarkSpans(text, "\\+([^+]+)\\+", spans, new UnderlineSpan());

        // Tachado: ~texto~
        findAndMarkSpans(text, "~([^~]+)~", spans, new StrikethroughSpan());

        // Construir el texto limpio (sin símbolos)
        String cleanText = removeMarkdownSymbols(text);
        SpannableString spannable = new SpannableString(cleanText);

        // Aplicar los spans al texto limpio
        applySpansToCleanText(text, cleanText, spans, spannable);

        return spannable;
    }

    private static void findAndMarkSpans(String text, String regex, List<SpanInfo> spans, Object span) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            // Guardamos la posición del texto interior (sin los símbolos)
            spans.add(new SpanInfo(matcher.start(1), matcher.end(1), span));
        }
    }

    private static String removeMarkdownSymbols(String text) {
        // Eliminar todos los símbolos de markdown
        String result = text;
        result = result.replaceAll("\\*([^*]+)\\*", "$1");
        result = result.replaceAll("_([^_]+)_", "$1");
        result = result.replaceAll("\\+([^+]+)\\+", "$1");
        result = result.replaceAll("~([^~]+)~", "$1");
        return result;
    }

    private static void applySpansToCleanText(String originalText, String cleanText, List<SpanInfo> spans, SpannableString spannable) {
        // Para cada span, calcular su posición en el texto limpio
        for (SpanInfo spanInfo : spans) {
            // Obtener el texto que estaba entre los símbolos
            String markedText = originalText.substring(spanInfo.start, spanInfo.end);
            
            // Buscar ese texto en el texto limpio
            int cleanStart = cleanText.indexOf(markedText);
            if (cleanStart != -1) {
                int cleanEnd = cleanStart + markedText.length();
                spannable.setSpan(spanInfo.span, cleanStart, cleanEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
    }

    // Clase interna para almacenar información de los spans
    private static class SpanInfo {
        int start;
        int end;
        Object span;

        SpanInfo(int start, int end, Object span) {
            this.start = start;
            this.end = end;
            this.span = span;
        }
    }
}