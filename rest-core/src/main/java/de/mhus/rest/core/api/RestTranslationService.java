package de.mhus.rest.core.api;

import java.util.Locale;

import com.fasterxml.jackson.databind.JsonNode;

public interface RestTranslationService {

    String translateError(Locale locale, JsonNode errArray);

    String translateError(Locale locale, String msg);

}
