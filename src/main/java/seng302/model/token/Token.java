package seng302.model.token;

import seng302.model.GeoPoint;

/**
 * A class describing a game token
 * Created by wmu16 on 28/08/17.
 */
public class Token extends GeoPoint {

    private TokenType tokenType;

    public Token(TokenType tokenType, double lat, double lng) {
        super(lat, lng);
        this.tokenType = tokenType;
    }

    public TokenType getTokenType() {
        return tokenType;
    }

}
