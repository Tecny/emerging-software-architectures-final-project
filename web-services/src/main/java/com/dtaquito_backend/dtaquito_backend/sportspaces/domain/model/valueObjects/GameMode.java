package com.dtaquito_backend.dtaquito_backend.sportspaces.domain.model.valueObjects;

public enum GameMode {
    FUTBOL_11(22),
    FUTBOL_7(14),
    FUTBOL_8(16),
    FUTBOL_5(10),

    BILLAR_3(3);

    private final int maxPlayers;

    GameMode(int maxPlayers) {
        this.maxPlayers = maxPlayers;
    }
    public int getMaxPlayers() {
        return maxPlayers;
    }
}