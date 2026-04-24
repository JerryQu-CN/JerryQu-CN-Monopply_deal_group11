module com.example.monopoly_deal_game {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.example.monopoly_deal_game.controller to javafx.fxml;

    exports com.example.monopoly_deal_game.app;
    exports com.example.monopoly_deal_game.controller;
    exports com.example.monopoly_deal_game.model;
    exports com.example.monopoly_deal_game.logic;
    exports com.example.monopoly_deal_game.view;
    exports com.example.monopoly_deal_game.view.animation;
    exports com.example.monopoly_deal_game.view.cards;
    exports com.example.monopoly_deal_game.view.scene;
    exports com.example.monopoly_deal_game.network;
    exports com.example.monopoly_deal_game.persistence;
    exports com.example.monopoly_deal_game.ai;
}
