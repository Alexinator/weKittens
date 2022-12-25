package edu.vub.at.wekittens.interfaces;

public interface JWeKittens {
    // ################
    // ## AT to Java ##
    // ################

    JWeKittens registerATApp(ATWeKittens weKittens);

    public void foundNewPlayer(String newPlayer);

    public void startGameAT();
}
