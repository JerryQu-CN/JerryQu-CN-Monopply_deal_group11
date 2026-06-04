module com.example.monopoly_deal_game {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.example.monopoly_deal_game.controller to javafx.fxml;
    opens com.example.monopoly_deal_game.controller.lobby to javafx.fxml;

    exports com.example.monopoly_deal_game.app;
    exports com.example.monopoly_deal_game.controller;
    exports com.example.monopoly_deal_game.controller.dialog;
    exports com.example.monopoly_deal_game.controller.gameplay;
    exports com.example.monopoly_deal_game.controller.lobby;
    exports com.example.monopoly_deal_game.game.engine;
    exports com.example.monopoly_deal_game.game.state;
    exports com.example.monopoly_deal_game.logic;
    exports com.example.monopoly_deal_game.logic.payment;
    exports com.example.monopoly_deal_game.model;
    exports com.example.monopoly_deal_game.model.cards;
    exports com.example.monopoly_deal_game.network;
    exports com.example.monopoly_deal_game.view;
    exports com.example.monopoly_deal_game.view.animation;
}
