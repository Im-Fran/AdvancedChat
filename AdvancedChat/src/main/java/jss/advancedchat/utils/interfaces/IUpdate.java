package jss.advancedchat.utils.interfaces;

import java.util.function.Consumer;

public interface IUpdate {
    public void getUpdateVersion(Consumer<String> consumer);
}