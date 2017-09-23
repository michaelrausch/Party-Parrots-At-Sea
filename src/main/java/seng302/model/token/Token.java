package seng302.model.token;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import seng302.model.GeoPoint;

/**
 * A class describing a game token
 * Created by wmu16 on 28/08/17.
 */
public class Token extends GeoPoint {

    private TokenType tokenType;
    private Random random = new Random();

    public Token(TokenType tokenType, double lat, double lng) {
        super(lat, lng);
        this.tokenType = tokenType;
    }

    public TokenType getTokenType() {
        return tokenType;
    }

    /**
     * Assigns a random type to the token (including the random type token)
     */
    public void assignRandomType() {
        tokenType = TokenType.values()[random.nextInt(TokenType.values().length)];
    }

    /**
     * Assigns a random, concrete type to the token (cannot be the random type)
     */
    public void realiseRandom() {
        List<TokenType> tokenTypeList = new ArrayList<>(Arrays.asList(TokenType.values()));
        tokenTypeList.remove(TokenType.RANDOM);
        tokenType = tokenTypeList.get(random.nextInt(tokenTypeList.size()));
    }

    /**
     * Exists for testing purposes only
     */
    public void assignType(TokenType tokenType) {
        this.tokenType = tokenType;
    }
}
