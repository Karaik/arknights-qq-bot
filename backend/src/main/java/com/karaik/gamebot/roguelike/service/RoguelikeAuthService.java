package com.karaik.gamebot.roguelike.service;

import com.karaik.gamebot.roguelike.client.RoguelikeApiException;
import com.karaik.gamebot.roguelike.client.RoguelikeHttpClient;
import com.karaik.gamebot.roguelike.config.RoguelikeApiProperties;
import com.karaik.gamebot.roguelike.domain.auth.AuthFlow;
import com.karaik.gamebot.roguelike.domain.auth.CredTokenResponse;
import com.karaik.gamebot.roguelike.domain.auth.OAuthCodeResponse;
import com.karaik.gamebot.roguelike.domain.binding.BindingResponse;
import org.springframework.stereotype.Service;

@Service
public class RoguelikeAuthService {

    private final RoguelikeHttpClient httpClient;
    private final RoguelikeApiProperties properties;

    public RoguelikeAuthService(RoguelikeHttpClient httpClient, RoguelikeApiProperties properties) {
        this.httpClient = httpClient;
        this.properties = properties;
    }

    public AuthFlow authenticate() {
        if (properties.getToken() == null || properties.getToken().isBlank()) {
            throw new RoguelikeApiException("Hypergryph token is missing; please configure HYPERGRYPH_TOKEN.");
        }

        OAuthCodeResponse codeResponse = httpClient.requestOAuthCode();
        if (codeResponse == null || codeResponse.data() == null || codeResponse.data().code() == null) {
            throw new RoguelikeApiException("Grant response missing code");
        }

        CredTokenResponse credResponse = httpClient.requestCredAndToken(codeResponse.data().code());
        if (credResponse == null || credResponse.data() == null) {
            throw new RoguelikeApiException("Credential response missing data");
        }

        String cred = credResponse.data().cred();
        String token = credResponse.data().token();
        if (cred == null || token == null) {
            throw new RoguelikeApiException("Missing cred/token in response");
        }

        BindingResponse bindingResponse = httpClient.requestBindings(cred, token);
        String uid = bindingResponse.data().list().stream()
                .filter(game -> "arknights".equalsIgnoreCase(game.appCode()))
                .findFirst()
                .flatMap(game -> game.bindingList().stream().findFirst())
                .map(BindingResponse.Binding::uid)
                .orElseThrow(() -> new RoguelikeApiException("No Arknights binding found"));

        return new AuthFlow(cred, token, uid);
    }
}
