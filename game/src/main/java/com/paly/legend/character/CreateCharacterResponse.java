package com.paly.legend.character;

public class CreateCharacterResponse {

    private long characterId;

    public CreateCharacterResponse() {
    }

    public CreateCharacterResponse(long characterId) {
        this.characterId = characterId;
    }

    public long getCharacterId() {
        return characterId;
    }

    public void setCharacterId(long characterId) {
        this.characterId = characterId;
    }
}

