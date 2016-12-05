package io.klerch.alexa.translator.skill.util;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.translate.Translate;
import com.google.api.services.translate.model.TranslationsListResponse;
import io.klerch.alexa.tellask.util.resource.ResourceUtteranceReader;
import io.klerch.alexa.tellask.util.resource.YamlReader;
import io.klerch.alexa.translator.skill.SkillConfig;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GoogleTranslation {
    private static Logger log = Logger.getLogger(GoogleTranslation.class.getName());
    private Translate translate;
    private final String locale;
    private final YamlReader yamlReader;

    public GoogleTranslation(final String locale) {
        this.locale = locale;
        final ResourceUtteranceReader reader = new ResourceUtteranceReader("/out", "/languages.yml");
        this.yamlReader = new YamlReader(reader, locale);

        try {
            this.translate = new Translate.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    GsonFactory.getDefaultInstance(), null)
                    .setApplicationName(SkillConfig.getGoogleProjectName())
                    .build();
        } catch (final GeneralSecurityException | IOException ex) {
            log.log(Level.SEVERE, null, ex);
        }
    }

    public String translate(final String term, final String language) {
        final Optional<String> languageCode = this.yamlReader.getRandomUtterance(language);

        if (languageCode.isPresent()) {
            final Translate.Translations.List list;
            try {
                list = translate.new Translations().list(
                        Collections.singletonList(term), languageCode.get());
                list.setKey(SkillConfig.getGoogleApiKey());
                final TranslationsListResponse response = list.execute();

                return !response.isEmpty() && !response.getTranslations().isEmpty() ?
                        response.getTranslations().get(0).getTranslatedText() : "";
            } catch (IOException e) {
                log.severe(e.getMessage());
            }
        }
        return "";
    }
}
