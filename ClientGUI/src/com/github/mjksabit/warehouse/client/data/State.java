package com.github.mjksabit.warehouse.client.data;

import com.github.mjksabit.warehouse.client.controller.Menu;
import com.github.mjksabit.warehouse.client.model.Car;
import com.github.mjksabit.warehouse.client.view.Card;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.Node;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

public class State {

    private State(){}

    private static State instance;

    public static State get() {
        if (instance==null)
            instance = new State();
        return instance;
    }

    private Menu menu;



}
